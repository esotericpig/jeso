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

package com.esotericpig.jeso;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class Sys {
  public static final String OS_NAME = getSafeProp("os.name","Unknown");
  public static final OSFamily OS_FAMILY = OSFamily.guessFromName(OS_NAME);
  
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
}
