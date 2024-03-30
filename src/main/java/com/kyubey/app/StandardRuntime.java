package com.kyubey.app;

import com.kyubey.compilation.Compiler;
import com.kyubey.compilation.Semant;
import com.kyubey.lambda.Evaluator;
import com.kyubey.lambda.LambdaExpr;
import com.kyubey.lambda.parser.LambdaParser;
import com.kyubey.lambda.parser.ParseException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import static com.kyubey.lambda.LambdaExpr.APP;

public class StandardRuntime {
    private final PrintStream printStream;
    private final LambdaExpr initial;

    private final Compiler compiler;
    private final Evaluator evaluator;

    public StandardRuntime(InputStream is, PrintStream ps, String input, Evaluator.Strategy strategy) throws ParseException {
        printStream = ps;
        evaluator = new Evaluator(strategy);
        initial = LambdaParser.fromStr(input).start();
        compiler = new Compiler("");
    }

    public void run() {
        // compile program
        LambdaExpr program;
        try {
            program = compiler.compile();
        } catch (Semant.SemanticException e) {
            throw new RuntimeException(e);
        }

        // execute program
        var steps = new LinkedList<LambdaExpr>();
        if (initial != null) program = APP(program, initial);
        var normal = evaluator.toNormal(program, steps);
        printStream.println(normal);
    }

}
