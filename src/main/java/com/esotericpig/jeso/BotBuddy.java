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
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

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
 * </pre>
 * 
 * <p>Similar Projects:</p>
 * <ul>
 *   <li><a href="https://github.com/Denysss/Robot-Utils" target="_blank">Robot-Utils by Denys Shynkarenko (@Denysss) [GitHub]</a></li>
 *   <li><a href="https://github.com/renatoathaydes/Automaton" target="_blank">Automaton by Renato Athaydes (@renatoathaydes) [GitHub]</a></li>
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
 * @see    java.awt.Robot
 * @see    java.awt.datatransfer.Clipboard
 * @see    java.awt.PointerInfo
 * @see    java.awt.Toolkit
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
  
  protected Robot bot;
  protected Clipboard clip;
  protected int fastDelay;
  protected boolean isAutoDelay;
  protected int longDelay;
  protected PointerInfo pointer;
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
    longDelay = buddy.longDelay;
    pointer = buddy.pointer;
    shortDelay = buddy.shortDelay;
    tool = buddy.tool;
  }
  
  public BotBuddy(Builder builder) {
    // Set required vars first (other vars may depend on them)
    setBot(builder.bot);
    setClip(builder.clip);
    setPointer(builder.pointer);
    setTool(builder.tool);
    
    // Set other vars (options)
    setAutoWaitForIdle(builder.isAutoWaitForIdle);
    setFastDelay(builder.fastDelay);
    setLongDelay(builder.longDelay);
    setShortDelay(builder.shortDelay);
    
    if(builder.isAutoDelay) {
      setAutoDelay(builder.autoDelay);
    }
  }
  
  public BotBuddy click() {
    bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    
    return this;
  }
  
  public BotBuddy click(int x,int y) {
    return move(x,y).click();
  }
  
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
    
    return this;
  }
  
  public BotBuddy delayAuto() {
    if(isAutoDelay) {
      bot.delay(bot.getAutoDelay());
    }
    
    return this;
  }
  
  public BotBuddy delayFast() {
    bot.delay(fastDelay);
    
    return this;
  }
  
  public BotBuddy delayLong() {
    bot.delay(longDelay);
    
    return this;
  }
  
  public BotBuddy delayShort() {
    bot.delay(shortDelay);
    
    return this;
  }
  
  public BotBuddy doubleClick() {
    int autoDelay = getAutoDelay();
    boolean isAutoDelay = isAutoDelay();
    
    setAutoDelay(false);
    click().delayFast().click();
    
    if(isAutoDelay) {
      bot.delay(autoDelay);
      setAutoDelay(autoDelay);
    }
    
    return this;
  }
  
  public BotBuddy doubleClick(int x,int y) {
    return move(x,y).doubleClick();
  }
  
  public BotBuddy enter() {
    bot.keyPress(KeyEvent.VK_ENTER);
    bot.keyRelease(KeyEvent.VK_ENTER);
    
    return this;
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
  
  public BotBuddy move(int x,int y) {
    bot.mouseMove(x,y);
    
    return this;
  }
  
  public BotBuddy paste() {
    bot.keyPress(KeyEvent.VK_CONTROL);
    bot.keyPress(KeyEvent.VK_V);
    bot.keyRelease(KeyEvent.VK_V);
    bot.keyRelease(KeyEvent.VK_CONTROL);
    
    return this;
  }
  
  public BotBuddy paste(String text) {
    return copy(text).paste();
  }
  
  public BotBuddy paste(int x,int y,String text) {
    return click(x,y).paste(text);
  }
  
  public BotBuddy waitForIdle() {
    bot.waitForIdle();
    
    return this;
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
  
  public BotBuddy setPointer(PointerInfo pointer) {
    if(pointer == null) {
      throw new IllegalArgumentException("PointerInfo cannot be null");
    }
    
    this.pointer = pointer;
    
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
  
  public Point getCoords() {
    return pointer.getLocation();
  }
  
  public int getFastDelay() {
    return fastDelay;
  }
  
  public int getLongDelay() {
    return longDelay;
  }
  
  public PointerInfo getPointer() {
    return pointer;
  }
  
  public int getShortDelay() {
    return shortDelay;
  }
  
  public Toolkit getTool() {
    return tool;
  }
  
  public int getXCoord() {
    return getCoords().x;
  }
  
  public int getYCoord() {
    return getCoords().y;
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
    protected PointerInfo pointer;
    protected int shortDelay = DEFAULT_SHORT_DELAY;
    protected Toolkit tool;
    
    public Builder() throws AWTException {
      this(new Robot());
    }
    
    public Builder(Robot bot) {
      this.bot = bot;
      pointer = MouseInfo.getPointerInfo();
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
    
    public Builder pointer(PointerInfo pointer) {
      this.pointer = pointer;
      
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
}
