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

/**
 * @author Jonathan Bradley Whited
 */
public final class Bools {
  public static final List<String> TRUE_BOOL_STRS = Collections.unmodifiableList(Arrays.asList(
      "1","on","t","true","y","yes"));

  public static boolean parse(String str) {
    str = str.trim().toLowerCase(Locale.ENGLISH);

    for(String tbs: TRUE_BOOL_STRS) {
      if(str.equals(tbs)) {
        return true;
      }
    }

    return false;
  }

  private Bools() {
    throw new UtilClassException();
  }
}
