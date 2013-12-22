package ambroscum.expressions;

import ambroscum.*;
import java.util.*;
import ambroscum.values.Value;
import ambroscum.values.BooleanValue;

public class ExpressionTernary extends Expression
{
	private Expression cond;
	private Expression expr1;
	private Expression expr2;
	
	public ExpressionTernary(Expression cond, Expression expr1, Expression expr2)
	{
		this.cond = cond;
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		if (((BooleanValue) cond.evaluate(values)).getValue())
			return expr1.evaluate(values);
		else
			return expr2.evaluate(values);
	}
	
	@Override
	public String toString()
	{
		return "(" + cond + " ? " + expr1 + " : " + expr2 + ")";
	}
}