package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;

public class ReturnLine extends Line
{
	private Expression expr;
	
	ReturnLine(TokenStream stream)
	{
		expr = Expression.interpret(stream);
		// if (stream.getFirst() == )
	}
}