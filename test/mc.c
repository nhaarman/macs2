#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#include <sylvan.h>
#include <llmsset.h>

/* Configuration */
static int report_levels = 0; // report states at start of every level
static int report_table = 0; // report table size at end of every level
static int run_par = 1; // set to 1 = use PAR strategy; set to 0 = use BFS strategy
static int check_deadlocks = 1; // set to 1 to check for deadlocks

/* Globals */
typedef struct set
{
    BDD bdd;
    BDD variables; // all variables in the set (used by satcount)
} *set_t;

typedef struct relation
{
    BDD bdd;
    BDD variables; // all variables in the relation (used by relprod)
} *rel_t;

static size_t vector_size; // size of vector
static size_t bits_per_integer; // number of bits per integer in the vector
BDDVAR* vector_variables; // maps variable index to BDD variable
static int next_count; // number of partitions of the transition relation
static rel_t *next; // each partition of the transition relation

#define Abort(...) { fprintf(stderr, __VA_ARGS__); exit(-1); }

/* Load a set from file */
static set_t
set_load(FILE* f)
{
    sylvan_serialize_fromfile(f);

    size_t bdd;
    size_t vector_size;
    
    if (fread(&bdd, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");
    if (fread(&vector_size, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");

    BDDVAR vec_to_bddvar[bits_per_integer * vector_size];
    if (fread(vec_to_bddvar, sizeof(BDDVAR), bits_per_integer * vector_size, f) != bits_per_integer * vector_size)
        Abort("Invalid input file!\n");

    LACE_ME;

    set_t set = (set_t)malloc(sizeof(struct set));
    set->bdd = sylvan_ref(sylvan_serialize_get_reversed(bdd));
    set->variables = sylvan_ref(sylvan_set_fromarray(vec_to_bddvar, bits_per_integer * vector_size));

    return set;
}

/* Load a relation from file */
static rel_t
rel_load(FILE* f)
{
    sylvan_serialize_fromfile(f);

    size_t bdd;
    size_t vector_size;
    if (fread(&bdd, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");
    if (fread(&vector_size, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");

    BDDVAR vec_to_bddvar[bits_per_integer * vector_size];
    BDDVAR prime_vec_to_bddvar[bits_per_integer * vector_size];
    if (fread(vec_to_bddvar, sizeof(BDDVAR), bits_per_integer * vector_size, f) != bits_per_integer * vector_size)
        Abort("Invalid input file!\n");
    if (fread(prime_vec_to_bddvar, sizeof(BDDVAR), bits_per_integer * vector_size, f) != bits_per_integer * vector_size)
        Abort("Invalid input file!\n");

    LACE_ME;

    rel_t rel = (rel_t)malloc(sizeof(struct relation));
    rel->bdd = sylvan_ref(sylvan_serialize_get_reversed(bdd));
    BDD x = sylvan_ref(sylvan_set_fromarray(vec_to_bddvar, bits_per_integer * vector_size));
    BDD x2 = sylvan_ref(sylvan_set_fromarray(prime_vec_to_bddvar, bits_per_integer * vector_size));
    rel->variables = sylvan_ref(sylvan_set_addall(x, x2));
    sylvan_deref(x);
    sylvan_deref(x2);

    return rel;
}

static void
print_example(BDD example)
{
    char str[vector_size * bits_per_integer];
    size_t i, j;

    LACE_ME;

    if (example != sylvan_false) {
        sylvan_sat_one(example, vector_variables, vector_size * bits_per_integer, str);
        printf("[");
        for (i=0; i<vector_size; i++) {
            uint32_t res = 0;
            for (j=0; j<bits_per_integer; j++) {
                if (str[bits_per_integer*i+j] == 1) res++;
                res<<=1;
            }
            if (i>0) printf(",");
            printf("%" PRIu32, res);
        }
        printf("]");
    }
}

/* Straight-forward implementation of parallel reduction */
TASK_5(BDD, go_par, BDD, cur, BDD, visited, size_t, from, size_t, len, BDD*, deadlocks)
{
    if (len == 1) {
        // Calculate NEW successors (not in visited)
        BDD succ = sylvan_ref(sylvan_relprod_paired(cur, next[from]->bdd, next[from]->variables));
        if (deadlocks) {
            // check which BDDs in deadlocks do not have a successor in this relation
            BDD anc = sylvan_ref(sylvan_relprod_paired_prev(succ, next[from]->bdd, next[from]->variables));
            *deadlocks = sylvan_ref(sylvan_diff(*deadlocks, anc));
            sylvan_deref(anc);
        }
        BDD result = sylvan_ref(sylvan_diff(succ, visited));
        sylvan_deref(succ);
        return result;
    } else {
        BDD deadlocks_left;
        BDD deadlocks_right;
        if (deadlocks) {
            deadlocks_left = *deadlocks;
            deadlocks_right = *deadlocks;
        }

        // Recursively calculate left+right
        SPAWN(go_par, cur, visited, from, (len+1)/2, deadlocks ? &deadlocks_left: NULL);
        BDD right = CALL(go_par, cur, visited, from+(len+1)/2, len/2, deadlocks ? &deadlocks_right : NULL);
        BDD left = SYNC(go_par);

        // Merge results of left+right
        BDD result = sylvan_ref(sylvan_or(left, right));
        sylvan_deref(left);
        sylvan_deref(right);

        if (deadlocks) {
            *deadlocks = sylvan_ref(sylvan_and(deadlocks_left, deadlocks_right));
            sylvan_deref(deadlocks_left);
            sylvan_deref(deadlocks_right);
        }

        return result;
    }
}

/* PAR strategy, parallel strategy (operations called in parallel *and* parallelized by Sylvan) */
VOID_TASK_1(par, set_t, set)
{
    BDD visited = set->bdd;
    BDD new = sylvan_ref(visited);
    size_t counter = 1;
    do {
        printf("Level %zu... ", counter++);
        if (report_levels) {
            printf("%zu states... ", (size_t)sylvan_satcount(visited, set->variables));
        }

        // calculate successors in parallel
        BDD cur = new;
        BDD deadlocks = cur;
        new = CALL(go_par, cur, visited, 0, next_count, check_deadlocks ? &deadlocks : NULL);
        sylvan_deref(cur);

        if (check_deadlocks) {
            printf("found %zu deadlock states... ", (size_t)sylvan_satcount(deadlocks, set->variables));
            if (deadlocks != sylvan_false) {
                printf("example: ");
                print_example(deadlocks);
                printf("... ");
                check_deadlocks = 0;
            }
        }

        // visited = visited + new
        BDD old_visited = visited;
        visited = sylvan_ref(sylvan_or(visited, new));
        sylvan_deref(old_visited);

        if (report_table) {
            size_t filled, total;
            sylvan_table_usage(&filled, &total);
            printf("done, table: %0.1f%% full (%zu nodes).\n", 100.0*(double)filled/total, filled);
        } else {
            printf("done.\n");
        }
    } while (new != sylvan_false);
    sylvan_deref(new);
    set->bdd = visited;
}

/* Sequential version of merge-reduction */
TASK_5(BDD, go_bfs, BDD, cur, BDD, visited, size_t, from, size_t, len, BDD*, deadlocks)
{
    if (len == 1) {
        // Calculate NEW successors (not in visited)
        BDD succ = sylvan_ref(sylvan_relprod_paired(cur, next[from]->bdd, next[from]->variables));
        if (deadlocks) {
            // check which BDDs in deadlocks do not have a successor in this relation
            BDD anc = sylvan_ref(sylvan_relprod_paired_prev(succ, next[from]->bdd, next[from]->variables));
            *deadlocks = sylvan_ref(sylvan_diff(*deadlocks, anc));
            sylvan_deref(anc);
        }
        BDD result = sylvan_ref(sylvan_diff(succ, visited));
        sylvan_deref(succ);
        return result;
    } else {
        BDD deadlocks_left;
        BDD deadlocks_right;
        if (deadlocks) {
            deadlocks_left = *deadlocks;
            deadlocks_right = *deadlocks;
        }

        // Recursively calculate left+right
        BDD left = CALL(go_bfs, cur, visited, from, (len+1)/2, deadlocks ? &deadlocks_left : NULL);
        BDD right = CALL(go_bfs, cur, visited, from+(len+1)/2, len/2, deadlocks ? &deadlocks_right : NULL);

        // Merge results of left+right
        BDD result = sylvan_ref(sylvan_or(left, right));
        sylvan_deref(left);
        sylvan_deref(right);

        if (deadlocks) {
            *deadlocks = sylvan_ref(sylvan_and(deadlocks_left, deadlocks_right));
            sylvan_deref(deadlocks_left);
            sylvan_deref(deadlocks_right);
        }

        return result;
    }
}

/* BFS strategy, sequential strategy (but operations are parallelized by Sylvan) */
VOID_TASK_1(bfs, set_t, set)
{
    BDD visited = set->bdd;
    BDD new = sylvan_ref(visited);
    size_t counter = 1;
    do {
        printf("Level %zu... ", counter++);
        if (report_levels) {
            printf("%zu states... ", (size_t)sylvan_satcount(visited, set->variables));
        }

        BDD cur = new;
        BDD deadlocks = cur;
        new = CALL(go_bfs, cur, visited, 0, next_count, check_deadlocks ? &deadlocks : NULL);
        sylvan_deref(cur);

        if (check_deadlocks) {
            printf("found %zu deadlock states... ", (size_t)sylvan_satcount(deadlocks, set->variables));
            if (deadlocks != sylvan_false) {
                printf("example: ");
                print_example(deadlocks);
                printf("... ");
                check_deadlocks = 0;
            }
        }

        // visited = visited + new
        BDD old_visited = visited;
        visited = sylvan_ref(sylvan_or(visited, new));
        sylvan_deref(old_visited);

        if (report_table) {
            size_t filled, total;
            sylvan_table_usage(&filled, &total);
            printf("done, table: %0.1f%% full (%zu nodes).\n", 100.0*(double)filled/total, filled);
        } else {
            printf("done.\n");
        }
    } while (new != sylvan_false);
    sylvan_deref(new);
    set->bdd = visited;
}

/* Obtain current wallclock time */
static double
wctime()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec + 1E-6 * tv.tv_usec);
}

int
main(int argc, char **argv)
{
    // Filename in argv[0]
    if (argc == 1) {
        fprintf(stderr, "Usage: mc <filename>\n");
        return -1;
    }

    FILE *f = fopen(argv[1], "r");
    if (f == NULL) {
        fprintf(stderr, "Cannot open file '%s'!\n", argv[1]);
        return -1;
    }

    // Init Lace
    lace_init(0, 1000000); // auto-detect number of workers, use a 1,000,000 size task queue
    lace_startup(0, NULL, NULL); // auto-detect program stack, do not use a callback for startup

    // Init Sylvan
    // Nodes table size: 24 bytes * 2**N_nodes
    // Cache table size: 36 bytes * 2**N_cache
    // With: N_nodes=25, N_cache=24: 1.3 GB memory
    sylvan_init(25, 24, 6); // granularity 6 is decent default value - 1 means "use cache for every operation"

    // Read and report domain info (integers per vector and bits per integer)
    if (fread(&vector_size, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");
    if (fread(&bits_per_integer, sizeof(size_t), 1, f) != 1) Abort("Invalid input file!\n");

    printf("Vector size: %zu\n", vector_size);
    printf("Bits per integer: %zu\n", bits_per_integer);
    printf("Number of BDD variables: %zu\n", vector_size * bits_per_integer);

    // Read mapping vector variable to BDD variable
    vector_variables = (BDDVAR*)malloc(sizeof(BDDVAR) * bits_per_integer * vector_size);
    if (fread(vector_variables, sizeof(BDDVAR), bits_per_integer * vector_size, f) != bits_per_integer * vector_size)
        Abort("Invalid input file!\n");

    // Skip some unnecessary data (mapping of primed vector variables to BDD variables)
    if (fseek(f, bits_per_integer * vector_size * sizeof(BDDVAR), SEEK_CUR) != 0) Abort("Invalid input file!\n");

    // Read initial state
    printf("Loading initial state... ");
    fflush(stdout);
    set_t states = set_load(f);
    printf("done.\n");

    // Read transitions
    if (fread(&next_count, sizeof(int), 1, f) != 1) Abort("Invalid input file!\n");
    next = (rel_t*)malloc(sizeof(rel_t) * next_count);

    printf("Loading transition relations... ");
    fflush(stdout);
    int i;
    for (i=0; i<next_count; i++) {
        next[i] = rel_load(f);
        printf("%d, ", i);
        fflush(stdout);
    }
    fclose(f);
    printf("done.\n");

    // Report statistics
    printf("Read file '%s'\n", argv[1]);
    printf("%zu integers per state, %zu bits per integer, %d transition groups\n", vector_size, bits_per_integer, next_count);
    printf("BDD nodes:\n");
    printf("Initial states: %zu BDD nodes\n", sylvan_nodecount(states->bdd));
    for (i=0; i<next_count; i++) {
        printf("Transition %d: %zu BDD nodes\n", i, sylvan_nodecount(next[i]->bdd));
    }

    // Run garbage collection
    sylvan_gc();

    LACE_ME;

    if (run_par) {
        double t1 = wctime();
        CALL(par, states);
        double t2 = wctime();
        printf("PAR Time: %f\n", t2-t1);
    } else {
        double t1 = wctime();
        CALL(bfs, states);
        double t2 = wctime();
        printf("BFS Time: %f\n", t2-t1);
    }

    // Now we just have states
    printf("Final states: %zu states\n", (size_t)sylvan_satcount(states->bdd, states->variables));
    printf("Final states: %zu BDD nodes\n", sylvan_nodecount(states->bdd));

    sylvan_report_stats();

    return 0;
}
