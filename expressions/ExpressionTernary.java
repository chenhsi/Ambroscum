package ambroscum.expressions;

import ambroscum.*;
import java.util.*;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.OptimizedException;
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
		// this is throwing SyntaxError because that is what if lines throw,
		// but I'm not sure I approve
		Value evaluated = cond.evaluate(values);
		if (!(evaluated instanceof BooleanValue))
			throw new SyntaxError("Expected a boolean for ternary expression condition: " + evaluated);
		if (((BooleanValue) evaluated).getValue())
			return expr1.evaluate(values);
		else
			return expr2.evaluate(values);
	}
	
	@Override
	public String toString()
	{
		return "(" + cond + " ? " + expr1 + " : " + expr2 + ")";
	}
	
	public Expression getCond()
	{
		return cond;
	}
	public Expression getTrueCase()
	{
		return expr1;
	}
	public Expression getFalseCase()
	{
		return expr2;
	}
	
	public Expression localOptimize()
	{
		cond = cond.localOptimize();
		if (cond instanceof ExpressionLiteral)
		{
			Value condValue = ((ExpressionLiteral) cond).getValue();
			if (condValue instanceof BooleanValue)
			{
				Expression always = ((BooleanValue) condValue).getValue() ? expr1 : expr2;
				always = always.localOptimize();
				return always;
			}
			else
				throw new OptimizedException(new SyntaxError("Expected a boolean for ternary expression condition: " + condValue));
		}
		expr1 = expr1.localOptimize();
		expr2 = expr2.localOptimize();
		return this;
	}
}