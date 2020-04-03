import com.esotericpig.jeso.botbuddy.BotBuddy;
import com.esotericpig.jeso.botbuddy.UserIsActiveException;

import java.awt.Point;

import java.awt.event.KeyEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * <pre>
 * This is a CLI app for cheating at an online typing game.
 * 
 * When running, a prompt will appear.
 * Commands:
 * - q, quit, e, exit    exit the app
 * - c, coords           output and set the starting coordinates
 * - t, type <words>     type the following words
 * 
 * Usual steps:
 * 1) Hover your mouse cursor over where the words should be typed in
 *    and run the "c" command.
 * 2) Then enter "t word word word..." and the words will automatically
 *    be typed in.
 * 
 * Example:
 *   > c
 *   coords: (1107,486)
 *   > t big put way year made along here plant had face an family
 *   > q
 * 
 * You can either type in the words manually or copy & paste them from
 * the XHR data from Chrome's Developer tools.
 * 
 * You can first test this using a text editor.
 * 
 * Compiling on Linux (after building the Jar):
 *   $ javac -cp .:../build/libs/* TenFastFingers.java
 * 
 * Running on Linux:
 *   $ java -cp .:../build/libs/* TenFastFingers
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 * 
 * @see https://10fastfingers.com/multiplayer
 */
public class TenFastFingers {
  public static void main(String[] args) {
    Point startCoords = new Point(1107,719); // term @ 24 height w/ bottom aligned to input tag
    Scanner stdin = new Scanner(System.in);
    
    while(true) {
      System.out.print("> ");
      String line = stdin.nextLine().trim();
      
      if(line.isEmpty()) { break; }
      
      String[] parts = line.split("\\s+");
      String cmd = parts[0].toLowerCase(Locale.ENGLISH);
      int cmdLetter = cmd.codePointAt(0);
      
      // exit, quit
      if(cmdLetter == 'e' || cmdLetter == 'q') { break; }
      
      parts = Arrays.copyOfRange(parts,1,parts.length);
      
      // coords
      if(cmdLetter == 'c') {
        startCoords = BotBuddy.getCoords();
        
        System.out.println("coords: (" + startCoords.x + ", " + startCoords.y + ")");
      }
      // type
      else if(cmdLetter == 't' && parts.length >= 1) {
        try(BotBuddy bb = BotBuddy.builder().build()) {
          bb.beginSafeMode()
            .click(startCoords.x,startCoords.y)
            .delayAuto()
            .setAutoDelay(5);
          
          for(String word: parts) {
            bb.type(word)
              .type(KeyEvent.VK_SPACE)
              .delay(20);
          }
          
          bb.endSafeMode();
        }
        catch(UserIsActiveException e) {
          System.err.println("error: " + e.getLocalizedMessage());
        }
        catch(Exception e) {
          System.err.println("error: " + e.getLocalizedMessage());
          e.printStackTrace();
        }
      }
      else {
        System.out.println("invalid command: " + cmd);
      }
    }
  }
}
