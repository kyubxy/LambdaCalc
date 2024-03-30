import com.kyubey.lambda.Evaluator;
import com.kyubey.lambda.LambdaExpr;
import com.kyubey.lambda.parser.LambdaParser;
import com.kyubey.lambda.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvaluationTest {
    private LambdaExpr parse(String rstr) {
        LambdaParser parser = LambdaParser.fromStr(rstr, true);
        try {
            return parser.start();
        } catch (ParseException e) {
            throw new RuntimeException("Parse error in the tests, " + rstr + " is not valid lambda calculus");
        }
    }

    @Test
    public void testOneStepReduceVariable() {
        var exp = parse("x");
        var next = exp.accept(new Evaluator());
        assertEquals(exp, next);
    }

    @Test
    public void testOneStepReduceAbstraction() {
        var exp = parse("/x.x");
        var next = exp.accept(new Evaluator());
        assertEquals(exp, next);
    }

    @Test
    public void testOneStepReduceApplication() {
        var exp = parse("xy");
        var next = exp.accept(new Evaluator());
        assertEquals(exp, next);
    }

    @Test
    public void testBasicRedex() {
        var start = parse("(/x.x)y");
        var next = start.accept(new Evaluator());
        var exp = parse("y");
        assertEquals(exp, next);
    }

    @Test
    public void testBasicRedexSameE2() {
        var start = parse("(/x.x)x");
        var next = start.accept(new Evaluator());
        var exp = parse("x");
        assertEquals(exp, next);
    }

    @Test
    public void testFreeVariable() {
        var start = parse("(/x.xy)z");
        var next = start.accept(new Evaluator());
        var exp = parse("zy");
        assertEquals(exp, next);
    }

    @Test
    public void testVariableCapture() {
        // the inner \x.x can be alpha converted to say \k.k, which would not be replaced
        // at the y redex
        var actualstart = parse("(/x.(/x.x))y");
        var expectnext = parse("/x.x");

        var actualnext = actualstart.accept(new Evaluator());
        assertEquals(expectnext, actualnext);
    }

    @Test
    public void testEagerEvalSimple() {
        // example taken from https://comp1100-pal.github.io/worksheets/2020/05/03/lambda-calculus-complete
        String actualStart = "(/x.xx)((/y.zy)w)";
        String[] expectedSteps = new String[]{
                "(/x.xx)(zw)",
                "(zw)(zw)"
        };

        var eval = new Evaluator(Evaluator.Strategy.STRATEGY_EAGER);

        var as = parse(actualStart);
        var an = as.accept(eval);

        for (var expstep : expectedSteps) {
            var en = parse(expstep);
            assertEquals(en, an);
            an = eval.oneStep(an);
        }
    }

    @Test
    public void testLazyEvalSimple() {
        // example taken from https://comp1100-pal.github.io/worksheets/2020/05/03/lambda-calculus-complete
        // (and corrected slightly)
        String actualStart = "(/x.xx)((/y.zy)w)";
        String[] expectedSteps = new String[]{
                "((/y.zy)w)((/y.zy)w)",
                "(zw)(zw)"
        };

        var eval = new Evaluator(Evaluator.Strategy.STRATEGY_LAZY);

        var as = parse(actualStart);
        var an = as.accept(eval);

        for (var expstep : expectedSteps) {
            var en = parse(expstep);
            assertEquals(en, an);
            an = eval.oneStep(an);
        }
    }

    @Test
    public void testNormal() {
        String actualStart = "(/x.xx)((/y.zy)w)";
        String expectedNorm = "(zw)(zw)";
        var actualNorm = new Evaluator().toNormal(parse(actualStart));
        assertEquals(parse(expectedNorm), actualNorm);
    }

    private static Stream<Arguments> churchRosserTestInputs() {
        return Stream.of(
                  Arguments.of("(/x.xx)((/y.zy)w)", "(zw)(zw)")
                , Arguments.of("(/x./y.yxy)(xx)z", "z(xx)z")
                , Arguments.of("(/x./y.x)(/x./y.y)w", "/x./y.y")
                , Arguments.of("(/x.xxx)(/y.yw)", "ww(/y.yw)")
        );
    }

    @ParameterizedTest
    @MethodSource("churchRosserTestInputs")
    public void testChurchRosser(String actualStart, String expectedNorm) {
        var eager = new Evaluator(Evaluator.Strategy.STRATEGY_EAGER);
        var lazy = new Evaluator(Evaluator.Strategy.STRATEGY_LAZY);
        var eagerNorm = eager.toNormal(parse(actualStart));
        var lazyNorm = lazy.toNormal(parse(actualStart));
        assertEquals(parse(expectedNorm), eagerNorm, "eagerNorm");
        assertEquals(parse(expectedNorm), lazyNorm, "lazyNorm");
        // => eager == lazy
    }

    @Test
    public void testYComb() {
        var Y  = parse("/f.(/x.f(xx))(/x.f(xx))");
        var Yp = new Evaluator().toNormal(Y, 10);
        assertEquals(Y, Yp);
    }
}
