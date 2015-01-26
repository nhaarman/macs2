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

  private BDD mUpTransitionReversed;
  private BDD mRightTransitionReversed;
  private BDD mDownTransitionReversed;
  private BDD mLeftTransitionReversed;

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

  public BDD leftTransitionReversed() {
    if (mLeftTransitionReversed == null) {
      initAllTransitions();
    }

    return mLeftTransitionReversed;
  }

  public BDD upTransitionReversed() {
    if (mUpTransitionReversed == null) {
      initAllTransitions();
    }

    return mUpTransitionReversed;
  }

  public BDD rightTransitionReversed() {
    if (mRightTransitionReversed == null) {
      initAllTransitions();
    }

    return mRightTransitionReversed;
  }

  public BDD downTransitionReversed() {
    if (mDownTransitionReversed == null) {
      initAllTransitions();
    }

    return mDownTransitionReversed;
  }

  private void initAllTransitions() {
    mUpTransition = directedTransition(Direction.UP);
    mRightTransition = directedTransition(Direction.RIGHT);
    mDownTransition = directedTransition(Direction.DOWN);
    mLeftTransition = directedTransition(Direction.LEFT);

    mAllTransitions = or(mLeftTransition, mUpTransition, mRightTransition, mDownTransition);

    mUpTransitionReversed = directedTransitionReversed(Direction.UP);
    mRightTransitionReversed = directedTransitionReversed(Direction.RIGHT);
    mDownTransitionReversed = directedTransitionReversed(Direction.DOWN);
    mLeftTransitionReversed = directedTransitionReversed(Direction.LEFT);
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
    int[] values = getRowsAndColsForDirection(i, j, direction);
    int row0 = i;
    int col0 = j;
    int row1 = values[2];
    int col1 = values[3];
    int row2 = values[4];
    int col2 = values[5];

    Field[][] fields = mScreen.getFields();
    BDD noMove = mBDDBuilder.zero();

    if (fields[row0][col0] == Field.WALL || fields[row1][col1] == Field.WALL) {
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

  private BDD directedTransitionReversed(final Direction direction) {
    BDD allTransitionsReversed = mBDDBuilder.zero();
    for (int i = 0; i < mScreen.height(); i++) {
      for (int j = 0; j < mScreen.width(); j++) {
        allTransitionsReversed.orWith(directedOnePointTransitionReversed(i, j,
                direction));
      }
    }

    return allTransitionsReversed;
  }

  private BDD directedOnePointTransitionReversed(final int i, final int j, final Direction direction)
  {
    int[] values = getRowsAndColsForDirection(i, j, direction);
    int rowb1 = values[0]; //where the man is moving backward
    int colb1 = values[1];
    int row0 = i; //where the man is now
    int col0 = j;
    int row1 = values[2]; //where potentially the box stays
    int col1 = values[3];

    Field[][] fields = mScreen.getFields();
    BDD noMove = mBDDBuilder.zero();

    if (fields[row0][col0] == Field.WALL || fields[rowb1][colb1] == Field.WALL) {
      return noMove;
    }

    BDD placeToMoveBackward = mBDDBuilder.varFor(rowb1, colb1);
    BDD moveManOnly = mBDDBuilder.createStateForMan(row0, col0)
            .and(mBDDBuilder.createStateForMan(rowb1, colb1).replace(mBDDBuilder.getReversedPairing()))
            .and(sameBlocks());

    if (fields[row1][col1] == Field.WALL) {
      return placeToMoveBackward.ite(
              noMove, //there is a box so man did not come from there
              moveManOnly// we can move because it is empty
      );
    }

    BDD placeWherePotentiallyBoxWasPushed = mBDDBuilder.varFor(row1, col1);
    Set<Integer> ignore = new HashSet<>();
    ignore.add(mBDDBuilder.translate(row0, col0, REGULAR));
    ignore.add(mBDDBuilder.translate(row1, col1, REGULAR));
    BDD moveMaWithBlock = mBDDBuilder.createStateForMan(row0, col0)
            .and(mBDDBuilder.createStateForMan(rowb1, colb1).replace(mBDDBuilder.getReversedPairing()))
            .and(mBDDBuilder.negatedPrimaryVarFor(row1, col1))
            .and(mBDDBuilder.primaryVarFor(row0, col0))
            .and(sameBlocksExcept(ignore));

    return placeToMoveBackward.ite(
            noMove,
            placeWherePotentiallyBoxWasPushed.ite(//if true then there is a box
              moveMaWithBlock.or(moveManOnly), //if true then ether block was moved or not. We cannot know that
              moveManOnly// if false then there were no box
    ));
  }

  /**
   *
   * @param i         row index
   * @param j         column index
   * @param direction direction of transfer
   * @return          an array where two first elements are indexes of field
   *                  behind the starting position of a man, next two elements
   *                  are the indexes of a field where man is moving towards,
   *                  and last two elements are indexes of field where bax will
   *                  be moved if th is pushed.
   */
  private int[] getRowsAndColsForDirection(final int i, final int j, final Direction direction) {
    switch (direction) {
      case DOWN:
        return new int[]{i-1, j, i + 1, j, i + 2, j};
      case LEFT:
        return new int[]{i, j+1, i, j - 1, i, j - 2};
      case UP:
        return new int[]{i+1, j, i - 1, j, i - 2, j};
      case RIGHT:
        return new int[]{i, j-1, i, j + 1, i, j + 2};
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
