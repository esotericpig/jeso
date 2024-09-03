/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

import com.esotericpig.jeso.Bools;
import com.esotericpig.jeso.OSFamily;
import com.esotericpig.jeso.Strs;
import com.esotericpig.jeso.code.LineOfCode;
import com.esotericpig.jeso.code.ParseCodeException;
import com.esotericpig.jeso.io.StringListReader;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * <pre>
 * <b>BotBuddyCode</b> is a simple scripting "language" for {@link com.esotericpig.jeso.botbuddy.BotBuddy}.
 *   It is <b>not</b> Turing complete.
 *
 * The idea was to make a very simple parser, without including the overhead of Groovy/JRuby into Jeso.
 *
 * It can handle Ruby-like string literals and heredoc, and simple methods (no params).
 *
 * It can accept the following input:
 * - {@link java.io.BufferedReader}
 * - {@link java.nio.file.Path} [use {@link java.nio.file.Paths}.get(...)]
 * - {@link java.util.List}&lt;String&gt; using {@link com.esotericpig.jeso.io.StringListReader}
 * - {@link String} using {@link java.io.StringReader}
 *
 * Example usage with a file:{@code
 *   try(BotBuddyCode bbc = BotBuddyCode.builder(Paths.get("file.txt")).build()) {
 *     // Don't execute any code, just output result of interpreting:
 *     System.out.println(bbc.interpretDryRun());
 *   }}
 *
 * Example usage with a list of strings:{@code
 *   List<String> list = new LinkedList<>();
 *   list.add("puts 'Hello World'");
 *   list.add("");
 *   list.add("get_coords");
 *
 *   try(BotBuddyCode bbc = BotBuddyCode.builder(list).build()) {
 *     // Interpret and execute code
 *     bbc.interpret();
 *   }}
 *
 * Example of functionality:{@code
 *   # This is a comment
 *
 *   puts <<EOS # Heredoc
 *       Hello World
 *     EOS # End tag can be indented
 *
 *   puts <<-EOS # ltrim to min indent
 *       Hello World
 *     EOS
 *
 *   paste 592 254 <<-EOS # Heredoc with other args
 *       Hello World
 *     EOS
 *
 *   # Method names are flexible
 *   begin_safe_mode
 *   endSafeMode
 *
 *   # Quoted strings can also have newlines
 *   puts "Hello \"
 *   World\""
 *   puts 'Hello \'World\''
 *
 *   # Special quotes like Ruby, where you choose the terminator
 *   puts %(Hello \) World)
 *   puts %^Hello \^ World^
 *
 *   # Define your own (user) method
 *   # - Cannot take in args
 *   def my_method
 *     get_coords
 *     get_pixel 1839 894
 *     printscreen # Saves file to current directory
 *     getOSFamily
 *   end
 *
 *   # Can call multiple methods in one line
 *   call my_method myMethod}
 *
 * Real world example:{@code
 *   puts "Get ready..."
 *   delay 2000
 *
 *   begin_safe_mode
 *
 *   paste 1187 492  "Sakana"
 *   paste 1450 511  "Fish"
 *   click 1851 1021
 *   delay_long
 *
 *   paste 1187 492  "Niku"
 *   paste 1450 511  "Meat"
 *   click 1851 1021
 *
 *   end_safe_mode}
 * </pre>
 *
 * @author Jonathan Bradley Whited
 * @see com.esotericpig.jeso.botbuddy.BotBuddy
 * @see java.nio.file.Paths
 * @see com.esotericpig.jeso.io.StringListReader
 * @see java.io.StringReader
 * @see com.esotericpig.jeso.botbuddy.BotBuddyCodeApp
 */
public class BotBuddyCode implements Closeable {
  public static final int DEFAULT_COMMENT_CHAR = '#';
  public static final int DEFAULT_ESCAPE_CHAR = '\\';
  public static final String INSTRUCTION_CALL_ID = "call";
  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+",Pattern.UNICODE_CHARACTER_CLASS);

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(BufferedReader input) {
    return new Builder(input);
  }

  public static Builder builder(Path path) {
    return new Builder(path);
  }

  public static Builder builder(Path path,Charset charset) {
    return new Builder(path,charset);
  }

  public static Builder builder(CharSequence str) {
    return new Builder(str);
  }

  public static Builder builder(List<String> strList) {
    return new Builder(strList);
  }

  protected BotBuddy buddy;
  protected StringBuilder buffer = new StringBuilder();
  protected int commentChar;
  protected int escapeChar;
  protected Executors executors;
  protected boolean hadCode = false;
  protected boolean hadInput = false;
  protected boolean hadInstruction = false;
  protected BufferedReader input = null;
  protected String instructionName = null;
  protected String line = null;
  protected int lineChar = 0;
  protected int lineIndex = 0;
  protected int lineNumber = 0;
  protected ReadWriteLock lock = new ReentrantReadWriteLock();
  protected StringBuilder output = new StringBuilder();
  protected Map<String,UserMethod> userMethods = new HashMap<>();

  protected BotBuddyCode(Builder builder) throws AWTException,IOException {
    if(builder.buddy == null) {
      builder.buddy(BotBuddy.builder().build());
    }
    if(builder.executors == null) {
      builder.executors(DefaultExecutors.defaultExecutors);
    }
    if(builder.input == null) {
      if(builder.path == null) {
        throw new IllegalArgumentException("Input or Path must be set");
      }

      if(builder.charset == null) {
        builder.input(Files.newBufferedReader(builder.path));
      }
      else {
        builder.input(Files.newBufferedReader(builder.path,builder.charset));
      }
    }

    setBuddy(builder.buddy);
    setCommentChar(builder.commentChar);
    setEscapeChar(builder.escapeChar);
    setExecutors(builder.executors);
    input = builder.input;
  }

