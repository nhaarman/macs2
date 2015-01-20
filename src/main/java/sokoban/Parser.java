package sokoban;

public class Parser {

  public Field[][] parse(final String[] lines) {
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

}
