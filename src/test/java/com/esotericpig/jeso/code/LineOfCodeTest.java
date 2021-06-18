/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
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
 * @author Jonathan Bradley Whited
 */
public class LineOfCodeTest {
  public static final int MAX_VALUE = 1111;

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
  public void testBasic() {
    final int lineColumn = rand.nextInt(MAX_VALUE);
    final int lineNumber = rand.nextInt(MAX_VALUE);

    final LineOfCode loc1 = new LineOfCode(lineNumber,lineColumn);
    final LineOfCode loc2 = new LineOfCode(lineNumber,lineColumn);

    System.out.println(loc1.toString());
    System.out.println(loc2.toString());

    assertEquals(lineColumn,loc1.getColumn());
    assertEquals(lineNumber,loc1.getNumber());

    assertEquals(loc1.getColumn(),loc2.getColumn());
    assertEquals(loc1.getNumber(),loc2.getNumber());

    assertEquals(loc1,loc2);
    assertEquals(loc1.hashCode(),loc2.hashCode());
    assertEquals(loc1.toString(),loc2.toString());
  }

  public void testChange(LineOfCode loc1,LineOfCode loc2,int newNum,int newCol) {
    assertEquals(newCol,loc2.getColumn());
    assertEquals(newNum,loc2.getNumber());

    assertNotEquals(loc1,loc2);
    assertNotEquals(loc1.hashCode(),loc2.hashCode());
    assertNotEquals(loc1.toString(),loc2.toString());
  }

  @Test
  public void testNext() {
    final LineOfCode loc = new LineOfCode(rand.nextInt(MAX_VALUE),rand.nextInt(MAX_VALUE));

    testChange(loc,loc.next(),loc.getNumber() + 1,1);
    testChange(loc,loc.next(0),loc.getNumber() + 1,1);
    testChange(loc,loc.next(loc.getColumn() + 1),loc.getNumber(),loc.getColumn() + 1);
    testChange(loc,loc.next(loc.getColumn() + 1,2),loc.getNumber() + 1,1);
    testChange(loc,loc.next(loc.getColumn() + 2,2),loc.getNumber(),loc.getColumn() + 2);

    testChange(loc,loc.nextColumn(),loc.getNumber(),loc.getColumn() + 1);
    testChange(loc,loc.nextColumn(2),loc.getNumber(),loc.getColumn() + 2);

    testChange(loc,loc.nextNumber(),loc.getNumber() + 1,loc.getColumn());
    testChange(loc,loc.nextNumber(2),loc.getNumber() + 2,loc.getColumn());
  }

  @Test
  public void testPrev() {
    final int lineNumber = 99;
    final int maxColumn = 110;

    final LineOfCode locCol1 = new LineOfCode(lineNumber,1);
    final LineOfCode locCol2 = new LineOfCode(lineNumber,2);
    final LineOfCode locCol3 = new LineOfCode(lineNumber,3);

    testChange(locCol1,locCol1.prev(),locCol1.getNumber() - 1,1);
    testChange(locCol1,locCol1.prev(maxColumn),locCol1.getNumber() - 1,maxColumn);
    testChange(locCol2,locCol2.prev(maxColumn),locCol2.getNumber(),locCol2.getColumn() - 1);
    testChange(locCol2,locCol2.prev(maxColumn,2),locCol2.getNumber() - 1,maxColumn);
    testChange(locCol3,locCol3.prev(maxColumn,2),locCol3.getNumber(),locCol3.getColumn() - 2);

    testChange(locCol3,locCol3.prevColumn(),locCol3.getNumber(),locCol3.getColumn() - 1);
    testChange(locCol3,locCol3.prevColumn(2),locCol3.getNumber(),locCol3.getColumn() - 2);

    testChange(locCol3,locCol3.prevNumber(),locCol3.getNumber() - 1,locCol3.getColumn());
    testChange(locCol3,locCol3.prevNumber(2),locCol3.getNumber() - 2,locCol3.getColumn());
  }
}
