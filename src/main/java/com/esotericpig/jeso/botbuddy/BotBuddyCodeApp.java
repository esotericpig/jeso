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

package com.esotericpig.jeso.botbuddy;

import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyCodeApp {
  public static void main(String[] args) {
    test0();
  }
  
  public static void test0() {
    List<String> l = new LinkedList<>();
    l.add("right_click 658 317");
    //l.add("printscreen");
    
    BotBuddy bb = null;
    
    //try(BotBuddyCode bbc = BotBuddyCode.builder(Paths.get("stock/bb.rb")).build()) {
    try(BotBuddyCode bbc = BotBuddyCode.builder(l).build()) {
      bb = bbc.getBuddy();
      
      //System.out.print(bbc.interpretDryRun());
      bbc.interpret();
    }
    catch(Exception ex) {
      System.out.println(ex);
      ex.printStackTrace();
    }
    finally {
      if(bb != null) {
        bb.releasePressed();
      }
    }
  }
  
  public static void test1() {
  }
}
