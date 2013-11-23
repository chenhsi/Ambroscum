package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.FunctionNotFoundException;

public class ExpressionCall extends Expression
{
	private Expression func;
	private List<Expression> operands;
	
	public ExpressionCall(Expression function, Expression firstOperand, TokenStream stream)
	{
		func = function;
		operands = new LinkedList<Expression> ();
		operands.add(firstOperand);
		operands.add(Expression.interpret(stream));
	}
	
	public ExpressionCall(Expression function, TokenStream stream)
	{
		func = function;
		operands = new LinkedList<Expression> ();
		while (true)
		{
			Token next = stream.getFirst();
			if (next.toString().equals(")"))
			{
				stream.removeFirst();
				break;
			}
			operands.add(Expression.interpret(stream));
		}
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		List<Value> eval = new LinkedList<Value> ();
		for (Expression expr : operands)
			eval.add(expr.evaluate(values));
		Value f = func.evaluate(values);
		if (!(f instanceof Function))
			throw new FunctionNotFoundException(func + " does not evaluate to a function");
		return ((Function) f).evaluate(eval, values);
	}
	
	@Override
	public String toString()
	{
		return "(" + func + " " + operands.toString() + ")";
	}
}