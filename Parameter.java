/**
 * Represents a parameter to a function call.
 * <p>
 * Currently simply a wrapper for a String, but is intended to include type
 * information when implemented.
 * 
 * @author Chen-Hsi Steven Bi
 * @version 1.0
 */

package ambroscum;

public class Parameter
{
	private final String name;
	
	/**
	 * Constructs a <code>Parameter</code> based on the input string.
	 * 
	 * @param	the name of the parameter variable
	 */
	public Parameter(String n)
	{
		name = n;
	}
	
	/**
	 * Returns a representation of the parameter, which currently is merely
	 * the name of the parameter variable.
	 * 
	 * @return	the name of the parameter variable
	 */
	public String toString()
	{
		return name;
	}
}