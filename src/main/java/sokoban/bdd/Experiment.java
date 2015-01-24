package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import sokoban.Field;
import sokoban.Parser;

import java.io.File;
import java.io.IOException;

/**
 * Created by rafal on 23.01.15.
 */
public class Experiment
{
   private final int screenWidth;
   private final int screenHeight;
   private final int noOfBitsForWidth;
   private final int noOfBitsForHeight;
   private final BDDFactory factory;
   private final State initialStateFields;

   private BDDPairing mPairing;
   private BDDPairing mPairingReversed;
   private BDD mSet;

   private BDD initStateBDD;
   private BDD goalState;

   public static void main(String[] args)
           throws IOException
   {
      String filePath = "/home/rafal/0_Workspaces/IdeaProjects/macs2/screen.0";

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

      this.factory = BDDFactory.init(10000, 10000); //TODO why those values?
      int varNum = noOfBitsForHeight
              + noOfBitsForWidth
              + screenWidth*screenHeight;
      factory.setVarNum(varNum * 2);

      initVariablesSetAndPairing(varNum);
      initInitialStateAndGoalState(initialStateFields);
   }

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

   private BDD initInitialStateAndGoalState(final State state)
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

      return initStateBDD;
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

   ////////////////////////// BUILDER PART END ///////////////////////////////

   ////////////////////////// SOLVER PART ///////////////////////////////

   public void run()
   {
      BDD transRight = transitionRight();
      BDD newState = initStateBDD.relprod(transRight, mSet).replace(mPairing);
      BDD sumState = initStateBDD.or(newState);

      initStateBDD.printDot();
      transRight.printDot();
      newState.printDot();
      sumState.printDot();
   }

   private BDD transitionRight()
   {
      BDD manState = createStateForMan(1, 2);
      BDD manPrimed = createStateForMan(1, 3).replace(mPairingReversed);
      BDD manTrans = manState.and(manPrimed);

      BDD manState2 = createStateForMan(1, 3);
      BDD manPrimed2 = createStateForMan(1, 4).replace(mPairingReversed);
      BDD manTrans2 = manState2.and(manPrimed2);

      return manTrans.or(manTrans2);
   }

   ////////////////////////// SOLVER PART END ///////////////////////////////
}