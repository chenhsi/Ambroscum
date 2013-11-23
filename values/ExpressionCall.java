package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;

public class ExpressionCall extends Expression
{
	private Expression func; // not sure how func is being implemented yet
	private List<Expression> list;
	
	public ExpressionCall(Expression function, Expression firstOperand, TokenStream stream)
	{
		func = function;
		list = new LinkedList<Expression> ();
		list.add(firstOperand);
		list.add(Expression.interpret(stream));
	}
	
	public ExpressionCall(Expression function, TokenStream stream)
	{
		func = function;
		list = new LinkedList<Expression> ();
		while (true)
		{
			Token next = stream.getFirst();
			if (next.toString().equals(")"))
			{
				stream.removeFirst();
				break;
			}
			list.add(Expression.interpret(stream));
		}
	}
	
	@Override
	public Value evaluate(IdentifierMap values)
	{
		return null;
//		return list.get(0).evaluate(values).call("_" + func, list.get(1).evaluate(values));
	}
	
	@Override
	public String toString()
	{
		return "(" + func + " " + list.toString() + ")";
	}
}