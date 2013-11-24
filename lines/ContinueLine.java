package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class ContinueLine extends Line
{
	ContinueLine(TokenStream stream)
	{
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after continue: " + temp);
	}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	
	@Override
	public void evaluate(IdentifierMap values) {}
	
	@Override
	public String toString()
	{
		return "(continue)";
	}
}