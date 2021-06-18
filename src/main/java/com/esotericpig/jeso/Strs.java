/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

/**
 * @author Jonathan Bradley Whited
 */
public final class Strs {
  public static StringBuilder ltrim(StringBuilder sb) {
    int len = sb.length();

    if(len == 0) {
      return sb;
    }

    int i = 0;

    while(i < len) {
      int cp = sb.codePointAt(i);

      if(!Character.isWhitespace(cp)) {
        break;
      }

      i += Character.charCount(cp); // Do it this way, else need to decrement after the loop
    }

    return sb.delete(0,i);
  }

  public static StringBuilder rtrim(StringBuilder sb) {
    int len = sb.length();

    if(len == 0) {
      return sb;
    }

    int i = len;

    while(i > 0) {
      --i; // Do it this way, else need to increment after the loop

      // Trailing (2nd) surrogate code unit of a pair?
      if(Character.isLowSurrogate(sb.charAt(i))) {
        continue; // Go to high/leading (1st) surrogate code unit of the pair
      }

      int cp = sb.codePointAt(i);

      if(!Character.isWhitespace(cp)) {
        i += Character.charCount(cp);

        break;
      }
    }

    return sb.delete(i,len);
  }

  public static StringBuilder trim(StringBuilder sb) {
    return rtrim(ltrim(sb));
  }

  private Strs() {
    throw new UtilClassException();
  }
}
