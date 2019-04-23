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
public class Sys {
  public static final String OS_NAME = System.getProperty("os.name","Unknown");
  public static final OSFamily OS_FAMILY = OSFamily.guessFromName(OS_NAME);
  
  /**
   * @author Jonathan Bradley Whited (@esotericpig)
   * @see    <a href="https://en.wikipedia.org/wiki/Category:Operating_system_families" target="_blank">OS Families [Wikipedia]</a>
   */
  public static enum OSFamily {
    LINUX("linux"),MACOS("darwin mac osx"),WINDOWS("win","darwin"),UNKNOWN;
    
    public static OSFamily guessFromName(String osName) {
      osName = osName.replaceAll("\\s+","").toLowerCase(Locale.ENGLISH);
      
      OSFamily osFamily = UNKNOWN;
      
      for(OSFamily osf: values()) {
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
    
    protected String[] badWords;
    protected String[] goodWords;
    
    private OSFamily() {
      this(null);
    }
    
    private OSFamily(String goodWords) {
      this(goodWords,null);
    }
    
    private OSFamily(String goodWords,String badWords) {
      this.badWords = (badWords != null) ? badWords.split("\\s+") : null;
      this.goodWords = (goodWords != null) ? goodWords.split("\\s+") : null;
    }
    
    public String[] getBadWords() {
      return badWords;
    }
    
    public String[] getGoodWords() {
      return goodWords;
    }
  }
}
