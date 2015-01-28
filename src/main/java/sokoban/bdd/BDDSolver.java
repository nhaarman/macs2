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
    mIntermediateBDDs.add(mInitialBDD);

    BDD vOld = mBDDBuilder.zero();
    BDD vNew = mInitialBDD;
    while (!vOld.equals(vNew) && vNew.and(mGoalBDD).satCount(mVariableSet) == 0) {
      vOld = vNew;
      vNew = vOld.or(
          applyTransition(vOld, mTransitionBuilder.allTransitions())
      );
      mIntermediateBDDs.add(vNew);
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


  private String findTrace() {
    StringBuilder stringBuilder = new StringBuilder();
    ListIterator<BDD> iterator = mIntermediateBDDs.listIterator(mIntermediateBDDs.size());

    BDD finalSetOfStates = iterator.previous(); //there is at least initial state
    BDD stateToLookFor = finalSetOfStates.and(mGoalBDD);

    while (iterator.hasPrevious()) {
      BDD previous = iterator.previous();

         /*
         Because we never know whether the box was pushed or man only
         came next to box. The reverse transition produces two possible states.
         Fortunately it can be reduced by "anding" it with set of states
         that is currently examined. As a result we got either the state where
         the box is pulled back or man moves backward without pulling the box.
          */

      if (cameFromThisSide(previous, stateToLookFor, Direction.LEFT)) {
        stringBuilder.insert(0, 'l');
        stateToLookFor = applyTransition(stateToLookFor, mTransitionBuilder.leftTransitionReversed()).and(previous);
      } else if (cameFromThisSide(previous, stateToLookFor, Direction.UP)) {
        stringBuilder.insert(0, 'u');
        stateToLookFor = applyTransition(stateToLookFor, mTransitionBuilder.upTransitionReversed()).and(previous);
      } else if (cameFromThisSide(previous, stateToLookFor, Direction.RIGHT)) {
        stringBuilder.insert(0, 'r');
        stateToLookFor = applyTransition(stateToLookFor, mTransitionBuilder.rightTransitionReversed()).and(previous);
      } else if (cameFromThisSide(previous, stateToLookFor, Direction.DOWN)) {
        stringBuilder.insert(0, 'd');
        stateToLookFor = applyTransition(stateToLookFor, mTransitionBuilder.downTransitionReversed()).and(previous);
      } else {
        throw new IllegalStateException("Cannot backtrack current state!");
      }
    }

    return stringBuilder.toString();
  }

  private boolean cameFromThisSide(final BDD previousState, final BDD
      stateToLookFor, final Direction direction) {
    BDD transition;
    switch (direction) {
      case LEFT:
        transition = mTransitionBuilder.leftTransition();
        break;
      case UP:
        transition = mTransitionBuilder.upTransition();
        break;
      case RIGHT:
        transition = mTransitionBuilder.rightTransition();
        break;
      case DOWN:
        transition = mTransitionBuilder.downTransition();
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid Direction value: " +
                direction
        );
    }

    return !previousState.relprod(transition, mVariableSet).replace
        (mPairing).and(stateToLookFor).isZero();
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
