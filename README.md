# jeso

Java utils to make Java less verbose and more fun.

Inspired largely by Ruby.

Name = Java + gesso + esoteric.

**Note:** currently in development on v0.0.1.

## Contents

- [Requirements](#requirements)
- [Using](#using)
- [Code](#code)
    - [BotBuddy](#botbuddy)
    - [BotBuddyCode](#botbuddycode)
- [License](#license)

## [Requirements](#contents)

- Java 8 or later

## [Using](#contents)

Currently, an official release hasn't been made yet, but you can go ahead and use it in your project by building a pre-release yourself:

- `$ ./gradlew(.bat) clean buildRelease`

Then use the following files in your project:

- build/libs/jeso-*.jar
- build/libs/jeso-*-sources.jar
- build/distributions/jeso-*-javadoc.zip

Alternatively you can build everything into one jar:

- `$ ./gradlew(.bat) clean buildFatRelease`
- build/libs/jeso-*-all.jar

## [Code](#contents)

| Class | Summary |
| ----- | ------- |
| [BotBuddy](#botbuddy) | A simple wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html) |
| [BotBuddyCode](#botbuddycode) | A very simple scripting "language" interpreter for [BotBuddy](#botbuddy) |

### [BotBuddy](#code)

[BotBuddy](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddy.java) is a simple wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html).

**Warning** for **Linux** users:  
> On Wayland, Java's Robot will not work. You will need to either use X11 or XWayland, until either OpenJDK or Wayland is fixed.

For one example, it can be used for moving the mouse and pasting in text.

- It can automate tedious tasks that a website/program has not implemented yet, such as inputting a CSV file into a website, row by row.
- It can be used for automating tedious tasks in games, such as moving your character to a place to fish and then moving your character home to dump the fish.
- It can be used for testing/QAing your desktop applications.

Example usage:

```Java
import com.esotericpig.jeso.botbuddy.BotBuddy;

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

Most methods can also be chained together:

```Java
buddy.beep()
     .click([int button])
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
     .shortcutFast(BotBuddy.Shortcut shortcut)
     .waitForIdle()
     .wheel(int amount)
     .set*(*);
```

Unchainable methods:

```Java
buddy.printScreen(Rectangle screenRect);
buddy.printScreen(int width,int height);
buddy.printScreen(int x,int y,int width,int height);
buddy.getPixel(Point coords);
buddy.getPixel(int x,int y);
buddy.getScreenHeight();
buddy.getScreenSize();
buddy.getScreenWidth();
buddy.get*(*);
```

A Safe Mode has been added for convenience. If the user ever moves their mouse, then **UserIsActiveException** will be thrown. After each operation, it just checks the mouse coordinates, while updating its internal coordinates accordingly to the operations. Example:

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
catch(UserIsActiveException ex) {
  // If you move your mouse, "Daddy" will not be executed
  System.out.println("User is active! Stopping all automatic operations.");
}
```

If your program clicks into a virtual machine, you can change the OS to change the keyboard shortcut keys (e.g., paste):

```Java
buddy.setOSFamily(OSFamily.MACOS);
```

When writing your own scripts, you can use these helper methods:

- `BotBuddy.getCoords();`
- `BotBuddy.getXCoord();`
- `BotBuddy.getYCoord();`

Alternatively, you can do one of the following for getting the mouse coordinates:

- Linux: Install `xdotool` and run `xdotool getmouselocation`.

Similar projects:

- Robot-Utils by Denys Shynkarenko (@Denysss) [[GitHub](https://github.com/Denysss/Robot-Utils)]
- Automaton by Renato Athaydes (@renatoathaydes) [[GitHub](https://github.com/renatoathaydes/Automaton)]

### [BotBuddyCode](#code)

[BotBuddyCode](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCode.java) is a very simple scripting "language" interpreter for [BotBuddy](#botbuddy). It is **not** Turing complete.

See [BotBuddyCodeTest.bbc](src/test/resources/BotBuddyCodeTest.bbc) for a quick example of functionality. If you were to interpret this file dryly, then it would produce this output: [BotBuddyCodeTestOutput.txt](src/test/resources/BotBuddyCodeTestOutput.txt).

The idea was to make a very simple parser, without including the overhead of Groovy/JRuby into *jeso*. In a future, separate project, I may add Groovy/JRuby support.

It can handle Ruby-like string literals and [heredoc](https://en.wikipedia.org/wiki/Here_document), and simple methods (no params).

It can accept the following input:

- [java.io.BufferedReader](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html)
- [java.nio.file.Path](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html) [use [java.nio.file.Paths](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html).get(...)]
- [java.util.List](https://docs.oracle.com/javase/8/docs/api/java/util/List.html)&lt;[String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html)&gt; using [com.esotericpig.jeso.io.StringListReader](src/main/java/com/esotericpig/jeso/io/StringListReader.java)
- [String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html) using [java.io.StringReader](https://docs.oracle.com/javase/8/docs/api/java/io/StringReader.html)

Example usage with a file:
```Java
try(BotBuddyCode bbc = BotBuddyCode.builder(Paths.get("file.txt")).build()) {
  // Don't execute any code, just output result of interpreting:
  System.out.println(bbc.interpretDryRun());
}
```

Example usage with a list of strings:
```Java
List<String> list = new LinkedList<>();
list.add("puts 'Hello World'");
list.add("");
list.add("get_coords");

try(BotBuddyCode bbc = BotBuddyCode.builder(list).build()) {
  // Interpret and execute code
  bbc.interpret();
}
```

Example of functionality:
```Ruby
# This is a comment

puts <<EOS # Heredoc
    Hello World
  EOS # End tag can be indented

puts <<-EOS # ltrim to min indent
    Hello World
  EOS

paste 592 254 <<-EOS # Heredoc with other args
    Hello World
  EOS

# Method names are flexible
begin_safe_mode
endSafeMode

# Quoted strings can also have newlines
puts "Hello \"
World\""
puts 'Hello \'World\''

# Special quotes like Ruby, where you choose the terminator
puts %(Hello \) World)
puts %^Hello \^ World^

# Define your own (user) method
# - Cannot take in args
def my_method
  get_coords
  get_pixel 1839 894
  printscreen # Saves file to current directory
  getOSFamily
end

# Can call multiple methods in one line
call my_method myMethod
```

Real world example:
```Ruby
puts "Get ready..."
delay 2000

begin_safe_mode

paste 1187 492  "Sakana"
paste 1450 511  "Fish"
click 1851 1021
delay_long

paste 1187 492  "Niku"
paste 1450 511  "Meat"
click 1851 1021

end_safe_mode
```

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
