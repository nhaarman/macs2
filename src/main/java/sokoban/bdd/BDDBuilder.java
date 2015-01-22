package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

import sokoban.Field;

import static sokoban.bdd.Utils.not;
import static sokoban.bdd.VariableType.BOX;
import static sokoban.bdd.VariableType.MAN;

class BDDBuilder {

  private final BDDFactory mFactory;

  private final int mScreenWidth;

  private final int mScreenHeight;

  BDDBuilder(final BDDFactory factory, final int screenWidth, final int screenHeight) {
    mFactory = factory;
    mScreenWidth = screenWidth;
    mScreenHeight = screenHeight;
  }

  /**
   * Creates a BDD representing the given state.
   *
   * @param state The state to create a BDD for.
   *
   * @return The BDD.
   */
  public BDD toBDD(final State state) {
    BDD result = mFactory.one();

    Field[][] fields = state.getFields();
    for (int i = 0; i < fields.length; i++) {
      for (int j = 0; j < fields[0].length; j++) {
        switch (fields[i][j]) {
          case MAN_ON_GOAL:
          case MAN:
            result.andWith(varOf(i, j, MAN)).andWith(not(varOf(i, j, BOX)));
            break;
          case BLOCK_ON_GOAL:
          case BLOCK:
            result.andWith(not(varOf(i, j, MAN))).andWith(varOf(i, j, BOX));
            break;
          case GOAL:
          case EMPTY:
            result.andWith(not(varOf(i, j, MAN))).andWith(not(varOf(i, j, BOX)));
            break;
          case WALL:
            /* We ignore walls */
        }
      }
    }

    return result;
  }

  /**
   * Returns a BDD representing the goal state, based on given state.
   *
   * @param state The state to create the BDD for.
   *
   * @return The BDD.
   */
  public BDD getGoalBDD(final State state) {
    BDD result = mFactory.one();

    Field[][] fields = state.getFields();
    for (int i = 0; i < fields.length; i++) {
      for (int j = 0; j < fields[0].length; j++) {
        switch (fields[i][j]) {
          case MAN_ON_GOAL:
            result.andWith(varOf(i, j, BOX));
            break;
          case BLOCK_ON_GOAL:
            result.andWith(varOf(i, j, BOX));
            break;
          case GOAL:
            result.andWith(varOf(i, j, BOX));
            break;
          case MAN:
          case BLOCK:
          case EMPTY:
          case WALL:
        }
      }
    }

    return result;
  }

  /**
   * Creates a variable set containing all the main variables.
   */
  public BDD variableSet() {
    int[] vars = new int[mScreenWidth * mScreenHeight * 2];

    int i = 0;
    for (int j = 0; j < mScreenWidth * mScreenHeight; j++) {
      vars[i] = 4 * j;
      vars[i + 1] = 4 * j + 1;
      i += 2;
    }

    return mFactory.makeSet(vars);
  }

  public BDDPairing getPairing() {
    BDDPairing pairing = mFactory.makePair();

    for (int i = 0; i < mScreenWidth * mScreenHeight; i++) {
      pairing.set(i * 4 + 2, i * 4);
      pairing.set(i * 4 + 3, i * 4 + 1);
    }

    return pairing;
  }

  /**
   * Returns the variable that represents given position and variable type.
   *
   * @param i    The row number, starting from 0.
   * @param j    The column number, starting from 0.
   * @param type The variable type.
   *
   * @return The variable.
   */
  public BDD varOf(final int i, final int j, final VariableType type) {
    return mFactory.ithVar(translate(i, j, type));
  }

  /**
   * Translates a square position and variable type to a variable number.
   *
   * @param i    The row number, starting from 0.
   * @param j    The column number, starting from 0.
   * @param type The variable type.
   *
   * @return The variable number in the BDD
   */
  private int translate(final int i, final int j, final VariableType type) {
    return mScreenWidth * 4 * i + j * 4 + type.ordinal();
  }


}
