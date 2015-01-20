package sokoban;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Parser {

  public Field[][] parse(final File file) throws IOException {
    if (!file.exists()) {
      throw new IllegalArgumentException("File does not exist!");
    }

    return parse(readLines(file));
  }

  private Field[][] parse(final String[] lines) {
    System.out.println("Parsing input:");
    for (String line : lines) {
      System.out.println(line);
    }

    // Determine number of rows and columns
    int rows = lines.length;
    if (rows == 0) {
      throw new IllegalArgumentException("No rows in screen!");
    }

    int cols = maxLineLength(lines);
    if (cols == 0) {
      throw new IllegalArgumentException("No columns in screen!");
    }

    return parseScreen(lines, rows, cols);
  }

  private int maxLineLength(final String[] lines) {
    int max = 0;
    for (String line : lines) {
      max = Math.max(max, line.length());
    }
    return max;
  }

  private Field[][] parseScreen(final String[] lines, final int rows, final int cols) {
    Field[][] screen = createMatrix(rows - 2, cols - 2, Field.EMPTY);

    for (int i = 0; i < rows - 2; i++) {
      String s = lines[i + 1];
      Field[] row = screen[i];

      for (int j = 0; j < cols - 2 && j < s.length(); j++) {
        row[j] = Field.convertFromScreenInput(s.charAt(j + 1));
      }
    }

    return screen;
  }

  private Field[][] createMatrix(final int rows, final int cols, final Field initialValue) {
    Field[][] result = new Field[rows][cols];
    for (int i = 0; i < result.length; i++) {
      for (int j = 0; j < result[i].length; j++) {
        result[i][j] = initialValue;
      }
    }
    return result;
  }

  private static String[] readLines(final File file) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      List<String> lines = reader.lines().map(line -> line).collect(toList());
      return lines.toArray(new String[lines.size()]);
    }
  }

}
