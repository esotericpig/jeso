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
public class Strs {
  public static StringBuilder rtrim(StringBuilder sb) {
    for(int i = sb.length() - 1; i >= 0;) {
      if(Character.isLowSurrogate(sb.charAt(i))) {
        --i;
        
        continue;
      }
      
      int cp = sb.codePointAt(i);
      
      if(!Character.isWhitespace(cp)) {
        break;
      }
      
      int count = Character.charCount(cp);
      
      sb.delete(i,i + count + 1); // +1 because exclusive
      i -= count;
    }
    
    return sb;
  }
  
  private Strs() {
    throw new UnsupportedOperationException("Cannot construct a utility class");
  }
}
