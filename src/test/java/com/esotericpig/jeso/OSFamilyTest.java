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
 * <pre>
 * </pre>
 *
 * @author Jonathan Bradley Whited
 */
public class OSFamilyTest {
  @BeforeEach
  public void setUpEach() {
  }

  @AfterEach
  public void tearDownEach() {
  }

  @Test
  public void testOSes() {
    assertEquals(OSFamily.LINUX,OSFamily.guessFromName("GNU/Linux Fedora"));
    assertEquals(OSFamily.MACOS,OSFamily.guessFromName("Apple Darwin"));
    assertEquals(OSFamily.MACOS,OSFamily.guessFromName("Apple Mac OS X"));
    assertEquals(OSFamily.MACOS,OSFamily.guessFromName("Apple OS X"));
    assertEquals(OSFamily.WINDOWS,OSFamily.guessFromName("Microsoft Windows XP"));
  }
}
