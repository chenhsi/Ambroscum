package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.ExpressionCall;

public class CallLine extends Line
{
	private final Expression expr;
	
	@Override
	public boolean expectsBlock() {return false; }
	@Override
	public void setBlock(Block b) {}
	
	CallLine(TokenStream stream)
	{
		expr = Expression.interpret(stream);
		if (!(expr instanceof ExpressionCall))
			throw new SyntaxError("Single expression not a valid line: " + expr);
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after call: " + temp);
	}
	
	@Override
	public void evaluate(IdentifierMap values)
	{
		expr.evaluate(values);
	}
	
	@Override
	public String toString()
	{
		return "(call " + expr + ")";
	}
}