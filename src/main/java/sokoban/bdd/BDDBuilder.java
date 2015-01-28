package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;

import java.util.stream.IntStream;

import sokoban.parser.Field;
import sokoban.parser.Screen;

import static sokoban.bdd.VariableType.PRIME;
import static sokoban.bdd.VariableType.REGULAR;

class BDDBuilder {

  public static final int NODENUM = 100000;
  public static final int CACHESIZE = 100000;

  private final Screen mScreen;
  private final BDDFactory mFactory;

  private final int mNoOfBitsForHeight;
  private final int mNoOfBitsForWidth;
  private final int mVarNum;

  private BDDPairing mPairing;
  private BDDPairing mPairingReversed;
  private BDD mVariableSet;

  BDDBuilder(final Screen screen) {
    mScreen = screen;

    mNoOfBitsForHeight = Integer.SIZE - Integer.numberOfLeadingZeros(screen.height()-1);
    mNoOfBitsForWidth = Integer.SIZE - Integer.numberOfLeadingZeros(screen.width()-1);

    mVarNum = mNoOfBitsForHeight + mNoOfBitsForWidth + screen.width() * screen.height();

    mFactory = BDDFactory.init(NODENUM, CACHESIZE);
    mFactory.setVarNum(mVarNum * 2);
  }

  /**
   * Creates a BDD representing the given screen.
   *
   * @return The BDD.
   */
  public BDD createScreenBDD() {
    BDD result = mFactory.one();

    Field[][] fields = mScreen.getFields();

    for (int i = 0; i < fields.length; i++) {
      for (int j = 0; j < fields[i].length; j++) {
        switch (fields[i][j]) {
          case MAN_ON_GOAL:
          case MAN:
            result.andWith(createStateForMan(i, j));
            result.andWith(negatedVarFor(i, j));
            break;
          case BOX_ON_GOAL:
          case BOX:
            result.andWith(varFor(i, j));
            break;
          case GOAL:
          case EMPTY:
            result.andWith(negatedVarFor(i, j));
            break;
          case WALL:
            /* We ignore walls */
        }
      }
    }

    return result;
  }

  /**
   * Returns a BDD representing the goal state, based on given screen.
   *
   * @return The BDD.
   */
  public BDD createGoalBDD() {
    BDD result = mFactory.one();

    Field[][] fields = mScreen.getFields();

    for (int i = 0; i < fields.length; i++) {
      for (int j = 0; j < fields[i].length; j++) {
        if (fields[i][j].isGoal()) {
          result.andWith(varFor(i, j));
        }
      }
    }

    return result;
  }

  /**
   * Creates a variable set containing all the main variables.
   */
  public BDD getVariableSet() {
    if (mVariableSet == null) {
      int[] set = IntStream.range(0, mVarNum)
                           .parallel()
                           .map(i -> i * 2)
                           .toArray();
      mVariableSet = mFactory.makeSet(set);
    }

    return mVariableSet;
  }

  public BDDPairing getPairing() {
    if (mPairing == null) {
      createPairings();
    }

    return mPairing;
  }

  void createPairings() {
    mPairing = mFactory.makePair();
    mPairingReversed = mFactory.makePair();

    IntStream.range(0, mVarNum)
             .parallel()
             .forEach(
                 i -> {
                   mPairing.set(2 * i + 1, 2 * i);
                   mPairingReversed.set(2 * i, 2 * i + 1);
                 }
             );
  }

  public BDDPairing getReversedPairing() {
    if (mPairingReversed == null) {
      createPairings();
    }

    return mPairingReversed;
  }

  BDD createStateForMan(final int row, final int column) {
    BDD result = mFactory.one();

    Boolean[] rowBool = intToBits(mNoOfBitsForHeight, row);
    Boolean[] colBool = intToBits(mNoOfBitsForWidth, column);
    Boolean[] both = BDDUtils.concat(rowBool, colBool);

    for (int i = 0; i < both.length; i++) {
      if (both[i]) {
        result.andWith(mFactory.ithVar(i * 2));
      } else {
        result.andWith(mFactory.nithVar(i * 2));
      }
    }

    return result;
  }

  Boolean[] intToBits(final int noOfBits, final int integer) {
    Boolean[] bits = new Boolean[noOfBits];
    for (int i = noOfBits - 1; i >= 0; i--) {
      bits[i] = (integer & 1 << i) != 0;
    }
    return bits;
  }


  public BDD zero() {
    return mFactory.zero();
  }

  public BDD one() {
    return mFactory.one();
  }

  public BDD ithVar(final int i) {
    return mFactory.ithVar(i);
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
  int translate(final int i, final int j, final VariableType type) {
    return (mScreen.width() * i + j + mNoOfBitsForHeight + mNoOfBitsForWidth) * 2 + type.ordinal();
  }

  BDD varFor(final int i, final int j) {
    return mFactory.ithVar(translate(i, j, REGULAR));
  }

  BDD negatedVarFor(final int i, final int j) {
    return mFactory.nithVar(translate(i, j, REGULAR));
  }

  BDD primaryVarFor(final int i, final int j) {
    return mFactory.ithVar(translate(i, j, PRIME));
  }

  BDD negatedPrimaryVarFor(final int i, final int j) {
    return mFactory.nithVar(translate(i, j, PRIME));
  }

  public int getVarNum() {
    return mVarNum;
  }

  public int getNoOfBitsForWidth() {
    return mNoOfBitsForWidth;
  }

  public int getNoOfBitsForHeight() {
    return mNoOfBitsForHeight;
  }
}
