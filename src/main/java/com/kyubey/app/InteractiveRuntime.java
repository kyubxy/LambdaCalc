package com.kyubey.app;

import com.kyubey.Properties;
import com.kyubey.lambda.Evaluator;
import com.kyubey.lambda.LambdaExpr;
import com.kyubey.lambda.parser.LambdaParser;
import com.kyubey.lambda.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InteractiveRuntime {
    private static final String PROMPT = "λ > ";

    private enum ExecutionOutcome {
        CONTINUE,
        EXIT,
        SKIP
    }

    private static class EvalHandler {
        private Evaluator evaluator;
        private Evaluator.Strategy st;

        public EvalHandler(Evaluator.Strategy s) {
            setStrategy(s);
        }

        public void setStrategy(Evaluator.Strategy s) {
            evaluator = new Evaluator(s);
            st = s;
        }

        public Evaluator.Strategy getStrategy() {
            return st;
        }

        public Evaluator get() {
            return evaluator;
        }
    }

    private boolean verboseEval;
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
        System.out.print(PROMPT);
        var input = sc.next();
        switch (parseCommands(input)) {
            case SKIP -> { return false; }
            case EXIT -> { return true; }
        }
        parseLambda(input);
        return false;
    }

    private void parseLambda(String input) {
        LambdaParser parser = LambdaParser.fromStr(input);
        LambdaExpr expr;
        try {
            expr = parser.start();
        } catch (ParseException e) {
            System.out.println("Parse error!"); // TODO: we need better error messages
            return;
        }
        List<LambdaExpr> steps = new ArrayList<>();
        var norm = eval.get().toNormal(expr, steps);
        if (verboseEval) {
            for(var step : steps) {
                System.out.println(step);
            }
        } else {
            System.out.println(norm);
        }
    }

    public ExecutionOutcome parseCommands(String input) {
        if (!input.startsWith(":")) return ExecutionOutcome.CONTINUE;

        // this is such a dumb idea

        if (input.equalsIgnoreCase(":h")) {
            System.out.println("Welcome to the Lambda interactive runtime v" + Properties.VERSION);
            System.out.println("Type a lambda expression to have it evaluated to normal form or use one of the following commands\n");
            System.out.println(":q - quit");
            System.out.println(":s - view evaluation mode");
            System.out.println(":s[e|l] - set the evaluation mode to either eager(e) or lazy(l)");
            System.out.println(":v - view verbosity");
            System.out.println(":v[t|f] - whether to print all lines in evaluation - true(t) or false(f)");
            System.out.println(":c - (will attempt to) clear the terminal window");
            System.out.println();
            System.out.println("We encode the lambda symbol λ using the backslash character \\\n");
            return ExecutionOutcome.SKIP;
        }

        if (input.equalsIgnoreCase(":q"))
            return ExecutionOutcome.EXIT;

        if (input.equalsIgnoreCase(":v")) {
            System.out.println("Verbosity: " + verboseEval);
            return ExecutionOutcome.SKIP;
        }

        if (input.equalsIgnoreCase(":s")) {
            System.out.println("Strategy: " + eval.getStrategy());
            return ExecutionOutcome.SKIP;
        }

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

        if (input.equalsIgnoreCase(":vt")) {
            verboseEval = true;
            System.out.println("Verbosity enabled");
        }

        if (input.equalsIgnoreCase(":vt")) {
            verboseEval = false;
            System.out.println("Verbosity disabled");
        }

        if (input.equalsIgnoreCase(":c")) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }

        return ExecutionOutcome.SKIP;
    }
}
