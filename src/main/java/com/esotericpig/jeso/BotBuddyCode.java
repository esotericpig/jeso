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

package com.esotericpig.jeso;

import java.awt.AWTException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

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
  protected StringBuilder instructions = new StringBuilder();
  protected String line = null;
  protected int lineChar = 0;
  protected int lineIndex = 0;
  protected int lineNumber = 0;
  
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
    
    setBuddy(builder.buddy);
    setCommentChar(builder.commentChar);
    setEscapeChar(builder.escapeChar);
    input = builder.input;
  }
  
  @Override
  public void close() throws IOException {
    // For Garbage Collection (GC)
    buddy = null;
    buffer = null;
    instructions = null;
    line = null;
    
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
    instructions.setLength(0);
    line = null;
    lineNumber = 0;
    
    while(readLine() != null) {
      resetLine();
      seekToNonWhitespace();
      
      // Ignore empty line or comment (handled in seek)
      if(isLineEnd()) {
        continue;
      }
      
      // Instruction name
      String name = readToWhitespace().toString();
      
      if(!execute) {
        instructions.append('[').append(name).append("]\n");
      }
      
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
          throw new ParseException("Internal code is broken causing an infinite loop",lineNumber,lineIndex);
        }
      } while(line != null && hasLineChar()); // nextLine() might have been called
      
      if(execute) {
        // TODO: execute
      }
      else {
        for(Arg arg: args) {
          instructions.append("- \"").append(arg.getValue()).append("\"\n");
        }
      }
    }
    
    return instructions.toString();
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
  
  public boolean hasLineChar() {
    return lineIndex < line.length();
  }
  
  public boolean isLineEnd() {
    return lineIndex >= line.length();
  }
  
  public static class Arg {
    protected int lineColumn;
    protected int lineNumber;
    protected String value;
    
    public Arg(String value,int lineNumber,int lineColumn) {
      this.lineColumn = lineColumn;
      this.lineNumber = lineNumber;
      this.value = value;
    }
    
    @Override
    public String toString() {
      StringBuilder str = new StringBuilder(value.length() + 11);
      
      str.append(lineNumber).append(':').append(lineColumn);
      str.append(": \"").append(value).append("\"\n");
      
      return str.toString();
    }
    
    public int getLineColumn() {
      return lineColumn;
    }
    
    public int getLineNumber() {
      return lineNumber;
    }
    
    public String getValue() {
      return value;
    }
  }
  
  public static class Builder {
    protected BotBuddy buddy = null;
    protected Charset charset = StandardCharsets.UTF_8;
    protected int commentChar = DEFAULT_COMMENT_CHAR;
    protected int escapeChar = DEFAULT_ESCAPE_CHAR;
    protected BufferedReader input = null;
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
    
    public Builder path(Path path) {
      this.path = path;
      
      return this;
    }
  }
  
  public static class ParseException extends Exception {
    protected int lineColumn;
    protected int lineNumber;
    
    public ParseException(String message,int lineNumber,int lineColumn) {
      super((new StringBuilder(message.length() + 9))
        .append(lineNumber).append(':').append(lineColumn).append(": ").append(message)
        .toString());
      
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
  
  // TODO: remove
  public static void main(String[] args) {
    try(BotBuddyCode bbc = BotBuddyCode.builder(java.nio.file.Paths.get("bb.rb")).build()) {
      System.out.print(bbc.interpretDryRun());
    }
    catch(Exception ex) {
      System.out.println(ex);
      ex.printStackTrace();
    }
  }
}
