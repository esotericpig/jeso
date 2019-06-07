# Jeso

Java utils to make Java less verbose and more fun.

Inspired largely by Ruby.

Name = Java + gesso + esoteric.

**Note:** currently in development on v0.2.0.

## Contents

- [Requirements](#requirements)
- [Using](#using)
- [Code](#code)
- [License](#license)

## [Requirements](#contents)

- Java 8 or later

## [Using](#contents)

Currently, an official release hasn't been made yet, but you can go ahead and use it in your project by building a pre-release yourself:

- `$ ./gradlew(.bat) clean buildRelease -x check -x test`

You can probably safely exclude "check" and "test" to build it faster, as those checks should have already been run when committing the code.

Then use the following files in your project:

- build/libs/jeso-*.jar
- build/libs/jeso-*-sources.jar
- build/distributions/jeso-*-javadoc.zip

Alternatively, you can build everything into one "fat" jar (including dependent jars):

- `$ ./gradlew(.bat) clean buildFatRelease -x check -x test`
- build/libs/jeso-*-all.jar

## [Code](#contents)

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [Arys](#arys) | Utility class for Arrays | - | [Arys.java](src/main/java/com/esotericpig/jeso/Arys.java) |
| [Bools](#bools) | Utility class for Booleans | - | [Bools.java](src/main/java/com/esotericpig/jeso/Bools.java) |
| [Duplicable](#duplicable) | Generic replacement for Cloneable/clone() | - | [Duplicable.java](src/main/java/com/esotericpig/jeso/Duplicable.java) |
| [Strs](#strs) | Utility class for Strings | - | [Strs.java](src/main/java/com/esotericpig/jeso/Strs.java) |
| [Sys](#sys) | Utility class for System | - | [Sys.java](src/main/java/com/esotericpig/jeso/Sys.java) |

[BotBuddy Package](#botbuddy-package)

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [BotBuddy](#botbuddy) | Wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html) | - | [BotBuddy.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddy.java) |
| [BotBuddyCode](#botbuddycode) | Very simple scripting "language" interpreter for [BotBuddy](#botbuddy) | - | [BotBuddyCode.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCode.java) |
| [BotBuddyCodeApp](#botbuddycodeapp) | Simple CLI app for [BotBuddyCode](#botbuddycode) that can take in a file or read piped-in input (pipeline) | - | [BotBuddyCodeApp.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCodeApp.java) |

[IO Package](#io-package)

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [StringListReader](#stringlistreader) | Reader for a list of Strings | - | [StringListReader.java](src/main/java/com/esotericpig/jeso/io/StringListReader.java) |

#### [Arys](#code)

A utility class for Arrays.

For easier reading, used `println` by itself.

```Groovy
String[] breakfast = {"coffee","coffee",null,"eggs","eggs",null,"toast","turkey sausage"};
String[] newArray = null;
Random rand = new Random();

// Remove nulls; varargs
// - [coffee, coffee, eggs, eggs, toast, turkey sausage]
println( Arrays.toString(Arys.compact(breakfast)) );

// Move nulls to end; mutable
// - [coffee, coffee, eggs, eggs, toast, turkey sausage, null, null]
println( Arrays.toString(Arys.compactMut(breakfast)) );

// Join into a String; varargs
// - 123
println( Arys.join(1,2,3) );

// Join into a String with a custom separator; varargs
// - coffee,coffee,eggs,eggs,toast,turkey sausage,null,null
// - coffee | coffee | eggs | eggs | toast | turkey sausage | null | null
println( Arys.joins(',',breakfast) );
println( Arys.joins(" | ",breakfast) );

// Create a new array using Reflection
// - [Ljava.lang.String;@][3]
newArray = Arys.newArray(breakfast,3);
println( newArray + "][" + newArray.length + "]" );

// Get a random element; varargs
// - eggs
// - coffee
println( Arys.sample(breakfast) );
println( Arys.sample(rand,breakfast) );

// Get multiple random elements; varargs
// - [eggs, null, coffee]
// - [coffee, turkey sausage, eggs]
println( Arrays.toString(Arys.samples(3,breakfast)) );
println( Arrays.toString(Arys.samples(3,rand,breakfast)) );

// Remove duplicate elements; varargs
// - [coffee, eggs, toast, turkey sausage, null]
println( Arrays.toString(Arys.unique(breakfast)) );

// Remove duplicate elements (pad with nulls); mutable
// - [coffee, eggs, toast, turkey sausage, null, null, null, null]
println( Arrays.toString(Arys.uniqueMut(breakfast)) );

// Create a new array from a List using Reflection
// - [Ljava.lang.String;@][8]
newArray = Arys.toArray(breakfast,Arrays.asList(breakfast));
println( newArray + "][" + newArray.length + "]" );
```

#### [Bools](#code)

A utility class for Booleans.

```Java
// ["1","on","t","true","y","yes"] are all true and case-insensitive
Bools.parse("On"); // true
```

#### [Duplicable](#code)

A Generic replacement for Cloneable/clone().

Almost every Library has their own, so let's reinvent the wheel.

[Java Cloning: Even Copy Constructors Are Not Enough](https://dzone.com/articles/java-cloning-even-copy-constructors-are-not-suffic) [DZone]  
[CopyConstructorExample.java](https://github.com/njnareshjoshi/exercises/blob/master/src/org/programming/mitra/exercises/CopyConstructorExample.java) [GitHub]

```Java
import com.esotericpig.jeso.Duplicable;

public class Testbed
  public static void main(String[] args) {
    Alumnus alum1 = new Alumnus("Bob","MySchool","MyJob");
    Alumnus alum2 = alum1.dup();
    
    // Same school
    alum2.name = "Fred";
    alum2.job = "CoolJob";
    
    System.out.println(alum1);
    System.out.println(alum2);
  }
}

class User implements Duplicable<User> {
  public String name;
  
  public User(String name) { this.name = name; }
  public User(User user) { this.name = user.name; }
  
  public User dup() { return new User(this); }
}

class Student extends User {
  public String school;
  
  public Student(String name,String school) { super(name); this.school = school; }
  public Student(Student student) { super(student); this.school = student.school; }
  
  @Override
  public Student dup() { return new Student(this); }
}

class Alumnus extends Student {
  public String job;
  
  public Alumnus(String name,String school,String job) { super(name,school); this.job = job; }
  public Alumnus(Alumnus alumnus) { super(alumnus); this.job = alumnus.job; }
  
  @Override
  public Alumnus dup() { return new Alumnus(this); }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    sb.append(name).append(":\t").append('[');
    sb.append(school).append(',');
    sb.append(job).append(']');
    
    return sb.toString();
  }
}
```

#### [Strs](#code)

A utility class for Strings.

#### [Sys](#code)

A utility class for System.

### [BotBuddy Package](#code)

#### [BotBuddy](#code)

A wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html).

**Warning** for **Linux** users:  

- On Wayland, Java's Robot will not work. You will need to either use X11 or XWayland, until either OpenJDK or Wayland is fixed.

It can be used for...

- Moving the mouse and pasting in text.
- Automating tedious tasks that a website/program has not implemented yet, such as inputting a CSV file into a website, row by row.
- Automating tedious tasks in games, such as moving your character to a place to fish and then moving your character home to dump the fish.
- Testing/QAing your desktop applications.

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
     .beginFastMode()
     .beginSafeMode()
     .clearPressed()
     .clearPressedButtons()
     .clearPressedKeys()
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
     .drag(int fromX,int fromY,int toX,int toY,[int button])
     .endFastMode()
     .endSafeMode()
     .enter([String text])
     .enter([int x,int y,String text])
     .key(int keyCode)
     .leftClick([int x,int y])
     .middleClick([int x,int y])
     .move(int x,int y)
     .paste([String text])
     .paste([int x,int y,String text])
     .pressKey([int x,int y],int keyCode)
     .pressMouse([int x,int y],int button)
     .releaseButtons()
     .releaseKey([int x,int y],int keyCode)
     .releaseKeys()
     .releaseMouse([int x,int y],int button)
     .releasePressed()
     .rightClick([int x,int y])
     .shortcut(BotBuddy.Shortcut shortcut)
     .stash()
     .unstash()
     .waitForIdle()
     .wheel(int amount)
     .set*(*);
```

Unchainable methods:

```Java
buddy.printScreen()
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

A Safe Mode has been added for convenience. If the user ever moves their mouse, then **UserIsActiveException** will be thrown. After each operation, it just checks the mouse coordinates, while updating its internal coordinates accordingly to the operations.

In addition, the pressed keys and pressed mouse buttons are stored internally if Release Mode is on (on by default), so that you can release everything currently pressed down to alleviate problems for the user when active.

Example:

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
  // Release all keys and/or mouse buttons pressed down by the automatic operations
  buddy.releasePressed();
  
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

#### [BotBuddyCode](#code)

A very simple scripting "language" interpreter for [BotBuddy](#botbuddy). It is **not** Turing complete.

See [BotBuddyCodeTest.bbc](src/test/resources/BotBuddyCodeTest.bbc) for a quick example of functionality. If you were to interpret this file dryly, then it would produce this output: [BotBuddyCodeTestOutput.txt](src/test/resources/BotBuddyCodeTestOutput.txt).

The idea was to make a very simple parser, without including the overhead of Groovy/JRuby into *Jeso*. In a future, separate project, I may add Groovy/JRuby support.

It can handle Ruby-like string literals and [heredoc](https://en.wikipedia.org/wiki/Here_document), and simple methods (no params).

It can accept the following input:

- [java.io.BufferedReader](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html)
- [java.nio.file.Path](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html) [use [java.nio.file.Paths.get(...)](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Paths.html#get-java.lang.String-java.lang.String...-)]
- List&lt;String&gt; using [com.esotericpig.jeso.io.StringListReader](#stringlistreader)
- String using [java.io.StringReader](https://docs.oracle.com/javase/8/docs/api/java/io/StringReader.html)

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

#### [BotBuddyCodeApp](#code)

A simple CLI app for [BotBuddyCode](#botbuddycode) that can take in a file or read piped-in input (pipeline).

Print help:

- `java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp --help`

Help:

```Makefile
Usage: BotBuddyCodeApp [options] <file> [options]

Interprets the contents of <file> using BotBuddyCode.
Data can also be piped in, without using a file.

Options:
    -n, --dry-run            Do not execute any code, only output the interpretation
    ---
    -h, --help               Print this help

Examples:
    BotBuddyCodeApp -n mydir/myfile.bbc
    BotBuddyCodeApp 'My Dir/My File.bbc'
    echo 'get_coords' | BotBuddyCodeApp
```

Examples:

```Console
$ java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp file.txt
$ java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp -n file.txt
$ echo 'get_pixel 100 100' | java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp
$ echo 'get_pixel 100 100' | java -cp 'build/libs/*' com.esotericpig.jeso.botbuddy.BotBuddyCodeApp -n
```

### [IO Package](#code)

#### [StringListReader](#code)

A [java.io.Reader](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html) for a List of Strings.

This was specifically made for [BotBuddyCode](#botbuddycode), but can be used wherever a Reader is.

For each new String in a List, it will produce a newline (`\n`).

Example usage:

```Java
List<String> list = new LinkedList<>();
list.add("name = ' hello World '");
list.add("name.strip!");
list.add("name.capitalize!");
list.add("");
list.add("puts name");

try(BufferedReader lin = new BufferedReader(new StringListReader(list))) {
  String line = null;
  
  while((line = lin.readLine()) != null) {
    System.out.println(line);
  }
}
```

Also see:

- [StringListReaderTest](src/test/java/com/esotericpig/jeso/io/StringListReaderTest.java)
- [BotBuddyCode.Builder.input(List&lt;String&gt; strList)](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCode.java)

## [License](#contents)
[GNU LGPL v3+](LICENSE)

> Jeso ([https://github.com/esotericpig/jeso](https://github.com/esotericpig/jeso))  
> Copyright (c) 2019 Jonathan Bradley Whited (@esotericpig)  
> 
> Jeso is free software: you can redistribute it and/or modify  
> it under the terms of the GNU Lesser General Public License as published by  
> the Free Software Foundation, either version 3 of the License, or  
> (at your option) any later version.  
> 
> Jeso is distributed in the hope that it will be useful,  
> but WITHOUT ANY WARRANTY; without even the implied warranty of  
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the  
> GNU Lesser General Public License for more details.  
> 
> You should have received a copy of the GNU Lesser General Public License  
> along with Jeso. If not, see <http://www.gnu.org/licenses/>.  
