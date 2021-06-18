/*
 * This file is part of Jeso.
 * Copyright (c) 2020-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

/**
 * @author Jonathan Bradley Whited
 * @since  0.3.5
 */
public final class Chars {
  public static String toString(int codePoint) {
    StringBuilder sb = new StringBuilder();

    sb.appendCodePoint(codePoint);

    return sb.toString();
  }

  private Chars() {
    throw new UtilClassException();
  }
}
