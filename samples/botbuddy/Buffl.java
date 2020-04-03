/**
 * This file is part of Jeso.
 * Copyright (c) 2020 Jonathan Bradley Whited (@esotericpig)
 * 
 * Jeso is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jeso is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jeso. If not, see <https://www.gnu.org/licenses/>.
 */

import com.esotericpig.jeso.botbuddy.BotBuddy;
import com.esotericpig.jeso.botbuddy.UserIsActiveException;

import java.awt.AWTException;
import java.awt.Point;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import java.util.regex.Pattern;

/**
 * <pre>
 * This is a CLI app for automatically inserting the questions & answers from
 * a CSV file into Buffl's flashcard website.
 * 
 * Its main advantage is creating lists from the data. This functionality isn't
 * currently offered by Buffl, and shows a unique way of "adding" functionality
 * to a website manually.
 * 
 * When running, a prompt will appear.
 * Commands:
 * - q, quit, e, exit          exit the app
 * - c, coords                 set up all coordinates
 * - b, buffl <CSV pathname>   read the CSV file and input into Buffl
 * 
 * Example:
 *   > c
 *   Please hover your mouse over the question input box 'Enter Text'...
 *   coords: (832, 493)
 *   Please hover your mouse over the answer input box 'Add List Point'...
 *   coords: (1400, 485)
 *   Please hover your mouse over the plus button '+'...
 *   coords: (1855, 1005)
 *   > b buffl.csv
 *   > q
 * 
 * Compiling (after building the Jar):
 *   $ javac -cp '../../build/libs/*' Buffl.java
 * 
 * Running:
 *   [@linux]$   java -cp '.:../../build/libs/*' Buffl
 *   [@windows]$ java -cp '.;../../build/libs/*' Buffl
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 * 
 * @see https://www.buffl.co
 */
public class Buffl {
  public static void main(String[] args) {
    Buffl buffl = new Buffl();
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
      
      try {
        // coords
        if(cmdLetter == 'c') {
          buffl.setUpCoords();
        }
        // buffl
        else if(cmdLetter == 'b' && parts.length >= 1) {
          buffl.readAndInputCSVFile(parts[0].trim());
        }
        else {
          System.out.println("invalid command: " + cmd);
        }
      }
      catch(UserIsActiveException e) {
        System.err.println("error: " + e.getLocalizedMessage());
      }
      catch(Exception e) {
        System.err.println("error: " + e.getLocalizedMessage());
        e.printStackTrace();
      }
    }
  }
  
  protected Point questionCoords = new Point(976,501);
  protected Point answerCoords = new Point(1411,496);
  protected Point plusCoords = new Point(1853,1009);
  
  public Point getAndPrintCoords() {
    Point coords = BotBuddy.getCoords();
    
    System.out.println("coords: (" + coords.x + ", " + coords.y + ")");
    
    return coords;
  }
  
  public void setUpCoords() throws AWTException {
    try(BotBuddy bb = BotBuddy.builder().build()) {
      bb.setLongDelay(3000);
      
      System.out.println("Please hover your mouse over the question input box 'Enter Text'...");
      bb.delayLong();
      questionCoords = getAndPrintCoords();
      
      System.out.println("Please hover your mouse over the answer input box 'Add List Point'...");
      bb.delayLong();
      answerCoords = getAndPrintCoords();
      
      System.out.println("Please hover your mouse over the plus button '+'...");
      bb.delayLong();
      plusCoords = getAndPrintCoords();
    }
  }
  
  public void readAndInputCSVFile(String csvPathname) throws AWTException,IOException {
    List<List<String>> csv = readCSVFile(csvPathname);
    
    try(BotBuddy bb = BotBuddy.builder().build()) {
      bb.delay(4000)
        .beginSafeMode();
      
      for(List<String> row: csv) {
        if(row.size() < 3) { continue; }
        
        String question = row.get(0);
        String answer1 = row.get(1);
        String answer2 = row.get(2);
        
        System.out.println(question + "...");
        
        bb.paste(questionCoords.x,questionCoords.y,question)
          .enter(answerCoords.x,answerCoords.y,answer1)
          .delayAuto()
          .enter(answerCoords.x,answerCoords.y,answer2)
          .delayAuto()
          .click(plusCoords.x,plusCoords.y)
          .delayLong();
      }
      
      bb.endSafeMode();
    }
  }
  
  /**
   * <pre>
   * This is a very basic, quick & dirty CSV file reader,
   * not meant for production use.
   * 
   * - Must have 1 header row.
   * - Values are separated by commas.
   *   - Values are trimmed.
   * - Rows are separated by newlines (or EOF).
   * - Values can be quoted with double quotes.
   *   - A double quote is escaped with another double quote.
   * </pre>
   */
  public List<List<String>> readCSVFile(String csvPathname) throws IOException {
    List<List<String>> csv = new ArrayList<>();
    Path path = Paths.get(csvPathname);
    
    try(Scanner fin = new Scanner(Files.newBufferedReader(path))) {
      // Ignore first header row.
      if(fin.nextLine() == null) { return csv; }
      
      final Pattern pattern = Pattern.compile(
        "\\s*((\"([^\"]|\"\")*\")|([^,\n]*))\\s*,?\\s*"
      );
      List<String> row = new ArrayList<>();
      
      fin.useDelimiter("[,\n]");
      
      while(fin.hasNext()) {
        String value = fin.findWithinHorizon(pattern,0);
        
        if(value == null) { break; }
        
        value = value.trim();
        
        boolean newRow = !value.endsWith(",");
        
        // Slow, but just for this simple script.
        value = value.replaceAll("(\\A\")|(\"?\\s*,?\\z)","");
        value = value.replace("\"\"","\"");
        
        row.add(value);
        
        if(newRow) {
          csv.add(row);
          
          row = new ArrayList<>();
        }
      }
    }
    
    return csv;
  }
}
