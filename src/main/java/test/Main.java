package test;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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

//  private final BDD rTransitionA;
//
//  private final BDD rTransitionB;
//
//  private final BDD rTransitionC;
//
//  private final BDD rTransitionD;
//
//  private final BDD rTransitionE;

  private final List<BDD> mIntermediateBDDs = new LinkedList<>();

  private final BDD mTransitionA;
  private final BDD mTransitionB;
  private final BDD mTransitionC;
  private final BDD mTransitionD;
  private final BDD mTransitionE;
  private final BDD rTransitionA;
  private final BDD rTransitionB;
  private final BDD rTransitionC;
  private final BDD rTransitionD;
  private final BDD rTransitionE;

  public Main() {
    mFactory = BDDFactory.init(4, 4);

    mFactory.setVarNum(4);

    /**
     * This example starts in (0,0), with goal state (1,1).
     * A valid trace thus is 'bc'.
     *
     * (0,1) --c--> (1,1)
     *   ^            ^
     *   |            |
     *   b            d
     *   |            |
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
    mTransitionA = not(s0).and(not(s1)).and(s0p).and(not(s1p)); // From (0,0) to (1,0)
    mTransitionB = not(s0).and(not(s1)).and(not(s0p)).and(s1p); // From (0,0) to (0,1)
    mTransitionC = not(s0).and(s1).and(s0p).and(s1p);           // From (0,1) to (1,1)
    mTransitionD = s0.and(not(s1)).and(s0p).and(s1p);           // From (1,0) to (1,1)
    
    /* An extra transition from (0,0) to (1,1) */
    mTransitionE = not(s0).and(not(s1)).and(s0p).and(s1p);

    /* We bundle these transitions */
    mTransitions = mFactory.zero()
                           .or(mTransitionA)
                           .or(mTransitionB)
                           .or(mTransitionC)
                           .or(mTransitionD)
                           .or(mTransitionE)
    ;

    rTransitionA = s0.and(not(s1)).and(not(s0p)).and(not(s1p));
    rTransitionB = not(s0).and(s1).and(not(s0p)).and(not(s1p));
    rTransitionC = s0.and(s1).and(not(s0p)).and(s1p);
    rTransitionD = s0.and(s1).and(s0p).and(not(s1p));
    rTransitionE = s0.and(s1).and(not(s0p)).and(not(s1p));

    /* With transition E enabled, there are 3 possible solutions: 'bc', 'ad' and 'e'. To get path 'bc' as a solution, comment all references to mTransitionE and rTransitionE.  */
    
  }

  public static void main(final String[] args) {
    new Main().run();
  }

  private void run() {
    /*
     * We now start the breadth first search, using variant 2 in the sheets.
     * We also store the intermediate BDDs to allow for backtracking
     */
    BDD vOld = mFactory.zero();
    BDD vNew = mInit;

    while (!vOld.equals(vNew)) {
      mIntermediateBDDs.add(vNew);
      vOld = vNew;
      vNew = vOld.or(vOld.relprod(mTransitions, mSet).replace(mPairing));
    }

    if (vNew.and(mGoal).satCount(mSet) == 0) {
      System.out.println("No solution found");
    } else {
      System.out.println("Solution found!");
      findTrace(vNew);
    }

  }

  private void findTrace(BDD vNew) {
    StringBuilder stringBuilder = new StringBuilder();

    ListIterator<BDD> iterator = mIntermediateBDDs.listIterator(mIntermediateBDDs.size() - 1);
    BDD goal = mGoal;

    while (iterator.hasPrevious()) {
      BDD a = iterator.previous();
    
      /* Find out which transition led to go from a to b */
      BDD reverseTransition;
      if (canMakeTransition(a, goal, mTransitionA)) {
        stringBuilder.insert(0, 'a');
        reverseTransition = rTransitionA;
      } else if (canMakeTransition(a, goal, mTransitionB)) {
        stringBuilder.insert(0, 'b');
        reverseTransition = rTransitionB;
      } else if (canMakeTransition(a, goal, mTransitionC)) {
        stringBuilder.insert(0, 'c');
        reverseTransition = rTransitionC;
      } else if (canMakeTransition(a, goal, mTransitionD)) {
        stringBuilder.insert(0, 'd');
        reverseTransition = rTransitionD;
      } else if (canMakeTransition(a, goal, mTransitionE)) {
        stringBuilder.insert(0, 'e');
        reverseTransition = rTransitionE;
      } else {
        throw new IllegalStateException("Cannot backtrack current state!");
      }
      goal = goal.relprod(reverseTransition, mSet).replace(mPairing);
    }

    System.out.println(stringBuilder);
  }

  private boolean canMakeTransition(final BDD a, final BDD goal, final BDD transition) {
    return !a.relprod(transition, mSet).replace(mPairing).and(goal).isZero();
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
