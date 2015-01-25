package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import sokoban.Field;
import sokoban.Parser;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by rafal on 23.01.15.
 */
public class Experiment
{
   private final int screenWidth;
   private final int screenHeight;
   private final int noOfBitsForWidth;
   private final int noOfBitsForHeight;
   private final int varNum;
   private final BDDFactory factory;
   private final State initialStateFields;

   private BDDPairing mPairing;
   private BDDPairing mPairingReversed;
   private BDD mSet;

   private BDD initStateBDD;
   private BDD goalState;

   private BDD allTransitions;
   private BDD upTransition;
   private BDD rightTransition;
   private BDD downTransition;
   private BDD leftTransition;

   public static void main(String[] args)
           throws IOException
   {
      String filePath = "/home/rafal/0_Workspaces/IdeaProjects/Sokoban2/screens/screen.387";

      Experiment experiment = new Experiment(filePath);
      experiment.run();
   }

   ////////////////////////// BUILDER PART ///////////////////////////////

   public Experiment(final String filePath)
           throws IOException
   {
      this.initialStateFields = new State((new Parser()).parse(new File(filePath)));
      this.screenHeight = initialStateFields.getFields().length;
      this.screenWidth = initialStateFields.getFields()[0].length;

      this.noOfBitsForHeight
              = Integer.SIZE - Integer.numberOfLeadingZeros(screenHeight);
      this.noOfBitsForWidth
              = Integer.SIZE - Integer.numberOfLeadingZeros(screenWidth);

      this.factory = BDDFactory.init(10000000, 200000); //TODO why those values?
      this.varNum = noOfBitsForHeight
              + noOfBitsForWidth
              + screenWidth*screenHeight;
      factory.setVarNum(varNum * 2);

      initVariablesSetAndPairing(varNum);
      initInitialStateAndGoalState(initialStateFields);
      initAllTransitions();
   }

   //////////////////////////////////// INITIALIZE VARIABLES AND PAIRING PART
   private void initVariablesSetAndPairing(int varNum)
   {
      mPairing = factory.makePair();
      mPairingReversed = factory.makePair();
      int[] set = new int[varNum];
      for (int i = 0; i < varNum; i++)
      {
         int regVarNo = i*2;
         int pairVarNo = i*2 + 1;

         mPairing.set(pairVarNo, regVarNo);
         mPairingReversed.set(regVarNo, pairVarNo);
         set[i] = regVarNo;
      }
      mSet = factory.makeSet(set);
   }

   //////////////////////////////////// INITIALIZE STATE AND GOAL STATE PART
   private void initInitialStateAndGoalState(final State state)
   {
      Field[][] fields = state.getFields();
      initStateBDD = factory.one();
      goalState = factory.one();

      for(int i = 0; i < fields.length; i++)
      {
         for (int j = 0; j < fields[i].length; j++)
         {
            switch (fields[i][j]) {
               case MAN_ON_GOAL:
                  goalState.andWith(getVar(i, j));
               case MAN:
                  initStateBDD.andWith(createStateForMan(i, j));
                  initStateBDD.andWith(getNVar(i, j));
                  break;
               case BLOCK_ON_GOAL:
                  goalState.andWith(getVar(i, j));
               case BLOCK:
                  initStateBDD.andWith(getVar(i, j));
                  break;
               case GOAL:
                  goalState.andWith(getVar(i, j));
               case EMPTY:
                  initStateBDD.andWith(getNVar(i, j));
                  break;
               case WALL:
               /* We ignore walls */
            }
         }
      }
   }

   private BDD createStateForMan(int row, int column)
   {
      // TODO check if the order matters
      BDD result = factory.one();

      Boolean[] rowBool = intToBits(noOfBitsForHeight, row);
      Boolean[] colBool = intToBits(noOfBitsForWidth, column);
      Boolean[] both = Utils.concat(rowBool, colBool);

      for (int i = 0; i < both.length; i++)
      {
         if (both[i])
            result.andWith(factory.ithVar(i*2));
         else
            result.andWith(factory.nithVar(i * 2));
      }

      return result;
   }

   private Boolean[] intToBits(int noOfBits, int integer)
   {
      Boolean[] bits = new Boolean[noOfBits];
      for (int i = noOfBits-1; i >= 0; i--) {
         bits[i] = (integer & (1 << i)) != 0;
      }
      return bits;
   }


   //TODO implement this in a more elegant way
   private BDD getVar(final int i, final int j)
   {
      return factory.ithVar(translate(i, j) * 2);
   }

   private BDD getNVar(final int i, final int j)
   {
      return factory.nithVar(translate(i, j) * 2);
   }

   private BDD getVarPri(final int i, final int j)
   {
      return factory.ithVar(translate(i, j) * 2 + 1);
   }

   private BDD getNVarPri(final int i, final int j)
   {
      return factory.nithVar(translate(i, j) * 2 + 1);
   }

   private int translate(final int i, final int j) {
      return screenWidth * i + j + noOfBitsForHeight + noOfBitsForWidth;
   }

   //////////////////////////////////// INITIALIZE TRANSITIONS PART
   private void initAllTransitions()
   {
      this.upTransition = directedTransition(Direction.UP);
      this.rightTransition = directedTransition(Direction.RIGHT);
      this.downTransition = directedTransition(Direction.DOWN);
      this.leftTransition = directedTransition(Direction.LEFT);

      this.allTransitions = upTransition
              .or(rightTransition)
              .or(downTransition)
              .or(leftTransition);
   }

