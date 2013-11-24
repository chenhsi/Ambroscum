package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;

public class EmptyLine extends Line {
	
	EmptyLine(TokenStream stream) 	{
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after empty line: " + temp);
	}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	@Override
	public void evaluate(IdentifierMap values) {}
	
	@Override
	public String toString() {
		return "";
	}
}