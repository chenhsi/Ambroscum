package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.AssertionError;
import ambroscum.error.SyntaxError;
import ambroscum.values.BooleanLiteral;

public class AssertLine extends Line
{
	private Expression test, errorMessage;

	AssertLine(TokenStream stream)
	{
		test = Expression.interpret(stream);
		Token token = stream.removeFirst();
		if (token != Token.NEWLINE)
		{
			if (!token.toString().equals(":"))
				throw new SyntaxError("Expecting ':' token in assert line");
			errorMessage = Expression.interpret(stream);
			Token temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token in assert line: " + temp);
		}
	}

	public void evaluate(IdentifierMap values)
	{
		Value testVal = test.evaluate(values);
		if (!(testVal.equals(BooleanLiteral.TRUE)))
		{
			if (errorMessage == null)
				throw new AssertionError(errorMessage.evaluate(values).toString());
			else
				throw new AssertionError("");
		}
	}
}