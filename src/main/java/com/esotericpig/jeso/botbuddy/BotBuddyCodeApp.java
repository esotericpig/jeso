/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

import com.esotericpig.jeso.code.ParseCodeException;

import java.awt.AWTException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <pre>
 * To run this app and view its options:{@code
 *   $ java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp --help
 * }</pre>
 *
 * @author Jonathan Bradley Whited
 */
public class BotBuddyCodeApp {
  public static void main(String[] args) {
    try {
      BotBuddyCodeApp app = new BotBuddyCodeApp(args);

      if(app.parseArgs()) {
        return;
      }
      if(app.interpretPipe()) {
        return;
      }
      if(app.interpretFile()) {
        return;
      }
    }
    catch(ParseCodeException ex) {
      System.out.println("ParseCodeError: " + ex.getMessage());

      if(ex.getCause() != null) {
        ex.getCause().printStackTrace();
      }
    }
    catch(UserIsActiveException ex) {
      System.out.println("Error: " + ex.getMessage());
    }
    catch(Exception ex) {
      ex.printStackTrace();

      if(ex.getCause() != null) {
        ex.getCause().printStackTrace();
      }
    }
  }

  protected String[] args;
  protected BotBuddy buddy = null;
  protected BotBuddyCode.Builder builder = BotBuddyCode.builder();
  protected int indent = 4;
  protected boolean isDryRun = false;
  protected String name = getClass().getSimpleName();
  protected int optionsIndent = 24;
  protected Path path = null;

  public BotBuddyCodeApp(String[] args) throws AWTException {
    this.args = args.clone();
    buddy = BotBuddy.builder().build();

    builder.buddy(buddy);
  }

  public boolean interpretFile() throws AWTException,IOException,ParseCodeException {
    if(args.length < 1) {
      printHelp();

      return true;
    }
    if(path == null) {
      printHelp("Error: No file specified.");

      return true;
    }

    // Clear piped-in input
    builder.input().path(path);

    try(BotBuddyCode bbc = builder.build()) {
      if(isDryRun) {
        System.out.println(bbc.interpretDryRun());
      }
      else {
        bbc.interpret();
      }

      return true;
    }
    finally {
      buddy.releasePressed();
    }
  }

  public boolean interpretPipe() throws AWTException,IOException,ParseCodeException {
    // Do not use try-with-resource and do not call close(), because using System.in

    try {
      BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

      // If don't check this, then will wait for input
      if(!input.ready()) {
        // Nothing was piped in, like "echo 'get_coords' | java...BotBuddyCodeApp"
        return false;
      }

      BotBuddyCode bbc = builder.input(input).build();

      if(isDryRun) {
        System.out.println(bbc.interpretDryRun());
      }
      else {
        bbc.interpret();
      }

      return bbc.hadInput() && path == null;
    }
    finally {
      buddy.releasePressed();
    }
  }

  public boolean parseArgs() {
    for(String arg: args) {
      if(arg.equals("-h") || arg.equals("--help")) {
        printHelp();

        return true;
      }
      else if(arg.equals("-n") || arg.equals("--dry-run")) {
        isDryRun = true;
      }
      else {
        if(path != null) {
          printHelp("Error: Too many files specified; only one file is allowed.");

          return true;
        }

        path = Paths.get(arg.trim());

        if(Files.notExists(path)) {
          printHelp("Error: File does not exist: " + path.toFile().getAbsolutePath());

          return true;
        }
      }
    }

    return false;
  }

  public void printHelp() {
    printHelp(null);
  }

  public void printHelp(String errorMessage) {
    println("Usage: {n} [options] <file> [options]");
    println();
    println("Interprets the contents of <file> using BotBuddyCode.");
    println("Data can also be piped in, without using a file.");
    println();
    println("Options:");
    println("{i}-n, --dry-run {o} Do not execute any code, only output the interpretation");
    println("{i}---");
    println("{i}-h, --help {o} Print this help");
    println();
    println("Examples:");
    println("{i}{n} -n mydir/myfile.bbc");
    println("{i}{n} 'My Dir/My File.bbc'");
    println("{i}echo 'get_coords' | {n}");

    if(errorMessage != null) {
      println();
      println(errorMessage);
    }
  }

  public void println() {
    System.out.println();
  }

  public void println(String message) {
    StringBuilder sb = new StringBuilder(message.length());
    int totalIndent = 0;

    for(int i = 0; i < message.length(); ) {
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
