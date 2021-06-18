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
public final class LineOfCode {
  public static final LineOfCode FIRST = new LineOfCode();

  private final int column;
  private final int number;

  protected LineOfCode() {
    this(1,1);
  }

  public LineOfCode(final int number,final int column) {
    this.column = column;
    this.number = number;
  }

  public LineOfCode next() {
    return new LineOfCode(number + 1,1);
  }

  public LineOfCode next(final int maxColumn) {
    return next(maxColumn,1);
  }

  public LineOfCode next(final int maxColumn,int columnInc) {
    columnInc = Math.abs(columnInc);

    final int col = column + columnInc;

    if(col > maxColumn) {
      return new LineOfCode(number + 1,1);
    }

    return new LineOfCode(number,col);
  }

  public LineOfCode nextColumn() {
    return nextColumn(1);
  }

  public LineOfCode nextColumn(final int columnInc) {
    return new LineOfCode(number,column + columnInc);
  }

  public LineOfCode nextNumber() {
    return nextNumber(1);
  }

  public LineOfCode nextNumber(final int numberInc) {
    return new LineOfCode(number + numberInc,column);
  }

  public LineOfCode prev() {
    return (number <= 1) ? FIRST : (new LineOfCode(number - 1,1));
  }

  public LineOfCode prev(final int maxColumn) {
    return prev(maxColumn,1);
  }

  public LineOfCode prev(final int maxColumn,int columnDec) {
    columnDec = Math.abs(columnDec);

    final int col = column - columnDec;

    if(col < 1) {
      if(number <= 1) {
        return FIRST;
      }

      return new LineOfCode(number - 1,maxColumn);
    }

    return new LineOfCode(number,col);
  }

  public LineOfCode prevColumn() {
    return prevColumn(1);
  }

  public LineOfCode prevColumn(final int columnDec) {
    return new LineOfCode(number,column - columnDec);
  }

  public LineOfCode prevNumber() {
    return prevNumber(1);
  }

  public LineOfCode prevNumber(final int numberDec) {
    return new LineOfCode(number - numberDec,column);
  }

  public int getColumn() {
    return column;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof LineOfCode)) {
      return false;
    }

    LineOfCode loc = (LineOfCode)obj;

    return column == loc.column && number == loc.number;
  }

  @Override
  public int hashCode() {
    int result = 1;

    result = 31 * result + column;
    result = 31 * result + number;

    return result;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder(9);

    str.append('(').append(number).append(':').append(column).append(')');

    return str.toString();
  }
}
