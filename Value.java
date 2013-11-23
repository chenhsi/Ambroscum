/**
 * Represents a value. Can be stored in a variable, returned by an expression, etc.
 * 
 * @author Chen-Hsi Steven Bi, Jinglun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.util.*;

public class Value
{
	public static final Value TRUE = new Value();
	
	private Map<String, Value> fields;
	private Map<String, Function> functions;
	
	public Value()
	{
		
	}
}