/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

import com.esotericpig.jeso.Chars;
import com.esotericpig.jeso.Duplicable;
import com.esotericpig.jeso.OSFamily;
import com.esotericpig.jeso.Sys;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <pre>
 * <b>BotBuddy</b> is a simple wrapper around {@link java.awt.Robot java.awt.Robot}.
 *
 * Example:
 *   BotBuddy buddy = BotBuddy.builder().autoDelay(33).autoWaitForIdle(false).build();
 *
 *   buddy.paste(999,493,"Fish").enter(1427,500,"Sakana");
 *   buddy.click(1853,1015).delayLong();
 *
 * Construct a class:
 *   BotBuddy.builder().build();
 *
 * There's a Safe Mode that throws {@link UserIsActiveException} if the user moves the mouse.
 * In addition, the pressed keys and pressed mouse buttons are stored internally if
 * Release Mode is on (on by default), so that you can release everything currently
 * pressed down to alleviate problems for the user when active.
 *   try {
 *     buddy.beginSafeMode()
 *          .enter(1470,131,"Mommy")
 *          .delay(2000) // Move your mouse during this time
 *          .enter(1470,131,"Daddy")
 *          .endSafeMode();
 *   }
 *   catch(UserIsActiveException ex) {
 *     // Release all keys and/or mouse buttons pressed down by our automatic operations
 *     buddy.releasePressed();
 *
 *     // If you move your mouse, "Daddy" will not be executed
 *     System.out.println("User is active! Stopping all automatic operations.");
 *   }
 *
 * If you click into a virtual machine, you can change the OS for Shortcuts:
 *   buddy.setOSFamily(OSFamily.MACOS);
 *
 * See {@link com.esotericpig.jeso.botbuddy.BotBuddyCode} for a simple scripting "language" for this class.
 * See {@link com.esotericpig.jeso.botbuddy.BotBuddyCodeApp} for a simple app that can take in a file that uses BotBuddyCode.
 * </pre>
 *
 * <p>Similar Projects:</p>
 * <ul>
 *   <li>Robot-Utils by Denys Shynkarenko (@Denysss) <a href="https://github.com/Denysss/Robot-Utils" target="_blank">[GitHub]</a></li>
 *   <li>Automaton by Renato Athaydes (@renatoathaydes) <a href="https://github.com/renatoathaydes/Automaton" target="_blank">[GitHub]</a></li>
 * </ul>
 *
 * <p>Getting mouse coordinates:</p>
 * <ul>
 *   <li>Linux: Install "xdotool" and do: {@code xdotool getmouselocation}</li>
 *   <li>Java:  {@link #getCoords()}, {@link #getXCoord()}, {@link #getYCoord()}</li>
 * </ul>
 *
 * @author Jonathan Bradley Whited
 * @see BotBuddy.Builder
 * @see java.awt.Robot
 * @see java.awt.datatransfer.Clipboard
 * @see java.awt.MouseInfo#getPointerInfo()
 * @see java.awt.PointerInfo#getLocation()
 * @see java.awt.Toolkit
 * @see com.esotericpig.jeso.botbuddy.BotBuddyCode
 * @see com.esotericpig.jeso.botbuddy.BotBuddyCodeApp
 */
public class BotBuddy implements AutoCloseable,Duplicable<BotBuddy> {
  /**
   * For double clicks, etc.
   */
  public static final int DEFAULT_FAST_DELAY = 44;

  /**
   * After doing a series of commands, for waiting for a webpage to load, etc.
   */
  public static final int DEFAULT_LONG_DELAY = 1100;

  /**
   * For auto delay, or for a manual delay between fast and long if auto delay is set differently.
   */
  public static final int DEFAULT_SHORT_DELAY = 110;
  public static final int DEFAULT_AUTO_DELAY = DEFAULT_SHORT_DELAY;

  public static Builder builder() throws HeadlessException {
    return new Builder();
  }

  public static Builder builder(Robot bot) throws HeadlessException {
    return new Builder(bot);
  }

  public static Builder builder(GraphicsDevice screen) throws AWTException,HeadlessException {
    return new Builder(screen);
  }

  public static Point getCoords() throws HeadlessException,SecurityException {
    // DO NOT store PointerInfo!
    // - If you store PointerInfo in an instance variable, #getLocation() will not be up-to-date.
    return MouseInfo.getPointerInfo().getLocation();
  }

  public static boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }

  public static int getXCoord() throws HeadlessException,SecurityException {
    return getCoords().x;
  }

  public static int getYCoord() throws HeadlessException,SecurityException {
    return getCoords().y;
  }

  protected Robot bot;
  protected Clipboard clip;
  protected int defaultButton;
  protected int fastDelay;
  protected boolean isAutoDelay;
  protected boolean isReleaseMode;
  protected boolean isSafeMode = false;
  protected int leftButton;
  protected int longDelay;
  protected int middleButton;
  protected OSFamily osFamily;
  protected LinkedList<Integer> pressedButtons = new LinkedList<>();
  protected LinkedList<Integer> pressedKeys = new LinkedList<>();
  protected int rightButton;
  protected Point safeCoords = null;
  protected int shortDelay;
  protected Deque<Stash> stashes = new LinkedList<>();
  protected Toolkit tool;

  protected BotBuddy(BotBuddy buddy) {
    // Do NOT copy over #pressedButtons and #pressedKeys, as it could cause a double release

    bot = buddy.bot;
    clip = buddy.clip;
    defaultButton = buddy.defaultButton;
    fastDelay = buddy.fastDelay;
    isAutoDelay = buddy.isAutoDelay;
    isReleaseMode = buddy.isReleaseMode;
    isSafeMode = buddy.isSafeMode;
    leftButton = buddy.leftButton;
    longDelay = buddy.longDelay;
    middleButton = buddy.middleButton;
    osFamily = buddy.osFamily;
    rightButton = buddy.rightButton;
    safeCoords = (buddy.safeCoords != null) ? (new Point(buddy.safeCoords)) : null;
    shortDelay = buddy.shortDelay;
    tool = buddy.tool;

    for(Stash stash: buddy.stashes) {
      stashes.addLast(stash.dup());
    }
  }

  protected BotBuddy(Builder builder) throws AWTException,HeadlessException {
    if(builder.bot == null) {
      builder.bot(new Robot());
    }
    if(builder.tool == null) {
      builder.tool(Toolkit.getDefaultToolkit());
    }
    if(builder.clip == null) {
      builder.clip(builder.tool.getSystemClipboard());
    }

    // Set required vars first (other vars may depend on them)
    setBot(builder.bot);
    setClip(builder.clip);
    setTool(builder.tool);

    // Set other vars (options)
    setAutoWaitForIdle(builder.isAutoWaitForIdle);
    setDefaultButton(builder.defaultButton);
    setFastDelay(builder.fastDelay);
    setLeftButton(builder.leftButton);
    setLongDelay(builder.longDelay);
    setMiddleButton(builder.middleButton);
    setOSFamily(builder.osFamily);
    setReleaseMode(builder.isReleaseMode);
    setRightButton(builder.rightButton);
    setShortDelay(builder.shortDelay);

    if(builder.isAutoDelay) {
      setAutoDelay(builder.autoDelay);
    }
  }

  public BotBuddy dup() {
    return new BotBuddy(this);
  }

  /**
   * @since 0.3.6
   */
  @Override
  public void close() {
    releasePressed();
  }

  public BotBuddy beep() {
    tool.beep();

    return checkIfSafe();
  }

  public BotBuddy beginFastMode() {
    // Do NOT check if "getAutoDelay() == fastDelay" and bail because it will mess up #endFastMode()
    // - If #endFastMode() also checks it, then it will always be true (after this call)
    // - This will also affect #doubleClick(int).
    return stash().setAutoDelay(fastDelay);
  }

  public BotBuddy beginSafeMode() {
    isSafeMode = true;
    safeCoords = getCoords();

    return this;
  }

  public BotBuddy checkIfSafe() {
    return checkIfSafe(null);
  }

  public BotBuddy checkIfSafe(Point coords) {
    if(!isSafeMode) {
      return this;
    }

    // In multi-screen environments, x and y can be negative, so test null instead of (-1,-1)
    if(safeCoords == null) {
      safeCoords = getCoords();
    }
    else {
      if(coords != null) {
        safeCoords.setLocation(coords);
      }

      if(!getCoords().equals(safeCoords)) {
        throw new UserIsActiveException();
      }
    }

    return this;
  }

  public BotBuddy clearPressed() {
    return clearPressedButtons().clearPressedKeys();
  }

  public BotBuddy clearPressedButtons() {
    pressedButtons.clear();

    return this;
  }

  public BotBuddy clearPressedKeys() {
    pressedKeys.clear();

    return this;
  }

  public BotBuddy click() {
    return click(defaultButton);
  }

  public BotBuddy click(int button) {
    bot.mousePress(button);
    bot.mouseRelease(button);

    return checkIfSafe();
  }

  public BotBuddy click(int x,int y) {
    return move(x,y).click();
  }

  public BotBuddy click(int x,int y,int button) {
    return move(x,y).click(button);
  }

  public BotBuddy clicks(int... buttons) {
    for(int button: buttons) {
      click(button);
    }

    return this;
  }

  public BotBuddy copy(String text) {
    return copy(text,null);
  }

  public BotBuddy copy(String text,ClipboardOwner owner) {
    clip.setContents(new StringSelection(text),owner);

    return delayAuto();
  }

  public BotBuddy delay(int delay) {
    bot.delay(delay);

    return checkIfSafe();
  }

  public BotBuddy delayAuto() {
    if(isAutoDelay) {
      bot.delay(bot.getAutoDelay());
    }

    return checkIfSafe();
  }

  public BotBuddy delayFast() {
    bot.delay(fastDelay);

    return checkIfSafe();
  }

  public BotBuddy delayLong() {
    bot.delay(longDelay);

    return checkIfSafe();
  }

  public BotBuddy delayShort() {
    bot.delay(shortDelay);

    return checkIfSafe();
  }

  public BotBuddy doubleClick() {
    return doubleClick(defaultButton);
  }

  public BotBuddy doubleClick(int button) {
    final boolean stash = (getAutoDelay() != fastDelay);

    if(stash) {
      beginFastMode();
    }

    click(button).click(button);

    if(stash) {
      endFastMode();
    }

    return this;
  }

  public BotBuddy doubleClick(int x,int y) {
    return move(x,y).doubleClick();
  }

  public BotBuddy doubleClick(int x,int y,int button) {
    return move(x,y).doubleClick(button);
  }

  public BotBuddy drag(int fromX,int fromY,int toX,int toY) {
    return drag(fromX,fromY,toX,toY,defaultButton);
  }

  public BotBuddy drag(int fromX,int fromY,int toX,int toY,int button) {
    return pressButton(fromX,fromY,button)
           .releaseButton(toX,toY,button);
  }

  public BotBuddy endFastMode() {
    final int fastDelay = getAutoDelay(); // See #beginFastMode()

    unstash();

    final int remainingDelay = getAutoDelay() - fastDelay;

    if(remainingDelay > 0) {
      delay(remainingDelay);
    }

    return this;
  }

  public BotBuddy endSafeMode() {
    isSafeMode = false;
    safeCoords = null;

    return this;
  }

  public BotBuddy enter() {
    bot.keyPress(KeyEvent.VK_ENTER);
    bot.keyRelease(KeyEvent.VK_ENTER);

    return checkIfSafe();
  }

  public BotBuddy enter(String text) {
    return paste(text).enter();
  }

  public BotBuddy enter(int x,int y) {
    return click(x,y).enter();
  }

  public BotBuddy enter(int x,int y,String text) {
    return paste(x,y,text).enter();
  }

  public BotBuddy leftClick() {
    return click(leftButton);
  }

  public BotBuddy leftClick(int x,int y) {
    return move(x,y).leftClick();
  }

  public BotBuddy middleClick() {
    return click(middleButton);
  }

  public BotBuddy middleClick(int x,int y) {
    return move(x,y).middleClick();
  }

  public BotBuddy move(int x,int y) {
    bot.mouseMove(x,y);

    return checkIfSafe(new Point(x,y));
  }

  public BotBuddy paste() {
    return shortcut(Shortcuts.PASTE);
  }

  public BotBuddy paste(String text) {
    return copy(text).paste();
  }

  public BotBuddy paste(int x,int y) {
    return click(x,y).paste();
  }

  public BotBuddy paste(int x,int y,String text) {
    return click(x,y).paste(text);
  }

  public BotBuddy pressButton(int button) {
    bot.mousePress(button);

    if(isReleaseMode) {
      pressedButtons.addFirst(button);
    }

    return checkIfSafe();
  }

  public BotBuddy pressButton(int x,int y,int button) {
    return move(x,y).pressButton(button);
  }

  public BotBuddy pressButtons(int... buttons) {
    for(int button: buttons) {
      pressButton(button);
    }

    return this;
  }

  public BotBuddy pressKey(int keyCode) {
    bot.keyPress(keyCode);

    if(isReleaseMode) {
      pressedKeys.addFirst(keyCode);
    }

    return checkIfSafe();
  }

  public BotBuddy pressKey(int x,int y,int keyCode) {
    return move(x,y).pressKey(keyCode);
  }

  public BotBuddy pressKeys(int... keyCodes) {
    for(int keyCode: keyCodes) {
      pressKey(keyCode);
    }

    return this;
  }

  public BufferedImage printScreen() throws SecurityException {
    Dimension screenSize = getScreenSize();

    return printScreen(screenSize.width,screenSize.height);
  }

  public BufferedImage printScreen(Rectangle screenRect) throws SecurityException {
    return bot.createScreenCapture(screenRect);
  }

  public BufferedImage printScreen(int width,int height) throws SecurityException {
    return printScreen(new Rectangle(width,height));
  }

  public BufferedImage printScreen(int x,int y,int width,int height) throws SecurityException {
    return printScreen(new Rectangle(x,y,width,height));
  }

  public BotBuddy releaseButton(int button) {
    bot.mouseRelease(button);

    if(isReleaseMode) {
      pressedButtons.removeFirstOccurrence(button);
    }

    return checkIfSafe();
  }

  public BotBuddy releaseButton(int x,int y,int button) {
    return move(x,y).releaseButton(button);
  }

  public BotBuddy releaseButtons() {
    for(ListIterator<Integer> it = pressedButtons.listIterator(); it.hasNext(); it.remove()) {
      bot.mouseRelease(it.next());
    }

    return this;
  }

  public BotBuddy releaseButtons(int... buttons) {
    for(int button: buttons) {
      releaseButton(button);
    }

    return this;
  }

  public BotBuddy releaseKey(int keyCode) {
    bot.keyRelease(keyCode);

    if(isReleaseMode) {
      pressedKeys.removeFirstOccurrence(keyCode);
    }

    return checkIfSafe();
  }

  public BotBuddy releaseKey(int x,int y,int keyCode) {
    return move(x,y).releaseKey(keyCode);
  }

  public BotBuddy releaseKeys() {
    for(ListIterator<Integer> it = pressedKeys.listIterator(); it.hasNext(); it.remove()) {
      bot.keyRelease(it.next());
    }

    return this;
  }

  public BotBuddy releaseKeys(int... keyCodes) {
    for(int keyCode: keyCodes) {
      releaseKey(keyCode);
    }

    return this;
  }

  public BotBuddy releasePressed() {
    // Release keys first as more important
    return releaseKeys().releaseButtons();
  }

  public BotBuddy rightClick() {
    return click(rightButton);
  }

  public BotBuddy rightClick(int x,int y) {
    return move(x,y).rightClick();
  }

  public BotBuddy rollButtons(int... buttons) {
    pressButtons(buttons);

    for(int i = buttons.length - 1; i >= 0; --i) {
      releaseButton(buttons[i]);
    }

    return this;
  }

  public BotBuddy rollKeys(int... keyCodes) {
    pressKeys(keyCodes);

    for(int i = keyCodes.length - 1; i >= 0; --i) {
      releaseKey(keyCodes[i]);
    }

    return this;
  }

  public BotBuddy shortcut(Shortcut shortcut) {
    return shortcut.press(this);
  }

  public BotBuddy stash() {
    stashes.push(new Stash());

    return this;
  }

  public BotBuddy type(int keyCode) {
    bot.keyPress(keyCode);
    bot.keyRelease(keyCode);

    return checkIfSafe();
  }

  /**
   * @since 0.3.5
   */
  public BotBuddy type(String text) {
    return type(text,true);
  }

  /**
   * @since 0.3.5
   */
  protected BotBuddy type(String text,boolean ensure) {
    for(int i = 0; i < text.length(); ) {
      int keyChar = text.codePointAt(i);
      int[] keyCodes = KeyCodes.getCharCodes(keyChar,!ensure);

      if(keyCodes == null) {
        if(ensure) {
          paste(Chars.toString(keyChar)); // Ensure success
        }
      }
      else {
        rollKeys(keyCodes); // Roll the keys like #paste() does: '$' => Shift + 4
      }

      i += Character.charCount(keyChar);
    }

    return this;
  }

  /**
   * @since 0.3.5
   */
  public BotBuddy type(int x,int y,int keyCode) {
    return click(x,y).type(keyCode);
  }

  /**
   * @since 0.3.5
   */
  public BotBuddy type(int x,int y,String text) {
    return click(x,y).type(text);
  }

  public BotBuddy types(int... keyCodes) {
    for(int keyCode: keyCodes) {
      type(keyCode);
    }

    return this;
  }

  /**
   * @since 0.3.5
   */
  public BotBuddy typeUnsurely(String text) {
    return type(text,false);
  }

  /**
   * @since 0.3.5
   */
  public BotBuddy typeUnsurely(int x,int y,String text) {
    return click(x,y).typeUnsurely(text);
  }

  public BotBuddy unstash() {
    Stash stash = stashes.poll();

    if(stash != null) {
      stash.clear();
      stash = null;
    }

    return this;
  }

  public BotBuddy waitForIdle() {
    bot.waitForIdle();

    return checkIfSafe();
  }

  public BotBuddy wheel(int amount) {
    bot.mouseWheel(amount);

    return checkIfSafe();
  }

  public BotBuddy setAutoDelay(int autoDelay) {
    this.isAutoDelay = autoDelay > 0;

    bot.setAutoDelay(autoDelay);

    return this;
  }

  public BotBuddy setAutoDelay(boolean isAutoDelay) {
    this.isAutoDelay = isAutoDelay && shortDelay > 0;

    bot.setAutoDelay(isAutoDelay ? shortDelay : 0);

    return this;
  }

  public BotBuddy setAutoWaitForIdle(boolean isAutoWaitForIdle) {
    bot.setAutoWaitForIdle(isAutoWaitForIdle);

    return this;
  }

  public BotBuddy setBot(Robot bot) {
    if(bot == null) {
      throw new IllegalArgumentException("Robot cannot be null");
    }

    this.bot = bot;
    this.isAutoDelay = bot.getAutoDelay() > 0;

    return this;
  }

  public BotBuddy setClip(Clipboard clip) {
    if(clip == null) {
      throw new IllegalArgumentException("Clipboard cannot be null");
    }

    this.clip = clip;

    return this;
  }

  public BotBuddy setDefaultButton(int defaultButton) {
    this.defaultButton = defaultButton;

    return this;
  }

  public BotBuddy setFastDelay(int fastDelay) {
    this.fastDelay = fastDelay;

    return this;
  }

  public BotBuddy setLeftButton(int leftButton) {
    this.leftButton = leftButton;

    return this;
  }

  public BotBuddy setLongDelay(int longDelay) {
    this.longDelay = longDelay;

    return this;
  }

  public BotBuddy setMiddleButton(int middleButton) {
    this.middleButton = middleButton;

    return this;
  }

  public BotBuddy setOSFamily(OSFamily osFamily) {
    this.osFamily = osFamily;

    return this;
  }

  public BotBuddy setReleaseMode(boolean isReleaseMode) {
    this.isReleaseMode = isReleaseMode;

    return this;
  }

  public BotBuddy setRightButton(int rightButton) {
    this.rightButton = rightButton;

    return this;
  }

  public BotBuddy setShortDelay(int shortDelay) {
    this.shortDelay = shortDelay;

    return this;
  }

  public BotBuddy setTool(Toolkit tool) {
    if(tool == null) {
      throw new IllegalArgumentException("Toolkit cannot be null");
    }

    this.tool = tool;

    return this;
  }

  public int getAutoDelay() {
    return bot.getAutoDelay();
  }

  public boolean isAutoDelay() {
    return isAutoDelay;
  }

  public boolean isAutoWaitForIdle() {
    return bot.isAutoWaitForIdle();
  }

  public Robot getBot() {
    return bot;
  }

  public Clipboard getClip() {
    return clip;
  }

  public int getDefaultButton() {
    return defaultButton;
  }

  public int getFastDelay() {
    return fastDelay;
  }

  public int getLeftButton() {
    return leftButton;
  }

  public int getLongDelay() {
    return longDelay;
  }

  public int getMiddleButton() {
    return middleButton;
  }

  public OSFamily getOSFamily() {
    return osFamily;
  }

  public Color getPixel(Point coords) {
    return getPixel(coords.x,coords.y);
  }

  public Color getPixel(int x,int y) {
    return bot.getPixelColor(x,y);
  }

  public boolean isReleaseMode() {
    return isReleaseMode;
  }

  public int getRightButton() {
    return rightButton;
  }

  public boolean isSafeMode() {
    return isSafeMode;
  }

  public int getScreenHeight() {
    return getScreenSize().height;
  }

  public Dimension getScreenSize() {
    return tool.getScreenSize();
  }

  public int getScreenWidth() {
    return getScreenSize().width;
  }

  public int getShortDelay() {
    return shortDelay;
  }

  public Toolkit getTool() {
    return tool;
  }

  /**
   * <pre>
   * <b>BotBuddy.Builder</b> constructs a {@link BotBuddy} using the Builder Design Pattern.
   *
   * The logic in this class is very minimal, unless absolutely necessary.
   * </pre>
   *
   * @author Jonathan Bradley Whited
   * @see <a href="https://en.wikipedia.org/wiki/Builder_pattern" target="_blank">Builder Pattern [Wikipedia]</a>
   */
  public static class Builder {
    protected int autoDelay = DEFAULT_AUTO_DELAY;
    protected Robot bot = null;
    protected Clipboard clip = null;
    protected int defaultButton;
    protected int fastDelay = DEFAULT_FAST_DELAY;
    protected boolean isAutoDelay = true;
    protected boolean isAutoWaitForIdle = true;
    protected boolean isReleaseMode = true;
    protected int leftButton;
    protected int longDelay = DEFAULT_LONG_DELAY;
    protected int middleButton;
    protected OSFamily osFamily = Sys.OS_FAMILY;
    protected int rightButton;
    protected int shortDelay = DEFAULT_SHORT_DELAY;
    protected Toolkit tool = null;

    protected Builder() throws HeadlessException {
      leftButton(InputEvent.BUTTON1_DOWN_MASK);

      switch(MouseInfo.getNumberOfButtons()) {
        case 1:
          middleButton(InputEvent.BUTTON1_DOWN_MASK);
          rightButton(InputEvent.BUTTON1_DOWN_MASK);
          break;
        case 2:
          middleButton(InputEvent.BUTTON2_DOWN_MASK);
          rightButton(InputEvent.BUTTON2_DOWN_MASK);
          break;
        default:
          middleButton(InputEvent.BUTTON2_DOWN_MASK);
          rightButton(InputEvent.BUTTON3_DOWN_MASK);
          break;
      }

      defaultButton(leftButton);
    }

    protected Builder(Robot bot) throws HeadlessException {
      this();

      bot(bot);
    }

    protected Builder(GraphicsDevice screen) throws AWTException,HeadlessException {
      this(new Robot(screen));
    }

    public BotBuddy build() throws AWTException,HeadlessException {
      return new BotBuddy(this);
    }

    public Builder autoDelay(int autoDelay) {
      this.autoDelay = autoDelay;
      isAutoDelay = true;

      return this;
    }

    public Builder autoDelay(boolean isAutoDelay) {
      this.isAutoDelay = isAutoDelay;

      if(isAutoDelay && autoDelay < 1) {
        autoDelay = DEFAULT_AUTO_DELAY;
      }

      return this;
    }

    public Builder autoWaitForIdle(boolean isAutoWaitForIdle) {
      this.isAutoWaitForIdle = isAutoWaitForIdle;

      return this;
    }

    public Builder bot() {
      // Because bot(null) is ambiguous

      this.bot = null;

      return this;
    }

    public Builder bot(Robot bot) {
      this.bot = bot;

      return this;
    }

    public Builder bot(GraphicsDevice screen) throws AWTException,HeadlessException {
      this.bot = new Robot(screen);

      return this;
    }

    public Builder clip(Clipboard clip) {
      this.clip = clip;

      return this;
    }

    public Builder defaultButton(int defaultButton) {
      this.defaultButton = defaultButton;

      return this;
    }

    public Builder fastDelay(int fastDelay) {
      this.fastDelay = fastDelay;

      return this;
    }

    public Builder leftButton(int leftButton) {
      this.leftButton = leftButton;

      return this;
    }

    public Builder longDelay(int longDelay) {
      this.longDelay = longDelay;

      return this;
    }

    public Builder middleButton(int middleButton) {
      this.middleButton = middleButton;

      return this;
    }

    public Builder osFamily(OSFamily osFamily) {
      this.osFamily = osFamily;

      return this;
    }

    public Builder releaseMode(boolean isReleaseMode) {
      this.isReleaseMode = isReleaseMode;

      return this;
    }

    public Builder rightButton(int rightButton) {
      this.rightButton = rightButton;

      return this;
    }

    public Builder shortDelay(int shortDelay) {
      this.shortDelay = shortDelay;

      return this;
    }

    public Builder tool(Toolkit tool) {
      this.tool = tool;

      return this;
    }
  }

  /**
   * <pre>
   * This class can really be used for any automated operations, not just
   *   keyboard shortcuts.
   * </pre>
   *
   * @author Jonathan Bradley Whited
   */
  @FunctionalInterface
  public static interface Shortcut {
    public abstract BotBuddy press(BotBuddy buddy);
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public static class Shortcuts {
    public static final Shortcut PASTE;
    public static final Shortcut PASTE_DEFAULT;
    public static final Shortcut PASTE_MACOS;

    static {
      PASTE_DEFAULT = buddy -> buddy.rollKeys(KeyEvent.VK_CONTROL,KeyEvent.VK_V);
      PASTE_MACOS = buddy -> buddy.rollKeys(KeyEvent.VK_META,KeyEvent.VK_V);
      PASTE = buddy -> {
        switch(buddy.getOSFamily()) {
          case MACOS: return PASTE_MACOS.press(buddy);
        }

        return PASTE_DEFAULT.press(buddy);
      };
    }
  }

  /**
   * @author Jonathan Bradley Whited
   */
  public class Stash implements Duplicable<Stash> {
    public int autoDelay;
    public boolean isStashed = false;

    public Stash() {
      autoDelay = getAutoDelay();

      isStashed = true;
    }

    protected Stash(Stash stash) {
      autoDelay = stash.autoDelay;
      isStashed = stash.isStashed;
    }

    public Stash dup() {
      return new Stash(this);
    }

    public void clear() {
      if(!isStashed) {
        return;
      }

      isStashed = false;

      setAutoDelay(autoDelay);
    }
  }
}
