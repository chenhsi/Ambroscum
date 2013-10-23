// map of variables to types and values
// basically implemented as a Map<Identifier, Tuple<Type, Value>>

/**
 * Represents a mapping of variable names to (Type, Value) tuples.
 * Can be contained within an outer scope.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.util.*;

public class IdentifierMap
{
	private IdentifierMap previousblock; // represents link to previous block of code, might be null
	private HashMap<String, Value> map;
	
	public IdentifierMap(IdentifierMap prev) {
		previousblock = prev;
		map = new HashMap<String, Value>();
	}
	
	public void add(String name, Type type, Value value)
	{
		throw new UnsupportedOperationException();
	}
	
	public void set(String name, Value value)
	{
		throw new UnsupportedOperationException();
	}
	
	public Value get(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	class TypeValue
	{
		Type t;
		Value v;
	}
}
