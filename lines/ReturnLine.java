package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;

public class ReturnLine extends Line
{
	private Expression expr;
	private Value value;
	
	ReturnLine(TokenStream stream)
	{
		expr = Expression.interpret(stream);
		if (stream.getFirst() != Token.NEWLINE)
			throw new SyntaxError("Extra Expression");
	}
	
	public void evaluate(IdentifierMap values)
	{
		value = expr.evaluate(values);
	}
	
	public Value getValue()
	{
		return value;
	}
}