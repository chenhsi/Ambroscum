/**
 * Represents a value. Can be stored in a variable, returned by an expression, etc.
 * 
 * @author Chen-Hsi Steven Bi, Jinglun Edward Gao
 * @version 1.0
 */

package ambroscum.values;

import java.util.*;
import ambroscum.errors.AmbroscumError;
public abstract class Value
{
	public Value()
	{
		
	}
	
	public Value dereference(Value v) {
		throw new AmbroscumError("Cannot dereference " + getClass());
	}
	
	public boolean equals(Object other)
	{
		return other instanceof Value && true; // needs working on
	}
}