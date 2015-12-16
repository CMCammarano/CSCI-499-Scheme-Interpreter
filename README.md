README
---------------------------

Name: Colin Cammarano

Email: colincammarano@gmail.com

Overview:
---------------------------

This is a Scheme interpreter written in Java (version 1.8, build 66).

The purpose of this project is to create a command line tool that can accept user input (in the Scheme programming language), parse it, then display the results back to the user. Several basic Scheme primitives and functions (documented below) are supported by this interpreter.

Code:
---------------------------

SchemeEnvironment.java:
 + The main entry point class for the Scheme interpreter. This class was designed and implemented as part of the assignment. Parsing support for all of the required Scheme primitives and functions was implemented within this file. The main method contains a quasi-infinite loop that runs until the user types the (exit) command, at which point, the loop exits and the program terminates normally. Within this loop, the user is prompted to input a command into the command line. Once entered, the command string is parsed by a static method read in the SchemeObject class, which outputs a SchemeObject (a linked list of SchemePairs) containing the entered text. Once the object is created, the type of command entered is determined. If the command is valid, it is parsed by one of several methods; otherwise, the application throws an exception and the user is prompted to enter a new command. Exceptions are used to handle incorrect inputs.

Other Files:
---------------------------

README.md:
+ This file. A readme.

Building the Application:
---------------------------

The application can be built with javac, or the source code can be included in an IDE project.


Implemented Primitives and Forms:
---------------------------

The following Scheme Forms were implemented:
+ lambda
+ let
+ cond*
+ if
+ define
+ set!
+ quote

The following Scheme Primitives were implemented:
+ Addition (+)
+ Subtraction (-)
+ Multiplication (*)
+ Less Than (<)
+ equal?
+ list
+ cons
+ car
+ cdr
+ set-car!
+ set-cdr!

Two other functions were defined:
+ (exit) - Terminates the process


Attributions:
---------------------------

A special thanks to Professor Michael Shindler for providing the basis for this assignment.
