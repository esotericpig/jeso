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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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

import java.util.function.UnaryOperator;

/**
 * <pre>
 * <b>BotBuddy</b> is a simple wrapper around <b>{@link java.awt.Robot java.awt.Robot}</b>.
 * 
 * Example:
 *   BotBuddy buddy = BotBuddy.builder().autoDelay(33).autoWaitForIdle(false).build();
 *   
 *   buddy.paste(999,493,"Fish").enter(1427,500,"Sakana");
 *   buddy.click(1853,1015).delayLong();
 * 
 * You can construct a class in many ways:
 *   BotBuddy.builder().build();           // Recommended way
 *   new BotBuddy();                       // Convenience method for less code
 *   new BotBuddy.Builder().build();       // Not recommended, but fine
 *   new BotBuddy(new BotBuddy.Builder()); // Not recommended, but fine
 * 
 * There's a Safe Mode that throws BotBuddy.SafeModeException if the user moves the mouse:
 *   try {
 *     buddy.beginSafeMode()
 *          .enter(1470,131,"Mommy")
 *          .delay(2000) // Move your mouse during this time
 *          .enter(1470,131,"Daddy")
 *          .endSafeMode();
 *   }
 *   catch(BotBuddy.SafeModeException ex) {
 *     // If you move your mouse, "Daddy" will not be executed
 *     System.out.println("User is active! Stopping all automatic operations.");
 *   }
 * 
 * If you click into a virtual machine, you can change the OS for Shortcuts:
 *   buddy.setOSFamily(OSFamily.MACOS);
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
 *   <li>Linux: Install `xdotool` and run `xdotool getmouselocation`</li>
 *   <li>Java:  {@link #getCoords()}, {@link #getXCoord()}, {@link #getYCoord()}</li>
 * </ul>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 * 
 * @see    BotBuddy.Builder
 * @see    Robot
 * @see    Clipboard
 * @see    MouseInfo#getPointerInfo()
 * @see    java.awt.PointerInfo#getLocation()
 * @see    Toolkit
 */
public class BotBuddy implements Cloneable {
  /**
   * For double clicks, etc.
   */
  public static final int DEFAULT_FAST_DELAY = 44;
  
  /**
   * After doing a series of commands, for waiting for a webpage to load, etc.
   */
  public static final int DEFAULT_LONG_DELAY = 1100;
  
  public static final int DEFAULT_MOUSE_BUTTON = InputEvent.BUTTON1_DOWN_MASK;
  
  /**
   * For auto delay, or for a manual delay between fast and long if auto delay is set differently.
   */
  public static final int DEFAULT_SHORT_DELAY = 110;
  public static final int DEFAULT_AUTO_DELAY = DEFAULT_SHORT_DELAY;
  
  public static Builder builder() throws AWTException {
    return new Builder();
  }
  
  public static Builder builder(Robot bot) {
    return new Builder(bot);
  }
  
  public static Builder builder(GraphicsDevice screen) throws AWTException {
    return new Builder(screen);
  }
  
  public static Point getCoords() {
    // DO NOT store PointerInfo!
    // - If you store PointerInfo in an instance variable, #getLocation() will not be up-to-date.
    return MouseInfo.getPointerInfo().getLocation();
  }
  
  public static boolean isHeadless() {
    return GraphicsEnvironment.isHeadless();
  }
  
  public static int getXCoord() {
    return getCoords().x;
  }
  
  public static int getYCoord() {
    return getCoords().y;
  }
  
  protected Robot bot;
  protected Clipboard clip;
  protected int fastDelay;
  protected boolean isAutoDelay;
  protected boolean isSafeMode = false;
  protected int longDelay;
  protected OSFamily osFamily;
  protected Point safeCoords = null;
  protected int shortDelay;
  protected Toolkit tool;
  
  public BotBuddy() throws AWTException {
    this(new Builder());
  }
  
