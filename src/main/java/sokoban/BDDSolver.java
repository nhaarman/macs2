package sokoban;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.io.IOException;

public class BDDSolver implements Solver {

  private final Field[][] mFields;

  private final BDDFactory mFactory;

  private enum VariableType {MAN, BOX, MAN_PRIME, BOX_PRIME}

  private BDD mScreenBDD;

  private BDD mGoalBDD;

  public BDDSolver(final Field[][] fields) {
    mFields = fields;
    mFactory = BDDFactory.init(4, 4); // TODO: What are good values for this?
  }

  @Override
  public void solve() throws IOException {
    initScreen(mFields);

    mScreenBDD.printDot();
    mGoalBDD.printDot();
  }

  private void initScreen(final Field[][] screen) {
    mFactory.setVarNum(screen.length * screen[0].length * 2);

    mScreenBDD = mFactory.one();
    mGoalBDD = mFactory.one();

    for (int i = 0; i < mFields.length; i++) {
      for (int j = 0; j < mFields[0].length; j++) {
        switch (mFields[i][j]) {
          case MAN_ON_GOAL:
            mGoalBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case MAN:
            mScreenBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.MAN))).andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)).not());
            break;
          case BLOCK_ON_GOAL:
            mGoalBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case BLOCK:
            mScreenBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.MAN)).not()).andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)));
            break;
          case GOAL:
            mGoalBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case EMPTY:
            mScreenBDD.andWith(mFactory.ithVar(translate(i, j, VariableType.MAN)).not()).andWith(mFactory.ithVar(translate(i, j, VariableType.BOX)).not());
            break;
          case WALL:
            /* We ignore walls */
        }
      }
    }
  }

  /**
   * Translates a square position and variable type to a variable number
   *
   * @param i    the row number, starting from 0
   * @param j    the column number, starting from 0
   * @param type the variable type.
   *
   * @return the variable number in the BDD
   */
  private int translate(final int i, final int j, final VariableType type) {
    return mFields[0].length * i * 2 + j * 2 + type.ordinal();
  }

}
