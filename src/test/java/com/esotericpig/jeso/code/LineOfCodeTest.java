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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class LineOfCodeTest {
  protected Random rand;
  
  @BeforeEach
  public void setUpEach() {
    rand = new Random();
  }
  
  @AfterEach
  public void tearDownEach() {
    rand = null;
  }
  
  @Test
  public void testAll() {
    final int lineColumn = rand.nextInt(1111);
    final int lineNumber = rand.nextInt(1111);
    
    LineOfCode loc1 = new LineOfCode();
    LineOfCode loc2 = new LineOfCode(lineNumber,lineColumn);
    LineOfCode loc3 = new LineOfCode(loc2);
    LineOfCode loc4 = loc3.clone();
    
    loc1.setColumn(lineColumn);
    loc1.setNumber(lineNumber);
    
    System.out.println(loc1.toString());
    
    assertEquals(lineColumn,loc1.getColumn());
    assertEquals(lineColumn,loc2.getColumn());
    assertEquals(lineColumn,loc3.getColumn());
    assertEquals(lineColumn,loc4.getColumn());
    assertEquals(lineNumber,loc1.getNumber());
    assertEquals(lineNumber,loc2.getNumber());
    assertEquals(lineNumber,loc3.getNumber());
    assertEquals(lineNumber,loc4.getNumber());
    
    loc1.reset();
    loc2.resetColumn();
    loc2.resetNumber();
    
    assertEquals(loc1.getColumn(),loc2.getColumn());
    assertEquals(loc1.getNumber(),loc2.getNumber());
    
    assertNotEquals(loc1.nextColumn(),loc1.nextColumn());
    assertNotEquals(loc1.nextNumber(),loc1.nextNumber());
  }
}
