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

import com.esotericpig.jeso.OSFamily;

import com.esotericpig.jeso.code.LineOfCode;
import com.esotericpig.jeso.code.ParseCodeException;

import com.esotericpig.jeso.io.StringListReader;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.regex.Pattern;

// TODO: use ReentrantLock

/**
 * <pre>
 * // TODO: flesh this out
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyCode implements Closeable {
  public static final int DEFAULT_COMMENT_CHAR = '#';
  public static final int DEFAULT_ESCAPE_CHAR = '\\';
  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+",Pattern.UNICODE_CHARACTER_CLASS);
  
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
  protected BufferedReader input = null;
  protected String instructionName = null;
  protected String line = null;
  protected int lineChar = 0;
  protected int lineIndex = 0;
  protected int lineNumber = 0;
  protected StringBuilder output = new StringBuilder();
  
  public BotBuddyCode(Builder builder) throws AWTException,IOException {
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
    // For Garbage Collection (GC)
    // - Do NOT call clear(), etc., as Builder may be used again to build a new one
    buddy = null;
    buffer = null;
    executors = null;
    instructionName = null;
    line = null;
    output = null;
    
    if(input != null) {
      input.close();
      input = null;
    }
  }
  
  public ParseCodeException buildParseCodeException(String message) {
    return ParseCodeException.build(lineNumber,lineIndex,message,instructionName);
  }
  
  public ParseCodeException buildParseCodeException(String message,Throwable cause) {
    return ParseCodeException.build(lineNumber,lineIndex,message,instructionName,cause);
  }
  
  public void interpret() throws IOException,ParseCodeException {
    interpret(true);
  }
  
  public String interpret(boolean execute) throws IOException,ParseCodeException {
    instructionName = null;
    line = null;
    lineNumber = 0;
    output.setLength(0);
    
    while(nextLine() != null) {
      if(!seekToNonWhitespace()) {
        continue; // Ignore empty line or comment (handled in seek)
      }
      
      // Instruction name
      final LineOfCode loc = new LineOfCode(lineNumber,lineIndex);
      instructionName = readToWhitespace().toString();
      
      // Instruction args
      List<Arg> args = new ArrayList<>();
      
      while(hasLineChar()) {
        final int prevLineIndex = lineIndex;
        final int prevLineNumber = lineNumber;
        
        if(!seekToNonWhitespace()) {
          break;
        }
        
        final LineOfCode argLoc = new LineOfCode(lineNumber,lineIndex);
        
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
        
        args.add(new Arg(buffer.toString(),argLoc));
        
        // nextLine() might have been called (e.g., heredoc), so check if null
        if(line == null) {
          break;
        }
      }
      
      // Execute/output instruction
      Instruction instruction = new Instruction(instructionName,args,loc);
      
      if(execute) {
        Executor executor = executors.get(instruction);
        
        if(executor == null) {
          throw instruction.buildParseCodeException("Instruction '" + instruction.id + "' from '"
            + instruction.name + "' does not exist");
        }
        
        executor.execute(buddy,instruction);
      }
      else {
        // WARNING: If you change this, update "/src/test/resources/BotBuddyCodeTestOutput.txt",
        //            else, the test will fail. For this reason, don't use #toString() methods.
        output.append('[');
        output.append(instruction.id).append(':').append(instruction.name);
        output.append("]:(");
        output.append(instruction.loc.getNumber()).append(':').append(instruction.loc.getColumn());
        output.append("):");
        output.append(executors.contains(instruction) ? "exists" : "none");
        output.append('\n');
        
        for(int i = 0; i < instruction.args.length; ++i) {
          Arg arg = instruction.args[i];
          
          output.append("- [");
          output.append(i);
          output.append("]:(");
          output.append(arg.loc.getNumber()).append(':').append(arg.loc.getColumn());
          output.append("): '");
          output.append(arg.value);
          output.append("'\n");
        }
      }
    }
    
    return output.toString();
  }
  
  public String interpretDryRun() throws IOException,ParseCodeException {
    return interpret(false);
  }
  
  public String nextLine() throws IOException {
    if((line = input.readLine()) != null) {
      lineChar = 0;
      lineIndex = 0;
      ++lineNumber;
    }
    
    return line;
  }
  
  public int nextLineChar() {
    lineChar = line.codePointAt(lineIndex);
    lineIndex += Character.charCount(lineChar);
    
    return lineChar;
  }
  
  public StringBuilder readHeredoc() throws IOException,ParseCodeException {
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
    
    final int prevLineColumn = lineIndex;
    final String endTag = readToLineEnd().toString();
    
    // End tag cannot have whitespaces
    // - To help prevent the user from fat-fingering "<<-EOS" as "<< -EOS"
    // - Because a whitespace denotes a new arg "EOS 10 20" ("E O S 10 20" would be impossible)
    if(!endTag.equals(WHITESPACE_PATTERN.matcher(endTag).replaceAll(""))) {
      throw ParseCodeException.build(lineNumber,prevLineColumn,
        "Invalid heredoc with whitespaces in the tag or unquoted string",instructionName);
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
              
              // Is end tag: "EOS 10 20"
              if(Character.isWhitespace(lineChar)) {
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
          for(int i = minIndent; i < hdLine.length();) {
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
  
  public StringBuilder readQuote(int endQuote) throws IOException {
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
  
  public StringBuilder readSpecialQuote() throws IOException,ParseCodeException {
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
  
  public StringBuilder readToLineEnd() {
    buffer.setLength(0);
    
    if(lineIndex > 0) {
      buffer.appendCodePoint(lineChar);
    }
    
    while(hasLineChar()) {
      buffer.appendCodePoint(nextLineChar());
    }
    
    return buffer;
  }
  
  public StringBuilder readToWhitespace() {
    buffer.setLength(0);
    
    if(lineIndex > 0) {
      buffer.appendCodePoint(lineChar);
    }
    
    while(hasLineChar()) {
      if(Character.isWhitespace(nextLineChar())) {
        break;
      }
      
      buffer.appendCodePoint(lineChar);
    }
    
    return buffer;
  }
  
  /**
   * @return true if found a non-whitespace char, else false
   */
  public boolean seekToNonWhitespace() {
    while(hasLineChar()) {
      if(!Character.isWhitespace(nextLineChar())) {
        // Ignore comment
        if(lineChar == commentChar) {
          lineIndex = line.length(); // Go to end
          
          return false;
        }
        
        return true;
      }
    }
    
    return false;
  }
  
  public void setBuddy(BotBuddy buddy) {
    this.buddy = buddy;
  }
  
  public void setCommentChar(int commentChar) {
    this.commentChar = commentChar;
  }
  
  public void setEscapeChar(int escapeChar) {
    this.escapeChar = escapeChar;
  }
  
  public void setExecutors(Executors executors) {
    this.executors = executors;
  }
  
  public BotBuddy getBuddy() {
    return buddy;
  }
  
  public int getCommentChar() {
    return commentChar;
  }
  
  public int getEscapeChar() {
    return escapeChar;
  }
  
  public Executors getExecutors() {
    return executors;
  }
  
  public boolean isEndOfLine() {
    return lineIndex >= line.length();
  }
  
  public boolean hasLineChar() {
    return lineIndex < line.length();
  }
  
  public static class Arg {
    public LineOfCode loc;
    public String value;
    
    public Arg(String value,int lineNumber,int lineColumn) {
      this(value,new LineOfCode(lineNumber,lineColumn));
    }
    
    public Arg(String value,LineOfCode loc) {
      if(value == null) {
        throw new IllegalArgumentException("Value cannot be null");
      }
      if(loc == null) {
        throw new IllegalArgumentException("LineOfCode cannot be null");
      }
      
      this.loc = loc;
      this.value = value;
    }
    
    @Override
    public String toString() {
      StringBuilder str = new StringBuilder(11 + value.length());
      
      str.append(loc).append(": '").append(value).append('\'');
      
      return str.toString();
    }
  }
  
  public static class Builder {
    protected BotBuddy buddy = null;
    protected Charset charset = StandardCharsets.UTF_8;
    protected int commentChar = DEFAULT_COMMENT_CHAR;
    protected int escapeChar = DEFAULT_ESCAPE_CHAR;
    protected Executors executors = null;
    protected BufferedReader input = null;
    protected Path path = null;
    
    public Builder(BufferedReader input) {
      input(input);
    }
    
    public Builder(Path path) {
      path(path);
    }
    
    public Builder(Path path,Charset charset) {
      path(path,charset);
    }
    
    public Builder(CharSequence str) {
      input(str);
    }
    
    public Builder(List<String> strList) {
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
  
  public static class DefaultExecutors {
    public static final Executors defaultExecutors = new Executors();
    
    static {
      defaultExecutors.addBase();
    }
  }
  
  @FunctionalInterface
  public static interface Executor {
    public abstract void execute(BotBuddy buddy,Instruction inst) throws ParseCodeException;
  }
  
  public static class Executors {
    /**
     * <pre>
     * This MUST match the number of base entries in #addBase() for testing,
     *   because a JUnit test will fail if an entry has been overwritten accidentally.
     * </pre>
     */
    public static final int BASE_COUNT = 36;
    
    protected Map<String,Executor> entries;
    
    public Executors() {
      // Default loadFactor is 0.75, so make it so we have enough on init.
      //   Use 0.74 because casting doesn't round, and don't want to use Math.round().
      this((int)(BASE_COUNT / 0.74));
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
     * WARNING:
     *   If you add a new executor in this method, you MUST update #BASE_COUNT.
     *   There is a JUnit test that checks to make sure #BASE_COUNT and
     *     DefaultExecutors size are equal.
     *   This helps prevent forgetting to change the ID when copy & pasting:
     *     // "getxcoord" is overwritten by the 2nd line
     *     put("getxcoord",(buddy,inst) -> BotBuddy.getXCoord());
     *     put("getxcoord",(buddy,inst) -> BotBuddy.getYCoord());
     * </pre>
     */
    public void addBase() {
      // TODO: printScreen(): save to file or clipboard?
      // TODO: shortcut/shortcutFast(): load 2nd file or add logic for methods?
      //                                1) can use Shortcuts.PASTE, etc.
      //                                2) shortcut "getcoords\nbeep" (use heredoc/string); StringReader
      
      // Static methods
      put("getcoords",(buddy,inst) -> {
        Point coords = BotBuddy.getCoords();
        System.out.println("(" + coords.x + "," + coords.y + ")");
      });
      put("getxcoord",(buddy,inst) -> System.out.println(BotBuddy.getXCoord()));
      put("getycoord",(buddy,inst) -> System.out.println(BotBuddy.getYCoord()));
      
      // Main methods
      put("beep",(buddy,inst) -> buddy.beep());
      put("beginsafemode",(buddy,inst) -> buddy.beginSafeMode());
      put("click",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.click(); break;
          case 1:  buddy.click(inst.getInt(0)); break;
          case 2:  buddy.click(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.click(inst.getInt(0),inst.getInt(1),inst.getInt(2)); break;
        }
      });
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
      put("endsafemode",(buddy,inst) -> buddy.endSafeMode());
      put("enter",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.enter(); break;
          case 1:  buddy.enter(inst.getStr(0)); break;
          case 2:  buddy.enter(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.enter(inst.getInt(0),inst.getInt(1),inst.getStr(2)); break;
        }
      });
      put("key",(buddy,inst) -> buddy.key(inst.getInt(0)));
      put("move",(buddy,inst) -> buddy.move(inst.getInt(0),inst.getInt(1)));
      put("paste",(buddy,inst) -> {
        switch(inst.args.length) {
          case 0:  buddy.paste(); break;
          case 1:  buddy.paste(inst.getStr(0)); break;
          case 2:  buddy.paste(inst.getInt(0),inst.getInt(1)); break;
          default: buddy.paste(inst.getInt(0),inst.getInt(1),inst.getStr(2)); break;
        }
      });
      put("presskey",(buddy,inst) -> buddy.pressKey(inst.getInt(0)));
      put("pressmouse",(buddy,inst) -> buddy.pressMouse(inst.getInt(0)));
      put("releasekey",(buddy,inst) -> buddy.releaseKey(inst.getInt(0)));
      put("releasemouse",(buddy,inst) -> buddy.releaseMouse(inst.getInt(0)));
      put("waitforidle",(buddy,inst) -> buddy.waitForIdle());
      put("wheel",(buddy,inst) -> buddy.wheel(inst.getInt(0)));
      
      // Extra methods
      put("puts",(buddy,inst) -> {
        if(inst.args.length < 1) {
          System.out.println();
        }
        else {
          for(Arg arg: inst.args) {
            System.out.print(arg.value);
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
      put("setshortdelay",(buddy,inst) -> {
        buddy.setShortDelay(inst.getInt(0));
        System.out.println("setShortDelay: " + buddy.getShortDelay());
      });
      
      // Getters
      put("getpixel",(buddy,inst) -> {
        // Probably don't need alpha I think; probably always 255
        Color pixel = buddy.getPixel(inst.getInt(0),inst.getInt(1));
        int pixelWord = (pixel.getRed() << 16) | (pixel.getGreen() << 8) | (pixel.getBlue());
        StringBuilder str = new StringBuilder(47);
        
        str.append("(r=").append(pixel.getRed());
        str.append(",g=").append(pixel.getGreen());
        str.append(",b=").append(pixel.getBlue());
        str.append(") | Hex=").append(Integer.toHexString(pixelWord).toUpperCase(Locale.ENGLISH));
        str.append(" | RGB=").append(pixelWord);
        
        System.out.println(str);
      });
      put("getosfamily",(buddy,inst) -> System.out.println(buddy.getOSFamily()));
      put("getscreenheight",(buddy,inst) -> System.out.println(buddy.getScreenHeight()));
      put("getscreensize",(buddy,inst) -> {
        Dimension size = buddy.getScreenSize();
        
        System.out.println("" + size.width + "x" + size.height);
      });
      put("getscreenwidth",(buddy,inst) -> System.out.println(buddy.getScreenWidth()));
    }
    
    public boolean contains(Instruction inst) {
      return entries.containsKey(inst.id);
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
  
  public static class Instruction {
    public static final Pattern TO_ID_PATTERN = Pattern.compile("[\\s_\\-\\.]+"
      ,Pattern.UNICODE_CHARACTER_CLASS);
    public static final String[] TRUE_BOOLS = new String[]{"1","on","t","true","y","yes"};
    
    public static String toID(String name) {
      return TO_ID_PATTERN.matcher(name).replaceAll("").toLowerCase(Locale.ENGLISH);
    }
    
    public Arg[] args;
    public String id;
    public LineOfCode loc;
    public String name;
    
    public Instruction(String name,Arg[] args,int lineNumber,int lineColumn) {
      this(name,args,new LineOfCode(lineNumber,lineColumn));
    }
    
    public Instruction(String name,Arg[] args,LineOfCode loc) {
      if(name == null) {
        throw new IllegalArgumentException("Name cannot be null");
      }
      if(args == null) {
        throw new IllegalArgumentException("Args cannot be null");
      }
      if(loc == null) {
        throw new IllegalArgumentException("LineOfCode cannot be null");
      }
      
      this.args = args;
      this.id = toID(name);
      this.loc = loc;
      this.name = name;
    }
    
    public Instruction(String name,List<Arg> args,int lineNumber,int lineColumn) {
      this(name,args,new LineOfCode(lineNumber,lineColumn));
    }
    
    public Instruction(String name,List<Arg> args,LineOfCode loc) {
      this(name,args.toArray(new Arg[args.size()]),loc);
    }
    
    public ParseCodeException buildParseCodeException(String message) {
      return ParseCodeException.build(loc,message,name);
    }
    
    public ParseCodeException buildParseCodeException(String message,Throwable cause) {
      return ParseCodeException.build(loc,message,name,cause);
    }
    
    public Arg getArg(int index) throws ParseCodeException {
      if(index >= args.length) {
        throw buildParseCodeException("Not enough args");
      }
      
      return args[index];
    }
    
    public boolean getBool(int index) throws ParseCodeException {
      Arg arg = getArg(index);
      String value = WHITESPACE_PATTERN.matcher(arg.value).replaceAll("").toLowerCase(Locale.ENGLISH);
      
      // TODO: puts this code in a Strs or Bools class
      // Boolean.parseBoolean() only does a case-insensitive match on "true", so don't use it
      for(String trueBool: TRUE_BOOLS) {
        if(value.equals(trueBool)) {
          return true;
        }
      }
      
      return false;
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
    
    public String getStr(int index) throws ParseCodeException {
      return getArg(index).value;
    }
    
    @Override
    public String toString() {
      StringBuilder str = new StringBuilder();
      
      str.append('[').append(id).append(':').append(name).append("]:").append(loc).append('\n');
      
      for(int i = 0; i < args.length; ++i) {
        str.append("- [").append(i).append("]:").append(args[i]).append('\n');
      }
      
      return str.toString();
    }
  }
  
  // TODO: make own simple app here; take in file, can do dry run; ButBuddyCodeApp or here?
  public static void main(String[] args) {
    List<String> l = new LinkedList<>();
    l.add("get_coords");
    l.add("get_os_family");
    l.add("");
    l.add("puts 'hi'");
    
    try(BotBuddyCode bbc = BotBuddyCode.builder(Paths.get("stock/bb.rb")).build()) {
    //try(BotBuddyCode bbc = BotBuddyCode.builder(l).build()) {
      //System.out.print(bbc.interpretDryRun());
      bbc.interpret();
    }
    catch(Exception ex) {
      System.out.println(ex);
      ex.printStackTrace();
    }
  }
}
