/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * </pre>
 *
 * @author Jonathan Bradley Whited
 */
public class SysTest {
  @BeforeEach
  public void setUpEach() {
  }

  @AfterEach
  public void tearDownEach() {
  }

  @Test
  public void testOS() {
    System.out.println("OS Name:   " + Sys.OS_NAME);
    System.out.println("OS Family: " + Sys.OS_FAMILY);
  }
}
