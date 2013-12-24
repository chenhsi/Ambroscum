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
	
	/**
	 * @return this.ref
	 */
	public Value dereference(String ref) {
		throw new AmbroscumError("Cannot dereference " + getClass());
	}
	/**
	 * Attempts to set the value of this.ref
	 */
	public void setDereference(String ref, Value val) {
		throw new AmbroscumError("Cannot dereference " + getClass());
	}
	
	public boolean equals(Object other)
	{
		return other instanceof Value && true; // needs working on
	}
	
	/**
	 * @return A representation of this Value. Optimally, should be able
	 * to be entered into the interpreter and evaluated to get the same value.
	 */
	public String repr() {
		return toString();
	}
}