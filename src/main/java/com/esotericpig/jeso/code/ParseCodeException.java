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

package com.esotericpig.jeso.code;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class ParseCodeException extends Exception {
  public static String buildMessage(String message,int lineNumber,int lineColumn) {
    StringBuilder msg = new StringBuilder(message.length() + 11);
    
    msg.append('(').append(lineNumber).append(':').append(lineColumn).append(')');
    msg.append(": ").append(message);
    
    return msg.toString();
  }
  
  public static String buildMessage(String message,LineOfCode loc) {
    return buildMessage(message,loc.getNumber(),loc.getColumn());
  }
  
  protected LineOfCode loc;
  
  public ParseCodeException(String message,int lineNumber,int lineColumn) {
    this(message,lineNumber,lineColumn,null);
  }
  
  public ParseCodeException(String message,int lineNumber,int lineColumn,Throwable cause) {
    this(message,new LineOfCode(lineNumber,lineColumn),cause);
  }
  
  public ParseCodeException(String message,LineOfCode loc) {
    this(message,loc,null);
  }
  
  public ParseCodeException(String message,LineOfCode loc,Throwable cause) {
    super(buildMessage(message,loc),cause);
    
    this.loc = loc;
  }
  
  public int getLineColumn() {
    return loc.getColumn();
  }
  
  public int getLineNumber() {
    return loc.getNumber();
  }
  
  public LineOfCode getLoc() {
    return loc;
  }
}
