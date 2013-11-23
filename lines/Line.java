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
		Iterator<Token> streamIter = stream.iterator();
		while (streamIter.hasNext()) {
			Token next = streamIter.next();
			String str = next.getString();
			if (str.equals("=")) {
				return new AssignmentLine(stream);
			} else if (str.equals("\n")) {
				throw new UnsupportedOperationException("Call expressions not implemented!");
			}
		}
		//int index = first.indexOf(" = ");
		//return new AssignmentLine(first.substring(0, index), first.substring(index + 3));
		
		throw new UnsupportedOperationException();
	}
}
