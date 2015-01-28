package sokoban.parser;

public enum Field {

  EMPTY, WALL, MAN, BOX, GOAL, BOX_ON_GOAL, MAN_ON_GOAL;

  public static Field convertFromScreenInput(final char c) {
    switch (c) {
      case '#':
        return WALL;
      case '@':
        return MAN;
      case '.':
        return GOAL;
      case ' ':
        return EMPTY;
      case '$':
        return BOX;
      case '*':
        return BOX_ON_GOAL;
      case '+':
        return MAN_ON_GOAL;
      default:
        throw new IllegalArgumentException(String.format("Illegal character %s in screen!", c));
    }
  }

  public String convertToSmvString() {
    switch (this) {
      case WALL:
        return "w";
      case MAN:
        return "m";
      case GOAL:
        return "g";
      case EMPTY:
        return "e";
      case BOX:
        return "b";
      case BOX_ON_GOAL:
        return "bog";
      case MAN_ON_GOAL:
        return "mog";
      default:
        throw new IllegalArgumentException("Invalid Field " + this);
    }
  }

  public char convertToScreenInput() {
    switch (this) {
      case WALL:
        return '#';
      case MAN:
        return '@';
      case GOAL:
        return '.';
      case EMPTY:
        return ' ';
      case BOX:
        return '$';
      case BOX_ON_GOAL:
        return '*';
      case MAN_ON_GOAL:
        return '+';
      default:
        throw new IllegalArgumentException("Invalid Field " + this);
    }
  }

  public boolean isGoal() {
    return this == GOAL || this == MAN_ON_GOAL || this == BOX_ON_GOAL;
  }
}
