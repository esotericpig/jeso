# TODO | Jeso

## Important!

- [ ] Move out `Duplicable` into its own project
- [ ] Move out `BotBuddy` into its own project
    - Will still use `jeso` as a dependency
    - Will not include Ruby/Groovy support (that would go in new projects botbuddy-ruby/groovy)
    - [ ] Change BotBuddyCode wording to call it a DSL in README/doc? (domain-specific language)
- [ ] `math`/`csv` packages will be in own projects
- [ ] Move TODOs appropriately to these projects
- [ ] Add all projects to jcenter/maven/whatever

## v1.0.0 [on hold]

- [x] BotBuddy
    - [x] Add safe mode (if user moves mouse)
    - [x] Mac OS X support for paste
    - [x] Take in File or Array/List (of String); allow single/double quotes and/or Ruby heredocs:
        - `paste 999  493  "Fish"`
        - `enter 1427 500  "Sakana"`
        - `click 1853 1015`
    - [x] Make #doubleClick() more generic; use above method
    - [x] Store history of keys/mouse pressed, so can release them if UserIsActiveException during Safe Mode
- [ ] Add appropriate logging (slf4j & Logback?); search and remove printlns
- [ ] CSV class
- [x] In Gradle, add a task to check/download the Wrapper checksum. [Release Checksums](https://gradle.org/release-checksums/)
- [x] In Gradle, add a fat jar task that includes everything in one jar (javadoc, source, dependencies)
- [ ] Arys/Cols/Strs:
    - sample(), unique(), compact(), join(). max_length(...)
- [ ] Look at own code and see where a generic method could be better.
- [ ] Look at top stackoverflow Qs in Java.
- [ ] Strs.index(str,-1) and Strs.split(str,3,-2) (can do ranges, etc.).
- [ ] Use Java reflection to interpolate vars in text like Ruby (but just very slow).
- [ ] isEmpty() for String that tests null and strip (probably 2 methods).
- [x] Map method that will put if not exist and return new.
    - This has already been solved in Java 8 with:
        - `value = map.computeIfAbsent("key",k -> new TimeConsumingClass());`
- [ ] Arys.include?; Array *=; Arys.reverse; Arys.to_map; Maps.invert; Maps.zip; Arys.flatten; %w(...) So pass in "Bob Fred George" and makes into an array. Arys.count; Arys.swap(index1,index2);
- [ ] A class that adds up max length for formatting data?
    - Store the args into a list, then pass into printf.
    - f = new Format(System.out);
    - f.begin();
    - f.out("%s[max_name] ","Fred");
    - f.out("%s[max_name] ","George");
    - f.end(); // Sends all to out
- [ ] Add my LinkedList; for storing nodes for fast insertion
- [ ] Some type of EscapeBase, and EscapeJava class extends it
     - Can add/remove what is escaped/unescaped in Map
     - Can specify end tag; default: "
     - Can specify escape char; default: \
- [x] Add [SpotBugs](https://spotbugs.github.io/) to Gradle?
- [ ] Add appropriate documentation
    - [ ] In all classes (Javadoc)
    - [ ] In README.md
    - [ ] In HACKING.md?
    - [ ] Add @since for all (use library version)
- [ ] Mimic Ruby's [OptionParser](https://ruby-doc.org/stdlib-2.6.3/libdoc/optparse/rdoc/OptionParser.html), but add sub-commands capability? Now possible thanks to Java 8 lambdas.
- [ ] Move BigIntBase, etc. from senpi to here (jeso.math)?
    - [ ] BigRealBase & Radix Point instead of BigDecBase & InvalidDotException
        - [ ] Can use dot or comma (user-specified); default gets from locale
