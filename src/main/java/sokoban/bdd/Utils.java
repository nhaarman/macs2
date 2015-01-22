package sokoban.bdd;

import net.sf.javabdd.BDD;

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
  
}
