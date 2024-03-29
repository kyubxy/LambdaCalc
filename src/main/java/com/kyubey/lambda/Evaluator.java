package com.kyubey.lambda;

import static com.kyubey.lambda.LambdaExpr.absH;
import static com.kyubey.lambda.LambdaExpr.asAbs;

public class Evaluator implements LambdaExpr.Visitor<LambdaExpr> {
    public enum Strategy {
        STRATEGY_LAZY,
        STRATEGY_EAGER,
    }

    private final Strategy strategy;

    public Evaluator(Strategy str) {
        strategy = str;
    }

    public Evaluator() {
        this(Strategy.STRATEGY_LAZY);
    }

    public LambdaExpr toNormal(LambdaExpr expr) {
        return toNormal(expr, 0);
    }

    public LambdaExpr toNormal(LambdaExpr expr, int maxIters) {
        LambdaExpr current = expr;
        int iters = 0;
        while (true) {
            var next = current.accept(this);

            if (maxIters > 0) {
                iters++;
                if (iters > maxIters)
                    return current;
            } else {
                if (next.equals(current))
                    return current;
            }
            current = next;
        }
    }

    public LambdaExpr oneStep(LambdaExpr expr) {
        return expr.accept(this);
    }

    @Override
    public LambdaExpr visit(LambdaExpr.Variable expr) {
        return expr;
    }

    @Override
    public LambdaExpr visit(LambdaExpr.Abstract expr) {
        return expr;
    }

    @Override
    public LambdaExpr visit(LambdaExpr.Application expr) {
        if (strategy == Strategy.STRATEGY_LAZY) {
            var lst = expr.getE1();
            var rst = expr.getE2();
            // acquire the left subexpression of the application and check if it's an abstraction
            var lastr = asAbs(lst);
            if (lastr == null) return new LambdaExpr.Application(lst.accept(this), rst.accept(this));
            // what we want: right subtree -> [bound vars in left subtree]
            var subst = new Substitution(lastr.getHead(), rst);
            return lastr.accept(subst);
        } else {
            // TODO:
            throw new RuntimeException("not implemented");
        }
    }

    static class Substitution implements LambdaExpr.Visitor<LambdaExpr> {
        String f;
        LambdaExpr r;

        // M[x:=N] <-> expr.accept(new Substitutor(x, N))

        public Substitution(String find, LambdaExpr replace) {
            f = find; r = replace;
        }

        @Override
        public LambdaExpr visit(LambdaExpr.Variable expr) {
            return expr.getIdentifier().equals(f) ? r : expr;
        }

        @Override
        public LambdaExpr visit(LambdaExpr.Abstract expr) {
            var body = expr.getBody();

            // if an abstraction in the subtree shadows our find variable, terminate the recursion
            var h = absH(body);
            if (h != null && h.equals(f))
                return body;
            else
                return body.accept(this);
        }

        @Override
        public LambdaExpr visit(LambdaExpr.Application expr) {
            var e1 = expr.getE1().accept(this);
            var e2 = expr.getE2().accept(this);
            return new LambdaExpr.Application(e1, e2);
        }
    }
}
