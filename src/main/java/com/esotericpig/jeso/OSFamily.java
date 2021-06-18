/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * @author Jonathan Bradley Whited
 * @see <a href="https://en.wikipedia.org/wiki/Category:Operating_system_families" target="_blank">OS Families [Wikipedia]</a>
 */
public enum OSFamily {
  LINUX("linux"),MACOS("darwin mac osx"),WINDOWS("win","darwin"),UNKNOWN;

  public static final List<OSFamily> VALUES = Collections.unmodifiableList(Arrays.asList(values()));

  public static OSFamily getRandValue(Random rand) {
    return VALUES.get(rand.nextInt(VALUES.size()));
  }

  public static OSFamily guessFromName(String osName) {
    osName = osName.replaceAll("\\s+","").toLowerCase(Locale.ENGLISH);

    OSFamily osFamily = UNKNOWN;

    for(OSFamily osf: VALUES) {
      if(osf.badWords == null && osf.goodWords == null) {
        continue;
      }

      if(osf.goodWords != null) {
        boolean isNext = true;

        for(String word: osf.goodWords) {
          if(osName.contains(word)) {
            isNext = false;
            break;
          }
        }

        if(isNext) {
          continue;
        }
      }

      if(osf.badWords != null) {
        boolean isNext = false;

        for(String word: osf.badWords) {
          if(osName.contains(word)) {
            isNext = true;
            break;
          }
        }

        if(isNext) {
          continue;
        }
      }

      osFamily = osf;
      break;
    }

    return osFamily;
  }

  protected final List<String> badWords;
  protected final List<String> goodWords;

  private OSFamily() {
    this(null);
  }

  private OSFamily(String goodWords) {
    this(goodWords,null);
  }

  private OSFamily(String goodWords,String badWords) {
    this.badWords = (badWords != null)
        ? Collections.unmodifiableList(Arrays.asList(badWords.split("\\s+"))) : null;
    this.goodWords = (goodWords != null)
        ? Collections.unmodifiableList(Arrays.asList(goodWords.split("\\s+"))) : null;
  }

  public List<String> getBadWords() {
    return badWords;
  }

  public List<String> getGoodWords() {
    return goodWords;
  }
}
