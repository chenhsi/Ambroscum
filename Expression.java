package ambroscum;

import ambroscum.values.*;

public abstract class Expression
{
	public static Expression interpret(String rawcode)
	{
		if (rawcode.equals("True"))
			return new ExpressionLiteral(new Value()); // should actually fill this out
		if (rawcode.equals("False"))
			return new ExpressionLiteral(new Value()); // should actually fill this out
	}
	
	public abstract Value evaluate(IdentifierMap values);
}
