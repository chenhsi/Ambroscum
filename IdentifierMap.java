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

	public void add(String name, Value value)
	{
		// Not sure how to deal with overriding variables
		map.put(name, value);
	}

	public void set(String name, Value value)
	{
		IdentifierMap containingScope = getContainingScope(name, this);
		if (containingScope == null || containingScope == this)
			map.put(name, value);
		else
			containingScope.map.put(name, value);
	}

	public Value get(String name)
	{
		IdentifierMap containingScope = getContainingScope(name, this);
		if (containingScope == null) {
			throw new RuntimeException("Variable not found: " + name);
		}
		return containingScope.map.get(name);
	}

	// Returns the smallest IdentifierMap that contains the identifier
	// Returns null if no such Map exists
	private static IdentifierMap getContainingScope(String identifier, IdentifierMap lowest) {
		while (lowest != null) {
			if (lowest.map.containsKey(identifier))
				return lowest;
			lowest = lowest.previousblock;
		}
		return null;
	}
}
