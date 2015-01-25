package sokoban.bdd;

import sokoban.Field;

class Screen {

  private final Field[][] mFields;

  Screen(final Field[][] fields) {
    mFields = fields;
  }

  public Field[][] getFields() {
    return mFields.clone();
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder((width() + 2) * height());

    for (Field[] row : mFields) {
      for (Field field : row) {
        result.append(field.convertToScreenInput());
      }

      result.append(System.lineSeparator());
    }

    return result.toString();
  }

  public int width() {
    return mFields[0].length;
  }

  public int height() {
    return mFields.length;
  }
}
