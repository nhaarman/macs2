package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import sokoban.Field;
import sokoban.Solver;

public class BDDSolver implements Solver {

  private final BDDBuilder mBDDBuilder;
  private final TransitionBuilder mTransitionBuilder;
  private final BDD mGoalBDD;
  private final BDD mVariableSet;
  private final BDDPairing mPairing;
  private BDD mInitialBDD;
  private boolean mSolutionFound;
  private List<BDD> mIntermediateBDDs;
  private String mInitialLurd;

  public BDDSolver(final Field[][] fields) {
    Screen screen = new Screen(fields);

    mBDDBuilder = new BDDBuilder(screen);
    mTransitionBuilder = new TransitionBuilder(mBDDBuilder, screen);

    mInitialBDD = mBDDBuilder.createScreenBDD();
    mGoalBDD = mBDDBuilder.createGoalBDD();

    mVariableSet = mBDDBuilder.getVariableSet();
    mPairing = mBDDBuilder.getPairing();
  }

  @Override
  public boolean solve() {
    return solve("");
  }

  @Override
  public boolean solve(final String initialLurd) {
    mInitialLurd = initialLurd;
    mInitialBDD = applyLurd(mInitialBDD, initialLurd);

    /*
     * We now start the breadth first search, using variant 2 in the sheets.
     * We also store the intermediate BDDs to allow for backtracking
     */
    mIntermediateBDDs = new LinkedList<>();

    BDD vOld = mBDDBuilder.zero();
    BDD vNew = mInitialBDD;
    while (!vOld.equals(vNew) && vNew.and(mGoalBDD).satCount(mVariableSet) == 0) {
      mIntermediateBDDs.add(vNew);
      vOld = vNew;
      vNew = vOld.or(applyTransition(vOld, mTransitionBuilder.allTransitions()));
    }

    mSolutionFound = vNew.and(mGoalBDD).satCount(mVariableSet) != 0;
    return mSolutionFound;

  }

  @Override
  public String getLurd() {
    if (!mSolutionFound) {
      throw new IllegalStateException("No solution found!");
    }

    return mInitialLurd + findTrace();
  }

  //TODO could be implemented better so it works faster
  // Currently it take the same amount of time as search to get the solution.
  private String findTrace() {
    StringBuilder stringBuilder = new StringBuilder();
    ListIterator<BDD> iterator = mIntermediateBDDs.listIterator(mIntermediateBDDs.size());

    while (iterator.hasPrevious()) {
      BDD currentState = iterator.previous();

      if (leadsToGoalState(currentState, "l" + stringBuilder)) {
        stringBuilder.insert(0, 'l');
      } else if (leadsToGoalState(currentState, "u" + stringBuilder)) {
        stringBuilder.insert(0, 'u');
      } else if (leadsToGoalState(currentState, "r" + stringBuilder)) {
        stringBuilder.insert(0, 'r');
      } else if (leadsToGoalState(currentState, "d" + stringBuilder)) {
        stringBuilder.insert(0, 'd');
      } else {
        throw new IllegalStateException("Cannot backtrack current state!");
      }
    }

    return stringBuilder.toString();
  }

  private boolean leadsToGoalState(final BDD from, final String lurd) {
    return !applyLurd(from, lurd).and(mGoalBDD).isZero();
  }

  private BDD applyLurd(final BDD from, final String lurd) {
    BDD result = from;
    for (int i = 0; i < lurd.length(); i++) {
      BDD transition;
      switch (lurd.charAt(i)) {
        case 'l':
          transition = mTransitionBuilder.leftTransition();
          break;
        case 'u':
          transition = mTransitionBuilder.upTransition();
          break;
        case 'r':
          transition = mTransitionBuilder.rightTransition();
          break;
        case 'd':
          transition = mTransitionBuilder.downTransition();
          break;
        default:
          throw new IllegalArgumentException("Invalid character in lurd string: " + lurd.charAt(i));
      }
      result = applyTransition(result, transition);
    }
    return result;
  }

  private BDD applyTransition(final BDD bdd, final BDD transition) {
    return bdd.relprod(transition, mVariableSet).replace(mPairing);
  }

}
