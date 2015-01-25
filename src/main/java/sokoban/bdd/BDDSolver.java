package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import sokoban.Field;
import sokoban.Solver;

public class BDDSolver implements Solver {

  private final BDDBuilder mBDDBuilder;
  private final TransitionBuilder mTransitionBuilder;

  private final BDD mInitialBDD;
  private final BDD mGoalBDD;

  private final BDD mVariableSet;
  private final BDDPairing mPairing;

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
  public void solve() throws IOException {
     /*
     * We now start the breadth first search, using variant 2 in the sheets.
     * We also store the intermediate BDDs to allow for backtracking
     */
    List<BDD> intermediateBDDs = new LinkedList<>();

    BDD vOld = mBDDBuilder.zero();
    BDD vNew = mInitialBDD;
    while (!vOld.equals(vNew) && vNew.and(mGoalBDD).satCount(mVariableSet) == 0) {
      intermediateBDDs.add(vNew);
      vOld = vNew;
      vNew = vOld.or(vOld.relprod(mTransitionBuilder.allTransitions(), mVariableSet).replace(mPairing));
    }
    if (vNew.and(mGoalBDD).satCount(mVariableSet) == 0) {
      System.out.println("No solution found");
    } else {
      System.out.println("Solution found!");
      System.out.println(findTrace(intermediateBDDs));
    }
  }

  //TODO could be implemented better so it works faster
  // Currently it take the same amount of time as search to get the solution.
  private String findTrace(final List<BDD> intermediateBDDs) {
    StringBuilder stringBuilder = new StringBuilder();
    ListIterator<BDD> iterator = intermediateBDDs.listIterator(intermediateBDDs.size());

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

  private boolean leadsToGoalState(final BDD from, final String moves) {
    BDD test = from;
    for (int i = 0; i < moves.length(); i++) {
      switch (moves.charAt(i)) {
        case 'l':
          test = test.relprod(mTransitionBuilder.leftTransition(), mVariableSet).replace(mPairing);
          break;
        case 'u':
          test = test.relprod(mTransitionBuilder.upTransition(), mVariableSet).replace(mPairing);
          break;
        case 'r':
          test = test.relprod(mTransitionBuilder.rightTransition(), mVariableSet).replace(mPairing);
          break;
        case 'd':
          test = test.relprod(mTransitionBuilder.downTransition(), mVariableSet).replace(mPairing);
          break;
        default:
          throw new IllegalStateException("Illegal character in lurd string!");
      }
    }
    return !test.and(mGoalBDD).isZero();
  }

}
