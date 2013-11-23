package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionLiteral extends Expression
{
	private Value fixed;
	
	public ExpressionLiteral(Value value)
	{
		fixed = value;
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
}