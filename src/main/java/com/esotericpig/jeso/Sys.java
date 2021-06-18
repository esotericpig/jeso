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
public final class Sys {
  public static final String OS_NAME = getSafeProp("os.name","Unknown");
  public static final OSFamily OS_FAMILY = OSFamily.guessFromName(OS_NAME);

  public static String getSafeProp(String key) {
    return getSafeProp(key,null);
  }

  public static String getSafeProp(String key,String def) {
    try {
      return System.getProperty(key,def);
    }
    catch(SecurityException se) {
      // TODO: log it instead
      System.out.println(se);
      se.printStackTrace();
      return def;
    }
  }

  private Sys() {
    throw new UtilClassException();
  }
}
