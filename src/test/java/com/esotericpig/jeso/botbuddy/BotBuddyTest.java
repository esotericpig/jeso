/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.esotericpig.jeso.OSFamily;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * On a headless server, all of these tests will not run.
 * </pre>
 *
 * @author Jonathan Bradley Whited
 */
public class BotBuddyTest {
  public static final int MAX_BUTTONS = 11;

  /**
   * Max milliseconds for delays.
   */
  public static final int MAX_MS = 55000;

  public static boolean isHeadless() {
    boolean isHeadless = BotBuddy.isHeadless();

    if(isHeadless) {
      // TODO: add test name; make more meaningful
      System.out.println("This environment is headless. Ignoring Robot test.");
    }

    return isHeadless;
  }

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
  public void testAccessors() throws AWTException {
    if(isHeadless()) {
      return;
    }

    BotBuddy buddy = BotBuddy.builder().build();

    int autoDelay = rand.nextInt(MAX_MS);
    Robot bot = new Robot();
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    int defaultButton = rand.nextInt(MAX_BUTTONS);
    int fastDelay = rand.nextInt(MAX_MS);
    boolean isAutoDelay = rand.nextBoolean();
    boolean isAutoWaitForIdle = rand.nextBoolean();
    boolean isReleaseMode = rand.nextBoolean();
    int leftButton = rand.nextInt(MAX_BUTTONS);
    int longDelay = rand.nextInt(MAX_MS);
    int middleButton = rand.nextInt(MAX_BUTTONS);
    OSFamily osFamily = OSFamily.getRandValue(rand);
    int rightButton = rand.nextInt(MAX_BUTTONS);
    int shortDelay = rand.nextInt(MAX_MS - 1) + 1; // Must be > 0 for isAutoDelay
    Toolkit tool = Toolkit.getDefaultToolkit();

    buddy.clearPressed();
    buddy.clearPressedButtons();
    buddy.clearPressedKeys();

    assertEquals(autoDelay,buddy.setAutoDelay(autoDelay).getAutoDelay());
    assertEquals(bot,buddy.setBot(bot).getBot());
    assertEquals(clip,buddy.setClip(clip).getClip());
    assertEquals(defaultButton,buddy.setDefaultButton(defaultButton).getDefaultButton());
    assertEquals(fastDelay,buddy.setFastDelay(fastDelay).getFastDelay());
    assertEquals(isAutoDelay,buddy.setAutoDelay(isAutoDelay).isAutoDelay());
    assertEquals(isAutoWaitForIdle,buddy.setAutoWaitForIdle(isAutoWaitForIdle).isAutoWaitForIdle());
    assertEquals(isReleaseMode,buddy.setReleaseMode(isReleaseMode).isReleaseMode());
    assertEquals(leftButton,buddy.setLeftButton(leftButton).getLeftButton());
    assertEquals(longDelay,buddy.setLongDelay(longDelay).getLongDelay());
    assertEquals(middleButton,buddy.setMiddleButton(middleButton).getMiddleButton());
    assertEquals(osFamily,buddy.setOSFamily(osFamily).getOSFamily());
    assertEquals(rightButton,buddy.setRightButton(rightButton).getRightButton());
    assertEquals(shortDelay,buddy.setShortDelay(shortDelay).getShortDelay());
    assertEquals(tool,buddy.setTool(tool).getTool());
  }

  @Test
  public void testBuilder() throws AWTException {
    if(isHeadless()) {
      return;
    }

    BotBuddy.Builder builder = null;

    builder = BotBuddy.builder();
    builder = BotBuddy.builder(new Robot());

    int autoDelay = rand.nextInt(MAX_MS - 1) + 1; // Must be > 0 for isAutoDelay
    Robot bot = new Robot();
    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    int defaultButton = rand.nextInt(MAX_BUTTONS);
    int fastDelay = rand.nextInt(MAX_MS);
    boolean isAutoDelay = true; // Always true because autoDelay > 0
    boolean isAutoWaitForIdle = rand.nextBoolean();
    boolean isReleaseMode = rand.nextBoolean();
    int leftButton = rand.nextInt(MAX_BUTTONS);
    int longDelay = rand.nextInt(MAX_MS);
    int middleButton = rand.nextInt(MAX_BUTTONS);
    OSFamily osFamily = OSFamily.getRandValue(rand);
    int rightButton = rand.nextInt(MAX_BUTTONS);
    int shortDelay = rand.nextInt(MAX_MS);
    Toolkit tool = Toolkit.getDefaultToolkit();

    builder = builder.autoDelay(autoDelay)
                     .autoDelay(isAutoDelay)
                     .autoWaitForIdle(isAutoWaitForIdle)
                     .bot(bot)
                     .clip(clip)
                     .defaultButton(defaultButton)
                     .fastDelay(fastDelay)
                     .leftButton(leftButton)
                     .longDelay(longDelay)
                     .middleButton(middleButton)
                     .osFamily(osFamily)
                     .releaseMode(isReleaseMode)
                     .rightButton(rightButton)
                     .shortDelay(shortDelay)
                     .tool(tool);

    assertEquals(autoDelay,builder.autoDelay);
    assertEquals(bot,builder.bot);
    assertEquals(clip,builder.clip);
    assertEquals(defaultButton,builder.defaultButton);
    assertEquals(fastDelay,builder.fastDelay);
    assertEquals(isAutoDelay,builder.isAutoDelay);
    assertEquals(isAutoWaitForIdle,builder.isAutoWaitForIdle);
    assertEquals(isReleaseMode,builder.isReleaseMode);
    assertEquals(leftButton,builder.leftButton);
    assertEquals(longDelay,builder.longDelay);
    assertEquals(middleButton,builder.middleButton);
    assertEquals(osFamily,builder.osFamily);
    assertEquals(rightButton,builder.rightButton);
    assertEquals(shortDelay,builder.shortDelay);
    assertEquals(tool,builder.tool);

    BotBuddy buddy = builder.build();

    assertEquals(autoDelay,buddy.getAutoDelay());
    assertEquals(bot,buddy.getBot());
    assertEquals(clip,buddy.getClip());
    assertEquals(defaultButton,buddy.getDefaultButton());
    assertEquals(fastDelay,buddy.getFastDelay());
    assertEquals(isAutoDelay,buddy.isAutoDelay());
    assertEquals(isAutoWaitForIdle,buddy.isAutoWaitForIdle());
    assertEquals(isReleaseMode,buddy.isReleaseMode());
    assertEquals(leftButton,buddy.getLeftButton());
    assertEquals(longDelay,buddy.getLongDelay());
    assertEquals(middleButton,buddy.getMiddleButton());
    assertEquals(osFamily,buddy.getOSFamily());
    assertEquals(rightButton,buddy.getRightButton());
    assertEquals(shortDelay,buddy.getShortDelay());
    assertEquals(tool,buddy.getTool());
  }

  @Test
  public void testMainMethods() throws AWTException {
    if(isHeadless()) {
      return;
    }

    BotBuddy buddy = null;

    buddy = BotBuddy.builder().build();
    buddy = buddy.dup();

    buddy.beginFastMode().endFastMode();
    buddy.beginSafeMode().endSafeMode();
    buddy.stash().stash().stash()
         .unstash().unstash().unstash().unstash(); // 4th unstash() should not throw an error

    // Don't do assertEquals(), as the mouse might have moved in that time
    System.out.println("Coords:  " + BotBuddy.getCoords());
    System.out.println("X coord: " + BotBuddy.getXCoord());
    System.out.println("Y coord: " + BotBuddy.getYCoord());
  }
}
