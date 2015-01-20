package sokoban;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class Sokoban {

  private final Parser mParser = new Parser();

  private final SmvWriter mSmvWriter = new SmvWriter();

  public static void main(final String[] args) throws IOException {
    if (args.length < 1) {
      throw new IllegalArgumentException("Usage: sokoban.Sokoban <input_file>");
    }
    new Sokoban().parseAndConvert(args[0]);
  }

  private void parseAndConvert(final String filePath) throws IOException {
    String[] lines = readLines(filePath);
    Field[][] screen = mParser.parse(lines);

    String smvFilePath = filePath + ".smv";

    mSmvWriter.writeSmv(screen, smvFilePath);
    executeNuSMVIfWanted(smvFilePath);
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

  private String[] readLines(final String filePath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
      List<String> lines = reader.lines().map(line -> line).collect(toList());
      return lines.toArray(new String[lines.size()]);
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
        previousWasInput=true;
      } else if (!s.contains("Input")) {
        previousWasInput = false;
        previous = s.trim();
        System.out.print(previous);
      }
    }
  }
}
