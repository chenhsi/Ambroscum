package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;
import java.util.*;

public class PrintLine extends Line
{
	private List<Expression> toPrint;
	private boolean newline;

	PrintLine(TokenStream stream, boolean newline)
	{
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
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		boolean first = true;
		for (Expression expr : toPrint)
		{
			if (!first)
				System.out.print(" ");
			System.out.print(expr.evaluate(values));
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
}