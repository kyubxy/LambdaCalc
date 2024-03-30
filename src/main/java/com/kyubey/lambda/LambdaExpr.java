package com.kyubey.lambda;

public abstract class LambdaExpr {
    private static final char LAMBDA = '\\';
    private static final char DOT = '.';

    private static String variableFormatter(String identifier) {
        if (identifier.length() == 1)
            return identifier;
        else
            return "'" + identifier + "'";
    }

    public static LambdaExpr VAR(String v) {
        return new Variable(v);
    }

    public static LambdaExpr ABS(String head, LambdaExpr body) {
        return new Abstract(head, body);
    }

    public static LambdaExpr APP(LambdaExpr e1, LambdaExpr e2) {
        return new Application(e1, e2);
    }

    public static Abstract asAbs(LambdaExpr e) {
        if (e instanceof Abstract a)
            return a;
        return null;
    }

    public static Variable asVar(LambdaExpr e) {
        if (e instanceof Variable x)
            return x;
        return null;
    }

    public static Application asApp(LambdaExpr e) {
        if (e instanceof Application x)
            return x;
        return null;
    }

    public static String absH(LambdaExpr e) {
        var a = asAbs(e);
        return a == null ? null : a.getHead();
    }

    public static LambdaExpr absB(LambdaExpr e) {
        var a = asAbs(e);
        return a == null ? null : a.getBody();
    }

    public static String varId(LambdaExpr e) {
        var v = asVar(e);
        return v == null ? null : v.getIdentifier();
    }

    public static LambdaExpr appE1(LambdaExpr e) {
        var a = asApp(e);
        return a == null ? null : a.getE1();
    }

    public static LambdaExpr appE2(LambdaExpr e) {
        var a = asApp(e);
        return a == null ? null : a.getE2();
    }

    public interface Visitor<R> {
        R visit(Variable expr);
        R visit(Abstract expr);
        R visit(Application expr);
    }

    public abstract <R> R accept (Visitor<R> v);

    public abstract boolean isAlphaEquivTo(LambdaExpr expr);

    public static class Variable extends LambdaExpr {
        private final String identifier;

        public Variable (String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public <R> R accept(Visitor<R> v) { return v.visit(this); }

        @Override
        public boolean isAlphaEquivTo(LambdaExpr expr) {
            return asVar(expr) != null && this.equals(expr);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Variable other))
                return false;
            return other.identifier.equals(identifier);
        }

        @Override
        public String toString() {
            return LambdaExpr.variableFormatter(identifier);
        }
    }

    public static class Abstract extends LambdaExpr {
        private final String head;
        private final LambdaExpr expr;

        public Abstract(String head, LambdaExpr expr) {
            this.head = head;
            this.expr = expr;
        }

        public String getHead() {
            return head;
        }

        public LambdaExpr getBody() {
            return expr;
        }

        @Override
        public <R> R accept(Visitor<R> v) { return v.visit(this); }

        @Override
        public boolean isAlphaEquivTo(LambdaExpr expr) {
            throw new RuntimeException("TODO: not implemented");
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Abstract other))
                return false;
            return other.head.equals(head) && other.expr.equals(expr);
        }

        @Override
        public String toString() {
            return LAMBDA + LambdaExpr.variableFormatter(head) + DOT + expr.toString();
        }
    }

    public static class Application extends LambdaExpr {
        private static final boolean WRAP_EVERYTHING = false;

        private final LambdaExpr e1;
        private final LambdaExpr e2;

        public Application(LambdaExpr e1, LambdaExpr e2)
        {
            this.e1 = e1;
            this.e2 = e2;
        }

        public LambdaExpr getE1() {
            return e1;
        }

        public LambdaExpr getE2() {
            return e2;
        }

        @Override
        public <R> R accept(Visitor<R> v) { return v.visit(this); }

        @Override
        public boolean isAlphaEquivTo(LambdaExpr expr) {
            var a = asApp(expr);
            return a != null && getE1().isAlphaEquivTo(a.getE1()) && getE2().isAlphaEquivTo(a.getE2());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Application other))
                return false;
            return other.e1.equals(e1) && other.e2.equals(e2);
        }

        private String wrapExp(LambdaExpr e) {
            if (WRAP_EVERYTHING)
                return "(" + e + ")";
            else
                return e instanceof Variable ? e.toString() : "(" + e + ")";
        }

        @Override
        public String toString() {
            return wrapExp(e1) + wrapExp(e2);
        }
    }
}
