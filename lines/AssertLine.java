package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.AssertionError;
import ambroscum.errors.SyntaxError;
import ambroscum.values.BooleanValue;
import ambroscum.values.Value;
import ambroscum.expressions.Expression;

public class AssertLine extends Line
{
	private Expression test, errorsMessage;

	AssertLine(TokenStream stream)
	{
		test = Expression.interpret(stream);
		Token token = stream.removeFirst();
		if (token != Token.NEWLINE)
		{
			if (!token.toString().equals(":"))
				throw new SyntaxError("Expecting ':' token in assert line");
			errorsMessage = Expression.interpret(stream);
			Token temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token in assert line: " + temp);
		}
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		Value testVal = test.evaluate(values);
		if (!(testVal.equals(BooleanValue.TRUE)))
		{
			if (errorsMessage == null)
				throw new AssertionError("Assertion failed" + ": " + errorsMessage.evaluate(values).toString());
			else
				throw new AssertionError("Assertion failed");
		}
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(assert ").append(test.toString());
		if (errorsMessage != null)
			sb.append(" : ").append(errorsMessage.toString());
		return sb.append(")").toString();
	}
}