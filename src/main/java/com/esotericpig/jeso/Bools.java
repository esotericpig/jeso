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

import java.util.Locale;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class Bools {
  public static final String[] TRUE_BOOL_STRS = new String[]{"1","on","t","true","y","yes"};
  
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
    throw new UnsupportedOperationException("Cannot construct a utility class");
  }
}
