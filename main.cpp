#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <time.h>
#include <sys/types.h>
#include <sys/time.h>
#include <inttypes.h>

#include <assert.h>

extern "C" {
	#include "llmsset.h"
	#include "sylvan.h"
	#include "lddmc.h"
}

#if USE_NUMA
#include "numa_tools.h"
#endif

static void
print_example(BDD example)
{
	int vector_size = 16;
	int bits_per_integer = 32;
	BDDVAR* vector_variables = (BDDVAR*) malloc(sizeof(BDDVAR) * bits_per_integer * vector_size);
	
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

int main(int argc, char **argv)
{
    lace_init(0, 1000000);
    lace_startup(0, NULL, NULL);
    
    sylvan_init(25,24,4);
    
    LACE_ME;
    
    BDD a = sylvan_ithvar(1);
    BDD b = sylvan_ithvar(2);
    BDD aAndB = sylvan_and(a,b);
    
    BDDVAR temp[2] = {1, 2};
    BDDSET set = sylvan_set_fromarray(temp, 2);

    std::cout << "1" << std::endl;

    long double satCount = sylvan_satcount(aAndB, set);



    std::cout << "a" << std::endl;

    exit(0);
}
