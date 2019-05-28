/**
 * This file is part of jeso.
 * Copyright (c) 2019 Jonathan Bradley Whited (@esotericpig)
 * 
 * jeso is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jeso is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jeso. If not, see <http://www.gnu.org/licenses/>.
 */

package com.esotericpig.jeso.botbuddy;

import com.esotericpig.jeso.code.ParseCodeException;

import java.awt.AWTException;

import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <pre>
 * To run this app and view its options:{@code
 *   java -cp build/libs/jeso-*.*.*.jar com.esotericpig.jeso.botbuddy.BotBuddyCodeApp --help}
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyCodeApp {
  public static void main(String[] args) {
    try {
      BotBuddyCodeApp app = new BotBuddyCodeApp(args);
      
      app.parseArgs();
      app.interpretFile();
    }
    catch(ParseCodeException ex) {
      System.out.println("ParseCodeException: " + ex.getMessage());
      
      if(ex.getCause() != null) {
        ex.getCause().printStackTrace();
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      
      if(ex.getCause() != null) {
        ex.getCause().printStackTrace();
      }
    }
  }
  
  protected String[] args;
  protected BotBuddyCode.Builder builder = BotBuddyCode.builder();
  protected int indent = 4;
  protected boolean isDryRun = false;
  protected String name = getClass().getSimpleName();
  protected int optionsIndent = 24;
  protected Path path = null;
  
  public BotBuddyCodeApp(String[] args) {
    this.args = args;
  }
  
  public void interpretFile() throws AWTException,IOException,ParseCodeException {
    try(BotBuddyCode bbc = builder.path(path).build()) {
      if(isDryRun) {
        System.out.println(bbc.interpretDryRun());
      }
      else {
        bbc.interpret();
      }
    }
  }
  
  public void parseArgs() {
    if(args.length < 1) {
      printHelp();
    }
    
    for(String arg: args) {
      if(arg.equals("-h") || arg.equals("--help")) {
        printHelp();
      }
      else if(arg.equals("-n") || arg.equals("--dry-run")) {
        isDryRun = true;
      }
      else {
        if(path != null) {
          printHelp("Error: Too many files specified; only one file is allowed.");
        }
        
        path = Paths.get(arg.trim());
      }
    }
    
    if(path == null) {
      printHelp("Error: No file specified.");
    }
  }
  
  public void printHelp() {
    printHelp(null);
  }
  
  public void printHelp(String errorMessage) {
    if(errorMessage != null) {
      println(errorMessage);
      println();
    }
    
    println("Usage: {n} [options] <file>");
    println();
    println("Interprets the contents of <file> using BotBuddyCode.");
    println();
    println("Options:");
    println("{i}-n, --dry-run {o} Do not execute any code, only output the interpretation");
    println("{i}---");
    println("{i}-h, --help {o} Print this help");
    println();
    println("Examples:");
    println("{i}{n} -n mydir/myfile.bbc");
    println("{i}{n} 'My Dir/My File.bbc'");
    
    System.exit(0);
  }
  
  public void println() {
    System.out.println();
  }
  
  public void println(String message) {
    StringBuilder sb = new StringBuilder(message.length());
    int totalIndent = 0;
    
    for(int i = 0; i < message.length();) {
      int cp = message.codePointAt(i);
      
      i += Character.charCount(cp);
      
      if(cp == '{') {
        int j = message.indexOf('}',i);
        
        if(j < 0) {
          throw ParseCodeException.build(1,i,"No matching curly brace: " + message);
        }
        
        String id = message.substring(i,j);
        
        if(id.equals("i")) {
          for(int k = 0; k < indent; ++k) {
            sb.append(' ');
          }
          
          totalIndent += indent;
        }
        else if(id.equals("n")) {
          sb.append(name);
        }
        else if(id.equals("o")) {
          for(int k = (i - totalIndent); k < optionsIndent; ++k) {
            sb.append(' ');
          }
        }
        else {
          throw ParseCodeException.build(1,i,"Invalid message format ID: " + message);
        }
        
        i = j + 1; // +1 for '}'
      }
      else {
        sb.appendCodePoint(cp);
      }
    }
    
    System.out.println(sb);
  }
}
