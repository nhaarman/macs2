package sokoban.bdd;

import sokoban.Field;

class State {

  private final Field[][] mFields;

  State(final Field[][] fields) {
    mFields = fields;
  }

  public Field[][] getFields() {
    return mFields.clone();
  }


}
