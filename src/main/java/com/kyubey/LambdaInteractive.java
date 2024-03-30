package com.kyubey;

import com.kyubey.app.interactive.InteractiveRuntime;
import com.kyubey.lambda.Evaluator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.FileInputStream;

public class LambdaInteractive {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("lambda").build()
                .description("An interactive evaluator for the simple lambda calculus")
                .version("${lambda} "+ Properties.VERSION);
        parser.addArgument("-s", "--strategy")
                .dest("strategy")
                .setDefault(Evaluator.Strategy.LAZY)
                .action(Arguments.store())
                .help("the evaluation strategy to use, defaults to lazy evaluation");
        parser.addArgument("-v", "--verbose")
                .dest("verbose")
                .setDefault(false)
                .action(Arguments.storeTrue())
                .help("outlines every line of the evaluation before returning the normal form");

        Namespace res = null;
        try {
            res = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }

        assert res != null;

        InteractiveRuntime irt = new InteractiveRuntime(res.get("strategy"), res.get("verbose"));
        irt.run();
    }
}
