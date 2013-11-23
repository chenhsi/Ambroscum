package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;
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
		
		while (!stream.isEmpty())
		{
			Token token = stream.removeFirst();
			if (token == Token.NEWLINE)
				break;
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
				expectExpr = true;
		}
	}
	
	public void evaluate(IdentifierMap values)
	{
		boolean first = true;
		for (Expression expr : toPrint)
		{
			System.out.print(expr.evaluate(values));
			if (!first)
				System.out.print(" ");
			first = false;
		}
		if (newline)
			System.out.println();
	}
}