package sokoban;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Main {

  private static BDDFactory sFactory;

  private static int width;

  private static BDD screenBDD;

  private static BDD goalBDD;

  // Screen: @_$.

  private enum VariableType {

    MAN, BOX, MAN_PRIME, BOX_PRIME

  }

  public static void main(final String[] args) throws IOException {
    if (args.length < 0) {
      throw new IllegalArgumentException("Provide an input file!");
    }

    sFactory = BDDFactory.init(4, 4);

    String[] strings = readLines(args[0]);
    Field[][] screen = new Parser().parse(strings);

    initScreen(screen);

    screenBDD.printDot();
    goalBDD.printDot();

    screenBDD.and(goalBDD).printDot();

  }

  private static String[] readLines(final String filePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
      List<String> lines = reader.lines().map(line -> line).collect(toList());
      return lines.toArray(new String[lines.size()]);
    }
  }

  private static void initScreen(final Field[][] screen) {
    sFactory.setVarNum(screen.length * screen[0].length * 2);

    screenBDD = sFactory.one();
    goalBDD = sFactory.one();

    for (int i = 0; i < screen.length; i++) {
      for (int j = 0; j < screen[0].length; j++) {
        switch (screen[i][j]) {
          case MAN_ON_GOAL:
            goalBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case MAN:
            screenBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.MAN))).andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)).not());
            break;
          case BLOCK_ON_GOAL:
            goalBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case BLOCK:
            screenBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.MAN)).not()).andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)));
            break;
          case GOAL:
            goalBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)));
            //noinspection fallthrough
          case EMPTY:
            screenBDD.andWith(sFactory.ithVar(translate(i, j, VariableType.MAN)).not()).andWith(sFactory.ithVar(translate(i, j, VariableType.BOX)).not());
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
  static int translate(int i, int j, VariableType type) {
    return width * i * 2 + j * 2 + type.ordinal();
  }

}
