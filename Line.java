/**
 * Represents a single line of code.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import ambroscum.lines.*;

public abstract class Line
{
	public abstract void evaluate(IdentifierMap values);
	
	public static Line evalAsLine(String code, Scope followingblock)
	{
		if (code.beginsWith("assert "))
			return new AssertLine(code.susbtring(7));
		if (code.beginsWith("print "))
			return new PrintLine(code.susbtring(6));
		if (code.contains(" = "))
		{
			int index = code.indexOf(" = ");
			return new AssignmentLine(code.substring(0, index), code.substring(index + 3));
		}
		throw new UnsupportedOperationException();
	}
}
