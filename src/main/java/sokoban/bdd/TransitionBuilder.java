package sokoban.bdd;

import net.sf.javabdd.BDD;

import java.util.HashSet;
import java.util.Set;

import sokoban.Field;

import static sokoban.bdd.Utils.or;
import static sokoban.bdd.VariableType.REGULAR;

public class TransitionBuilder {

  private final BDDBuilder mBDDBuilder;
  private final Screen mScreen;

  private BDD mUpTransition;
  private BDD mRightTransition;
  private BDD mDownTransition;
  private BDD mLeftTransition;

  private BDD mAllTransitions;

  public TransitionBuilder(final BDDBuilder bddBuilder, final Screen screen) {
    mBDDBuilder = bddBuilder;
    mScreen = screen;
  }

  public BDD allTransitions() {
    if (mAllTransitions == null) {
      initAllTransitions();
    }

    return mAllTransitions;
  }

  public BDD leftTransition() {
    if (mLeftTransition == null) {
      initAllTransitions();
    }

    return mLeftTransition;
  }

  public BDD upTransition() {
    if (mUpTransition == null) {
      initAllTransitions();
    }

    return mUpTransition;
  }

  public BDD rightTransition() {
    if (mRightTransition == null) {
      initAllTransitions();
    }

    return mRightTransition;
  }

  public BDD downTransition() {
    if (mDownTransition == null) {
      initAllTransitions();
    }

    return mDownTransition;
  }

  private void initAllTransitions() {
    mUpTransition = directedTransition(Direction.UP);
    mRightTransition = directedTransition(Direction.RIGHT);
    mDownTransition = directedTransition(Direction.DOWN);
    mLeftTransition = directedTransition(Direction.LEFT);

    mAllTransitions = or(mLeftTransition, mUpTransition, mRightTransition, mDownTransition);
  }

  private BDD directedTransition(final Direction direction) {
    BDD allTransitions = mBDDBuilder.zero();
    for (int i = 0; i < mScreen.height(); i++) {
      for (int j = 0; j < mScreen.width(); j++) {
        allTransitions.orWith(directedOnePointTransition(i, j, direction));
      }
    }

    return allTransitions;
  }

  private BDD directedOnePointTransition(final int i, final int j, final Direction direction) {
    // TODO make this method smaller
    int[] values = getRowsAndColsForDirection(i, j, direction);
    int row0 = values[0];
    int col0 = values[1];
    int row1 = values[2];
    int col1 = values[3];
    int row2 = values[4];
    int col2 = values[5];

    Field[][] fields = mScreen.getFields();
    BDD noMove = mBDDBuilder.zero();

    if (fields[row0][col0] == Field.WALL) {
      return noMove;
    }

    if (fields[row1][col1] == Field.WALL) {
      return noMove;
    }

    BDD placeNeighbour = mBDDBuilder.varFor(row1, col1);
    BDD moveManOnly = mBDDBuilder.createStateForMan(row0, col0)
                                 .and(mBDDBuilder.createStateForMan(row1, col1).replace(mBDDBuilder.getReversedPairing()))
                                 .and(sameBlocks());

    if (fields[row2][col2] == Field.WALL) {
      return placeNeighbour.ite(
          noMove, //there is a box so we cannot move there
          moveManOnly// we can move because it is empty
      );
    }

    BDD placeDoubleNeighbour = mBDDBuilder.varFor(row2, col2);
    Set<Integer> ignore = new HashSet<>();
    ignore.add(mBDDBuilder.translate(row1, col1, REGULAR));
    ignore.add(mBDDBuilder.translate(row2, col2, REGULAR));
    BDD moveManAndBlock = mBDDBuilder.createStateForMan(row0, col0)
                                     .and(mBDDBuilder.createStateForMan(row1, col1).replace(mBDDBuilder.getReversedPairing()))
                                     .and(mBDDBuilder.negatedPrimaryVarFor(row1, col1))
                                     .and(mBDDBuilder.primaryVarFor(row2, col2))
                                     .and(sameBlocksExcept(ignore));

    return placeNeighbour.ite(
        placeDoubleNeighbour.ite( //if true then there is a box
                                  noMove, //if true then we cannot move
                                  moveManAndBlock// if false then there is empty space so
                                  // we can move
        ), moveManOnly// we can move because it is empty
    );
  }

  // TODO create a class to be returned
  private int[] getRowsAndColsForDirection(final int i, final int j, final Direction direction) {
    switch (direction) {
      case DOWN:
        return new int[]{i, j, i + 1, j, i + 2, j};
      case LEFT:
        return new int[]{i, j, i, j - 1, i, j - 2};
      case UP:
        return new int[]{i, j, i - 1, j, i - 2, j};
      case RIGHT:
        return new int[]{i, j, i, j + 1, i, j + 2};
      default:
        throw new IllegalArgumentException("Invalid direction");
    }
  }

  private BDD sameBlocks() {
    return sameBlocksExcept(new HashSet<>());
  }

  private BDD sameBlocksExcept(final Set<Integer> changedBlocks) {
    BDD blocks = mBDDBuilder.one();
    int i = (mBDDBuilder.getNoOfBitsForHeight() + mBDDBuilder.getNoOfBitsForWidth()) * 2;
    while (i < mBDDBuilder.getVarNum() * 2) {
      if (!changedBlocks.contains(i)) {
        BDD regVar = mBDDBuilder.ithVar(i);
        BDD primedVar = mBDDBuilder.ithVar(i + 1);
        blocks.andWith(regVar.biimp(primedVar));
      }
      i += 2;
    }
    return blocks;
  }

}
