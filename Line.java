// a line of code
// srsly, what do you want

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
	
	private static Line evalAsLine(String code, Scope followingblock)
	{
		throw new UnsupportedOperationException();
	}
}
