package sokoban;

public enum Field {

  EMPTY, WALL, MAN, BLOCK, GOAL, BLOCK_ON_GOAL, MAN_ON_GOAL;

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
        return BLOCK;
      case '*':
        return BLOCK_ON_GOAL;
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