  @Override
  public void close() throws IOException {
    lock.writeLock().lock();

    try {
      // For Garbage Collection (GC)
      // - Do NOT call clear(), etc., as Builder may be used again to build a new one
      buddy = null;
      buffer = null;
      executors = null;
      instructionName = null;
      line = null;
      output = null;
      userMethods = null;

      if(input != null) {
        input.close();
        input = null;
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public UserMethod addUserMethod(Instruction instruction) throws ParseCodeException {
    lock.writeLock().lock();

    try {
      if(instruction.args.length < 1) {
        throw instruction.buildParseCodeException("Method has no name");
      }

      Arg methodNameArg = instruction.args[0];
      String methodName = methodNameArg.value;

      if(instruction.args.length > 1) {
        throw ParseCodeException.build(instruction.args[1].loc,"Methods cannot currently define params"
            ,methodName);
      }

      UserMethod userMethod = new UserMethod(instruction.loc,methodName);

      if(userMethods.containsKey(userMethod.id)) {
        throw ParseCodeException.build(methodNameArg.loc,"Method name is already defined as '" + userMethod.id
            + "'",methodName);
      }

      userMethods.put(userMethod.id,userMethod);

      return userMethod;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public ParseCodeException buildParseCodeException(String message) {
    return ParseCodeException.build(lineNumber,lineIndex,message,instructionName);
  }

  public ParseCodeException buildParseCodeException(String message,Throwable cause) {
    return ParseCodeException.build(lineNumber,lineIndex,message,instructionName,cause);
  }

  public void callUserMethod(Instruction instruction) throws ParseCodeException {
    lock.readLock().lock();

    try {
      instruction.getArg(0); // Throw an error if not at least 1 arg

      for(Arg arg: instruction.args) {
        String methodName = arg.value;
        String methodID = Instruction.toID(methodName);
        UserMethod userMethod = userMethods.get(methodID);

        if(userMethod == null) {
          throw ParseCodeException.build(arg.loc,"Method '" + methodID + "' from '" + methodName
              + "' does not exist",instruction.name);
        }

        for(Instruction inst: userMethod.instructions) {
          execute(inst);
        }
      }
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public void execute(Instruction instruction) throws ParseCodeException {
    lock.readLock().lock();

    try {
      // Special keywords
      if(instruction.id.equals(INSTRUCTION_CALL_ID)) {
        callUserMethod(instruction);
      }
      else {
        Executor executor = executors.get(instruction);

        if(executor == null) {
          throw instruction.buildParseCodeException("Instruction '" + instruction.id + "' from '"
              + instruction.name + "' does not exist");
        }

        executor.execute(buddy,instruction);
      }
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public void interpret() throws IOException,ParseCodeException {
    interpret(true);
  }

  public String interpret(boolean execute) throws IOException,ParseCodeException {
    lock.writeLock().lock();

    try {
      hadCode = false;
      hadInput = false;
      hadInstruction = false;
      instructionName = null;
      line = null;
      lineNumber = 0;
      output.setLength(0);

      UserMethod userMethod = null;

      while(nextLine() != null) {
        hadInput = true;

        if(!seekToNonWhitespace()) {
          continue; // Ignore empty line or comment (handled in seek)
        }

        hadCode = true;

        // Instruction name
        LineOfCode loc = new LineOfCode(lineNumber,lineIndex);

        instructionName = readToWhitespace().toString();

        Instruction instruction = new Instruction(loc,instructionName);

        // Special keywords
        if(instruction.id.equals("end")) {
          if(userMethod == null) {
            throw instruction.buildParseCodeException(
                "Invalid instruction; a method can only have one 'end'");
          }

          userMethod = null;

          continue;
        }

        // Instruction args
        List<Arg> args = new ArrayList<>();

        while(hasLineChar()) {
          final int prevLineIndex = lineIndex;
          final int prevLineNumber = lineNumber;

          if(!seekToNonWhitespace()) {
            break; // No more args (or comment)
          }

          loc = new LineOfCode(lineNumber,lineIndex);

          if(lineChar == '"' || lineChar == '\'') {
            readQuote(lineChar);
          }
          else if(lineChar == '%') {
            readSpecialQuote();
          }
          else if(lineChar == '<') {
            readHeredoc();
          }
          else {
            readToWhitespace();
          }

          // Was there a read/seek above? Or are we caught in an infinite loop parsing the same char?
          if(lineIndex == prevLineIndex && lineNumber == prevLineNumber) {
            throw buildParseCodeException("Internal code is broken causing an infinite loop");
          }

          args.add(new Arg(loc,buffer.toString()));

          // nextLine() might have been called (e.g., heredoc)
          if(line == null) {
            break;
          }
        }

        instruction.setArgs(args);

        // Special keywords
        if(instruction.id.equals("def")) {
          if(userMethod != null) {
            throw instruction.buildParseCodeException("Methods cannot be defined within methods");
          }

          userMethod = addUserMethod(instruction);

          if(!execute) {
            output(userMethod);
          }

          continue;
        }

        // Execute/Output instruction
        if(userMethod == null) {
          hadInstruction = true;

          if(execute) {
            execute(instruction);
          }
          else {
            output(instruction);
          }
        }
        else {
          userMethod.instructions.add(instruction);

          if(!execute) {
            outputWithIndent(instruction);
          }
        }
      }

      return output.toString();
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public String interpretDryRun() throws IOException,ParseCodeException {
    return interpret(false);
  }

  public String nextLine() throws IOException {
    lock.writeLock().lock();

    try {
      if((line = input.readLine()) != null) {
        lineChar = 0;
        lineIndex = 0;
        ++lineNumber;
      }

      return line;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public int nextLineChar() {
    lock.writeLock().lock();

    try {
      lineChar = line.codePointAt(lineIndex);
      lineIndex += Character.charCount(lineChar);

      return lineChar;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void output(Instruction instruction) {
    output(instruction,"");
  }

  public void output(Instruction instruction,String prefix) {
    // WARNING: If you change this, update "/src/test/resources/BotBuddyCodeTestOutput.txt",
    //            else, the JUnit test will fail. For this reason, don't use #toString() methods.

    lock.writeLock().lock();

    try {
      // Special keywords
      boolean isCallInst = instruction.id.equals(INSTRUCTION_CALL_ID);
      boolean isUserMethod = (instruction instanceof UserMethod);

      boolean hasPrefix = !prefix.isEmpty();
      String newlinePrefix = hasPrefix ? ("\n" + prefix) : null;

      output.append(prefix);
      output.append('[');
      output.append(instruction.id).append(':').append(instruction.name);
      output.append("]:(");
      output.append(instruction.loc.getNumber()).append(':').append(instruction.loc.getColumn());
      output.append("):");

      if(isCallInst) {
        output.append("exists");
      }
      else if(isUserMethod) {
        output.append("user");
      }
      else {
        output.append(executors.contains(instruction) ? "exists" : "none");
      }

      output.append('\n');

      for(int i = 0; i < instruction.args.length; ++i) {
        Arg arg = instruction.args[i];

        output.append(prefix);
        output.append("- [");
        output.append(i);
        output.append("]:(");
        output.append(arg.loc.getNumber()).append(':').append(arg.loc.getColumn());
        output.append("): ");

        if(isCallInst) {
          String methodID = Instruction.toID(arg.value);

          output.append('[');
          output.append(methodID).append(':').append(arg.value);
          output.append("]:");
          output.append(userMethods.containsKey(methodID) ? "exists" : "none");
        }
        else {
          output.append('\'');
          output.append(hasPrefix ? arg.value.replace("\n",newlinePrefix) : arg.value);
          output.append('\'');
        }

        output.append('\n');
      }
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void outputWithIndent(Instruction instruction) {
    output(instruction,"  > ");
  }

  public StringBuilder readHeredoc() throws IOException,ParseCodeException {
    lock.writeLock().lock();

    try {
      // '<...' instead of '<<...'
      if(!hasLineChar() || nextLineChar() != '<') {
        throw buildParseCodeException("Invalid heredoc '<' instead of '<<' or unquoted string");
      }
      // '<<' with EOL
      if(!hasLineChar()) {
        throw buildParseCodeException("Invalid heredoc without a tag or unquoted string");
      }

      boolean isIndent = (nextLineChar() == '-');

      if(isIndent) {
        if(hasLineChar()) {
          nextLineChar(); // Skip '-'
        }
        else {
          // '<<-' with EOL
          throw buildParseCodeException("Invalid heredoc without a tag or unquoted string");
        }
      }

      LineOfCode prevLoc = new LineOfCode(lineNumber,lineIndex);
      final String endTag = Strs.rtrim(readToEndOfLine()).toString();

      // End tag cannot have whitespaces (except for trailing whitespaces until EOL/comment)
      // - To help prevent the user from fat-fingering "<<-EOS" as "<< -EOS"
      // - Because a whitespace denotes a new arg "EOS 10 20" ("E O S 10 20" would be impossible)
      if(!endTag.equals(WHITESPACE_PATTERN.matcher(endTag).replaceAll(""))) {
        throw ParseCodeException.build(prevLoc
            ,"Invalid heredoc with whitespaces before or in the tag, or unquoted string",instructionName);
      }

      // Read the heredoc lines and (possible) indent (<<-)
      final int endTagChar0 = endTag.codePointAt(0);
      final int endTagChar0Count = Character.charCount(endTagChar0);
      List<String> heredocLines = new LinkedList<>();
      int minIndent = Integer.MAX_VALUE;

      while(nextLine() != null) {
        int indent = 0;

        buffer.setLength(0);

        // Read to non-whitespace for (possible) indent (<<-)
        // - Do NOT do "indent += Character.charCount(lineChar)"; indent code after loop uses charCount()
        for(; hasLineChar(); ++indent) {
          buffer.appendCodePoint(nextLineChar());

          if(!Character.isWhitespace(lineChar)) {
            break;
          }
        }

        // Is end tag?
        if(lineChar == endTagChar0) {
          boolean isEndTag = false;

          // End tag might be a single char (e.g., "E") with EOL, so don't test "hasLineChar()"
          for(int i = endTagChar0Count;;) {
            // Last char of end tag?
            if(i >= endTag.length()) {
              if(hasLineChar()) {
                nextLineChar();

                // Is end tag: "EOS 10 20" or "EOS#comment"
                if(Character.isWhitespace(lineChar) || isCommentChar()) {
                  isEndTag = true;
                }
                // Not end tag: "EOS?"
                else {
                  buffer.appendCodePoint(lineChar);
                }
              }
              // Is end tag: "EOS" with EOL
              else {
                isEndTag = true;
              }

              break;
            }
            if(isEndOfLine()) {
              break;
            }

            int endTagChar = endTag.codePointAt(i);

            buffer.appendCodePoint(nextLineChar());

            // Not end tag: "Everybody!"
            if(lineChar != endTagChar) {
              break;
            }

            i += Character.charCount(endTagChar);
          }

          if(isEndTag) {
            break;
          }
        }

        // Read rest of chars to line end
        while(hasLineChar()) {
          buffer.appendCodePoint(nextLineChar());
        }

        heredocLines.add(buffer.toString());

        // Update min indent
        if(isIndent && indent < minIndent) {
          minIndent = indent;
        }
      }

      // Convert heredoc lines to one string
      isIndent = (isIndent && minIndent > 0 && minIndent != Integer.MAX_VALUE);
      buffer.setLength(0);

      if(!heredocLines.isEmpty()) {
        for(Iterator<String> it = heredocLines.iterator();;) {
          String hdLine = it.next();

          if(isIndent) {
            int i = 0;

            // This is probably unnecessary, but just in case Unicode whitespace can be a surrogate pair
            for(int indentIndex = 0; indentIndex < minIndent && i < hdLine.length(); ++indentIndex) {
              int hdChar = hdLine.codePointAt(i);

              i += Character.charCount(hdChar);
            }

            while(i < hdLine.length()) {
              int hdChar = hdLine.codePointAt(i);

              buffer.appendCodePoint(hdChar);
              i += Character.charCount(hdChar);
            }
          }
          else {
            buffer.append(hdLine);
          }

          // Don't add last newline (chomp)
          if(!it.hasNext()) {
            break;
          }

          buffer.append('\n');
        }
      }

      return buffer;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public StringBuilder readQuote(int endQuote) throws IOException {
    lock.writeLock().lock();

    try {
      boolean hasEndQuote = false;

      buffer.setLength(0);

      while(true) {
        while(hasLineChar()) {
          nextLineChar();

          if(lineChar == endQuote) {
            hasEndQuote = true;

            break;
          }

          // Escaped char?
          if(lineChar == escapeChar) {
            if(hasLineChar()) {
              nextLineChar();

              // Escaped end quote (e.g., \") or escaped escape (e.g., \\)
              if(lineChar == endQuote || lineChar == escapeChar) {
                buffer.appendCodePoint(lineChar);
              }
              else {
                // To make it easier for non-programmers, don't output lineChar only.
                //   For example, "\a" will output that exactly (with the backslash).
                buffer.appendCodePoint(escapeChar).appendCodePoint(lineChar);
              }
            }
            // EOL
            else {
              // To make it easier for non-programmers, just output as is with no error.
              //   For example, "\" with EOL will output that exactly (a backslash).
              buffer.appendCodePoint(lineChar);
            }
          }
          else {
            buffer.appendCodePoint(lineChar);
          }
        }

        if(hasEndQuote || nextLine() == null) {
          break;
        }

        buffer.append('\n');
      }

      return buffer;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public StringBuilder readSpecialQuote() throws IOException,ParseCodeException {
    lock.writeLock().lock();

    try {
      // '%' with EOL or '% ...'
      if(!hasLineChar() || Character.isWhitespace(nextLineChar())) {
        throw buildParseCodeException(
            "Invalid special quote '%' without a tag, with spaces, or unquoted string");
      }

      switch(lineChar) {
        case '(': return readQuote(')');
        case '<': return readQuote('>');
        case '[': return readQuote(']');
        case '{': return readQuote('}');
      }

      return readQuote(lineChar);
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public StringBuilder readToEndOfLine() {
    lock.writeLock().lock();

    try {
      buffer.setLength(0);

      if(lineIndex > 0) {
        if(isCommentChar()) {
          return buffer;
        }

        buffer.appendCodePoint(lineChar);
      }

      while(hasLineChar()) {
        nextLineChar();

        if(isCommentChar()) {
          break;
        }

        buffer.appendCodePoint(lineChar);
      }

      return buffer;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public StringBuilder readToWhitespace() {
    lock.writeLock().lock();

    try {
      buffer.setLength(0);

      if(lineIndex > 0) {
        if(isCommentChar()) {
          return buffer;
        }

        buffer.appendCodePoint(lineChar);
      }

      while(hasLineChar()) {
        nextLineChar();

        if(Character.isWhitespace(lineChar) || isCommentChar()) {
          break;
        }

        buffer.appendCodePoint(lineChar);
      }

      return buffer;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * @return true if found a non-whitespace char, else false
   */
  public boolean seekToNonWhitespace() {
    lock.writeLock().lock();

    try {
      while(hasLineChar()) {
        if(!Character.isWhitespace(nextLineChar())) {
          if(isCommentChar()) {
            return false;
          }

          return true;
        }
      }

      return false;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void setBuddy(BotBuddy buddy) {
    lock.writeLock().lock();

    try {
      this.buddy = buddy;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void setCommentChar(int commentChar) {
    lock.writeLock().lock();

    try {
      this.commentChar = commentChar;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void setEscapeChar(int escapeChar) {
    lock.writeLock().lock();

    try {
      this.escapeChar = escapeChar;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public void setExecutors(Executors executors) {
    lock.writeLock().lock();

    try {
      this.executors = executors;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public BotBuddy getBuddy() {
    lock.readLock().lock();

    try {
      return buddy;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean hadCode() {
    lock.readLock().lock();

    try {
      return hadCode;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public int getCommentChar() {
    lock.readLock().lock();

    try {
      return commentChar;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean isCommentChar() {
    lock.writeLock().lock();

    try {
      // Ignore comment
      if(lineChar == commentChar) {
        lineIndex = line.length(); // Go to end

        return true;
      }

      return false;
    }
    finally {
      lock.writeLock().unlock();
    }
  }

  public boolean isEndOfLine() {
    lock.readLock().lock();

    try {
      return lineIndex >= line.length();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public int getEscapeChar() {
    lock.readLock().lock();

    try {
      return escapeChar;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public Executors getExecutors() {
    lock.readLock().lock();

    try {
      return executors;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean hadInput() {
    lock.readLock().lock();

    try {
      return hadInput;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean hadInstruction() {
    lock.readLock().lock();

    try {
      return hadInstruction;
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean hasLineChar() {
    lock.readLock().lock();

    try {
      return lineIndex < line.length();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  public boolean isReady() throws IOException {
    lock.readLock().lock();

    try {
      return input.ready();
    }
    finally {
      lock.readLock().unlock();
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class Arg {
    public LineOfCode loc;
    public String value;

    public Arg(int lineNumber,int lineColumn,String value) {
      this(new LineOfCode(lineNumber,lineColumn),value);
    }

    public Arg(LineOfCode loc,String value) {
      if(loc == null) {
        throw new IllegalArgumentException("LineOfCode cannot be null");
      }
      if(value == null) {
        throw new IllegalArgumentException("Value cannot be null");
      }

      this.loc = loc;
      this.value = value;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder(11 + value.length());

      sb.append(loc).append(": '").append(value).append('\'');

      return sb.toString();
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class Builder {
    protected BotBuddy buddy = null;
    protected Charset charset = StandardCharsets.UTF_8;
    protected int commentChar = DEFAULT_COMMENT_CHAR;
    protected int escapeChar = DEFAULT_ESCAPE_CHAR;
    protected Executors executors = null;
    protected BufferedReader input = null;
    protected Path path = null;

    protected Builder() {
    }

    protected Builder(BufferedReader input) {
      this();

      input(input);
    }

    protected Builder(Path path) {
      this();

      path(path);
    }

    protected Builder(Path path,Charset charset) {
      this();

      path(path,charset);
    }

    protected Builder(CharSequence str) {
      this();

      input(str);
    }

    protected Builder(List<String> strList) {
      this();

      input(strList);
    }

    public BotBuddyCode build() throws AWTException,IOException {
      return new BotBuddyCode(this);
    }

    public Builder buddy(BotBuddy buddy) {
      this.buddy = buddy;

      return this;
    }

    public Builder charset(Charset charset) {
      this.charset = charset;

      return this;
    }

    public Builder commentChar(int commentChar) {
      this.commentChar = commentChar;

      return this;
    }

    public Builder escapeChar(int escapeChar) {
      this.escapeChar = escapeChar;

      return this;
    }

    public Builder executors(Executors executors) {
      this.executors = executors;

      return this;
    }

    public Builder input() {
      // Because input(null) is ambiguous

      this.input = null;

      return this;
    }

    public Builder input(BufferedReader input) {
      this.input = input;

      return this;
    }

    public Builder input(CharSequence str) {
      this.input = new BufferedReader(new StringReader(str.toString()));

      return this;
    }

    public Builder input(List<String> strList) {
      this.input = new BufferedReader(new StringListReader(strList));

      return this;
    }

    public Builder path(Path path) {
      this.path = path;

      return this;
    }

    public Builder path(Path path,Charset charset) {
      this.charset = charset;
      this.path = path;

      return this;
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class DefaultExecutors {
    public static final Executors defaultExecutors = new Executors();

    static {
      defaultExecutors.addBase();
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  @FunctionalInterface
  public static interface Executor {
    public abstract void execute(BotBuddy buddy,Instruction inst) throws ParseCodeException;
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class Executors {
    /**
     * <pre>
     * This MUST match the number of base entries in #addBase() for testing,
     *   because a JUnit test will fail if an entry has been overwritten accidentally.
     * </pre>
     */
    public static final int BASE_COUNT = 71;

    protected Map<String,Executor> entries;

    public Executors() {
      // Default loadFactor is 0.75, so make it so we have enough on init, and a little extra
      this((int)Math.ceil(BASE_COUNT / 0.74));
    }

    /**
     * <pre>
     * This does NOT copy the entries, but uses {@code entries} directly as is.
     * </pre>
     *
     * @param entries the entries to be used directly (not copied)
     */
    public Executors(Map<String,Executor> entries) {
      this.entries = entries;
    }

    public Executors(int initCapacity) {
      this(new HashMap<>(initCapacity));
    }

    public Executors(int initCapacity,float loadFactor) {
      this(new HashMap<>(initCapacity,loadFactor));
    }

    /**
     * <pre>
     * <b>WARNING:</b>
     *   If you add a new executor in this method, you MUST update #BASE_COUNT.
     *   There is a JUnit test that checks to make sure #BASE_COUNT and
     *     DefaultExecutors size are equal.
     *   This helps prevent forgetting to change the ID when copy &amp; pasting:
     *     // "getxcoord" is overwritten by the 2nd line
     *     put("getxcoord",(buddy,inst) -&gt; BotBuddy.getXCoord());
     *     put("getxcoord",(buddy,inst) -&gt; BotBuddy.getYCoord());
     * </pre>
     */
    public void addBase() {
      // Static methods
      put("getcoords",(buddy,inst) -> {
        Point coords = BotBuddy.getCoords();

        System.out.println("(" + coords.x + "," + coords.y + ")");
      });
      put("getxcoord",(buddy,inst) -> System.out.println(BotBuddy.getXCoord()));
      put("getycoord",(buddy,inst) -> System.out.println(BotBuddy.getYCoord()));

      // Main methods
      put("beep",(buddy,inst) -> buddy.beep());
      put("beginfastmode",(buddy,inst) -> buddy.beginFastMode());
      put("beginsafemode",(buddy,inst) -> buddy.beginSafeMode());
      put("clearpressed",(buddy,inst) -> buddy.clearPressed());
      put("clearpressedbuttons",(buddy,inst) -> buddy.clearPressedButtons());
      put("clearpressedkeys",(buddy,inst) -> buddy.clearPressedKeys());
      put("click",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.click(); break;
          case 1:  buddy.click(inst.getInt(0)); break;
          case 2:  buddy.click(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.click(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("clicks",(buddy,inst) -> buddy.clicks(inst.getInts()));
      put("copy",(buddy,inst) -> buddy.copy(inst.getStr(0)));
      put("delay",(buddy,inst) -> buddy.delay(inst.getInt(0)));
      put("delayauto",(buddy,inst) -> buddy.delayAuto());
      put("delayfast",(buddy,inst) -> buddy.delayFast());
      put("delaylong",(buddy,inst) -> buddy.delayLong());
      put("delayshort",(buddy,inst) -> buddy.delayShort());
      put("doubleclick",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.doubleClick(); break;
          case 1:  buddy.doubleClick(inst.getInt(0)); break;
          case 2:  buddy.doubleClick(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.doubleClick(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("drag",(buddy,inst) -> {
        switch(inst.args.length) {
          case 4:
            buddy.drag(inst.getInt(0),inst.getInt(1),inst.getInt(2),inst.getInt(3));
            break;
          default:
            buddy.drag(inst.getInt(0),inst.getInt(1),inst.getInt(2),inst.getInt(3),inst.getInt(4));
            break;
        }
      });
      put("endfastmode",(buddy,inst) -> buddy.endFastMode());
      put("endsafemode",(buddy,inst) -> buddy.endSafeMode());
      put("enter",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.enter(); break;
          case 1:  buddy.enter(inst.getStr(0)); break;
          case 2:  buddy.enter(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.enter(inst.getInt(0),inst.getInt(1),inst.getStr(2)); break;
        }
      });
      put("leftclick",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.leftClick(); break;
          default: buddy.leftClick(inst.getInt(0),inst.getInt(1)); break;
        }
      });
      put("middleclick",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.middleClick(); break;
          default: buddy.middleClick(inst.getInt(0),inst.getInt(1)); break;
        }
      });
      put("move",(buddy,inst) -> buddy.move(inst.getInt(0),inst.getInt(1)));
      put("paste",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.paste(); break;
          case 1:  buddy.paste(inst.getStr(0)); break;
          case 2:  buddy.paste(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.paste(inst.getInt(0),inst.getInt(1),inst.getStr(2)); break;
        }
      });
      put("pressbutton",(buddy,inst) -> {
        switch(inst.args.length) {
          case 1:  buddy.pressButton(inst.getInt(0)); break;
          default: buddy.pressButton(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("pressbuttons",(buddy,inst) -> buddy.pressButtons(inst.getInts()));
      put("presskey",(buddy,inst) -> {
        switch(inst.args.length) {
          case 1:  buddy.pressKey(inst.getInt(0)); break;
          default: buddy.pressKey(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("presskeys",(buddy,inst) -> buddy.pressKeys(inst.getInts()));
      put("printscreen",(buddy,inst) -> {
        BufferedImage image = null;

        switch(inst.args.length) {
          case 0: image = buddy.printScreen(); break;
          case 2: image = buddy.printScreen(inst.getInt(0),inst.getInt(1)); break;
          default:
            image = buddy.printScreen(inst.getInt(0),inst.getInt(1),inst.getInt(2),inst.getInt(3));
            break;
        }

        ZonedDateTime dateTime = ZonedDateTime.now();
        DateTimeFormatter dateTimeF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss-SSS VV");
        String dateTimeS = dateTime.format(dateTimeF).replace('/','-');

        String fileFormat = "png";
        File file = new File("BotBuddy Screenshot " + dateTimeS + "." + fileFormat);

        if(file.exists()) {
          throw inst.buildParseCodeException("File already exists: " + file.getAbsolutePath());
        }

        System.out.println("Saving screenshot to: " + file.getAbsolutePath());

        try {
          ImageIO.write(image,fileFormat,file);
        }
        catch(IOException ex) {
          throw inst.buildParseCodeException("Failed to save screenshot: " + file.getAbsolutePath(),ex);
        }
      });
      put("releasebutton",(buddy,inst) -> {
        switch(inst.args.length) {
          case 1:  buddy.releaseButton(inst.getInt(0)); break;
          default: buddy.releaseButton(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("releasebuttons",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.releaseButtons(); break;
          default: buddy.releaseButtons(inst.getInts()); break;
        }
      });
      put("releasekey",(buddy,inst) -> {
        switch(inst.args.length) {
          case 1:  buddy.releaseKey(inst.getInt(0)); break;
          default: buddy.releaseKey(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
      put("releasekeys",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.releaseKeys(); break;
          default: buddy.releaseKeys(inst.getInts()); break;
        }
      });
      put("releasepressed",(buddy,inst) -> buddy.releasePressed());
      put("rightclick",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.rightClick(); break;
          default: buddy.rightClick(inst.getInt(0),inst.getInt(1)); break;
        }
      });
      put("rollbuttons",(buddy,inst) -> buddy.rollButtons(inst.getInts()));
      put("rollkeys",(buddy,inst) -> buddy.rollKeys(inst.getInts()));
      put("stash",(buddy,inst) -> buddy.stash());
      put("type",(buddy,inst) -> {
        switch(inst.args.length) {
          // @since 0.3.5
          case 3:
            try {
              buddy.type(inst.getInt(0),inst.getInt(1),inst.getInt(2));
            }
            catch(ParseCodeException ex) {
              buddy.type(inst.getInt(0),inst.getInt(1),inst.getStr(2));
            }
            break;
          default:
            try {
              buddy.type(inst.getInt(0));
            }
            catch(ParseCodeException ex) {
              buddy.type(inst.getStr(0));
            }
            break;
        }
      });
      put("types",(buddy,inst) -> buddy.types(inst.getInts()));
      // @since 0.3.5
      put("typeunsurely",(buddy,inst) -> {
        switch(inst.args.length) {
          case 3:  buddy.typeUnsurely(inst.getInt(0),inst.getInt(1),inst.getStr(2)); break;
          default: buddy.typeUnsurely(inst.getStr(0)); break;
        }
      });
      put("unstash",(buddy,inst) -> buddy.unstash());
      put("waitforidle",(buddy,inst) -> buddy.waitForIdle());
      put("wheel",(buddy,inst) -> buddy.wheel(inst.getInt(0)));

      // Extra methods
      put("puts",(buddy,inst) -> {
        if(inst.args.length < 1) {
          System.out.println();
        }
        else {
          for(Arg arg: inst.args) {
            System.out.println(arg.value);
          }
          System.out.println();
        }
      });

      // Setters
      put("setautodelay",(buddy,inst) -> {
        String msg = "setAutoDelay: ";

        try {
          buddy.setAutoDelay(inst.getInt(0));
          msg += buddy.getAutoDelay();
        }
        catch(ParseCodeException ex) {
          buddy.setAutoDelay(inst.getBool(0));
          msg += buddy.isAutoDelay();
        }

        System.out.println(msg);
      });
      put("setautowaitforidle",(buddy,inst) -> {
        buddy.setAutoWaitForIdle(inst.getBool(0));
        System.out.println("setAutoWaitForIdle: " + buddy.isAutoWaitForIdle());
      });
      put("setfastdelay",(buddy,inst) -> {
        buddy.setFastDelay(inst.getInt(0));
        System.out.println("setFastDelay: " + buddy.getFastDelay());
      });
      put("setlongdelay",(buddy,inst) -> {
        buddy.setLongDelay(inst.getInt(0));
        System.out.println("setLongDelay: " + buddy.getLongDelay());
      });
      put("setosfamily",(buddy,inst) -> {
        OSFamily osf = OSFamily.guessFromName(inst.getStr(0));

        buddy.setOSFamily(osf);
        System.out.println("setOSFamily: " + buddy.getOSFamily());
      });
      put("setreleasemode",(buddy,inst) -> {
        buddy.setReleaseMode(inst.getBool(0));
        System.out.println("setReleaseMode: " + buddy.isReleaseMode());
      });
      put("setshortdelay",(buddy,inst) -> {
        buddy.setShortDelay(inst.getInt(0));
        System.out.println("setShortDelay: " + buddy.getShortDelay());
      });

      // Getters
      put("getautodelay",(buddy,inst) -> System.out.println(buddy.getAutoDelay()));
      put("isautodelay",(buddy,inst) -> System.out.println(buddy.isAutoDelay()));
      put("isautowaitforidle",(buddy,inst) -> System.out.println(buddy.isAutoWaitForIdle()));
      put("getdefaultbutton",(buddy,inst) -> System.out.println(buddy.getDefaultButton()));
      put("getfastdelay",(buddy,inst) -> System.out.println(buddy.getFastDelay()));
      put("getleftbutton",(buddy,inst) -> System.out.println(buddy.getLeftButton()));
      put("getlongdelay",(buddy,inst) -> System.out.println(buddy.getLongDelay()));
      put("getmiddlebutton",(buddy,inst) -> System.out.println(buddy.getMiddleButton()));
      put("getosfamily",(buddy,inst) -> System.out.println(buddy.getOSFamily()));
      put("getpixel",(buddy,inst) -> {
        // Probably don't need alpha I think; probably always 255
        Color pixel = buddy.getPixel(inst.getInt(0),inst.getInt(1));
        int pixelWord = (pixel.getRed() << 16) | (pixel.getGreen() << 8) | (pixel.getBlue());
        StringBuilder sb = new StringBuilder(47);

        sb.append("(r=").append(pixel.getRed());
        sb.append(",g=").append(pixel.getGreen());
        sb.append(",b=").append(pixel.getBlue());
        sb.append(") | Hex=").append(Integer.toHexString(pixelWord).toUpperCase(Locale.ENGLISH));
        sb.append(" | RGB=").append(pixelWord);

        System.out.println(sb);
      });
      put("isreleasemode",(buddy,inst) -> System.out.println(buddy.isReleaseMode()));
      put("getrightbutton",(buddy,inst) -> System.out.println(buddy.getRightButton()));
      put("issafemode",(buddy,inst) -> System.out.println(buddy.isSafeMode()));
      put("getscreenheight",(buddy,inst) -> System.out.println(buddy.getScreenHeight()));
      put("getscreensize",(buddy,inst) -> {
        Dimension size = buddy.getScreenSize();

        System.out.println("" + size.width + "x" + size.height);
      });
      put("getscreenwidth",(buddy,inst) -> System.out.println(buddy.getScreenWidth()));
      put("getshortdelay",(buddy,inst) -> System.out.println(buddy.getShortDelay()));
    }

    public boolean contains(String id) {
      return containsID(id);
    }

    public boolean contains(Instruction inst) {
      return entries.containsKey(inst.id);
    }

    public boolean containsID(String id) {
      return entries.containsKey(id);
    }

    public boolean containsName(String name) {
      return entries.containsKey(Instruction.toID(name));
    }

    public Executor put(String id,Executor executor) {
      return putWithID(id,executor);
    }

    public Executor putWithID(String id,Executor executor) {
      return entries.put(id,executor);
    }

    public Executor putWithName(String name,Executor executor) {
      return entries.put(Instruction.toID(name),executor);
    }

    public Executor remove(String id) {
      return removeWithID(id);
    }

    public Executor removeWithID(String id) {
      return entries.remove(id);
    }

    public Executor removeWithName(String name) {
      return entries.remove(Instruction.toID(name));
    }

    public Executor get(String id) {
      return getWithID(id);
    }

    public Executor get(Instruction inst) {
      return entries.get(inst.id);
    }

    public Map<String,Executor> getEntries() {
      return entries;
    }

    public int getSize() {
      return entries.size();
    }

    public Executor getWithID(String id) {
      return entries.get(id);
    }

    public Executor getWithName(String name) {
      return entries.get(Instruction.toID(name));
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class Instruction {
    public static final Pattern TO_ID_PATTERN = Pattern.compile("[\\s_\\-\\.]+"
        ,Pattern.UNICODE_CHARACTER_CLASS);

    public static String toID(String name) {
      return TO_ID_PATTERN.matcher(name).replaceAll("").toLowerCase(Locale.ENGLISH);
    }

    public Arg[] args = new Arg[0];
    public String id;
    public LineOfCode loc;
    public String name;

    public Instruction(int lineNumber,int lineColumn,String name) {
      this(new LineOfCode(lineNumber,lineColumn),name);
    }

    public Instruction(LineOfCode loc,String name) {
      if(loc == null) {
        throw new IllegalArgumentException("LineOfCode cannot be null");
      }
      if(name == null) {
        throw new IllegalArgumentException("Name cannot be null");
      }

      this.id = toID(name);
      this.loc = loc;
      this.name = name;
    }

    public ParseCodeException buildParseCodeException(String message) {
      return ParseCodeException.build(loc,message,name);
    }

    public ParseCodeException buildParseCodeException(String message,Throwable cause) {
      return ParseCodeException.build(loc,message,name,cause);
    }

    public void setArgs(Arg[] args) {
      if(args == null) {
        throw new IllegalArgumentException("Args cannot be null");
      }

      this.args = args;
    }

    public void setArgs(List<Arg> args) {
      setArgs(args.toArray(new Arg[args.size()]));
    }

    public Arg getArg(int index) throws ParseCodeException {
      if(index >= args.length) {
        throw buildParseCodeException("Not enough args");
      }

      return args[index];
    }

    public boolean getBool(int index) throws ParseCodeException {
      return Bools.parse(getStr(0));
    }

    public int getInt(int index) throws ParseCodeException {
      Arg arg = getArg(index);

      try {
        return Integer.parseInt(arg.value);
      }
      catch(NumberFormatException ex) {
        // Use arg loc
        throw ParseCodeException.build(arg.loc,"Arg '" + arg.value + "' must be an int",name,ex);
      }
    }

    public int[] getInts() throws ParseCodeException {
      return getInts(1);
    }

    public int[] getInts(int minArgCount) throws ParseCodeException {
      int[] ints = new int[args.length];

      // Throw an exception if doesn't have the minimum number of args
      for(int i = 0; i < minArgCount; ++i) {
        int j = getInt(i); // Separate var, else ArrayIndexOutOfBoundsException

        ints[i] = j;
      }

      for(int i = minArgCount; i < args.length; ++i) {
        int j = getInt(i);

        ints[i] = j;
      }

      return ints;
    }

    public String getStr(int index) throws ParseCodeException {
      return getArg(index).value;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();

      sb.append('[').append(id).append(':').append(name).append("]:").append(loc).append('\n');

      for(int i = 0; i < args.length; ++i) {
        sb.append("- [").append(i).append("]:").append(args[i]).append('\n');
      }

      return sb.toString();
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class UserMethod extends Instruction {
    public List<Instruction> instructions = new LinkedList<>();

    public UserMethod(int lineNumber,int lineColumn,String name) {
      this(new LineOfCode(lineNumber,lineColumn),name);
    }

    public UserMethod(LineOfCode loc,String name) {
      super(loc,name);
    }
  }
}
