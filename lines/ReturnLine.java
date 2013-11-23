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
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after return: " + temp);
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