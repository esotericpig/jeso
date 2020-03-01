# Jeso

Java utils to make Java less verbose and more fun.

Inspired largely by Ruby.

Name = Java + gesso + esoteric.

**Note:** currently in development on v0.3.3.

## Contents

- [Requirements](#requirements)
- [Setup](#setup)
    - [GitHub Packages](#github-packages)
        - [Gradle](#gradle)
        - [Maven](#maven)
    - [Manually](#manually)
    - [Pre-Release](#pre-release)
- [Using](#using)
    - [Top Package](#top-package)
    - [BotBuddy Package](#botbuddy-package)
    - [Code Package](#code-package)
    - [IO Package](#io-package)
- [Hacking](#hacking)
- [License](#license)

## [Requirements](#contents)

- Java 8 or later

## [Setup](#contents)

Pick your poison...

### [GitHub Packages](#contents)

You can view the packages for this project [here](https://github.com/esotericpig/jeso/packages).

You can either use Gradle or Maven.

It takes a bit more setup, but worth it since you can use any GitHub library/package afterwards.

#### [Gradle](#contents)

See [here](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-gradle-for-use-with-github-packages) for more information.

If you don't want to use your GitHub password (recommended), first [create a token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line#creating-a-token) with at least the **read:packages** scope.

In **~/.gradle/gradle.properties**:
```Properties
gpr.user=username # Your GitHub username
gpr.key=token     # Your GitHub token (or password)
```

In your project's **build.gradle**:
```Groovy
repositories {
  maven {
    name = 'Jeso GitHub Package'
    url = uri('https://maven.pkg.github.com/esotericpig/jeso')
    
    credentials {
      username = project.findProperty('gpr.user') ?: System.getenv("USERNAME")
      password = project.findProperty('gpr.key') ?: System.getenv("PASSWORD")
    }
  }
}

dependencies {
  // TODO: Edit the version appropriately!
  implementation 'com.esotericpig.jeso:jeso:X.X.X'
}
```

#### [Maven](#contents)

See [here](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages) for more information.

If you don't want to use your GitHub password (recommended), first [create a token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line#creating-a-token) with at least the **read:packages** scope.

In **~/.m2/settings.xml**:
```XML
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>false</enabled></snapshots>
        </repository>
        
        <repository>
          <id>github</id>
          <name>Jeso GitHub Package</name>
          <url>https://maven.pkg.github.com/esotericpig/jeso</url>
        </repository>
      </repositories>
    </profile>
  </profiles>
  
  <servers>
    <server>
      <id>github</id>
      <!-- TODO: Your GitHub username -->
      <username>USERNAME</username>
      <!-- TODO: Your GitHub token (or password) -->
      <password>TOKEN</password>
    </server>
  </servers>
</settings>
```

In your project's **pom.xml**:
```XML
<dependencies>
  <dependency>
    <groupId>com.esotericpig.jeso</groupId>
    <artifactId>jeso</artifactId>
    <!-- TODO: Edit the version appropriately! -->
    <version>X.X.X</version>
  </dependency>
</dependencies>
```

Install the package*:
```
$ mvn install
```

*If you have bad internet, you'll need to call this multiple times until it downloads.

### [Manually](#contents)

Download the latest [Release](https://github.com/esotericpig/jeso/releases).

Then import the following files into your project:

| Release Files |
| --- |
| build/libs/jeso-x.x.x.jar |
| build/libs/jeso-x.x.x-sources.jar |
| build/distributions/jeso-x.x.x-javadoc.zip |

Alternatively, you can just import this one file, but it also includes dependent jars (if any):

| Release Files |
| --- |
| build/libs/jeso-x.x.x-all.jar |

### [Pre-Release](#contents)

To build a pre-release, please do the following:

```
$ git clone 'https://github.com/esotericpig/jeso.git'
$ cd jeso
$ ./gradlew(.bat) clean buildRelease -x check -x test
```

You can probably safely exclude *check* and *test* (like in the above example) to build it faster (i.e., to not download & install development/test dependencies), as those checks should have already been run when committing the code.

Then import the following files into your project:

| Release Files |
| --- |
| build/libs/jeso-x.x.x.jar |
| build/libs/jeso-x.x.x-sources.jar |
| build/distributions/jeso-x.x.x-javadoc.zip |

Alternatively, you can build everything into one "fat" jar, which includes dependent jars (if any):

`$ ./gradlew(.bat) clean buildFatRelease -x check -x test`

| Release Files |
| --- |
| build/libs/jeso-x.x.x-all.jar |

## [Using](#contents)

[Jeso Javadoc](https://esotericpig.github.io/docs/jeso/javadoc/index.html)

[Top Package](#top-package) [[Javadoc](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/package-summary.html)]

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [Arys](#arys) | Utility class for Arrays | [Arys.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/Arys.html) | [Arys.java](src/main/java/com/esotericpig/jeso/Arys.java) |
| [Bools](#bools) | Utility class for Booleans | [Bools.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/Bools.html) | [Bools.java](src/main/java/com/esotericpig/jeso/Bools.java) |
| [Duplicable](#duplicable) | Generic replacement for Cloneable/clone() | [Duplicable.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/Duplicable.html) | [Duplicable.java](src/main/java/com/esotericpig/jeso/Duplicable.java) |
| [OSFamily](#osfamily) | Enum for guessing the OS family from a String | [OSFamily.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/OSFamily.html) | [OSFamily.java](src/main/java/com/esotericpig/jeso/OSFamily.java) |
| [Strs](#strs) | Utility class for Strings | [Strs.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/Strs.html) | [Strs.java](src/main/java/com/esotericpig/jeso/Strs.java) |
| [Sys](#sys) | Utility class for System | [Sys.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/Sys.html) | [Sys.java](src/main/java/com/esotericpig/jeso/Sys.java) |

[BotBuddy Package](#botbuddy-package) [[Javadoc](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/botbuddy/package-summary.html)]

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [BotBuddy](#botbuddy) | Wrapper around [java.awt.Robot](https://docs.oracle.com/javase/8/docs/api/java/awt/Robot.html) | [BotBuddy.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/botbuddy/BotBuddy.html) | [BotBuddy.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddy.java) |
| BotBuddy.Shortcut | Functional interface for automatic operations for [BotBuddy](#botbuddy) | [BotBuddy.Shortcut.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/botbuddy/BotBuddy.Shortcut.html) | [BotBuddy.java#Shortcut](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddy.java) |
| [BotBuddyCode](#botbuddycode) | Very simple scripting "language" interpreter for [BotBuddy](#botbuddy) | [BotBuddyCode.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/botbuddy/BotBuddyCode.html) | [BotBuddyCode.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCode.java) |
| [BotBuddyCodeApp](#botbuddycodeapp) | Simple CLI app for [BotBuddyCode](#botbuddycode) that can take in a file or read piped-in input (pipeline) | [BotBuddyCodeApp.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/botbuddy/BotBuddyCodeApp.html) | [BotBuddyCodeApp.java](src/main/java/com/esotericpig/jeso/botbuddy/BotBuddyCodeApp.java) |

[Code Package](#code-package) [[Javadoc](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/code/package-summary.html)]

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| LineOfCode | Immutable class that stores a Line Number and Line Column | [LineOfCode.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/code/LineOfCode.html) | [LineOfCode.java](src/main/java/com/esotericpig/jeso/code/LineOfCode.java) |
| ParseCodeException | Runtime Exception that can store a [LineOfCode](#lineofcode) and build a detailed message with it | [ParseCodeException.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/code/ParseCodeException.html) | [ParseCodeException.java](src/main/java/com/esotericpig/jeso/code/ParseCodeException.java) |

[IO Package](#io-package) [[Javadoc](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/io/package-summary.html)]

| Class | Summary | Javadoc | File |
| ----- | ------- | ------- | ---- |
| [StringListReader](#stringlistreader) | Reader for a list of Strings | [StringListReader.html](https://esotericpig.github.io/docs/jeso/javadoc/com/esotericpig/jeso/io/StringListReader.html) | [StringListReader.java](src/main/java/com/esotericpig/jeso/io/StringListReader.java) |

### [Top Package](#using)

#### [Arys](#using)

A utility class for Arrays.

```Groovy
import com.esotericpig.jeso.Arys;

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

// Get multiple random elements using a shuffle strategy (don't repeat); varargs
// - [eggs, null, coffee]
// - [coffee, null, turkey sausage, coffee, null, eggs, eggs, toast]
// - [coffee, turkey sausage, eggs]
println( Arrays.toString(Arys.samples(3,breakfast)) );
println( Arrays.toString(Arys.samples(100,breakfast)) );
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

#### [Bools](#using)

A utility class for Booleans.

```Java
import com.esotericpig.jeso.Bools;

// ["1","on","t","true","y","yes"] are all true and case-insensitive
Bools.parse("On"); // true
```

#### [Duplicable](#using)

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
  protected User(User user) { this.name = user.name; }
  
  public User dup() { return new User(this); }
}

class Student extends User {
  public String school;
  
  public Student(String name,String school) { super(name); this.school = school; }
  protected Student(Student student) { super(student); this.school = student.school; }
  
  @Override
  public Student dup() { return new Student(this); }
}

class Alumnus extends Student {
  public String job;
  
  public Alumnus(String name,String school,String job) { super(name,school); this.job = job; }
  protected Alumnus(Alumnus alumnus) { super(alumnus); this.job = alumnus.job; }
  
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

#### [OSFamily](#using)

An enum for guessing the [OS family](https://en.wikipedia.org/wiki/Category:Operating_system_families) from a String.

Most users will only need [Sys.OS_FAMILY](#sys).

```Groovy
import com.esotericpig.jeso.OSFamily;

Random rand = new Random();

// Used for testing
println( OSFamily.getRandValue(rand) );

// - OSFamily.LINUX
// - OSFamily.MACOS
// - OSFamily.WINDOWS
// - OSFamily.UNKNOWN
println( OSFamily.guessFromName("GNU/Linux Fedora") );
println( OSFamily.guessFromName("Mac OS X") );
println( OSFamily.guessFromName("Microsoft Windows XP") );
println( OSFamily.guessFromName("TempleOS") );
```

#### [Strs](#using)

A utility class for Strings.

```Groovy
import com.esotericpig.jeso.Strs;

// Remove (left) leading whitespace; mutable
// - 'Hello World   '
println( "'" + Strs.ltrim(new StringBuilder("   Hello World   ")) + "'" );

// Remove (right) trailing whitespace; mutable
// - '   Hello World'
println( "'" + Strs.rtrim(new StringBuilder("   Hello World   ")) + "'" );

// Remove leading & trailing whitespace; mutable
// - 'Hello World'
println( "'" + Strs.trim(new StringBuilder("   Hello World   ")) + "'" );
```

#### [Sys](#using)

A utility class for System.

```Groovy
import com.esotericpig.jeso.Sys;

// "Unknown" if not set
println( Sys.OS_NAME );

// Uses the OSFamily enum
println( Sys.OS_FAMILY );

// Gets a System property and ignores SecurityException if thrown
// (will return the specified default value or null)
// - If you use System.getProperty(...), then it can potentially throw a SecurityException, but
//   you might want your app to continue to work even if "os.name" is blocked from being read
println( Sys.getSafeProp("os.name") ); // If not set, null is the default value
println( Sys.getSafeProp("os.name","Unknown") );
```

### [BotBuddy Package](#using)

#### [BotBuddy](#using)

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
     .clicks(int... buttons)
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
     .leftClick([int x,int y])
     .middleClick([int x,int y])
     .move(int x,int y)
     .paste([String text])
     .paste([int x,int y,String text])
     .pressButton([int x,int y],int button)
     .pressButtons(int... buttons)
     .pressKey([int x,int y],int keyCode)
     .pressKeys(int... keyCodes)
     .releaseButton([int x,int y],int button)
     .releaseButtons([int... buttons])
     .releaseKey([int x,int y],int keyCode)
     .releaseKeys([int... keyCodes])
     .releasePressed()
     .rightClick([int x,int y])
     .rollButtons(int... buttons)
     .rollKeys(int... keyCodes)
     .shortcut(BotBuddy.Shortcut shortcut)
     .stash()
     .type(int keyCode)
     .types(int... keyCodes)
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

- Linux:
    - Install "xdotool" and do: `xdotool getmouselocation`

Similar projects:

- Robot-Utils by Denys Shynkarenko (@Denysss) [[GitHub](https://github.com/Denysss/Robot-Utils)]
- Automaton by Renato Athaydes (@renatoathaydes) [[GitHub](https://github.com/renatoathaydes/Automaton)]

#### [BotBuddyCode](#using)

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
import com.esotericpig.jeso.botbuddy.BotBuddyCode;

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

#### [BotBuddyCodeApp](#using)

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

### [IO Package](#using)

#### [StringListReader](#using)

A [java.io.Reader](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html) for a List of Strings.

This was specifically made for [BotBuddyCode](#botbuddycode), but can be used wherever a Reader is.

For each new String in a List, it will produce a newline (`\n`).

Example usage:

```Java
import com.esotericpig.jeso.io.StringListReader;

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

## [Hacking](#contents)

For Windows, use **./gradlew.bat** instead.
```
$ git clone 'https://github.com/esotericpig/jeso.git'
$ cd jeso
$ ./gradlew tasks

$ ./gradlew check
$ ./gradlew test

$ ./gradlew build
$ ./gradlew buildRelease
$ ./gradlew buildFatRelease

$ ./gradlew publish

$ ./gradlew javadocZip
$ ./gradlew sourcesJar

$ ./gradlew checkGradleW
$ ./gradlew wgetGradleWSums

$ ./gradlew rsyncToGhp
```

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
