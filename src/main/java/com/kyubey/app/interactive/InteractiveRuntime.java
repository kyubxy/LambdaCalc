package com.kyubey.app.interactive;

import com.kyubey.lambda.Evaluator;
import com.kyubey.lambda.LambdaExpr;
import com.kyubey.lambda.parser.LambdaParser;
import com.kyubey.lambda.parser.ParseException;

import java.util.Arrays;
import java.util.Scanner;

public class InteractiveRuntime {
    private enum ExecutionOutcome {
        CONTINUE,
        EXIT,
        SKIP
    }

    private static class EvalHandler {
        private Evaluator evaluator;

        public EvalHandler(Evaluator.Strategy s) {
            setStrategy(s);
        }

        public void setStrategy(Evaluator.Strategy s) {
            evaluator = new Evaluator(s);
        }

        public Evaluator get() {
            return evaluator;
        }
    }

    private final boolean verboseEval;
    private final EvalHandler eval;
    private final Scanner sc;

    public InteractiveRuntime(Evaluator.Strategy strategy, boolean verboseEval) {
        this.verboseEval = verboseEval;

        eval = new EvalHandler(strategy);
        sc = new Scanner(System.in);
    }

    public void run() {
        //noinspection StatementWithEmptyBody
        while (!doCycle())
            ;
    }

    // returns true to exit
    private boolean doCycle() {
        var input = sc.next();
        switch (parseCommands(input)) {
            case SKIP -> { return false; }
            case EXIT -> { return true; }
        }
        parseLambda(input);
        return false;
    }

    public ExecutionOutcome parseCommands(String input) {
        if (!input.startsWith(":")) return ExecutionOutcome.CONTINUE;

        if (input.equalsIgnoreCase(":q"))
            return ExecutionOutcome.EXIT;

        if (input.equalsIgnoreCase(":se")) {
            eval.setStrategy(Evaluator.Strategy.EAGER);
            System.out.println("Evaluation strategy set to eager");
            return ExecutionOutcome.SKIP;
        }

        if (input.equalsIgnoreCase(":sl")) {
            eval.setStrategy(Evaluator.Strategy.LAZY);
            System.out.println("Evaluation strategy set to lazy");
            return ExecutionOutcome.SKIP;
        }

        return ExecutionOutcome.SKIP;
    }

    private void parseLambda(String input) {
        LambdaParser parser = LambdaParser.fromStr(input);
        LambdaExpr expr;
        try {
            expr = parser.start();
        } catch (ParseException e) {
            System.out.println("Parse error!\nUnexpected token " + e.currentToken +
                    ". Was expecting " + Arrays.deepToString(e.expectedTokenSequences));
            return;
        }

        if (verboseEval) {
            throw new RuntimeException("TODO: not implemented");
        } else {
            System.out.println(eval.get().toNormal(expr));
        }
    }
}
