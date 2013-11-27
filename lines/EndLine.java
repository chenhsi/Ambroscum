package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class EndLine extends Line
{
	
	private int indentation;
	
	EndLine() {}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return Block.ExitStatus.NORMAL;
//		throw new AssertionError("EndLine's evaluate should never be called");
	}
	
	@Override
	public String toString()
	{
		return "(end)";
	}
}