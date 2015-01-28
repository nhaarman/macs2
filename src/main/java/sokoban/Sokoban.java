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
import sokoban.jsoko.JSokoRunner;
import sokoban.parser.Field;
import sokoban.parser.Parser;
import sokoban.utils.FilteredPrintStream;

public class Sokoban {

  private static final Options sOptions;

  static {
    sOptions = new Options();
    sOptions.addOption("h", "help", false, "Shows this help message");
    sOptions.addOption("l", "lurd", true, "Initial lurd string");
    sOptions.addOption("r", false, "Run JSoko if a solution has been found");
    sOptions.addOption(
        OptionBuilder
            .hasArg()
            .withArgName("true/false")
            .withDescription("Filter JavaBDD messages such as garbage collection and cache resizing (default true)")
            .create('f')
    );
  }

  private Sokoban() {
  }

  public static void main(final String[] args) throws IOException, ParseException {
    System.setOut(new FilteredPrintStream(System.out));

    CommandLine cmd = parseArguments(args);

    if (cmd.hasOption('h') || args.length == 0 || args[0].charAt(0) == '-') {
      new HelpFormatter().printHelp("sokoban.Sokoban", sOptions);
      return;
    }

    if (!cmd.hasOption('f') || "true".equals(cmd.getOptionValue('f'))) {
      System.setErr(new FilteredPrintStream(System.err));
    }

    String initialLurd = getInitialLurd(cmd);

    File file = new File(args[0]);
    if (!file.exists()) {
      System.err.println("File does not exist");
      return;
    }

    Field[][] fields = new Parser().parse(file);

    BDDSolver solver = new BDDSolver(fields);
    long start = System.currentTimeMillis();
    boolean hasSolution = solver.solve(initialLurd);
    if (hasSolution) {
      findTrace(cmd, file, solver, start);
    } else {
      if (!initialLurd.isEmpty()) {
        System.err.println("No solution for initial lurd: " + initialLurd);
      }
      System.out.println("no solution");
    }

    System.exit(hasSolution ? 0 : 1);
  }

  private static void findTrace(final CommandLine cmd, final File file, final BDDSolver solver, final long start) throws IOException {
    System.err.println("Puzzle has a solution. Finding lurd. (Took " + (System.currentTimeMillis() - start) + "ms)");
    String lurd = solver.getLurd();
    System.out.println(lurd);

    long time = System.currentTimeMillis() - start;
    System.err.println("Total time: " + time + "ms");

    if (cmd.hasOption("r")) {
      new JSokoRunner().run(file, lurd);
    }
  }

  private static CommandLine parseArguments(final String[] args) throws ParseException {
    CommandLineParser parser = new BasicParser();
    CommandLine cmd = parser.parse(sOptions, args);

    return cmd;
  }

  private static String getInitialLurd(final CommandLine cmd) {
    String initialLurd = "";

    if (cmd.hasOption('l')) {
      initialLurd = cmd.getOptionValue('l');
      if (!isValidLurd(initialLurd)) {
        throw new IllegalArgumentException("Invalid initial lurd string: " + initialLurd);
      }
    }

    return initialLurd;
  }


  private static boolean isValidLurd(final String lurd) {
    return lurd.chars().filter(c -> c != 'l' && c != 'u' && c != 'r' && c != 'd').count() == 0;
  }
}
