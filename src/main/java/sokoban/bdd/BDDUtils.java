package sokoban.bdd;

import net.sf.javabdd.BDD;

import java.util.Arrays;
import java.util.stream.Stream;

class BDDUtils {

  private BDDUtils() {
  }

  /**
   * A little helper function to increase readability (not(bdd) instead of bdd.not()).
   *
   * @param bdd the BDD to negate.
   *
   * @return The negated BDD.
   */
  public static BDD not(final BDD bdd) {
    return bdd.not();
  }

  /**
   * Applies the 'or' operation to an array of BDDs.
   *
   * @param bdds The BDDs to apply the 'or' operation to.
   *
   * @return The resulting BDD.
   */
  public static BDD or(final BDD... bdds) {
    if (bdds == null || bdds.length == 0) {
      return null;
    }

    BDD result = bdds[0];
    for (int i = 1; i < bdds.length; i++) {
      result = result.or(bdds[i]);
    }
    return result;
  }

  /**
   * Concatenates arrays of bits to create variables for man.
   *
   * @param first  The first array.
   * @param second The second array.
   * @param <T>    The type of the arrays.
   *
   * @return The concatenated arrays.
   */
  public static <T> T[] concat(final T[] first, final T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

}
