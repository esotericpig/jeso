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
public class LineOfCode {
  public static final LineOfCode FIRST = new LineOfCode();
  
  protected final int column;
  protected final int number;
  
  protected LineOfCode() {
    this(1,1);
  }
  
  public LineOfCode(int number,int column) {
    this.column = column;
    this.number = number;
  }
  
  public LineOfCode next() {
    return new LineOfCode(number + 1,1);
  }
  
  public LineOfCode nextColumn() {
    return new LineOfCode(number,column + 1);
  }
  
  public LineOfCode nextNumber() {
    return new LineOfCode(number + 1,column + 1);
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
