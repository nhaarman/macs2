package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import sokoban.Field;
import sokoban.Parser;

import java.io.File;
import java.io.IOException;

import static sokoban.bdd.Utils.not;
import static sokoban.bdd.VariableType.BOX;
import static sokoban.bdd.VariableType.MAN;

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
   private final State initStateFields;

   public static void main(String[] args)
           throws IOException
   {


      String filePath = "/home/rafal/0_Workspaces/IdeaProjects/macs2/screen.0";

      Experiment experiment = new Experiment(filePath);
      experiment.run();
   }

   public Experiment(final String filePath)
           throws IOException
   {
      this.initStateFields = new State((new Parser()).parse(new File(filePath)));
      this.screenHeight = initStateFields.getFields().length;
      this.screenWidth = initStateFields.getFields()[0].length;

      this.noOfBitsForHeight
              = Integer.SIZE - Integer.numberOfLeadingZeros(screenHeight);
      this.noOfBitsForWidth
              = Integer.SIZE - Integer.numberOfLeadingZeros(screenWidth);

      this.factory = BDDFactory.init(10000, 10000);
      int varNum = (noOfBitsForHeight
              + noOfBitsForWidth
              + screenWidth*screenHeight) * RegPri.values().length;
      System.out.println(varNum);
      factory.setVarNum(varNum);

      // TODO remove
      System.out.println(screenWidth);
      System.out.println(screenHeight);
      System.out.println(noOfBitsForWidth);
      System.out.println(noOfBitsForHeight);
      System.out.println(initStateFields);
   }


   public void run()
   {
      // Create fields from file
      BDD initState = createInitState(initStateFields);

      initState.printDot();

   }

   private BDD createInitState(final State state)
   {
      Field[][] fields = state.getFields();
      BDD initStateBDD = factory.one();

      for(int i = 0; i < fields.length; i++)
      {
         for (int j = 0; j < fields[i].length; j++)
         {
            switch (fields[i][j]) {
               case MAN_ON_GOAL:
               case MAN:
                  initStateBDD.andWith(createStateForMan(i, j));
                  break;
               case BLOCK_ON_GOAL:
               case BLOCK:
                  BDD bddForBlock = varOf(i, j, RegPri.REGULAR);
                  initStateBDD.andWith(bddForBlock);
                  break;
               case GOAL:
               case EMPTY:
                  BDD bddForEmpty = varOf(i, j, RegPri.REGULAR).not();
                  initStateBDD.andWith(bddForEmpty);
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
         int manVarNo = i * RegPri.values().length;
         BDD variable = factory.ithVar(manVarNo);
         if (both[i])
            result.andWith(variable);
         else
            result.andWith(variable.not());
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

   public BDD varOf(final int i, final int j, final RegPri type) {
      int variableNumber = translate(i, j, type);
      return factory.ithVar(variableNumber);
   }

   private int translate(final int i, final int j, final RegPri type) {
      int offset = (noOfBitsForHeight + noOfBitsForWidth) * RegPri.values().length;
      return (screenWidth * i + j) * RegPri.values().length
              + type.ordinal()
              + offset;
   }

}

enum RegPri {REGULAR, PRIME}