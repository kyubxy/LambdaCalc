package com.kyubey;

import com.kyubey.lambda.Evaluator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.FileInputStream;
import java.io.PrintStream;

public class Lambda {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("lambda").build()
                .description("An evaluator and proof assistant for the simple lambda calculus")
                .version("${lambda} "+ Properties.VERSION);
        parser.addArgument("-h", "--help")
                .action(Arguments.help())
                .help("shows this menu");
        parser.addArgument("infile")
                .nargs("+")
                .type(FileInputStream.class)
                .setDefault(System.in)
                .help("a file containing the lambda calculus program to include");
        parser.addArgument("outfile")
                .nargs("?")
                .type(PrintStream.class)
                .setDefault(System.out)
                .help("where to keep a record of line by line evaluations");
        parser.addArgument("-in", "--input")
                .action(Arguments.store())
                .help("the lambda calculus expression to evaluate, if a program is " +
                        "specified, the input expression also gets access to its function names");
        parser.addArgument("-s", "--strategy")
                .setDefault(Evaluator.Strategy.LAZY)
                .type(Evaluator.Strategy.class)
                .action(Arguments.store())
                .help("the evaluation strategy to use [lazy | eager], defaults to lazy evaluation");

        try {
            Namespace res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}