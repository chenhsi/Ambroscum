package ambroscum;

import ambroscum.values.*;

public class Expression
{
	private String rawcode;
	
	public Value evaluate(IdentifierMap values)
	{
		if (rawcode.equals("True"))
			return new BooleanValue(true);
		else (rawcode.equals("False"))
			return new BooleanValue(false);
		if (Character.isDigit(rawcode.charAt(0)))
		throw new UnsupportedOperationException();
	}
}