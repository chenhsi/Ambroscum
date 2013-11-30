package ambroscum.expressions;

import java.util.Arrays;
import java.util.Stack;
import ambroscum.IdentifierMap;
import ambroscum.values.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.ExpressionOperator;

public abstract class Expression
{
	public abstract Value evaluate(IdentifierMap values);

	public static Expression interpret(TokenStream stream)
	{
		Expression result = greedy(stream);
		Stack<Expression> expressions = new Stack<Expression> ();
		Stack<ExpressionOperator> operators = new Stack<ExpressionOperator> ();
		expressions.push(result);
		while (isOperator(stream.getFirst()))
		{
			ExpressionOperator op = new ExpressionOperator(stream.removeFirst());
			result = expressions.pop();
			while (expressions.size() > 0 && op.getPriority() >= operators.peek().getPriority())
				result = new ExpressionCall(operators.pop(), expressions.pop(), result);
			expressions.push(result);
			operators.push(op);
			expressions.push(greedy(stream));
		}
		if (expressions.size() == 1)
			return result;
		result = expressions.pop();
		while (expressions.size() > 0)
			result = new ExpressionCall(operators.pop(), expressions.pop(), result);
		return result;
	}
	
	private static Expression greedy(TokenStream stream)
	{
		Token token = stream.removeFirst();
		Expression result = null;
		if (token == Token.NEWLINE)
			throw new SyntaxError("Expression expected");
		if (token.toString().equals("true"))
			result = new ExpressionLiteral(BooleanValue.TRUE);
		else if (token.toString().equals("false"))
			result = new ExpressionLiteral(BooleanValue.FALSE);
		else if (isNumber(token.toString()))
			result = parseNum(token.toString());
		else if (isString(token.toString()))
			result = parseString(token.toString());
		else if (IdentifierMap.isValidIdentifier(token.toString())) // is a reference
		{
			result = ExpressionReference.createExpressionReference(token, stream);
			if (stream.getFirst().toString().equals("("))
			{
				stream.removeFirst();
				result = new ExpressionCall(result, stream);
			}
		}
		else if (token.toString().equals("["))
		{
			result = new ExpressionList(token, stream);
			result = ExpressionReference.createExpressionReference(result, stream);
		}
		else if (token.toString().equals("("))
		{
			result = Expression.interpret(stream);
			if (!")".equals(stream.getFirst().toString())) {
				throw new SyntaxError("Expected \")\" at end of expression");
			}
			stream.removeFirst();
		}
//		else if (token.toString().equals("++") || token.toString().equals("--"))
//			result = new ExpressionCall(token.toString() + "_", greedy(stream));
		else if (isOperator(token))
		{
			ExpressionOperator op = new ExpressionOperator(token);
			if (op.toString().equals("-") || op.getNumOperands() == 1)
				result = new ExpressionCall(op, greedy(stream));
			else
				throw new SyntaxError(op + " cannot take only 1 operand");
		}
		else
			throw new SyntaxError("not recognized token: " + token);
		if (result == null)
			throw new AssertionError("sigh, qq expression");
		return result;
	}

	private static boolean isNumber(String text)
	{
		text = text.toLowerCase();
		if (text.length() >= 2 && text.charAt(0) == '0' && text.charAt(1) == 'b')
		{
			text = text.substring(2);
			for (char c : text.toCharArray())
				if (c != '0' && c != '1')
					return false;
		}
		else if (text.length() >= 2 && text.charAt(0) == '0' && text.charAt(1) == 'x')
		{
			text = text.substring(2);
			for (char c : text.toCharArray())
				if (!(Character.isDigit(c) || c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e' || c == 'f'))
					return false;
		}
		else
		{
			for (char c : text.toCharArray())
				if (!Character.isDigit(c))
					return false;
		}
		return true;
	}

	private static ExpressionLiteral parseNum(String text)
	{
		text = text.toLowerCase();
		int value;
		if (text.length() >= 2 && text.charAt(0) == '0' && text.charAt(1) == 'b')
			value = Integer.parseInt(text.substring(2), 2);
		else if (text.length() >= 2 && text.charAt(0) == '0' && text.charAt(1) == 'x')
			value = Integer.parseInt(text.substring(2), 16);
		else
			value = Integer.parseInt(text, 10);
		return new ExpressionLiteral(IntValue.fromInt(value));
	}

	// needs to deal with escape characters + unicode characters
	private static boolean isString(String text)
	{
		return text.charAt(0) == '"';
	}

	private static ExpressionLiteral parseString(String text)
	{
		return new ExpressionLiteral(new StringValue(text.substring(1, text.length() - 1)));
	}
	
	private static boolean isOperator(Token t)
	{
		return FunctionOperator.get(t.toString()) != null;
	}
}