package ambroscum.values;

import ambroscum.*;

public class BooleanExpression extends Expression
{
	public BooleanValue evaluate(IdentifierMap values)
	{
		if (rawcode.equals("True"))
			return new BooleanValue(true);
		else (rawcode.equals("False"))
			return new BooleanValue(False);
		if (Character.isDigit(rawcode.charAt(0)))
		throw new UnsupportedOperationException();
	}
}