  public BotBuddy(BotBuddy buddy) {
    bot = buddy.bot;
    clip = buddy.clip;
    fastDelay = buddy.fastDelay;
    isAutoDelay = buddy.isAutoDelay;
    isSafeMode = buddy.isSafeMode;
    longDelay = buddy.longDelay;
    osFamily = buddy.osFamily;
    safeCoords = (buddy.safeCoords != null) ? (new Point(buddy.safeCoords)) : null;
    shortDelay = buddy.shortDelay;
    tool = buddy.tool;
  }
  
  public BotBuddy(Builder builder) {
    // Set required vars first (other vars may depend on them)
    setBot(builder.bot);
    setClip(builder.clip);
    setTool(builder.tool);
    
    // Set other vars (options)
    setAutoWaitForIdle(builder.isAutoWaitForIdle);
    setFastDelay(builder.fastDelay);
    setLongDelay(builder.longDelay);
    setOSFamily(builder.osFamily);
    setShortDelay(builder.shortDelay);
    
    if(builder.isAutoDelay) {
      setAutoDelay(builder.autoDelay);
    }
  }
  
  public BotBuddy beep() {
    tool.beep();
    
    return this;
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
        throw new SafeModeException();
      }
    }
    
    return this;
  }
  
  public BotBuddy click() {
    return click(DEFAULT_MOUSE_BUTTON);
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
  
  @Override
  public BotBuddy clone() {
    return new BotBuddy(this);
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
    return doubleClick(DEFAULT_MOUSE_BUTTON);
  }
  
  public BotBuddy doubleClick(int button) {
    return shortcutFast((buddy) -> buddy.click(button).delayFast().click(button));
  }
  
  public BotBuddy doubleClick(int x,int y) {
    return move(x,y).doubleClick();
  }
  
  public BotBuddy doubleClick(int x,int y,int button) {
    return move(x,y).doubleClick(button);
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
  
  public BotBuddy key(int keyCode) {
    bot.keyPress(keyCode);
    bot.keyRelease(keyCode);
    
    return checkIfSafe();
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
  
  public BotBuddy pressKey(int keyCode) {
    bot.keyPress(keyCode);
    
    return checkIfSafe();
  }
  
  public BotBuddy pressMouse(int button) {
    bot.mousePress(button);
    
    return checkIfSafe();
  }
  
  public BufferedImage printScreen(Rectangle screenRect) {
    return bot.createScreenCapture(screenRect);
  }
  
  public BufferedImage printScreen(int width,int height) {
    return printScreen(new Rectangle(width,height));
  }
  
  public BufferedImage printScreen(int x,int y,int width,int height) {
    return printScreen(new Rectangle(x,y,width,height));
  }
  
  public BotBuddy releaseKey(int keyCode) {
    bot.keyRelease(keyCode);
    
    return checkIfSafe();
  }
  
  public BotBuddy releaseMouse(int button) {
    bot.mouseRelease(button);
    
    return checkIfSafe();
  }
  
  public BotBuddy shortcut(Shortcut shortcut) {
    return shortcut.press(this).checkIfSafe();
  }
  
  public BotBuddy shortcutFast(Shortcut shortcut) {
    int autoDelay = getAutoDelay();
    boolean isAutoDelay = this.isAutoDelay;
    
    setAutoDelay(false);
    shortcut.press(this);
    
    if(isAutoDelay) {
      setAutoDelay(autoDelay);
      bot.delay(autoDelay);
    }
    
    return checkIfSafe();
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
  
  public BotBuddy setFastDelay(int fastDelay) {
    this.fastDelay = fastDelay;
    
    return this;
  }
  
  public BotBuddy setLongDelay(int longDelay) {
    this.longDelay = longDelay;
    
    return this;
  }
  
  public BotBuddy setOSFamily(OSFamily osFamily) {
    this.osFamily = osFamily;
    
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
  
  public int getFastDelay() {
    return fastDelay;
  }
  
  public int getLongDelay() {
    return longDelay;
  }
  
  public Color getPixel(Point coords) {
    return getPixel(coords.x,coords.y);
  }
  
  public Color getPixel(int x,int y) {
    return bot.getPixelColor(x,y);
  }
  
  public OSFamily getOSFamily() {
    return osFamily;
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
   * @author Jonathan Bradley Whited (@esotericpig)
   * 
   * @see    <a href="https://en.wikipedia.org/wiki/Builder_pattern" target="_blank">Builder Pattern [Wikipedia]</a>
   */
  public static class Builder {
    protected int autoDelay = DEFAULT_AUTO_DELAY;
    protected Robot bot;
    protected Clipboard clip;
    protected int fastDelay = DEFAULT_FAST_DELAY;
    protected boolean isAutoDelay = true;
    protected boolean isAutoWaitForIdle = true;
    protected int longDelay = DEFAULT_LONG_DELAY;
    protected OSFamily osFamily = Sys.OS_FAMILY;
    protected int shortDelay = DEFAULT_SHORT_DELAY;
    protected Toolkit tool;
    
    public Builder() throws AWTException {
      this(new Robot());
    }
    
    public Builder(Robot bot) {
      this.bot = bot;
      tool = Toolkit.getDefaultToolkit();
      clip = tool.getSystemClipboard();
    }
    
    public Builder(GraphicsDevice screen) throws AWTException {
      this(new Robot(screen));
    }
    
    public BotBuddy build() {
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
    
    public Builder bot(Robot bot) {
      this.bot = bot;
      
      return this;
    }
    
    public Builder bot(GraphicsDevice screen) throws AWTException {
      this.bot = new Robot(screen);
      
      return this;
    }
    
    public Builder clip(Clipboard clip) {
      this.clip = clip;
      
      return this;
    }
    
    public Builder fastDelay(int fastDelay) {
      this.fastDelay = fastDelay;
      
      return this;
    }
    
    public Builder longDelay(int longDelay) {
      this.longDelay = longDelay;
      
      return this;
    }
    
    public Builder osFamily(OSFamily osFamily) {
      this.osFamily = osFamily;
      
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
  
  public static class SafeModeException extends RuntimeException {
  }
  
  /**
   * <pre>
   * This class can really be used for any automated operations, not just
   *   keyboard shortcuts.
   * 
   * Extends UnaryOperator and changes #apply() to #press() so that if the
   *   internals change in the future, it won't affect users that much.
   * </pre>
   * 
   * @author Jonathan Bradley Whited (@esotericpig)
   */
  @FunctionalInterface
  public static interface Shortcut extends UnaryOperator<BotBuddy> {
    public abstract BotBuddy press(BotBuddy buddy);
    
    public default BotBuddy apply(BotBuddy buddy) {
      return press(buddy);
    }
  }
  
  public static class Shortcuts {
    public static final Shortcut PASTE;
    public static final Shortcut PASTE_DEFAULT;
    public static final Shortcut PASTE_MACOS;
    
    static {
      PASTE_DEFAULT = (buddy) -> buddy.pressKey(KeyEvent.VK_CONTROL)
                                      .pressKey(KeyEvent.VK_V)
                                      .releaseKey(KeyEvent.VK_V)
                                      .releaseKey(KeyEvent.VK_CONTROL);
      PASTE_MACOS = (buddy) -> buddy.pressKey(KeyEvent.VK_META)
                                    .pressKey(KeyEvent.VK_V)
                                    .releaseKey(KeyEvent.VK_V)
                                    .releaseKey(KeyEvent.VK_META);
      PASTE = (buddy) -> {
        switch(buddy.getOSFamily()) {
          case MACOS: return PASTE_MACOS.press(buddy);
        }
        
        return PASTE_DEFAULT.press(buddy);
      };
    }
  }
}
