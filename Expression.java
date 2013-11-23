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
		if (isNumber(rawcode))
			return parseNum(rawcode);
		if (isString(rawcode))
			return parseString(rawcode);
		if (false) // is a reference
			throw new UnsupportedOperationException();
		if (true) // has operator/function
		{
			char[] array = rawcode.toCharArray();
			for (int i = array.length - 1; i >= 0; i--)
			{
				if (array[i] == ' ')
				{
					
				}
			}
		}
		// can't actually think of any other cases
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
	
	// needs to deal with escape characters + unicode characters
	private static boolean isString(String text)
	{
		return text.charAt(0) == '"' && text.indexOf('"', 1) == text.length() - 1;
	}
	
	private static ExpressionLiteral parseString(String text)
	{
		return new ExpressionLiteral(new StringLiteral(text));
	}
}