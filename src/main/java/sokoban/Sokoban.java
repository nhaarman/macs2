package sokoban;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;

import sokoban.bdd.BDDSolver;
import sokoban.nusmv.NuSMVSolver;

public class Sokoban {

  private Sokoban() {
  }

  public static void main(final String[] args) throws IOException, ParseException {
    Options options = new Options();

    options.addOption("h", "help", false, "Shows this help message");
    options.addOption("B", false, "Use the BDDSolver (default)");
    options.addOption("N", false, "Use the NuSMVSolver");
    options.addOption("W", false, "Don't show warnings");

    CommandLineParser parser = new BasicParser();
    CommandLine cmd = parser.parse(options, args);

    if (cmd.hasOption('h') || args.length == 0 || args[0].charAt(0) == '-') {
      new HelpFormatter().printHelp("sokoban.Sokoban", options);
      return;
    }

    if (cmd.hasOption('B') && cmd.hasOption('N')) {
      System.out.println("-N cannot be used in conjunction with -B");
      return;
    }

    File file = new File(args[0]);
    if (!file.exists()) {
      System.out.println("File does not exist");
      return;
    }

    if (cmd.hasOption('W')) {
      System.err.close();
    }

    Field[][] fields = new Parser().parse(file);

    Solver solver;
    if (cmd.hasOption('N')) {
      System.out.println("Using NuSMVSolver to solve sokoban puzzle");
      solver = new NuSMVSolver(fields, file);
    } else {
      System.out.println("Using BDDSolver to solve sokoban puzzle");
      solver = new BDDSolver(fields);
    }

    long start = System.currentTimeMillis();
    solver.solve();
    long time = System.currentTimeMillis() - start;

    System.out.println("Took: " + time + "ms");
  }
}
