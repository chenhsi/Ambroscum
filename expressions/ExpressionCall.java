package ambroscum.expressions;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.SyntaxError;
import ambroscum.values.Value;
import ambroscum.values.FunctionDeclaration;

public class ExpressionCall extends Expression
{
	private Expression func;
	private List<Expression> operands;
	
	public ExpressionCall(Expression function, Expression... eachOperand)
	{
		func = function;
		operands = new LinkedList<Expression> ();
		for (Expression expr : eachOperand)
			operands.add(expr);
	}
	
	public ExpressionCall(Expression function, TokenStream stream)
	{
		func = function;
		operands = new LinkedList<Expression> ();
		boolean first = true;
		while (true)
		{
			Token next = stream.getFirst();
			if (next.toString().equals(")"))
			{
				stream.removeFirst();
				break;
			}
			if (next == Token.NEWLINE)
				throw new SyntaxError("Unexpected end of line");
			if (first)
			{
				if (next == Token.COMMA)
					throw new SyntaxError("Unexpected delimiter in a print statement");
				operands.add(Expression.interpret(stream));
				first = false;
			}
			else
			{
				if (next != Token.COMMA)
					throw new SyntaxError("Unexpected token: " + next);
				stream.removeFirst();
				if (stream.getFirst().toString().equals(")"))
					throw new SyntaxError("Call ending with a comma");
				operands.add(Expression.interpret(stream));
			}
		}
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		List<Value> eval = new LinkedList<Value> ();
		for (Expression expr : operands)
			eval.add(expr.evaluate(values));
		Value f = func.evaluate(values);
		if (!(f instanceof FunctionDeclaration))
			throw new FunctionNotFoundException(func + " does not evaluate to a function");
		Value v = ((FunctionDeclaration) f).evaluate(eval, values);
		return v;
	}
	
	@Override
	public String toString()
	{
		return "(" + func + " " + operands.toString() + ")";
	}
}