package sokoban;

public enum Field {

  EMPTY, WALL, MAN, BLOCK, GOAL, BLOCK_ON_GOAL, MAN_ON_GOAL;

  public static Field convertFromScreenInput(char c) {
    switch (c) {
      case '#':
        return Field.WALL;
      case '@':
        return Field.MAN;
      case '.':
        return Field.GOAL;
      case ' ':
        return Field.EMPTY;
      case '$':
        return Field.BLOCK;
      case '*':
        return Field.BLOCK_ON_GOAL;
      case '+':
        return Field.MAN_ON_GOAL;
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
      case BLOCK:
        return "b";
      case BLOCK_ON_GOAL:
        return "bog";
      case MAN_ON_GOAL:
        return "mog";
      default:
        throw new IllegalArgumentException("Invalid Field " + this);
    }
  }
}
