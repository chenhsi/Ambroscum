package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class EndLine extends Line
{
	EndLine(Line parent)
	{
		super(parent);
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(end)";
	}
}