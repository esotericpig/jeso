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
public class LineOfCode implements Cloneable {
  protected int column;
  protected int number;
  
  public LineOfCode() {
    reset();
  }
  
  public LineOfCode(LineOfCode loc) {
    this(loc.number,loc.column);
  }
  
  public LineOfCode(int number,int column) {
    this.column = column;
    this.number = number;
  }
  
  @Override
  public LineOfCode clone() {
    return new LineOfCode(this);
  }
  
  public int nextColumn() {
    return column++;
  }
  
  public int nextNumber() {
    return number++;
  }
  
  public void reset() {
    resetColumn();
    resetNumber();
  }
  
  public void resetColumn() {
    column = 1;
  }
  
  public void resetNumber() {
    number = 1;
  }
  
  public int getColumn() {
    return column;
  }
  
  public int getNumber() {
    return number;
  }
  
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder(9);
    
    str.append('(').append(number).append(':').append(column).append(')');
    
    return str.toString();
  }
}
