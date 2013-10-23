package ambroscum;

import ambroscum.values.*;

public abstract class Expression
{
	public static Expression interpret(String rawcode)
	{
		if (rawcode.equals("True"))
			return new ExpressionLiteral(Value.TRUE);
		if (rawcode.equals("False"))
			throw new UnsupportedOperationException();
		if (false) // is digit
			throw new UnsupportedOperationException();
		if (false) // is a reference
			throw new UnsupportedOperationException();
		if (false) // has operator/function
			throw new UnsupportedOperationException();
		else // can't actually think of any other cases
			throw new UnsupportedOperationException();
	}
	
	public abstract Value evaluate(IdentifierMap values);
}
