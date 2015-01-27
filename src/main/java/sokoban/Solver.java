package sokoban;

import java.io.IOException;

public interface Solver {

  boolean solve() throws IOException;

  boolean solve(String initialLurd) throws IOException;

  String getLurd();

}
