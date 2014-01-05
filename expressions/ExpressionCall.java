package ambroscum.expressions;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.OptimizedException;
import ambroscum.values.Value;
import ambroscum.values.FunctionValue;
import ambroscum.values.FunctionOperator;

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
		if (!(f instanceof FunctionValue))
			throw new FunctionNotFoundException(func + " does not evaluate to a function");
		Value v = ((FunctionValue) f).evaluate(eval);
		return v;
	}
	
	@Override
	public String toString()
	{
		return "(" + func + " " + operands + ")";
	}
	
	public Expression getFunction()
	{
		return func;
	}
	
	public List<Expression> getOperands()
	{
		return operands;
	}
	
	@Override
	public Expression localOptimize()
	{
		func = func.localOptimize();
		for (int i = 0; i < operands.size(); i++)
		{
			Expression optimized = operands.get(i).localOptimize();
			operands.set(i, optimized);
		}
		if (func instanceof ExpressionOperator)
		{
			List<Value> eval = new LinkedList<Value> ();
			for (Expression expr : operands)
				if (expr instanceof ExpressionLiteral)
					eval.add(((ExpressionLiteral) expr).getValue());
				else
					return this;
			Value optimized = ((ExpressionOperator) func).getValue().evaluate(eval);
			return new ExpressionLiteral(optimized);
		}
		if (func instanceof ExpressionLiteral)
			throw new OptimizedException(new FunctionNotFoundException(func + " does not evaluate to a function"));
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations)
	{
		func.setDeclarations(declarations);
		for (Expression expr : operands)
			expr.setDeclarations(declarations);
	}
}