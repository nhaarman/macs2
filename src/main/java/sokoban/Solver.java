package sokoban;

import java.io.IOException;

public interface Solver {

  boolean solve() throws IOException;

  String getLurd();

}
