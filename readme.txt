----------
- README -
----------


IMPORTANT NOTE
--------------

I didn't notice the statement in the assignment that the computer is black and the human player is red until I was finished.
Therefore in prologue red corresponds to the computer player and black to the human player. In the GUI green is the human and
red is again the computer.


RUNNING THE JARS
----------------

The jars take two arguments:
        - Map name (the name of the prolog file that defines a map without the trailing .pl)
        - mode (optional), you can add the -simple flag to run the jar in core mode
        
Examples:
To run in simple mode (just prints tank's best move when you click it) on the squaremap map:
        java -jar ./tanks.jar squaremap -simple
        
To run in full mode on the basicmap map:
        java -jar ./tanks.jar basicmap