   private BDD directedTransition(final Direction direction)
   {
      BDD allTransitions = factory.zero();
      for (int i = 0; i < screenHeight; i++)
         for (int j = 0; j < screenWidth; j++)
            allTransitions.orWith(directedOnePointTransition(i, j, direction));

      return allTransitions;
   }

   private BDD directedOnePointTransition(final int i, final int j, final Direction direction)
   {
      // TODO make this method smaller
      int[] values = getRowsAndColsForDirection(i, j, direction);
      int row0 = values[0];
      int col0 = values[1];
      int row1 = values[2];
      int col1 = values[3];
      int row2 = values[4];
      int col2 = values[5];

      Field[][] fields = initialStateFields.getFields();
      BDD noMove = factory.zero();

      if (fields[row0][col0] == Field.WALL)
         return noMove;

      if (fields[row1][col1] == Field.WALL)
         return noMove;

      BDD placeNeighbour = getVar(row1, col1);
      BDD moveManOnly = createStateForMan(row0, col0)
              .and(createStateForMan(row1, col1).replace(mPairingReversed))
              .and(sameBlocks());

      if (fields[row2][col2] == Field.WALL)
      {
         return placeNeighbour.ite(
                 noMove, //there is a box so we cannot move there
                 moveManOnly// we can move because it is empty
         );
      }

      BDD placeDoubleNeighbour = getVar(row2, col2);
      Set<Integer> ignore = new HashSet<>();
      ignore.add(translate(row1, col1)*2);
      ignore.add(translate(row2, col2)*2);
      BDD moveManAndBlock = createStateForMan(row0, col0)
              .and(createStateForMan(row1, col1).replace(mPairingReversed))
              .and(getNVarPri(row1, col1))
              .and(getVarPri(row2, col2))
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
   private int[] getRowsAndColsForDirection(final int i, final int j,
                                            final Direction direction)
   {
      switch (direction)
      {
         case DOWN:
            return new int[]{i, j, i+1, j, i+2, j};
         case LEFT:
            return new int[]{i, j, i, j-1, i, j-2};
         case UP:
            return new int[]{i, j, i-1, j, i-2, j};
         case RIGHT:
            return new int[]{i, j, i, j+1, i, j+2};
         default:
            throw new RuntimeException("Invalid direction");
      }
   }

   private BDD sameBlocks()
   {
      return sameBlocksExcept(new HashSet<>());
   }

   private BDD sameBlocksExcept(final Set<Integer> changedBlocks)
   {
      BDD blocks = factory.one();
      int i = (noOfBitsForHeight + noOfBitsForWidth) * 2;
      while(i < varNum*2)
      {
         if (!changedBlocks.contains(i))
         {
            BDD regVar = factory.ithVar(i);
            BDD primedVar = factory.ithVar(i+1);
            blocks.andWith(regVar.biimp(primedVar));
         }
         i += 2;
      }
      return blocks;
   }

   ////////////////////////// BUILDER PART END ///////////////////////////////

   ////////////////////////// SOLVER PART ///////////////////////////////

   public void run()
   {
      /*
       * We now start the breadth first search, using variant 2 in the sheets.
       * We also store the intermediate BDDs to allow for backtracking
       */
      List<BDD> mIntermediateBDDs = new LinkedList<>();

      BDD vOld = factory.zero();
      BDD vNew = initStateBDD;
      while (!vOld.equals(vNew) && vNew.and(goalState).satCount(mSet) == 0) {
         mIntermediateBDDs.add(vNew);
         vOld = vNew;
         vNew = vOld.or(vOld.relprod(allTransitions, mSet).replace(mPairing));
      }
      if (vNew.and(goalState).satCount(mSet) == 0) {
         System.out.println("No solution found");
      } else {
         System.out.println("Solution found!");
         System.out.println(findTrace(mIntermediateBDDs));
      }
   }

   //TODO could be implemented better so it works faster
   // Currently it take the same amount of time as search to get the solution.
   private String findTrace(List<BDD> mIntermediateBDDs)
   {
      StringBuilder stringBuilder = new StringBuilder();
      ListIterator<BDD> iterator = mIntermediateBDDs.listIterator(mIntermediateBDDs.size());

      while (iterator.hasPrevious())
      {
         BDD currentState = iterator.previous();

         if(leadsToGoalState(currentState, "l" + stringBuilder))
            stringBuilder.insert(0, 'l');
         else if(leadsToGoalState(currentState, "u" + stringBuilder))
            stringBuilder.insert(0, 'u');
         else if(leadsToGoalState(currentState, "r" + stringBuilder))
            stringBuilder.insert(0, 'r');
         else if(leadsToGoalState(currentState, "d" + stringBuilder))
            stringBuilder.insert(0, 'd');
         else
            throw new IllegalStateException("Cannot backtrack current state!");
      }

      return stringBuilder.toString();
   }

   private boolean leadsToGoalState(final BDD from, final String moves)
   {
      BDD test = from;
      for (int i = 0; i < moves.length(); i++)
      {
         switch (moves.charAt(i))
         {
            case 'l':
               test = test.relprod(leftTransition, mSet).replace(mPairing);
               break;
            case 'u':
               test = test.relprod(upTransition, mSet).replace(mPairing);
               break;
            case 'r':
               test = test.relprod(rightTransition, mSet).replace(mPairing);
               break;
            case 'd':
               test = test.relprod(downTransition, mSet).replace(mPairing);
               break;
            default:
               throw new IllegalStateException("Illegal character in lurd string!");
         }
      }
      return !test.and(goalState).isZero();
   }

   ////////////////////////// SOLVER PART END ///////////////////////////////
}