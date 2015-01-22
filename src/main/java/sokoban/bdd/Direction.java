package sokoban.bdd;

enum Direction {

  LEFT, UP, RIGHT, DOWN;

  @Override
  public String toString() {
    switch (this) {
      case LEFT:
        return "l";
      case UP:
        return "u";
      case RIGHT:
        return "r";
      case DOWN:
        return "d";
      default:
        throw new IllegalArgumentException("Invalid Move");
    }
  }
}
