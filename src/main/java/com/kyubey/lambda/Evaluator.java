package com.kyubey.lambda;

import static com.kyubey.lambda.LambdaExpr.absH;
import static com.kyubey.lambda.LambdaExpr.asAbs;

public class Evaluator implements LambdaExpr.Visitor<LambdaExpr> {
    public enum Strategy {
        LAZY,
        EAGER,
    }

    private final Strategy strategy;

    public Evaluator(Strategy str) {
        strategy = str;
    }

    public Evaluator() {
        this(Strategy.LAZY);
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

    public LambdaExpr oneStep(LambdaExpr expr) { return expr.accept(this); }

    @Override
    public LambdaExpr visit(LambdaExpr.Variable expr) { return expr; }

    @Override
    public LambdaExpr visit(LambdaExpr.Abstract expr) { return expr; }

    @Override
    public LambdaExpr visit(LambdaExpr.Application expr) {
        /* so basically, you can think of beta reduction as matching a specific pattern of expression tree with
        another pattern of expression tree. and then whether the approach is top-down or bottom-up is determined
        based on whether the evaluating strategy is lazy or eager respectively. */
        var lst = expr.getE1();
        var rst = expr.getE2();
        var lstabs = asAbs(lst);
        if (strategy == Strategy.LAZY) {
            // acquire the left subexpression of the application and check if it's an abstraction
            if (lstabs == null) {
                // not an abstraction, propagate the recursion normally
                return new LambdaExpr.Application(lst.accept(this), rst.accept(this));
            } else {
                // matches the form, perform the transformation immediately
                // note we only return the body of the left abstraction
                return Substitution.doRedex(lstabs, rst);
            }
        } else /* if strategy == Strategy.STRATEGY_EAGER */ {
            // acquire the left subexpression of the application and check if it's an abstraction
            if (lstabs == null)  {
                // not an abstraction, propagate the recursion normally
                return new LambdaExpr.Application(lst.accept(this), rst.accept(this));
            } else {
                // only reduce if the right subtree is in normal form
                // NOTE: checking at each node whether the entire subtree is normal could be expensive
                // TODO: cache the normal checker
                if (rst.accept(new NormalChecker()))
                    return Substitution.doRedex(lstabs, rst);
                else
                    return new LambdaExpr.Application(lst.accept(this), rst.accept(this));
            }
        }
    }

    static class NormalChecker implements LambdaExpr.Visitor<Boolean> {

        @Override
        public Boolean visit(LambdaExpr.Variable expr) {
            return true;
        }

        @Override
        public Boolean visit(LambdaExpr.Abstract expr) {
            return expr.getBody().accept(this);
        }

        @Override
        public Boolean visit(LambdaExpr.Application expr) {
            if (asAbs(expr.getE1()) != null)
                return false;   // there is an abstraction => redex
            else
                // no abstraction => no redex
                return expr.getE1().accept(this) && expr.getE2().accept(this);
        }
    }

    static class Substitution implements LambdaExpr.Visitor<LambdaExpr> {
        private final String f;
        private final LambdaExpr r;


        // M[x:=N] <-> expr.accept(new Substitutor(x, N))
        public static LambdaExpr doRedex(LambdaExpr.Abstract expr, LambdaExpr replace) {
            var sub = new Substitution(expr.getHead(), replace);
            var outexpr = expr.accept(sub);
            // remove the head from abstractions
            var abs = asAbs(outexpr);
            return abs != null ? abs.getBody() : outexpr;
        }

        private Substitution(String find, LambdaExpr replace) {
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
                return expr;
            else
                return new LambdaExpr.Abstract(expr.getHead(), body.accept(this));
        }

        @Override
        public LambdaExpr visit(LambdaExpr.Application expr) {
            var e1 = expr.getE1().accept(this);
            var e2 = expr.getE2().accept(this);
            return new LambdaExpr.Application(e1, e2);
        }
    }
}
