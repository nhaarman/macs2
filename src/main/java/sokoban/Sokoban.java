package sokoban;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;

import sokoban.bdd.BDDSolver;

public class Sokoban {

  private Sokoban() {
  }

  public static void main(final String[] args) throws IOException, ParseException {
    System.setOut(new FilteredPrintStream(System.out));

    Options options = new Options();

    options.addOption("h", "help", false, "Shows this help message");
    options.addOption("l", "lurd", true, "Initial lurd string");
    options.addOption("r", false, "Run JSoko if a solution has been found");
    options.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("true/false")
            .withDescription("Filter JavaBDD messages such as garbage collection and cache resizing (default true)")
            .create('f')
    );

    CommandLineParser parser = new BasicParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption('h') || args.length == 0 || args[0].charAt(0) == '-') {
      new HelpFormatter().printHelp("sokoban.Sokoban", options);
      return;
    }

    if (!cmd.hasOption('f') || "true".equals(cmd.getOptionValue('f'))) {
      System.setErr(new FilteredPrintStream(System.err));
    }

    String initialLurd = "";
    if (cmd.hasOption('l')) {
      initialLurd = cmd.getOptionValue('l');
      if (!isValidLurd(initialLurd)) {
        System.err.println("Invalid initial lurd string: " + initialLurd);
        return;
      }
    }

    File file = new File(args[0]);
    if (!file.exists()) {
      System.err.println("File does not exist");
      return;
    }

    Field[][] fields = new Parser().parse(file);

    Solver solver = new BDDSolver(fields);
    long start = System.currentTimeMillis();
    boolean hasSolution = solver.solve(initialLurd);
    if (hasSolution) {
      System.err.println("Puzzle has a solution. Finding lurd. (Took " + (System.currentTimeMillis() - start) + "ms)");
      String lurd = solver.getLurd();
      System.out.println(lurd);

      long time = System.currentTimeMillis() - start;
      System.err.println("Total time: " + time + "ms");

      if (cmd.hasOption("r")) {
        new JSokoRunner().run(file, lurd);
      }

    } else {
      if (!initialLurd.isEmpty()) {
        System.err.println("No solution for initial lurd: " + initialLurd);
      }
      System.out.println("no solution");
    }

    System.exit(hasSolution ? 0 : 1);
  }


  private static boolean isValidLurd(final String lurd) {
    return lurd.chars().filter(c -> c != 'l' && c != 'u' && c != 'r' && c != 'd').count() == 0;
  }
}
