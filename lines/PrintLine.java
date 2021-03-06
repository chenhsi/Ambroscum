package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;
import ambroscum.values.Value;
import java.util.*;

public class PrintLine extends Line
{
	private List<Expression> toPrint;
	private boolean newline;

	PrintLine(Line parent, TokenStream stream, boolean newline)
	{
		super(parent);
		this.newline = newline;
		boolean expectExpr = true;
		toPrint = new LinkedList<Expression> ();

		while (true)
		{
			Token token = stream.getFirst();
			if (token == Token.NEWLINE)
			{
				stream.removeFirst();
				break;
			}
			if (expectExpr)
			{
				if (token == Token.COMMA)
					throw new SyntaxError("Unexpected delimited in a print statement");
				toPrint.add(Expression.interpret(stream));
				expectExpr = false;
			}
			else if (token != Token.COMMA)
				throw new SyntaxError("Unexpected token: " + token);
			else
			{
				expectExpr = true;
				stream.removeFirst();
			}
		}
	}
	
	public List<Expression> getPrintExpressions()
	{
		return toPrint;
	}
	
	public boolean isPrintNewline()
	{
		return newline;
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		boolean first = true;
		List<Value> printValues = new LinkedList<> ();
		for (Expression expr : toPrint)
			printValues.add(expr.evaluate(values));
		for (Value value : printValues)
		{
			if (!first)
				System.out.print(" ");
			System.out.print(value);
			first = false;
		}
		if (newline)
			System.out.println();
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(" + (newline ? "println " : "print ") + toPrint + ")";
	}
	
	@Override
	public Line localOptimize()
	{
		for (int i = 0; i < toPrint.size(); i++)
			toPrint.set(i, toPrint.get(i).localOptimize());
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		for (Expression expr : toPrint)
			expr.setDeclarations(declarations);
	}
}