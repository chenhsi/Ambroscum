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
		
//		if (code.contains(" = "))
//		{
//			int index = code.indexOf(" = ");
//			return new AssignmentLine(code.substring(0, index), code.substring(index + 3));
//		}
		throw new UnsupportedOperationException();
	}
}
