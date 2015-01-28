package sokoban.parser;

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

  public Field[][] parse(final String[] lines) {
    System.err.println("Parsing input:");
    for (String line : lines) {
      System.err.println(line);
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
    /*
    I changed this method - it does not to remove now outside wall, so I can
    replace inaccessible empty places outside the walls with walls, so that
    unnecessary variables won't be created for them. When it comes to reducing
    NuSMV variables there also should be implemented ignoring walls,
    so this change would not affect its performance
     */

    //TODO if whole line or column is a wall then remove
    Field[][] screen = createMatrix(rows, cols, Field.EMPTY);

    for (int i = 0; i < rows; i++) {
      String s = lines[i];
      Field[] row = screen[i];

      for (int j = 0; j < cols && j < s.length(); j++) {
        row[j] = Field.convertFromScreenInput(s.charAt(j));
      }

      // Replacing empty spaces outside with walls
      replaceBeginningOfLine(row, Field.WALL, Field.WALL);
      replaceEndOfLine(row, Field.WALL, Field.WALL);
    }

    return screen;
  }

  private void replaceBeginningOfLine(Field[] row,
                                      Field untilThisMet,
                                      Field replaceWith)
  {
    int i = 0;
    while (row[i] != untilThisMet)
    {
      row[i] = replaceWith;
      i++;
    }
  }

  private void replaceEndOfLine(Field[] row,
                                Field untilThisMet,
                                Field replaceWith)
  {
    int i = row.length - 1;
    while (row[i] != untilThisMet)
    {
      row[i] = replaceWith;
      i--;
    }
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
