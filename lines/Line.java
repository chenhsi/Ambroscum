/**
 * Represents a single line of code.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;

public abstract class Line
{
	public abstract void evaluate(IdentifierMap values);
	
	public static Line evalAsLine(TokenStream stream, Scope followingblock)
	{
		
		if (code.startsWith("assert "))
			return new AssertLine(code.substring(7));
		if (code.startsWith("print "))
			return new PrintLine(code.substring(6));
		if (code.contains(" = "))
		{
			int index = code.indexOf(" = ");
			return new AssignmentLine(code.substring(0, index), code.substring(index + 3));
		}
		throw new UnsupportedOperationException();
	}
}
