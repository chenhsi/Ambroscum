package ambroscum.lines;

import java.util.Map;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;
import ambroscum.values.NullValue;
import ambroscum.expressions.Expression;

public class ReturnLine extends Line
{
	private Expression expr;
	
	ReturnLine(Line parent, TokenStream stream)
	{
		super(parent);
		if (stream.getFirst() != Token.NEWLINE)
			expr = Expression.interpret(stream);
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after return: " + temp);
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		if (expr == null)
			setReturnValue(NullValue.NULL);
		else
			setReturnValue(expr.evaluate(values));
		return Block.ExitStatus.RETURN;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(return ");
		if (expr != null)
			sb.append(expr);
		return sb.append(")").toString();
	}
	
	@Override
	public Line localOptimize()
	{
		expr = expr.localOptimize();
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		expr.setDeclarations(declarations);
	}
}