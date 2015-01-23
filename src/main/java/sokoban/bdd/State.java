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


  @Override
  public String toString()
  {
    String output = "";
    for (int i = 0; i < mFields.length; i++)
    {
      for (int j = 0; j < mFields[i].length; j++)
      {
        output += mFields[i][j].convertToScreenInput();
      }
      output += System.getProperty("line.separator");
    }

    return output;
  }
}
