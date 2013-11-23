/**
 * Represents a single line of code.
 *
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import java.util.Iterator;

public abstract class Line
{
	public abstract void evaluate(IdentifierMap values);

	public static Line evalAsLine(TokenStream stream)
	{
		Token token = stream.removeFirst();
		if (token.toString().equals("assert"))
			return new AssertLine(stream);
		if (token.toString().equals("print") || token.toString().equals("println"))
			return new PrintLine(stream, token.toString().length() == 7);
		if (token.toString().equals("break"))
			return new BreakLine(stream);
		if (token.toString().equals("continue"))
			return new ContinueLine(stream);
		if (token.toString().equals("return"))
			return new ReturnLine(stream);
		// Look-ahead to see if we hit '=' before the next line
		TokenStream newStream = new TokenStream();
		newStream.add(token);
		while (true) {
			Token next = stream.removeFirst();
			if (next.toString().equals("="))
				return new AssignmentLine(newStream, stream);
			else if (next == Token.NEWLINE) {
				throw new UnsupportedOperationException("Call expressions not implemented!");
			} else {
				newStream.add(next);
			}
		}

		//throw new UnsupportedOperationException();
	}
}
