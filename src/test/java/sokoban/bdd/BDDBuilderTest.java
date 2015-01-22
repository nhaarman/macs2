package sokoban.bdd;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static sokoban.bdd.VariableType.BOX;
import static sokoban.bdd.VariableType.BOX_PRIME;
import static sokoban.bdd.VariableType.MAN;
import static sokoban.bdd.VariableType.MAN_PRIME;

public class BDDBuilderTest {

  private BDDBuilder mBDDBuilder;

  @Before
  public void setUp() throws Exception {
    BDDFactory factory = BDDFactory.init(10000, 10000);
    factory.setVarNum(4 * 2 * 4);

    mBDDBuilder = new BDDBuilder(factory, 4, 2);
  }

  @Test
  public void man00_is0() {
    BDD bdd = mBDDBuilder.varOf(0, 0, MAN);
    assertThat(bdd.var(), is(0));
  }

  @Test
  public void box00_is1() {
    BDD bdd = mBDDBuilder.varOf(0, 0, BOX);
    assertThat(bdd.var(), is(1));
  }

  @Test
  public void manPrime00_is2() {
    BDD bdd = mBDDBuilder.varOf(0, 0, MAN_PRIME);
    assertThat(bdd.var(), is(2));
  }

  @Test
  public void boxPrime00_is3() {
    BDD bdd = mBDDBuilder.varOf(0, 0, BOX_PRIME);
    assertThat(bdd.var(), is(3));
  }

  @Test
  public void man03_is12() {
    BDD bdd = mBDDBuilder.varOf(0, 3, MAN);
    assertThat(bdd.var(), is(12));
  }

  @Test
  public void box03_is13() {
    BDD bdd = mBDDBuilder.varOf(0, 3, BOX);
    assertThat(bdd.var(), is(13));
  }

  @Test
  public void manPrime03_is14() {
    BDD bdd = mBDDBuilder.varOf(0, 3, MAN_PRIME);
    assertThat(bdd.var(), is(14));
  }

  @Test
  public void boxPrime03_is15() {
    BDD bdd = mBDDBuilder.varOf(0, 3, BOX_PRIME);
    assertThat(bdd.var(), is(15));
  }

  @Test
  public void man13_is28() {
    BDD bdd = mBDDBuilder.varOf(1, 3, MAN);
    assertThat(bdd.var(), is(28));
  }

  @Test
  public void boxPrime13_is31() {
    BDD bdd = mBDDBuilder.varOf(1, 3, BOX_PRIME);
    assertThat(bdd.var(), is(31));
  }

  @Test
  public void variableSet() {
    BDD bdd = mBDDBuilder.variableSet();
    assertThat(bdd.scanSet(), is(new int[]{0, 1, 4, 5, 8, 9, 12, 13, 16, 17, 20, 21, 24, 25, 28, 29}));
  }

}