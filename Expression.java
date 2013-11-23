package ambroscum;

import ambroscum.values.*;

public abstract class Expression
{
	public abstract Value evaluate(IdentifierMap values);

	public static Expression interpret(String rawcode)
	{
		if (rawcode.equals("True"))
			return new ExpressionLiteral(Value.TRUE);
		if (rawcode.equals("False"))
			throw new UnsupportedOperationException();
		if (isNumber(rawcode)) // is digit
			return parseNum(rawcode);
		if (false) // is a reference
			throw new UnsupportedOperationException();
		if (false) // has operator/function
			throw new UnsupportedOperationException();
		else // can't actually think of any other cases
			throw new UnsupportedOperationException();
	}
	
	private static boolean isNumber(String text)
	{
		for (char c : text.toCharArray())
			if (!Character.isDigit(c))
				return false;
		return true;
	}
	
	private static ExpressionLiteral parseNum(String text)
	{
		return new ExpressionLiteral(new IntLiteral(Integer.parseInt(text)));
	}
}