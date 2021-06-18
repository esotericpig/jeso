/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jonathan Bradley Whited
 */
public class StrsTest {
  @BeforeEach
  public void setUpEach() {
  }

  @AfterEach
  public void tearDownEach() {
  }

  @Test
  public void testTrims() {
    testTrims("");
    testTrims(" ");
    testTrims("     ");
    testTrims("a");
    testTrims(" a");
    testTrims("     a");
    testTrims("a ");
    testTrims("a     ");
    testTrims(" a ");
    testTrims("     a     ");
    testTrims("abc");
    testTrims(" abc");
    testTrims("     abc");
    testTrims("abc ");
    testTrims("abc     ");
    testTrims(" abc ");
    testTrims("     abc     ");
  }

  public void testTrims(String str) {
    assertEquals(str.replaceFirst("\\A\\s+",""),Strs.ltrim(new StringBuilder(str)).toString());
    assertEquals(str.replaceFirst("\\s+\\z",""),Strs.rtrim(new StringBuilder(str)).toString());
    assertEquals(str.trim(),Strs.trim(new StringBuilder(str)).toString());
  }
}
