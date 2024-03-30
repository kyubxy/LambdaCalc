package com.kyubey.compilation;

public class Semant implements ILTree.Visitor<Semant.SemanticException> {
    @Override
    public SemanticException visit(ILTree.PureLc t) {
        return null;
    }

    @Override
    public SemanticException visit(ILTree.Dependant t) {
        return null;
    }

    public static class SemanticException extends Exception {
        public SemanticException(String msg) {
            super(msg);
        }
    }
}
