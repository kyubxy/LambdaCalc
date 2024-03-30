package com.kyubey.compilation;

import com.kyubey.lambda.LambdaExpr;

import java.util.List;

public abstract class ILTree {
    public interface Visitor<T> {
        T visit(PureLc t);
        T visit(Dependant t);
    }

    public abstract <R> R accept(Visitor<R> v);

    public static class PureLc extends ILTree {
        LambdaExpr expr;

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visit(this);
        }
    }

    public static class Dependant extends ILTree {
        List<ILTree> children;

        @Override
        public <R> R accept(Visitor<R> v) {
            return v.visit(this);
        }
    }
}
