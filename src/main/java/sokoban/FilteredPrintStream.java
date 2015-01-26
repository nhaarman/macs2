package sokoban;

import java.io.PrintStream;
import java.util.stream.Stream;

public class FilteredPrintStream extends PrintStream {

  private final String[] mBlackList = {"Garbage", "Resizing", "buddy", "Could", "Java"};

  public FilteredPrintStream(final PrintStream out) {
    super(out);
  }

  @Override
  public void println(final String s) {
    boolean shouldPrint = Stream.of(mBlackList).filter(s::startsWith).count() == 0;

    if (shouldPrint) {
      super.println(s);
    }
  }
}
