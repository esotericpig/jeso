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

import java.awt.AWTException;
import java.awt.Point;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import java.util.regex.Pattern;

/**
 * <pre>
 * 
 * // TODO: flesh this out
 * If you want to process a String, use StringReader:
 *   BotBuddyCode bbc = BotBuddyCode().builder(new StringReader(str)).build();
 *   
 *   bbc.interpretDryRun();
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 * 
 * @see java.io.StringReader
 * @see java.nio.file.Paths
 */
public class BotBuddyCode implements Closeable {
  public static final int DEFAULT_COMMENT_CHAR = '#';
  public static final int DEFAULT_ESCAPE_CHAR = '\\';
  
  public static Map<String,Instructor> addDefaultInstructors(Map<String,Instructor> instructors) {
    // TODO:
    // delay,delayAuto,delayFast,delayLong,delayShort
    // doubleClick x4
    // enter x4
    // key
    // move
    // pressKey, pressMouse, releaseKey, releaseMouse
    // printScreen x2
    // waitForIdle
    // wheel
    // setAutoDelay x2
    // setAutoWaitForIdle
    // setFastDelay,setLongDelay,setShortDelay
    // setOSFamily
    // getPixel
    // getOSFamily
    // rest of getters
    
    // Static methods
    instructors.put("getcoords",(buddy,inst) -> {
      Point coords = BotBuddy.getCoords();
      System.out.println("(" + coords.x + "," + coords.y + ")");
    });
    instructors.put("getxcoord",(buddy,inst) -> System.out.println(BotBuddy.getXCoord()));
    instructors.put("getycoord",(buddy,inst) -> System.out.println(BotBuddy.getYCoord()));
    
    // Main methods
    instructors.put("beep",(buddy,inst) -> buddy.beep());
    instructors.put("beginsafemode",(buddy,inst) -> buddy.beginSafeMode());
    instructors.put("click",(buddy,inst) -> {
      switch(inst.getArgsLength()) {
        case 0: buddy.click(); break;
        case 1: buddy.click(inst.getArg(0).parseInt()); break;
        case 2: buddy.click(inst.getArg(0).parseInt(),inst.getArg(1).parseInt()); break;
        default:
          buddy.click(inst.getArg(0).parseInt(),inst.getArg(1).parseInt(),inst.getArg(2).parseInt());
          break;
      }
    });
    instructors.put("copy",(buddy,inst) -> {
      inst.checkArgsLength(1);
      buddy.copy(inst.getArg(0).getValue());
    });
    instructors.put("endsafemode",(buddy,inst) -> buddy.endSafeMode());
    instructors.put("paste",(buddy,inst) -> {
      switch(inst.getArgsLength()) {
        case 0: buddy.paste(); break;
        case 1: buddy.paste(inst.getArg(0).getValue()); break;
        case 2: buddy.paste(inst.getArg(0).parseInt(),inst.getArg(1).parseInt()); break;
        default:
          buddy.paste(inst.getArg(0).parseInt(),inst.getArg(1).parseInt(),inst.getArg(2).getValue());
          break;
      }
    });
    
    // Setters
    
    // Getters
    
    return instructors;
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
  
  protected BotBuddy buddy;
  protected StringBuilder buffer = new StringBuilder();
  protected int commentChar;
  protected int escapeChar;
  protected BufferedReader input = null;
  protected Map<String,Instructor> instructors = null;
  protected String line = null;
  protected int lineChar = 0;
  protected int lineIndex = 0;
  protected int lineNumber = 0;
  protected StringBuilder output = new StringBuilder();
  
  public BotBuddyCode(Builder builder) throws AWTException,IOException {
    if(builder.buddy == null) {
      builder.buddy(BotBuddy.builder().build());
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
    if(builder.instructors == null) {
      builder.instructors(new HashMap<>()); // TODO: init capacity
      addDefaultInstructors(builder.instructors);
    }
    
    setBuddy(builder.buddy);
    setCommentChar(builder.commentChar);
    setEscapeChar(builder.escapeChar);
    input = builder.input;
    instructors = builder.instructors;
  }
  
  @Override
  public void close() throws IOException {
    // For Garbage Collection (GC)
    buddy = null;
    buffer = null;
    line = null;
    output = null;
    
    if(input != null) {
      input.close();
      input = null;
    }
  }
  
  public void incLineChar() {
    lineIndex += Character.charCount(lineChar);
  }
  
  public void interpret() throws IOException,ParseException {
    interpret(true);
  }
  
  public String interpret(boolean execute) throws IOException,ParseException {
    line = null;
    lineNumber = 0;
    output.setLength(0);
    
    while(readLine() != null) {
      resetLine();
      seekToNonWhitespace();
      
      // Ignore empty line or comment (handled in seek)
      if(isLineEnd()) {
        continue;
      }
      
      // Instruction name
      LinePoint linePoint = new LinePoint(lineNumber,lineIndex + 1);
      String name = readToWhitespace().toString();
      
      // Instruction args
      List<Arg> args = new ArrayList<>();
      
      do {
        seekToNonWhitespace();
        
        if(isLineEnd()) {
          break;
        }
        
        int prevLineIndex = lineIndex;
        int prevLineNumber = lineNumber;
        
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
        
        args.add(new Arg(buffer.toString(),prevLineNumber,prevLineIndex + 1));
        
        if(lineIndex == prevLineIndex && lineNumber == prevLineNumber) {
          throw new ParseException("Internal code is broken causing an infinite loop",lineNumber
            ,lineIndex + 1);
        }
      } while(line != null && hasLineChar()); // nextLine() might have been called
      
      // Execute/output instruction
      Instruction instruction = new Instruction(name,args,linePoint);
      
      if(execute) {
        // TODO: throw exception if doesn't exist
        Instructor instructor = instructors.get(instruction.getID());
        
        if(instructor != null) {
          instructor.execute(buddy,instruction);
        }
      }
      else {
        // TODO: add ID and whether inst exists
        output.append('[').append(instruction.getName()).append("]\n");
        
        for(Arg arg: args) {
          output.append("- \"").append(arg.getValue()).append("\"\n");
        }
      }
    }
    
    return output.toString();
  }
  
  public String interpretDryRun() throws IOException,ParseException {
    return interpret(false);
  }
  
  public StringBuilder readHeredoc() throws IOException,ParseException {
    // '<...' instead of '<<...'
    incLineChar();
    if(!hasLineChar() || readLineChar() != '<') {
      throw new ParseException("Invalid heredoc '<' instead of '<<' or unquoted string",lineNumber,lineIndex);
    }
    
    // '<<' with EOL
    incLineChar();
    if(!hasLineChar()) {
      throw new ParseException("Invalid heredoc without a tag or unquoted string",lineNumber,lineIndex);
    }
    
    boolean isIndent = (readLineChar() == '-');
    
    if(isIndent) {
      incLineChar();
    }
    
    int prevLineIndex = lineIndex;
    String endTag = readToLineEnd().toString();
    
    // '<<-' with EOL
    if(endTag.isEmpty()) {
      throw new ParseException("Invalid heredoc without a tag or unquoted string",lineNumber,prevLineIndex);
    }
    // '<< ...' or '<<- ...'
    if(!endTag.equals(endTag.trim())) {
      throw new ParseException("Invalid heredoc with spaces or unquoted string",lineNumber,prevLineIndex);
    }
    
    List<String> heredoc = new LinkedList<>();
    int minIndent = Integer.MAX_VALUE;
    
    while(readLine() != null) {
      int indent = 0;
      
      resetLine();
      buffer.setLength(0);
      
      // Read to non-whitespace
      for(; hasLineChar(); incLineChar(), indent += Character.charCount(lineChar)) {
        buffer.appendCodePoint(readLineChar());
        
        if(!Character.isWhitespace(lineChar)) {
          break;
        }
      }
      
      if(hasLineChar()) {
        // Is end tag?
        if(lineChar == endTag.codePointAt(0)) {
          boolean isEndTag = false;
          
          incLineChar();
          
          for(int i = 1; hasLineChar(); incLineChar()) {
            int endTagChar = endTag.codePointAt(i);
            
            buffer.appendCodePoint(readLineChar());
            
            // Not end tag: "Everybody!"
            if(lineChar != endTagChar) {
              break;
            }
            
            // Last char of end tag
            if((i += Character.charCount(endTagChar)) >= endTag.length()) {
              incLineChar();
              
              if(hasLineChar()) {
                // Is end tag: "EOS 10 20"
                if(Character.isWhitespace(readLineChar())) {
                  isEndTag = true;
                }
                // Not end tag: "EOS?"
                else {
                  buffer.appendCodePoint(lineChar);
                }
              }
              // Is end tag: "EOS"
              else {
                isEndTag = true;
              }
              
              break;
            }
          }
          
          if(isEndTag) {
            break;
          }
        }
        
        // Read rest of chars to line end
        for(incLineChar(); hasLineChar(); incLineChar()) {
          buffer.appendCodePoint(readLineChar());
        }
      }
      
      heredoc.add(buffer.toString());
      
      // Update min indent
      if(isIndent && indent < minIndent) {
        minIndent = indent;
      }
    }
    
    isIndent = (isIndent && minIndent > 0 && minIndent != Integer.MAX_VALUE);
    buffer.setLength(0);
    
    if(!heredoc.isEmpty()) {
      for(Iterator<String> it = heredoc.iterator();;) {
        String hd = it.next();
        
        if(isIndent) {
          for(int i = minIndent; i < hd.length();) {
            int hdChar = hd.codePointAt(i);
            
            buffer.appendCodePoint(hdChar);
            i += Character.charCount(hdChar);
          }
        }
        else {
          buffer.append(hd);
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
  
  public String readLine() throws IOException {
    if((line = input.readLine()) != null) {
      ++lineNumber;
    }
    
    return line;
  }
  
  public int readLineChar() {
    return (lineChar = line.codePointAt(lineIndex));
  }
  
  public StringBuilder readQuote(int endQuote) throws IOException {
    boolean hasEndQuote = false;
    
    buffer.setLength(0);
    incLineChar();
    
    while(true) {
      for(; hasLineChar(); incLineChar()) {
        readLineChar();
        
        // Escaped end quote (e.g., \")
        if(lineChar == escapeChar) {
          incLineChar();
          
          if(hasLineChar()) {
            if(readLineChar() == endQuote) {
              buffer.appendCodePoint(lineChar);
            }
            else {
              buffer.appendCodePoint(escapeChar).appendCodePoint(lineChar);
            }
          }
          else {
            buffer.appendCodePoint(lineChar);
          }
        }
        else if(lineChar == endQuote) {
          incLineChar();
          hasEndQuote = true;
          
          break;
        }
        else {
          buffer.appendCodePoint(lineChar);
        }
      }
      
      if(hasEndQuote || readLine() == null) {
        break;
      }
      
      buffer.append('\n');
      resetLine();
    }
    
    return buffer;
  }
  
  public StringBuilder readSpecialQuote() throws IOException,ParseException {
    incLineChar();
    
    if(!hasLineChar() || Character.isWhitespace(readLineChar())) {
      throw new ParseException("Invalid '%' without a tag or unquoted string",lineNumber,lineIndex);
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
    for(buffer.setLength(0); hasLineChar(); incLineChar()) {
      buffer.appendCodePoint(readLineChar());
    }
    
    return buffer;
  }
  
  public StringBuilder readToWhitespace() {
    for(buffer.setLength(0); hasLineChar(); incLineChar()) {
      if(Character.isWhitespace(readLineChar())) {
        break;
      }
      
      buffer.appendCodePoint(lineChar);
    }
    
    return buffer;
  }
  
  public void resetLine() {
    lineChar = 0;
    lineIndex = 0;
  }
  
  public int seekToNonWhitespace() {
    for(; hasLineChar(); incLineChar()) {
      if(!Character.isWhitespace(readLineChar())) {
        // Ignore comment
        if(lineChar == commentChar) {
          lineIndex = line.length(); // Go to end
        }
        
        break;
      }
    }
    
    return lineChar;
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
  
  public BotBuddy getBuddy() {
    return buddy;
  }
  
  public int getCommentChar() {
    return commentChar;
  }
  
  public int getEscapeChar() {
    return escapeChar;
  }
  
  public BufferedReader getInput() {
    return input;
  }
  
  public Map<String,Instructor> getInstructors() {
    return instructors;
  }
  
  public boolean hasLineChar() {
    return lineIndex < line.length();
  }
  
  public boolean isLineEnd() {
    return lineIndex >= line.length();
  }
  
  public static class Arg extends LinePoint {
    protected String value;
    
    public Arg(String value,int lineNumber,int lineColumn) {
      super(lineNumber,lineColumn);
      
      if(value == null) {
        throw new IllegalArgumentException("Value cannot be null");
      }
      
      this.value = value;
    }
    
    public int parseInt() throws ParseException {
      try {
        return Integer.parseInt(value);
      }
      catch(NumberFormatException ex) {
        throw new ParseException("Arg '" + value + "' must be an int",this,ex);
      }
    }
    
    public String getValue() {
      return value;
    }
    
    @Override
    public String toString() {
      StringBuilder str = new StringBuilder(value.length() + 11);
      
      str.append(lineNumber).append(':').append(lineColumn);
      str.append(": \"").append(value).append("\"\n");
      
      return str.toString();
    }
  }
  
  public static class Builder {
    protected BotBuddy buddy = null;
    protected Charset charset = StandardCharsets.UTF_8;
    protected int commentChar = DEFAULT_COMMENT_CHAR;
    protected int escapeChar = DEFAULT_ESCAPE_CHAR;
    protected BufferedReader input = null;
    protected Map<String,Instructor> instructors = null;
    protected Path path = null;
    
    public Builder(BufferedReader input) {
      input(input);
    }
    
    public Builder(Path path) {
      path(path);
    }
    
    public Builder(Path path,Charset charset) {
      input(path,charset);
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
    
    public Builder input(BufferedReader input) {
      this.input = input;
      
      return this;
    }
    
    public Builder input(Path path,Charset charset) {
      this.charset = charset;
      this.path = path;
      
      return this;
    }
    
    public Builder instructors(Map<String,Instructor> instructors) {
      this.instructors = instructors;
      
      return this;
    }
    
    public Builder path(Path path) {
      this.path = path;
      
      return this;
    }
  }
  
  public static class Instruction extends LinePoint {
    public static final Pattern ID_PATTERN = Pattern.compile("[\\s_\\-\\.]+"
      ,Pattern.UNICODE_CHARACTER_CLASS);
    
    public static final String toID(String name) {
      return ID_PATTERN.matcher(name).replaceAll("").toLowerCase(Locale.ENGLISH);
    }
    
    protected Arg[] args;
    protected String id;
    protected String name;
    
    public Instruction(String name,Arg[] args,int lineNumber,int lineColumn) {
      super(lineNumber,lineColumn);
      
      if(name == null) {
        throw new IllegalArgumentException("Name cannot be null");
      }
      if(args == null) {
        throw new IllegalArgumentException("Args cannot be null");
      }
      
      this.args = args;
      this.id = toID(name);
      this.name = name;
    }
    
    public Instruction(String name,List<Arg> args,int lineNumber,int lineColumn) {
      this(name,args.toArray(new Arg[args.size()]),lineNumber,lineColumn);
    }
    
    public Instruction(String name,Arg[] args,LinePoint linePoint) {
      this(name,args,linePoint.lineNumber,linePoint.lineColumn);
    }
    
    public Instruction(String name,List<Arg> args,LinePoint linePoint) {
      this(name,args,linePoint.lineNumber,linePoint.lineColumn);
    }
    
    public void checkArgsLength(int length) throws ParseException {
      if(args.length < length) {
        throw new ParseException("Must have " + length + " arg(s)",this);
      }
    }
    
    public Arg getArg(int index) {
      return args[index];
    }
    
    public Arg[] getArgs() {
      return args;
    }
    
    public int getArgsLength() {
      return args.length;
    }
    
    public String getID() {
      return id;
    }
    
    public String getName() {
      return name;
    }
  }
  
  @FunctionalInterface
  public static interface Instructor {
    public abstract void execute(BotBuddy buddy,Instruction inst) throws ParseException;
  }
  
  public static class LinePoint {
    protected int lineColumn;
    protected int lineNumber;
    
    public LinePoint(int lineNumber,int lineColumn) {
      this.lineColumn = lineColumn;
      this.lineNumber = lineNumber;
    }
    
    public int getLineColumn() {
      return lineColumn;
    }
    
    public int getLineNumber() {
      return lineNumber;
    }
  }
  
  public static class ParseException extends Exception {
    public static String buildMessage(String message,int lineNumber,int lineColumn) {
      StringBuilder msg = new StringBuilder(message.length() + 9);
      
      msg.append(lineNumber).append(':').append(lineColumn);
      msg.append(": ").append(message);
      
      return msg.toString();
    }
    
    protected int lineColumn;
    protected int lineNumber;
    
    public ParseException(String message,int lineNumber,int lineColumn) {
      this(message,lineNumber,lineColumn,null);
    }
    
    public ParseException(String message,int lineNumber,int lineColumn,Throwable cause) {
      super(buildMessage(message,lineNumber,lineColumn),cause);
      
      this.lineColumn = lineColumn;
      this.lineNumber = lineNumber;
    }
    
    public ParseException(String message,LinePoint linePoint) {
      this(message,linePoint.getLineNumber(),linePoint.getLineColumn());
    }
    
    public ParseException(String message,LinePoint linePoint,Throwable cause) {
      this(message,linePoint.getLineNumber(),linePoint.getLineColumn(),cause);
    }
    
    public int getLineColumn() {
      return lineColumn;
    }
    
    public int getLineNumber() {
      return lineNumber;
    }
  }
  
  // TODO: remove
  public static void main(String[] args) {
    try(BotBuddyCode bbc = BotBuddyCode.builder(java.nio.file.Paths.get("bb.rb")).build()) {
      //System.out.print(bbc.interpretDryRun());
      bbc.interpret();
    }
    catch(Exception ex) {
      System.out.println(ex);
      ex.printStackTrace();
    }
  }
}
