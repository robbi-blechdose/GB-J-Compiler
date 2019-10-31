# GB-J-Compiler
A compiler for the language GB-J targeting the GameBoy
---

Language documentation: TBA

Standard library documentation: TBA

### Features

- Compiles GB-J to RGBDS syntax assembly and then into a GameBoy (Color) ROM
- GB-J is tailored to the GameBoy
- An extensive standard library for:
    - Graphics
    - Input
    - Text output on the screen
    - GB Printer support!
- Debug output for the lexing phase (Tokens) and the parsing phase (Abstract Syntax Tree)

### Installation

Install Java on your machine (in case you haven't already).
Extract the release ZIP into a directory of your choice.
Done!

### Usage

The GB-J Compiler is a command-line tool. It can be run with `java -jar GB-J-Compiler.jar`
To learn about options and flags, simply run it with `-help`
