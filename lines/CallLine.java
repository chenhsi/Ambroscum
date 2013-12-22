package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionCall;

public class CallLine extends Line
{
	private final Expression expr;
	
	CallLine(Line parent, TokenStream stream)
	{
		super(parent);
		expr = Expression.interpret(stream);
		if (!(expr instanceof ExpressionCall))
			throw new SyntaxError("Single expression not a valid line: " + expr);
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after call: " + temp);
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		expr.evaluate(values);
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(call " + expr + ")";
	}
}