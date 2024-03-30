package com.kyubey.compilation;


import com.kyubey.lambda.LambdaExpr;

import java.util.List;
import java.util.Map;

public class Compiler {
    String input;
    public Compiler(String input) {
        this.input = input;
    }


    public LambdaExpr compile() throws Semant.SemanticException {
        // TODO: parsing
        ILTree tree = new ILTree.Dependant();

        // semantic analysis
        var exception = tree.accept(new Semant());
        if (exception != null) throw exception;

        // linking
        var linker = new Linker(tree);
        return linker.resolve();
    }
}
