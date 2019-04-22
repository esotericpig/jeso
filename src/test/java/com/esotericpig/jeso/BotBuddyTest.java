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
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;

import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class BotBuddyTest {
  /**
   * Max milliseconds for delays.
   */
  public static final int MAX_MS = 55000;
  
  protected Random rand;
  
  @BeforeEach
  public void setUpEach() {
    rand = new Random();
  }
  
  @AfterEach
  public void tearDownEach() {
    rand = null;
  }
  
  @Test
  public void testAccessors() {
    // TODO: test setters/getters
  }
  
  @Test
  public void testBuilders() throws AWTException {
    BotBuddy.Builder builder = null;
    
    builder = new BotBuddy.Builder();
    builder = new BotBuddy.Builder(new Robot());
    
    builder = BotBuddy.builder();
    builder = BotBuddy.builder(new Robot());
    
    int autoDelay = rand.nextInt(MAX_MS - 1) + 1;
    Robot bot = new Robot();
    int fastDelay = rand.nextInt(MAX_MS);
    boolean isAutoWaitForIdle = false;
    int longDelay = rand.nextInt(MAX_MS);
    PointerInfo pointer = MouseInfo.getPointerInfo();
    int shortDelay = rand.nextInt(MAX_MS);
    Toolkit tool = Toolkit.getDefaultToolkit();
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    builder = builder.autoDelay(autoDelay)
                     .autoDelay(true)
                     .autoWaitForIdle(isAutoWaitForIdle)
                     .bot(bot)
                     .clip(clip)
                     .fastDelay(fastDelay)
                     .longDelay(longDelay)
                     .pointer(pointer)
                     .shortDelay(shortDelay)
                     .tool(tool);
    
    assertEquals(autoDelay,builder.autoDelay);
    assertEquals(bot,builder.bot);
    assertEquals(clip,builder.clip);
    assertEquals(fastDelay,builder.fastDelay);
    assertEquals(isAutoWaitForIdle,builder.isAutoWaitForIdle);
    assertEquals(longDelay,builder.longDelay);
    assertEquals(pointer,builder.pointer);
    assertEquals(shortDelay,builder.shortDelay);
    assertEquals(tool,builder.tool);
    
    BotBuddy buddy = builder.build();
    
    assertEquals(autoDelay,buddy.getAutoDelay());
    assertEquals(bot,buddy.getBot());
    assertEquals(clip,buddy.getClip());
    assertEquals(fastDelay,buddy.getFastDelay());
    assertEquals(isAutoWaitForIdle,buddy.isAutoWaitForIdle());
    assertEquals(longDelay,buddy.getLongDelay());
    assertEquals(pointer,buddy.getPointer());
    assertEquals(shortDelay,buddy.getShortDelay());
    assertEquals(tool,buddy.getTool());
  }
  
  @Test
  public void testConstructors() throws AWTException {
    BotBuddy buddy = null;
    
    buddy = new BotBuddy();
    buddy = new BotBuddy(buddy);
    buddy = new BotBuddy(BotBuddy.builder());
    buddy = new BotBuddy(new BotBuddy.Builder());
  }
  
  @Test
  public void testMethods() {
    // TODO: test getCoords(), etc.; NOT click(), etc.
  }
}
