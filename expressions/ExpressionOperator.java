package ambroscum.expressions;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.Token;
import ambroscum.values.Value;
import ambroscum.values.FunctionOperator;

public class ExpressionOperator extends Expression
{
	private FunctionOperator op;
	
	public ExpressionOperator(String str)
	{
		op = FunctionOperator.get(str.toString());
	}
	
	public int getPriority()
	{
		return op.getPriority();
	}
	
	public int getNumOperands()
	{
		return op.getNumOperands();
	}
	
	public FunctionOperator getValue()
	{
		return op;
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
	
	@Override
	public boolean hasSideEffects()
	{
		return false;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations) {}
}