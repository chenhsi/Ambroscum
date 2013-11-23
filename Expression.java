package ambroscum;

import java.util.Arrays;
import ambroscum.values.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;

public abstract class Expression
{
	public abstract Value evaluate(IdentifierMap values);

	// need to implement unary operators and priority (* over +)
	public static Expression interpret(TokenStream stream)
	{
		Token token = stream.removeFirst();
		Expression result = null;
		if (token == Token.NEWLINE)
			throw new SyntaxError("Expression expected");
		if (token.toString().equals("True"))
			result = new ExpressionLiteral(BooleanLiteral.TRUE);
		else if (token.toString().equals("False"))
			result = new ExpressionLiteral(BooleanLiteral.FALSE);
		else if (isNumber(token.toString()))
			result = parseNum(token.toString());
		else if (isString(token.toString()))
			result = parseString(token.toString());
		else if (IdentifierMap.isValidIdentifier(token.toString())) // is a reference
		{
			result = new ExpressionReference(token, stream);
			if (stream.getFirst().toString().equals("("))
			{
				stream.removeFirst();
				result = new ExpressionCall(result, stream);
			}
		} else if (token.toString().equals("[")) {
			result = new ExpressionList(token, stream);
		}
		Token next = stream.getFirst();
		if (isOperator(next))
		{
			stream.removeFirst();
			result = new ExpressionCall(new ExpressionOperator(next), result, stream);
		}

		if (false) // has operator/function
			throw new UnsupportedOperationException();
		
		return result;
	}

	private static boolean isNumber(String text)
	{
		if (text.charAt(0) == '-')
			text = text.substring(1);
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
		return text.charAt(0) == '"';
	}

	private static ExpressionLiteral parseString(String text)
	{
		return new ExpressionLiteral(new StringLiteral(text));
	}
	
	private static final String[] OPERATOR_LIST = new String[] {"+", "-", "*", "/", "%", "and", "or"};
	static
	{
		Arrays.sort(OPERATOR_LIST);
	}
	private static boolean isOperator(Token t)
	{
		return Arrays.binarySearch(OPERATOR_LIST, t.toString()) >= 0;
	}
}