package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;

public class ContinueLine extends Line
{
	ContinueLine(TokenStream stream)
	{
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after continue: " + temp);
	}
	
	public void evaluate(IdentifierMap values) {}
}