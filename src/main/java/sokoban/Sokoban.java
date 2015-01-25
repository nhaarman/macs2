package sokoban;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import sokoban.bdd.BDDSolver;
import sokoban.nusmv.NuSMVSolver;

public class Sokoban {

  private Sokoban() {
  }

  public static void main(final String[] args) throws IOException, ParseException {
    System.setErr(new FilteredPrintStream(System.err));
    System.setOut(new FilteredPrintStream(System.out));

    Options options = new Options();

    options.addOption("h", "help", false, "Shows this help message");
    options.addOption("l", "lurd", true, "Initial lurd string");
    options.addOption("B", false, "Use the BDDSolver (default)");
    options.addOption("N", false, "Use the NuSMVSolver");

    CommandLineParser parser = new BasicParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption('h') || args.length == 0 || args[0].charAt(0) == '-') {
      new HelpFormatter().printHelp("sokoban.Sokoban", options);
      return;
    }

    if (cmd.hasOption('B') && cmd.hasOption('N')) {
      System.err.println("-N cannot be used in conjunction with -B");
      return;
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

    Solver solver;
    if (cmd.hasOption('N')) {
      solver = new NuSMVSolver(fields, file);
    } else {
      solver = new BDDSolver(fields);
    }

    long start = System.currentTimeMillis();
    boolean hasSolution = solver.solve(initialLurd);
    if (hasSolution) {
      System.err.println("Puzzle has a solution. Finding lurd. (Took " + (System.currentTimeMillis() - start) + "ms)");
      String lurd = solver.getLurd();
      System.out.println(lurd);
    } else {
      if (!initialLurd.isEmpty()) {
        System.err.println("No solution for initial lurd: " + initialLurd);
      }
      System.out.println("no solution");
    }

    long time = System.currentTimeMillis() - start;

    System.err.println("Total time: " + time + "ms");

    System.exit(hasSolution ? 0 : 1);
  }

  private static boolean isValidLurd(final String lurd) {
    return lurd.chars().filter(c -> c != 'l' && c != 'u' && c != 'r' && c != 'd').count() == 0;
  }
}
