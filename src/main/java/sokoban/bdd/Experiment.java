package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
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
   private final State initStateFields;

   public static void main(String[] args)
           throws IOException
   {
      BDDFactory factory = BDDFactory.init(10000, 10000);
      factory.setVarNum(4 * 2 * 4);

      String filePath = "/home/rafal/0_Workspaces/IdeaProjects/macs2/screen.0";

      Experiment experiment = new Experiment(factory, filePath);
      experiment.run();
   }

   public Experiment(final BDDFactory factory, final String filePath)
           throws IOException
   {
      this.factory = factory;
      this.initStateFields = new State((new Parser()).parse(new File(filePath)));
      this.screenHeight = initStateFields.getFields().length;
      this.screenWidth = initStateFields.getFields()[0].length;
      this.noOfBitsForHeight = (int) Math.ceil(Math.log(screenHeight));
      this.noOfBitsForWidth = (int) Math.ceil(Math.log(screenWidth));

      // TODO remove
      System.out.println(screenWidth);
      System.out.println(screenHeight);
      System.out.println(initStateFields);
   }


   public void run()
   {
      // Create fields from file
      BDD initState = createInitState(initStateFields);

   }

   private BDD createInitState(final State state)
   {
      Field[][] fields = state.getFields();
      BDD initStateBDD = factory.one();

      for(int i = 0; i < fields.length; i++)
      {
         for (int j = 0; j < fields[i].length; j++)
         {

         }
      }

      return initStateBDD;
   }

}
