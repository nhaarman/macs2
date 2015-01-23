package sokoban.bdd;

import net.sf.javabdd.BDD;

import java.util.Arrays;

class Utils {

  private Utils() {}

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
   * Helps to concatenate arrays of bits to create variables for man.
   *
   * @param first
   * @param second
   * @param <T>
   * @return
   */
  public static <T> T[] concat(T[] first, T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }
  
}
