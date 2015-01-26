package sokoban.nusmv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

import sokoban.Field;
import sokoban.Solver;

import static java.util.stream.Collectors.toList;

public class NuSMVSolver implements Solver {

  private final SmvWriter mSmvWriter;

  private final Field[][] mFields;

  private final File mOriginalFile;

  public NuSMVSolver(final Field[][] fields, final File originalFile) {
    mFields = fields;
    mOriginalFile = originalFile;
    mSmvWriter = new SmvWriter();
  }

  @Override
  public boolean solve() throws IOException {
    String smvFilePath = mOriginalFile.getAbsolutePath() + ".smv";

    mSmvWriter.writeSmv(mFields, smvFilePath);
    executeNuSMVIfWanted(smvFilePath);

    return true;
  }

  @Override
  public boolean solve(final String initialLurd) {
    throw new UnsupportedOperationException("Not yet implemented"); // TODO: Implement solve.
  }

  @Override
  public String getLurd() {
    throw new UnsupportedOperationException("Not yet implemented"); // TODO: Implement getLurd.
  }

  private void executeNuSMVIfWanted(final String smvFilePath) throws IOException {
    try (BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in))) {
      String line = null;
      while (line == null || !line.isEmpty() && !line.equalsIgnoreCase("y") && !line.equalsIgnoreCase("n")) {
        System.out.println("Execute NuSMV on generated smv file? (Y/n)");
        line = inputReader.readLine();
        if (line.isEmpty() || line.equalsIgnoreCase("Y")) {
          execNuSMV(smvFilePath);
        }
      }
    }
  }

  private void execNuSMV(final String smvFilePath) throws IOException {
    System.out.println("Executing NuSMV...");
    Process process = Runtime.getRuntime().exec(new String[]{"NuSMV", smvFilePath});

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      List<String> nuSMVOutput = reader.lines()
                                       .filter(
                                           s -> s.contains("move =")
                                               || s.contains("Input")
                                       )
                                       .map(s -> s.replace("move = ", ""))
                                       .collect(toList());
      if (nuSMVOutput.isEmpty()) {
        System.out.println("No counter example found.");
      } else {
        System.out.println("Counter example:");
        System.out.print("  ");
        nuSMVOutput.forEach(new NuSMVOutputConsumer());
      }

    }
    System.out.flush();
  }


  private static class NuSMVOutputConsumer implements Consumer<String> {

    private String previous;

    private boolean previousWasInput;

    @Override
    public void accept(final String s) {
      if (s.contains("Input") && previousWasInput) {
        System.out.print(previous);
        previousWasInput = true;
      } else if (!s.contains("Input")) {
        previousWasInput = false;
        previous = s.trim();
        System.out.print(previous);
      }
    }
  }
}
