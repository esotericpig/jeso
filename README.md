# jeso

Java utils to make Java less verbose and more fun.

Inspired largely by Ruby.

Name = Java + gesso + esoteric.

**Note:** currently in development on v0.0.1.

## Contents

- [Using](#using)
- [Code](#code)
    - [BotBuddy](#botbuddy)
- [License](#license)

## [Using](#contents)

Currently, an official release hasn't been made yet, but you can go ahead and use it in your project by building a pre-release yourself:

- `$ ./gradlew(.bat) clean buildRelease`

Then use the following files in your project:

- build/libs/jeso-*.jar
- build/libs/jeso-*-sources.jar
- build/distributions/jeso-*-javadoc.zip

## [Code](#contents)

| Class                 | Summary |
| --------------------- | ------- |
| [BotBuddy](#botbuddy) | A simple wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html) |

### [BotBuddy](#code)

[BotBuddy](src/main/java/com/esotericpig/jeso/BotBuddy.java) is a simple wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html).

For one example, it can be used for moving the mouse and pasting in text.

- It can automate tedious tasks that a website/program has not implemented yet, such as inputting a CSV file into a website, row by row.
- It can be used for automating tedious tasks in games, such as moving your character to a place to fish and then moving your character home to dump the fish.
- It can be used for testing/QAing your desktop applications.

Example usage:

```Java
import com.esotericpig.jeso.BotBuddy;

// ...class...main...

BotBuddy buddy = BotBuddy.builder().build();

buddy.paste(999,493,"Fish")
     .enter(1427,500,"Sakana");
buddy.click(1853,1015)
     .delayLong();
```

To construct a class, the [Builder Design Pattern](https://en.wikipedia.org/wiki/Builder_pattern) is used:

```Java
BotBuddy buddy = BotBuddy.builder()
                         .autoDelay(false)
                         .fastDelay(33)
                         .build();
```

All of the main methods can also be chained together:

```Java
buddy.click([int button])
     .click([int x,int y,int button])
     .copy(String text,[ClipboardOwner owner])
     .delay(int delay)
     .delayAuto()
     .delayFast()
     .delayLong()
     .delayShort()
     .doubleClick([int button])
     .doubleClick([int x,int y,int button])
     .enter([String text])
     .enter([int x,int y,String text])
     .key(int keyCode)
     .move(int x,int y)
     .paste([String text])
     .paste([int x,int y,String text])
     .pressKey(int keyCode)
     .pressMouse(int button)
     .releaseKey(int keyCode)
     .releaseMouse(int button)
     .shortcut(BotBuddy.Shortcut shortcut)
     .waitForIdle()
     .set*(*);
```

A Safe Mode has been added for convenience. If the user ever moves their mouse, then **BotBuddy.SafeModeException** will be thrown. After each operation, it just checks the mouse coordinates, while updating its internal coordinates accordingly to the operations. Example:

```Java
BotBuddy buddy = BotBuddy.builder().build();

System.out.println("Get ready...");
buddy.delay(2000);

try {
  buddy.beginSafeMode()
       .enter(1470,131,"Mommy")
       .delay(2000) // Move your mouse during this time
       .enter(1470,131,"Daddy")
       .endSafeMode();
}
catch(BotBuddy.SafeModeException ex) {
  // If you move your mouse, "Daddy" will not be executed
  System.out.println("User is active! Stopping all automatic operations.");
}
```

If your program clicks into a virtual machine, you can change the OS to change the keyboard shortcut keys (e.g., paste):

```Java
buddy.setOSFamily(OSFamily.MACOS);
```

When writing your own scripts, you can use these methods for getting the mouse coordinates:

- `BotBuddy.getCoords();`
- `BotBuddy.getXCoord();`
- `BotBuddy.getYCoord();`

Alternatively, you can do one of the following for getting the mouse coordinates:

- Linux: Install `xdotool` and run `xdotool getmouselocation`

Similar projects to [BotBuddy](src/main/java/com/esotericpig/jeso/BotBuddy.java):

- Robot-Utils by Denys Shynkarenko (@Denysss) [[GitHub](https://github.com/Denysss/Robot-Utils)]
- Automaton by Renato Athaydes (@renatoathaydes) [[GitHub](https://github.com/renatoathaydes/Automaton)]

## [License](#contents)
[GNU LGPL v3+](LICENSE)

> jeso ([https://github.com/esotericpig/jeso](https://github.com/esotericpig/jeso))  
> Copyright (c) 2019 Jonathan Bradley Whited (@esotericpig)  
> 
> jeso is free software: you can redistribute it and/or modify  
> it under the terms of the GNU Lesser General Public License as published by  
> the Free Software Foundation, either version 3 of the License, or  
> (at your option) any later version.  
> 
> jeso is distributed in the hope that it will be useful,  
> but WITHOUT ANY WARRANTY; without even the implied warranty of  
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the  
> GNU Lesser General Public License for more details.  
> 
> You should have received a copy of the GNU Lesser General Public License  
> along with jeso. If not, see <http://www.gnu.org/licenses/>.  
