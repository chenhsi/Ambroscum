package ambroscum;

import ambroscum.values.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;

public abstract class Expression
{
	public abstract Value evaluate(IdentifierMap values);

	public static Expression interpret(TokenStream stream)
	{
		Token token = stream.removeFirst();
		if (token.toString().equals("True"))
			return new ExpressionLiteral(BooleanLiteral.TRUE);
		if (token.toString().equals("False"))
			return new ExpressionLiteral(BooleanLiteral.FALSE);
		if (isNumber(token.toString()))
			return parseNum(token.toString());
		if (isString(token.toString()))
			return parseString(token.toString());
		if (false) // is a reference
			throw new UnsupportedOperationException();
		if (false) // has operator/function
		{
		}
		if (ExpressionReference.isValidReference(token.toString())) // is a reference
			return new ExpressionReference(null); //token.toString())
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