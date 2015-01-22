package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

import java.io.IOException;

import sokoban.Field;
import sokoban.Solver;

import static sokoban.bdd.Utils.not;

public class BDDSolver implements Solver {

  public static final int NODENUM = 100000;
  public static final int CACHESIZE = 100000;

  private final BDDFactory mFactory;
  private final BDDBuilder mBDDBuilder;

  private final State mInitialState;
  private final BDD mInitialBDD;
  private final BDD mGoalBDD;

  public BDDSolver(Field[][] fields) {

    fields = new Field[1][];
    fields[0] = new Field[2];
    fields[0][0] = Field.MAN;
    fields[0][1] = Field.EMPTY;

    mFactory = BDDFactory.init(NODENUM, CACHESIZE);
    mFactory.setVarNum(fields.length * fields[0].length * 4);

    mBDDBuilder = new BDDBuilder(mFactory, fields[0].length, fields.length);

    mInitialState = new State(fields);
    mInitialBDD = mBDDBuilder.toBDD(mInitialState);
    mGoalBDD = mBDDBuilder.getGoalBDD(mInitialState);
  }

  @Override
  public void solve() throws IOException {
//    BDD m0 = mBDDBuilder.varOf(0, 0, VariableType.MAN);
//    BDD b0 = mBDDBuilder.varOf(0, 0, VariableType.BOX);
//
//    BDD m0p = mBDDBuilder.varOf(0, 0, VariableType.MAN_PRIME);
//    BDD b0p = mBDDBuilder.varOf(0, 0, VariableType.BOX_PRIME);
//
//    BDD m1 = mBDDBuilder.varOf(0, 1, VariableType.MAN);
//    BDD b1 = mBDDBuilder.varOf(0, 1, VariableType.BOX);
//
//    BDD m1p = mBDDBuilder.varOf(0, 1, VariableType.MAN_PRIME);
//    BDD b1p = mBDDBuilder.varOf(0, 1, VariableType.BOX_PRIME);
//
//    BDD firstTransition =
//        mFactory.one()
//                .and(m0)
//                .and(not(b0))
//                .and(not(m1))
//                .and(not(b1))
//
//                .and(not(m0p))
//                .and(not(b0p))
//                .and(m1p)
//                .and(not(b1p));
//    BDD variableSet = mBDDBuilder.variableSet();
//    BDDPairing pairing = mBDDBuilder.getPairing();
//
//    BDD secondState = mInitialBDD.relprod(firstTransition, variableSet).replace(pairing);
//
//    mInitialBDD.printDot();
//    secondState.printDot();

  }


}
