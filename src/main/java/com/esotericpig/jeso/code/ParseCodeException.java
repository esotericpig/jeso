/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.code;

/**
 * @author Jonathan Bradley Whited
 */
public class ParseCodeException extends RuntimeException {
  public static ParseCodeException build(int lineNumber,int lineColumn,String message) {
    return new ParseCodeException(lineNumber,lineColumn,buildMessage(lineNumber,lineColumn,message));
  }

  public static ParseCodeException build(int lineNumber,int lineColumn,String message,String name) {
    return new ParseCodeException(lineNumber,lineColumn,buildMessage(lineNumber,lineColumn,message,name));
  }

  public static ParseCodeException build(int lineNumber,int lineColumn,String message,String name,Throwable cause) {
    return new ParseCodeException(lineNumber,lineColumn,buildMessage(lineNumber,lineColumn,message,name)
        ,cause);
  }

  public static ParseCodeException build(LineOfCode loc,String message) {
    return new ParseCodeException(loc,buildMessage(loc,message));
  }

  public static ParseCodeException build(LineOfCode loc,String message,String name) {
    return new ParseCodeException(loc,buildMessage(loc,message,name));
  }

  public static ParseCodeException build(LineOfCode loc,String message,String name,Throwable cause) {
    return new ParseCodeException(loc,buildMessage(loc,message,name),cause);
  }

  public static String buildMessage(int lineNumber,int lineColumn,String message) {
    return buildMessage(lineNumber,lineColumn,message,null);
  }

  public static String buildMessage(int lineNumber,int lineColumn,String message,String name) {
    int nameLen = (name != null) ? (name.length() + 1) : 0;
    StringBuilder msg = new StringBuilder(nameLen + 11 + message.length());

    if(name != null) {
      msg.append(name).append(':');
    }

    msg.append('(').append(lineNumber).append(':').append(lineColumn).append(')');
    msg.append(": ").append(message);

    return msg.toString();
  }

  public static String buildMessage(LineOfCode loc,String message) {
    return buildMessage(loc.getNumber(),loc.getColumn(),message,null);
  }

  public static String buildMessage(LineOfCode loc,String message,String name) {
    return buildMessage(loc.getNumber(),loc.getColumn(),message,name);
  }

  protected LineOfCode loc;

  public ParseCodeException(int lineNumber,int lineColumn) {
    this(new LineOfCode(lineNumber,lineColumn),null,null);
  }

  public ParseCodeException(int lineNumber,int lineColumn,Throwable cause) {
    this(new LineOfCode(lineNumber,lineColumn),null,cause);
  }

  public ParseCodeException(int lineNumber,int lineColumn,String message) {
    this(new LineOfCode(lineNumber,lineColumn),message,null);
  }

  public ParseCodeException(int lineNumber,int lineColumn,String message,Throwable cause) {
    this(new LineOfCode(lineNumber,lineColumn),message,cause);
  }

  public ParseCodeException(LineOfCode loc) {
    this(loc,null,null);
  }

  public ParseCodeException(LineOfCode loc,Throwable cause) {
    this(loc,null,cause);
  }

  public ParseCodeException(LineOfCode loc,String message) {
    this(loc,message,null);
  }

  public ParseCodeException(LineOfCode loc,String message,Throwable cause) {
    super(message,cause);

    this.loc = loc;
  }

  public int getLineColumn() {
    return loc.getColumn();
  }

  public int getLineNumber() {
    return loc.getNumber();
  }

  public LineOfCode getLineOfCode() {
    return loc;
  }
}
