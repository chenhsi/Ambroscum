// a line of code
// srsly, what do you want

/**
 * Represents a single line of code.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

public class Line
{
	private String rawcode; // the raw code srsly come on
	private Scope followingblock; // a block of code possibly associated with it
	
	public void evaluate(IdentifierMap values)
	{
		throw new UnsupportedOperationException();
	}
}
