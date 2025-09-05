# LambdaCalc

Implementation of simple lambda calculus in Java. A successor to my [previous LC prototype](https://github.com/kyubxy/lambdabasic).

[Calc is short for calculator](https://youtu.be/DRAAGKKNtx4).

## Usage
TODO:

## Building

This project uses **javacc** to generate code for the parsers. This only needs to be installed if the sources are regenerated 
(which is often only the case if the parser files are edited). Sources are generated on compilation.

### IntelliJ
Basically everything should just work right out of the box.

### Without an IDE
To build and run the application for the first time, use:

```sh
mvn package ; # this takes care of both installing dependencies and compilation
java -cp target/LambdaCalc-1.0-SNAPSHOT.jar com.kyubey.Lambda
```

The build script in `scripts/run.sh` also executes the above.

To run the unit tests, use

```sh
mvn test
```

## Eventual Goals
- Simple lambda calculus parsing 
- Beta reduction with both eager and lazy evaluation
- "Function naming" with basic find and replace algorithms
- A primitive automated proof assistant

