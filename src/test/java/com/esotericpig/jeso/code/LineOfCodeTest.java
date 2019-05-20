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
    
    LineOfCode loc1 = new LineOfCode(lineNumber,lineColumn);
    LineOfCode loc2 = new LineOfCode(lineNumber,lineColumn);
    
    System.out.println(loc1.toString());
    System.out.println(loc2.toString());
    
    assertNotEquals(loc1,loc1.next());
    assertNotEquals(loc1,loc1.nextColumn());
    assertNotEquals(loc1,loc1.nextNumber());
    
    assertEquals(lineColumn,loc1.getColumn());
    assertEquals(lineNumber,loc1.getNumber());
    
    assertEquals(loc1.getColumn(),loc2.getColumn());
    assertEquals(loc1.getNumber(),loc2.getNumber());
    
    assertEquals(loc1,loc2);
    assertEquals(loc1.hashCode(),loc2.hashCode());
    assertEquals(loc1.toString(),loc2.toString());
    
    LineOfCode[] locs = new LineOfCode[]{
      loc1.next(),
      loc1.nextColumn(),
      loc1.nextNumber()
    };
    
    for(LineOfCode loc: locs) {
      assertNotEquals(loc1,loc);
      assertNotEquals(loc1.hashCode(),loc.hashCode());
      assertNotEquals(loc1.toString(),loc.toString());
    }
    
    loc2 = loc1.nextColumn();
    
    assertEquals(loc1.getColumn() + 1,loc2.getColumn());
    assertEquals(loc1.getNumber(),loc2.getNumber());
    
    loc2 = loc1.nextNumber();
    
    assertEquals(loc1.getColumn(),loc2.getColumn());
    assertEquals(loc1.getNumber() + 1,loc2.getNumber());
  }
}
