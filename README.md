# How to build and run
1. Clone the repository
2. Start sbt in the repository
3. use `runMain test.SimpleSearch <directory>" to pick a directory to search

# Usage
After starting the program, you will be prompted to enter one or more search terms. Entering terms will show what files contain most of the entered words. A score is given based on how many words match. 100% means that all entered words (or words very similar to the entered words) occur. 50% means that half occur and so on.

# Assumptions
* Every file has a unique name
    * This could become a problem if the scope is expanded in a way that allows for multiple directories at the same time. Other parts of the code have had this thought in mind but the index assumes that files have a unique name.

# Improvements that can be made
* Use `BufferedReader` in order to minimize RAM usage at a given time 
* Stop using mutable state in the index creation. Its only used when creating the index and localized. Would still be better if it wasn't used.
* Single-responsibilty principle. Is it reasonable that the index should also contain the search logic? It would be more intuitive if the search logic used the index as an argument, instead of being contained in the index.
* The `IndexableFile`-class can throw unhandled read errors if a file of incorrect encoding is read. This is a pretty bad, but I don't have time to fix it.
