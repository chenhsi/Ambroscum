/**
 * Represents a value. Can be stored in a variable, returned by an expression, etc.
 * 
 * @author Chen-Hsi Steven Bi, Jinglun Edward Gao
 * @version 1.0
 */

package ambroscum.values;

import java.util.*;
import ambroscum.errors.AmbroscumError;
public abstract class Value {
	public Value()
	{
		
	}
	
	public boolean equals(Object other)
	{
		return other instanceof Value && this == other; // needs working on
	}
	
	/**
	 * @return A representation of this Value. Optimally, should be able
	 * to be entered into the interpreter and evaluated to get the same value.
	 */
	public String repr() {
		return toString();
	}
	
	/**
	 * Creates a copy of this object, with fields also being cloned.
	 * As a side effect, adds this object and its clone to the alreadyCloned map
	 * at the start of the method (i.e. before cloning the fields).
	 */
	public abstract Value deepClone(Map<Value, Value> alreadyCloned);
}