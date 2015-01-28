package sokoban.bdd;

import org.junit.Before;
import org.junit.Test;

import sokoban.parser.Field;
import sokoban.parser.Parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class InitialLurdTest {

  private BDDSolver mBDDSolver;

  @Before
  public void setUp() {
    String[] screen = {
        "#####",
        "#   #",
        "#+$ #",
        "#####"
    };

    Field[][] fields = new Parser().parse(screen);

    mBDDSolver = new BDDSolver(fields);
  }

  @Test
  public void emptyLurd_returnsTrue() {
    boolean result = mBDDSolver.solve("");
    assertThat(result, is(true));

    String lurdResult = mBDDSolver.getLurd();
    assertThat(lurdResult, is("urrdl"));
  }

  @Test
  public void initialLurdWithSolution_returnsTrue() {
    boolean result = mBDDSolver.solve("u");
    assertThat(result, is(true));
  }

  @Test
  public void initialLurdWithNoSolution_returnsFalse() {
    /* This pushes the box into a corner from which it cannot recover. */
    boolean result = mBDDSolver.solve("r");
    assertThat(result, is(false));
  }

  @Test
  public void initialLurdWithSolution_returnsProperLurd(){
    mBDDSolver.solve("udududu");
    String lurd = mBDDSolver.getLurd();

    assertThat(lurd, is("udududurrdl"));
  }

}
