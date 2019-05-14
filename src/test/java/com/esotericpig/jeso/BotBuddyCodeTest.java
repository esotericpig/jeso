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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
 * On a headless server, all of these tests will not run.
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyCodeTest {
  @BeforeEach
  public void setUpEach() {
  }
  
  @AfterEach
  public void tearDownEach() {
  }
  
  @Test
  public void testInterpretDryRun() throws AWTException,IOException,BotBuddyCode.ParseException,URISyntaxException {
    if(BotBuddyTest.isHeadless()) {
      return;
    }
    
    // Throws NullPointerException if a path doesn't exist in "test/resources/"
    Path bbcTestOutPath = Paths.get(getClass().getResource("/BotBuddyCodeTestOutput.txt").toURI());
    Path bbcTestPath = Paths.get(getClass().getResource("/BotBuddyCodeTest.bbc").toURI());
    
    String bbcTestOut = Files.lines(bbcTestOutPath).collect(Collectors.joining("\n")).trim();
    
    System.out.println("[" + bbcTestOutPath + "]:");
    System.out.println(bbcTestOut);
    System.out.println();
    
    try(BotBuddyCode bbc = BotBuddyCode.builder(bbcTestPath).build()) {
      System.out.println("[" + bbcTestPath + "]:");
      
      String instructions = bbc.interpretDryRun().trim();
      System.out.println(instructions);
      
      // Strings are trimmed above
      assertEquals(instructions,bbcTestOut);
    }
  }
}
