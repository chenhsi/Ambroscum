/**
 * Represents a single line of code.
 *
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;

public abstract class Line
{
	public abstract void evaluate(IdentifierMap values);

	public static Line evalAsLine(TokenStream stream)
	{
		Token token = stream.getFirst();
		if (token.toString().equals("assert"))
		{
			stream.removeFirst();
			return new AssertLine(stream);
		}
		if (token.toString().equals("print") || token.toString().equals("println"))
		{
			stream.removeFirst();
<<<<<<< HEAD
			return new PrintLine(stream, token.length() == 7);
		} else {
			// Look-ahead to see if we hit '=' before the next line
			Iterator<Token> streamIter;
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
=======
			return new PrintLine(stream, token.toString().length() == 7);
		}
		if (token.toString().equals("break"))
		{
			return new BreakLine();
		}
		if (token.toString().equals("continue"))
		{
			return new ContinueLine();
		}
		if (token.toString().equals("return"))
		{
			return new ReturnLine(stream);
		}
		
>>>>>>> dc8c10ef8926910674b0aec203c462eceb5636e6
//		if (code.contains(" = "))
//		{
//			int index = code.indexOf(" = ");
//			return new AssignmentLine(code.substring(0, index), code.substring(index + 3));
//		}
		throw new UnsupportedOperationException();
	}
}
