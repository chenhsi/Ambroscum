/**
 * Represents a value. Can be stored in a variable, returned by an expression, etc.
 * 
 * @author Chen-Hsi Steven Bi, Jinglun Edward Gao
 * @version 1.0
 */

package ambroscum.values;

import java.util.*;

public class Value
{
	private Map<String, Value> fields;
	private Map<String, Function> functions;
	
	public Value()
	{
		
	}
	
	public boolean equals(Object other)
	{
		return other instanceof Value && true; // needs working on
	}
}