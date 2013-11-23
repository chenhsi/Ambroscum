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
		if (stream.getFirst() != Token.NEWLINE)
			expr = Expression.interpret(stream);
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after return: " + temp);
	}
	
	@Override
	public void evaluate(IdentifierMap values)
	{
		// need to deal with null returns
		value = expr.evaluate(values);
	}
	
	public Value getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(return ");
		if (expr != null)
			sb.append(expr);
		return sb.append(")").toString();
	}
}