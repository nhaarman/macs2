package sokoban;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@SuppressWarnings("HardCodedStringLiteral")
public class SmvWriter {

  public void writeSmv(final Field[][] screen, final String filePath) throws IOException {
    File file = new File(filePath);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

      writeSquareModule(writer);
      writeMainModule(screen, writer);

      writer.flush();
      writer.close();
    }

    System.out.println(String.format("Smv code written into %s.", filePath));
  }

  private void writeSquareModule(final BufferedWriter writer) throws IOException {
    writer.write(
        "MODULE square(initValue, left, top, right, bottom, dleft, dtop, dright, dbottom, cil, cit, cir, cib, dcil, dcit, dcir, dcib, direction) " + '\n' +
            "" + '\n' +
            "  VAR " + '\n' +
            "    value    : {w, m, g, e, b, bog, mog}; " + '\n' +
            "" + '\n' +
            "  ASSIGN " + '\n' +
            "    init(value) := initValue; " + '\n' +
            "" + '\n' +
            "    -- On the basis of move, assign values to variables below " + '\n' +
            "    next(value) := " + '\n' +
            "        case " + '\n' +
            "            value = g & comingBox                   : bog; " + '\n' +
            "            (value = g | value = bog) & comingMan   : mog; " + '\n' +
            "            comingBox                               : b; " + '\n' +
            "            comingMan                               : m; " + '\n' +
            "            value = mog & isMovingSomewhere         : g; " + '\n' +
            "            value = m & isMovingSomewhere           : e; " + '\n' +
            "            TRUE                                    : value; " + '\n' +
            "        esac; " + '\n' +
            ' ' + '\n' +
            "  DEFINE " + '\n' +
            ' ' + '\n' +
            "--  Where can we go? " + '\n' +
            "    canGoLeft := " + '\n' +
            "        left = e | left = g | (left = b & (dleft = e | dleft = g)) | (left = bog & (dleft = e | dleft = g)); " + '\n' +
            "    canGoUp := " + '\n' +
            "        top = e | top = g | (top = b & (dtop = e | dtop = g)) | (top = bog & (dtop = e | dtop = g)); " + '\n' +
            "    canGoRight := " + '\n' +
            "        right = e | right = g | (right = b & (dright = e | dright = g)) | (right = bog & (dright = e | dright = g)); " + '\n' +
            "    canGoDown := " + '\n' +
            "        bottom = e | bottom = g | (bottom = b & (dbottom = e | dbottom = g)) | (bottom = bog & (dbottom = e | dbottom = g)); " + '\n' +
            ' ' + '\n' +
            "-- Additional defines " + '\n' +
            "    isAMan := value = m | value = mog; " + '\n' +
            "    isMovingLeft := isAMan & direction=l & canGoLeft; " + '\n' +
            "    isMovingUp := isAMan & direction=u & canGoUp; " + '\n' +
            "    isMovingRight := isAMan & direction=r & canGoRight; " + '\n' +
            "    isMovingDown := isAMan & direction=d & canGoDown; " + '\n' +
            "    isMovingSomewhere := isMovingLeft | isMovingUp | isMovingRight | isMovingDown; " + '\n' +
            ' ' + '\n' +
            "-- Single carries " + '\n' +
            "    col := isMovingLeft; " + '\n' +
            "    cot := isMovingUp; " + '\n' +
            "    cor := isMovingRight; " + '\n' +
            "    cob := isMovingDown; " + '\n' +
            ' ' + '\n' +
            "-- Double carries " + '\n' +
            "    dcol := isMovingLeft & (left = b | left = bog); " + '\n' +
            "    dcot := isMovingUp & (top = b | top = bog); " + '\n' +
            "    dcor := isMovingRight & (right = b | right = bog); " + '\n' +
            "    dcob := isMovingDown & (bottom = b | bottom = bog); " + '\n' +
            ' ' + '\n' +
            "-- Carry in " + '\n' +
            "    comingMan := cil | cit | cir | cib; " + '\n' +
            "    comingBox := dcil | dcit | dcir | dcib; " + '\n' +
            ' ' + '\n'
    );
  }

  private void writeMainModule(final Field[][] screen, final BufferedWriter writer) throws IOException {
    writer.write(
        "" +
            "MODULE main " + '\n' +
            "  IVAR " + '\n' +
            "    move     : {l, u, r, d}; " + '\n' +
            "  VAR " + '\n'
    );

    writeSquareInstances(screen, writer);
    writeSpec(screen, writer);
  }

  private void writeSquareInstances(final Field[][] screen, final BufferedWriter writer) throws IOException {
    int rows = screen.length;
    for (int i = 0; i < rows; i++) {
      int cols = screen[i].length;
      for (int j = 0; j < cols; j++) {

        writer.write("\ts" + i + '_' + j + "\t: square( ");
        writer.write(screen[i][j].convertToSmvString() + ", ");

        // value to the left
        writer.write(getNeighbour(i, j, 0, -1, rows, cols, ".value", "w") + ", ");

        // value above
        writer.write(getNeighbour(i, j, -1, 0, rows, cols, ".value", "w") + ", ");

        // value to the right
        writer.write(getNeighbour(i, j, 0, 1, rows, cols, ".value", "w") + ", ");

        // value below
        writer.write(getNeighbour(i, j, 1, 0, rows, cols, ".value", "w") + ", ");

        // value two squares to the left
        writer.write(getNeighbour(i, j, 0, -2, rows, cols, ".value", "w") + ", ");

        // value two squares above
        writer.write(getNeighbour(i, j, -2, 0, rows, cols, ".value", "w") + ", ");

        // value two squares to the right
        writer.write(getNeighbour(i, j, 0, 2, rows, cols, ".value", "w") + ", ");

        // value two squares below
        writer.write(getNeighbour(i, j, 2, 0, rows, cols, ".value", "w") + ", ");

        // carry to the left
        writer.write(getNeighbour(i, j, 0, -1, rows, cols, ".cor", "FALSE") + ", ");

        // carry above
        writer.write(getNeighbour(i, j, -1, 0, rows, cols, ".cob", "FALSE") + ", ");

        // carry to the right
        writer.write(getNeighbour(i, j, 0, 1, rows, cols, ".col", "FALSE") + ", ");

        // carry below
        writer.write(getNeighbour(i, j, 1, 0, rows, cols, ".cot", "FALSE") + ", ");

        // carry two squares to the left
        writer.write(getNeighbour(i, j, 0, -2, rows, cols, ".dcor", "FALSE") + ", ");

        // carry two squares above
        writer.write(getNeighbour(i, j, -2, 0, rows, cols, ".dcob", "FALSE") + ", ");

        // carry two squares to the right
        writer.write(getNeighbour(i, j, 0, 2, rows, cols, ".dcol", "FALSE") + ", ");

        // carry two squares below
        writer.write(getNeighbour(i, j, 2, 0, rows, cols, ".dcot", "FALSE") + ", ");

        writer.write("move );" + '\n');
      }
      writer.write("\n");
    }
  }

  String getNeighbour(final int row, final int column, final int deltaRow, final int deltaColumn, final int noOfRows, final int noOfColumns, final String ending, final String
      valueIfWrong) {
    int checkedRow = row + deltaRow;
    if (checkedRow < 0 || checkedRow >= noOfRows) {
      return valueIfWrong;
    }

    int checkedColumn = column + deltaColumn;
    if (checkedColumn < 0 || checkedColumn >= noOfColumns) {
      return valueIfWrong;
    }

    return "s" + checkedRow + '_' + checkedColumn + ending;
  }

  void writeSpec(final Field[][] screen, final Writer writer) throws IOException {
    boolean first = true;
    writer.write("\tSPEC AG ! (");

    for (int i = 0; i < screen.length; i++) {
      for (int j = 0; j < screen[i].length; j++) {
        Field field = screen[i][j];
        if (field == Field.GOAL || field == Field.BLOCK_ON_GOAL || field == Field.MAN_ON_GOAL) {
          if (!first) {
            writer.write(" & ");
          }
          writer.write("s" + i + '_' + j + ".value = bog");
          first = false;
        }
      }
    }

    writer.write(")\n");
  }
}
