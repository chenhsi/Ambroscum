package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class EndLine extends Line
{
	EndLine() {}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	@Override
	public void evaluate(IdentifierMap values) {}
	
	@Override
	public String toString()
	{
		return "(end)";
	}
}