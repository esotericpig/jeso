# TODO | jeso

## v1.0.0

- [ ] BotBuddy
    - [x] Add safe mode (if user moves mouse)
    - [x] Mac OS X support for paste
    - [ ] Add documentation
    - [ ] Take in File or Array/List (of String); allow single/double quotes and/or Ruby heredocs:
        - `paste 999  493  "Fish"`
        - `enter 1427 500  "Sakana"`
        - `click 1853 1015`
    - [ ] Make #doubleClick() more generic; use above method
- [ ] CSV class
- [x] In Gradle, add a task to check/download the Wrapper checksum. [Release Checksums](https://gradle.org/release-checksums/)
- [ ] Arrs/Cols/Strs (one class?):
    - sample(), unique(), compact(), join(). max_length(...)
- [ ] Look at own code and see where a generic method could be better.
- [ ] Look at top stackoverflow Qs in Java.
- [ ] Strs.index(str,-1) and Strs.split(str,3,-2) (can do ranges, etc.).
- [ ] Use Java reflection to interpolate vars in text like Ruby (but just very slow).
- [ ] isEmpty() for String that tests null and strip (probably 2 methods).
- [ ] Map method that will put if not exist and return new.
- [ ] Arrs.include?; Array *=; Arrs.reverse; Arrs.to_map; Maps.invert; Maps.zip; Arrs.flatten; %w(...) So pass in "Bob Fred George" and makes into an array. Arrs.count; Arrs.swap(index1,index2);
- [ ] A class that adds up max length for formatting data?
    - Store the args into a list, then pass into printf.
    - f = new Format(System.out);
    - f.begin();
    - f.out("%s[max_name] ","Fred");
    - f.out("%s[max_name] ","George");
    - f.end(); // Sends all to out
