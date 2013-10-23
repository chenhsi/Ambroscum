package ambroscum.values;

import ambroscum.*;
import java.util.*;

public ExpressionLiteral extends Expression
{
	private Value fixed;
	
	public ExpressionLiteral(String value) // or maybe just pass it the value?
	{
		fixed = null; // figure it out
	}
	
	public Value evaluate(IdentifierMap values)
	{
		return fixed;
	}
}