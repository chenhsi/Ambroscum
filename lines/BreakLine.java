package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class BreakLine extends Line
{
	BreakLine(TokenStream stream)
	{
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after break: " + temp);
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return Block.ExitStatus.BREAK;
	}
	
	@Override
	public String toString()
	{
		return "(break)";
	}
}