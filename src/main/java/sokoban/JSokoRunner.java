package sokoban;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class JSokoRunner {

  public void run(final File file, final String lurd) throws IOException {
    System.err.println("############################");
    System.err.println("About to run JSoko.");
    System.err.println("Make sure JSoko is not already running.");
    System.err.println("This program will start JSoko, and send some keystrokes to it.");
    System.err.println("Do not interact with your system until the man is moving, or 10 seconds have passed.");
    System.err.print("Press enter to confirm. >");
    System.err.flush();

    new BufferedReader(new InputStreamReader(System.in)).readLine();


Process process =    Runtime.getRuntime().exec(new String[]{"java", "-jar", "JSoko_1.73/JSoko.jar"});

    try {
      List<String> lines = new BufferedReader(new FileReader(file)).lines().collect(Collectors.toList());
      String screenString = String.join("\n", lines);
      StringSelection stringSelection = new StringSelection(screenString);

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, stringSelection);

      Robot robot = new Robot();

      robot.delay(5000);

      System.err.println("Sending keystrokes");

      /* Paste the screen */
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.delay(100);
      robot.keyPress(KeyEvent.VK_V);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_V);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      robot.delay(100);

      StringSelection lurdStringSelection = new StringSelection(lurd);
      clipboard.setContents(lurdStringSelection, lurdStringSelection);

      /* Paste the solution */
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.delay(100);
      robot.keyPress(KeyEvent.VK_P);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_P);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_CONTROL);
      robot.delay(100);

      /* Start animation */
      robot.keyPress(KeyEvent.VK_HOME);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_HOME);
      robot.delay(100);
      robot.keyPress(KeyEvent.VK_R);
      robot.delay(100);
      robot.keyRelease(KeyEvent.VK_R);

      System.err.println("Done sending keystrokes");

    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      process.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

}
