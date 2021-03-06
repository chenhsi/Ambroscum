package ambroscum.expressions;

import ambroscum.*;
import java.util.*;
import ambroscum.values.Value;

public class ExpressionLiteral extends Expression
{
	private Value fixed;
	
	public ExpressionLiteral(Value value)
	{
		fixed = value;
	}
	
	public Value getValue()
	{
		return fixed;
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		return fixed;
	}
	
	@Override
	public String toString()
	{
		return fixed.toString();
	}

	@Override
	public boolean hasSideEffects()
	{
		return false;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations) {}
}