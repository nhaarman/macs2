package test;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

public class Main {

  private final BDDFactory mFactory;

  private final BDD s0;

  private final BDD s1;

  private final BDD s0p;

  private final BDD s1p;

  private final BDDPairing mPairing;

  private final BDD mSet;

  private final BDD mInit;

  private final BDD mGoal;

  private final BDD mTransitions;

  private final BDD rTransitionA;

  private final BDD rTransitionB;

  private final BDD rTransitionC;

  private final BDD rTransitionD;

  public static void main(final String[] args) {
    /* Get rid of error stream */
    System.err.close();
    
    new Main().run();
  }

  public Main() {
    mFactory = BDDFactory.init(4, 4);

    mFactory.setVarNum(4);

    /**
     * This example starts in (0,0), with goal state (1,1).
     * A valid trace thus is 'bc'.
     *
     * (0,1) --c--> (1,1)
     *   ^            |
     *   |            |
     *   b            d
     *   |            v
     * (0,0) --a--> (1,0)
     *
     *
     * States are encoded as (s0, s1), with s0 and s1 boolean variables.
     * For s0' and s1', the names s0p and s1p are used.
     */


    /* First setup the variables */
    s0 = mFactory.ithVar(0);
    s1 = mFactory.ithVar(1);
    s0p = mFactory.ithVar(2);
    s1p = mFactory.ithVar(3);

    /*
     * The 'original' variables should be paired with their 'prime' variables.
     * This pairing enables replacing s0' with s0 and s1' with s0 after applying the relational product operation.
     */
    mPairing = mFactory.makePair();
    mPairing.set(2, 0);
    mPairing.set(3, 1);

    /* The set of variables that we use. Note that this only contains s0 and s1 */
    mSet = mFactory.makeSet(new int[]{0, 1});

    /* We start in state (0,0) */
    mInit = not(s0).and(not(s1));
    /* And want to go to (1,1) */
    mGoal = s0.and(s1);

    /* There are four transitions: */
    BDD transitionA = not(s0).and(not(s1)).and(s0p).and(not(s1p)); // From (0,0) to (1,0)
    BDD transitionB = not(s0).and(not(s1)).and(not(s0p)).and(s1p); // From (0,0) to (0,1)
    BDD transitionC = not(s0).and(s1).and(s0p).and(s1p);           // From (0,1) to (1,1)
    BDD transitionD = s0.and(s1).and(s0p).and(not(s1p));           // From (1,1) to (1,0)

    /* We bundle these transitions */
    mTransitions = mFactory.zero()
                           .or(transitionA)
                           .or(transitionB)
                           .or(transitionC)
                           .or(transitionD);
    
    /* Setup reverse transitions */
    rTransitionA = s0.and(not(s1)).and(not(s0p)).and(not(s1p));
    rTransitionB = not(s0).and(s1).and(not(s0p)).and(not(s1p));
    rTransitionC = s0.and(s1).and(not(s0p)).and(s1p);
    rTransitionD = s0.and(not(s1)).and(s0p).and(s1p);
  }

  private void run() {
    /*
     * We now start the breadth first search.
     * This is not exactly the way it was presented on the sheets, but this enables backtracking.
     */
    BDD vOld = mFactory.zero();
    BDD vNew = mInit;
    while (!vOld.equals(vNew) && vNew.and(mGoal).satCount(mSet) == 0) {
      vOld = vNew;
      vNew = vOld.relprod(mTransitions, mSet).replace(mPairing);
    }

    if (vNew.and(mGoal).satCount(mSet) == 0) {
      System.out.println("No solution found");
    } else {
      System.out.println("Solution found!");
      findTrace(vNew);
    }

  }

  private void findTrace(BDD vNew) {
  /*
   * To find a counter example, we need to backtrack from our final state to our initial state.
   * Therefore we setup all transitions in reverse, and try them out.
   * Whenever we did NOT make that transition, the result of relprod turns out as 'zero'.
   */

    /* Find the transitions that have been used to get to this state. */
    StringBuilder backtrack = new StringBuilder();

    /*
     * We loop until we're back in the initial state.
     * In each loop we try each transition until we find a transition that results in non zero,
     * and save this transition.
     */
    while (!vNew.equals(mInit)) {
      BDD tmp = vNew.relprod(rTransitionA, mSet).replace(mPairing);
      if (!tmp.isZero()) {
        backtrack.append('A');
        vNew = tmp;
        continue;
      }

      tmp = vNew.relprod(rTransitionB, mSet).replace(mPairing);
      if (!tmp.isZero()) {
        backtrack.append('B');
        vNew = tmp;
        continue;
      }

      tmp = vNew.relprod(rTransitionC, mSet).replace(mPairing);
      if (!tmp.isZero()) {
        backtrack.append('C');
        vNew = tmp;
        continue;
      }

      tmp = vNew.relprod(rTransitionD, mSet).replace(mPairing);
      if (!tmp.isZero()) {
        backtrack.append('D');
        vNew = tmp;
        continue;
      }
    }

    /* At this point, we have our trace (in reverse). The result should print out "Trace: BC" */
    System.out.println("Trace: " + backtrack.reverse().toString());
  }

  /**
   * A little helper function to increase readability (not(bdd) instead of bdd.not()).
   *
   * @param bdd the BDD to negate.
   *
   * @return The negated BDD.
   */
  private BDD not(final BDD bdd) {
    return bdd.not();
  }


}
