package ambroscum.expressions;

import java.util.Arrays;
import java.util.Stack;
import java.util.Map;
import ambroscum.IdentifierMap;
import ambroscum.values.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.lines.Line;
import ambroscum.expressions.ExpressionOperator;

public abstract class Expression
{
	private static int expressionCounter = 0;
	private final int expressionID;
	
	protected Expression()
	{
		expressionID = ++expressionCounter;
	}
	
	public int getID()
	{
		return expressionID;
	}
	
	public Expression localOptimize()
	{
		return this;
	}
	
	// ultimately, this should be overwritten by every subclass, and thus be abstract
	public void setDeclarations(Map<String, Expression> declarations)
	{
		throw new UnsupportedOperationException();
	}
	
	public abstract Value evaluate(IdentifierMap values);

//	do we not support ((3)) yet?
	public static Expression interpret(TokenStream stream)
	{
		Expression result = helperGetToken(stream);
		Stack<Expression> expressions = new Stack<Expression> ();
		Stack<ExpressionOperator> operators = new Stack<ExpressionOperator> ();
		expressions.push(result);
		while (FunctionOperator.isOperator(stream.getFirst()))
		{
			ExpressionOperator op = new ExpressionOperator(stream.removeFirst().toString());
			result = expressions.pop();
			while (expressions.size() > 0 && op.getPriority() >= operators.peek().getPriority())
				result = new ExpressionCall(operators.pop(), expressions.pop(), result);
			expressions.push(result);
			operators.push(op);
			expressions.push(helperGetToken(stream));
		}
		result = expressions.pop();
		while (expressions.size() > 0)
			result = new ExpressionCall(operators.pop(), expressions.pop(), result);
		if (stream.getFirst().toString().equals("?")) // nested ternary operators (without using parens) not supported
		{
			stream.removeFirst();
			Expression expr1 = extend(singleExpression(stream), stream);
			if (stream.removeFirst() != Token.COLON)
				throw new SyntaxError("Expected colon for ternary operator");
			Expression expr2 = extend(singleExpression(stream), stream);
			result = new ExpressionTernary(result, expr1, expr2);
		}
		return result;
	}
	
	private static Expression helperGetToken(TokenStream stream)
	{
		Expression expr = extend(singleExpression(stream), stream);
		Token token = stream.getFirst();
		if (token.toString().equals("++") || token.toString().equals("--"))
		{
			expr = new ExpressionIncrement(expr, token.toString().charAt(0) == '+', false);
			stream.removeFirst();
		}
		return expr;
	}
	
	public static Expression extend(Expression expr, TokenStream stream)
	{
		String nextToken = stream.getFirst().toString();
		while (nextToken.equals(".") || nextToken.equals("(") || nextToken.equals("["))
		{
			if (nextToken.equals("."))
			{
				stream.removeFirst();
				expr = new ExpressionIdentifier(expr, stream.removeFirst());
			}
			else if (nextToken.equals("("))
			{
				stream.removeFirst();
				expr = new ExpressionCall(expr, stream);
			}
			else if (nextToken.equals("["))
			{
				stream.removeFirst();
				expr = new ExpressionReference(expr, Expression.interpret(stream));
				Token temp = stream.removeFirst();
				if (!temp.toString().equals("]"))
					throw new SyntaxError("Expecting close bracket, found " + temp);
			}
			nextToken = stream.getFirst().toString();
		}
		return expr;
	}
	
	public static Expression singleExpression(TokenStream stream)
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
		else if (FunctionOperator.isOperator(token))
		{
			ExpressionOperator op = new ExpressionOperator(token.toString());
			if (op.toString().equals("-") || op.getNumOperands() == 1)
				result = new ExpressionCall(op, extend(singleExpression(stream), stream));
			else
				throw new SyntaxError(op + " cannot take only 1 operand");
		}
		else if (token.toString().equals("++") || token.toString().equals("--"))
		{
			Expression operand = extend(singleExpression(stream), stream);
			result = new ExpressionIncrement(operand, token.toString().charAt(0) == '+', true);
		}
		else if (IdentifierMap.isValidIdentifier(token.toString())) // is a reference
			result = new ExpressionIdentifier(token);
		else if (token.toString().equals("("))
		{
			result = Expression.interpret(stream);
			if (!")".equals(stream.removeFirst().toString()))
				throw new SyntaxError("Missing close parenthesis after expression");
		}
		else if (token.toString().equals("["))
			result = new ExpressionList(token, stream);
		else if (token.toString().equals("{"))
			result = new ExpressionDict(token, stream);
		else
			throw new SyntaxError("not recognized token: " + token);
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
				if (!(Character.isDigit(c) || (c >= 'a' && c <= 'f')))
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
}