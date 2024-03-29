import com.kyubey.lambda.LambdaExpr;
import com.kyubey.lambda.parser.LambdaParser;
import com.kyubey.lambda.parser.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private LambdaExpr parse(String rstr) throws ParseException {
        LambdaParser parser = LambdaParser.fromStr(rstr, true);
        return parser.start();
    }

    @Test
    public void testEmpty() throws ParseException {
        var actual = parse("");
        assertNull(actual);
    }

    @Test
    public void testVariable() throws ParseException {
        var actual = parse("x");
        var expected = new LambdaExpr.Variable("x");
        assertEquals(expected, actual);
    }

    @Test
    public void testAbstraction() throws ParseException {
        var actual = parse("/x.x");
        var expected = new LambdaExpr.Abstract("x", new LambdaExpr.Variable("x"));
        assertEquals(expected, actual);
    }

    @Test
    public void testImplicitNestedAbstraction() throws ParseException {
        var actual = parse("/x.y /y.x");
        var expected = new LambdaExpr.Abstract("x",
                new LambdaExpr.Application(
                        new LambdaExpr.Variable("y"),
                        new LambdaExpr.Abstract("y",
                                new LambdaExpr.Variable("x"))));
        assertEquals(expected, actual);
    }

    @Test
    public void testChurchBoolean() throws ParseException {
        var actual = parse("/x./y.x");
        var expected = new LambdaExpr.Abstract("x",
                        new LambdaExpr.Abstract("y", new LambdaExpr.Variable("x")));
        assertEquals(expected, actual);
    }

    @Test
    public void testApplicationBasic() throws ParseException {
        var actual = parse("xy");
        var expected = new LambdaExpr.Application(new LambdaExpr.Variable("x"), new LambdaExpr.Variable("y"));
        assertEquals(expected, actual);
    }

    @Test
    public void testImplicitApplicationAssociativity() throws ParseException {
        // application should be left-associative
        var actual = parse("xyz");
        var expected = new LambdaExpr.Application(new LambdaExpr.Application(new LambdaExpr.Variable("x"),
                new LambdaExpr.Variable("y")), new LambdaExpr.Variable("z"));
        assertEquals(expected, actual);
    }

    @Test
    public void testExplicitApplicationAssociativity() throws ParseException {
        var actual = parse("x(yz)");
        var expected = new LambdaExpr.Application(new LambdaExpr.Variable("x"),
                new LambdaExpr.Application(new LambdaExpr.Variable("y"), new LambdaExpr.Variable("z")));
        assertEquals(expected, actual);
    }

    @Test
    public void testMultipleBrackets() throws ParseException {
        var actual = parse("((xyz))");
        var expected = new LambdaExpr.Application(new LambdaExpr.Application(new LambdaExpr.Variable("x"),
                new LambdaExpr.Variable("y")), new LambdaExpr.Variable("z"));
        assertEquals(expected, actual);
    }

    @Test
    public void testMismatch() {
        String[] programs = new String[] {
                "(()", "())", "(", ")"
        };

        for(var p : programs)
            assertThrows(ParseException.class, () -> parse(p));
    }

    @Test
    public void testEmptyBracketed() {
        String program = "()";
        assertThrows(ParseException.class, () -> parse(program));
    }


    @Test
    public void testTwoDots() {
        String program = "..";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testTwoLambdas() {
        String program = "//";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testNoBodyInAbstraction() {
        String program = "/xxy.";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testSingleLambda() {
        String program = "/";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testSingleDot() {
        String program = ".";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testMissingLParen() {
        String program = "xy)";
        assertThrows(ParseException.class, () -> parse(program));
    }

    @Test
    public void testMissingRParen() {
        String program = "(xy";
        assertThrows(ParseException.class, () -> parse(program));
    }
}
