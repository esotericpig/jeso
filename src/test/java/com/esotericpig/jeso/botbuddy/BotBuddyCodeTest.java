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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.esotericpig.jeso.code.ParseCodeException;

import java.awt.AWTException;

import java.io.IOException;

import java.net.URISyntaxException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * On a headless server, some tests will not run.
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyCodeTest {
  public static String cleanOutput(String output) {
    output = output.trim();
    // Remove LineOfCode coords "(11,11)" for cleaner diffs if update file in future
    output = output.replaceAll("\\(\\d+\\:\\d+\\)\\:","");
    
    return output;
  }
  
  @BeforeEach
  public void setUpEach() {
  }
  
  @AfterEach
  public void tearDownEach() {
  }
  
  @Test
  public void testExecutors() {
    BotBuddyCode.Executors executors = BotBuddyCode.DefaultExecutors.defaultExecutors;
    
    // If this doesn't match, then an entry was accidentally overridden in #addBase().
    //   For example, "put("delayauto",...);" was called twice with the same ID, instead of using a new ID.
    assertEquals(BotBuddyCode.Executors.BASE_COUNT,executors.getSize());
    
    for(String id: executors.getEntries().keySet()) {
      // Possible if "delay_Auto" or something was typed in instead of "delayauto"
      assertEquals(BotBuddyCode.Instruction.toID(id),id);
    }
  }
  
  @Test
  public void testInterpretDryRun() throws AWTException,IOException,ParseCodeException,URISyntaxException {
    if(BotBuddyTest.isHeadless()) {
      return;
    }
    
    // Throws NullPointerException if a path doesn't exist in "test/resources/"
    Path bbcTestOutPath = Paths.get(getClass().getResource("/BotBuddyCodeTestOutput.txt").toURI());
    Path bbcTestPath = Paths.get(getClass().getResource("/BotBuddyCodeTest.bbc").toURI());
    
    String bbcTestOut = cleanOutput(Files.lines(bbcTestOutPath).collect(Collectors.joining("\n")));
    
    System.out.println("[" + bbcTestOutPath + "]:");
    System.out.println(bbcTestOut);
    System.out.println();
    
    try(BotBuddyCode bbc = BotBuddyCode.builder(bbcTestPath).build()) {
      System.out.println("[" + bbcTestPath + "]:");
      
      String bbcOut = cleanOutput(bbc.interpretDryRun());
      System.out.println(bbcOut);
      System.out.println();
      
      assertEquals(bbcTestOut,bbcOut);
    }
  }
}
