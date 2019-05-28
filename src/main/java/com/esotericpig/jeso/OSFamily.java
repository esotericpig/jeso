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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 * @see    <a href="https://en.wikipedia.org/wiki/Category:Operating_system_families" target="_blank">OS Families [Wikipedia]</a>
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
