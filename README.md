# How to build and run
1. Clone the repository
2. Start sbt in the repository
3. use "runMain test.SimpleSearch <directory>" to pick a directory to search

# Usage
After starting the program, you will be promted to enter one or more search terms. Entering terms will show what files contain most of the entered words. A score is given based on how many words match. 100% means that all entered words (or words very similar to the entered words) occur. 50% means that half occur and so on.

# Assumptions
* Every file has a unique name

# Possible improvements
* Use BufferedReader in order to 
* Stop using mutable state in the index creation
* Single-responsibilty principle. Is it reasonable that the index should also contain the search logic? It would be more intuitive if the search logic used the index as an argument, instead of being contained in the index.
