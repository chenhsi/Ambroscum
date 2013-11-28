package ambroscum.expressions;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.Token;
import ambroscum.values.Value;
import ambroscum.values.FunctionOperator;

public class ExpressionOperator extends Expression
{
	private FunctionOperator op;
	
	public ExpressionOperator(Token token)
	{
		op = FunctionOperator.get(token.toString());
	}
	
	public int getPriority()
	{
		return op.getPriority();
	}
	
	public int getNumOperands()
	{
		return op.getNumOperands();
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		return op;
	}
	
	@Override
	public String toString()
	{
		return op.toString();
	}
}