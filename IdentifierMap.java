/**
 * Represents a mapping of variable names to values.
 * <p>
 * When typing is implemented, this will also record type information about
 * variables.
 *
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.util.*;
import ambroscum.errors.*;
import ambroscum.values.Value;

public class IdentifierMap
{
	private static final Set<String> illegalIdentifiers;
	static
	{
		Set<String> illegal = new HashSet<String> ();
		illegal.add("true"); illegal.add("false");
		illegal.add("and"); illegal.add("or"); illegal.add("not");
		illegal.add("def"); illegal.add("class");
		illegal.add("if"); illegal.add("for"); illegal.add("while");
		illegal.add("elif"); illegal.add("else"); illegal.add("then");
		illegal.add("break"); illegal.add("continue"); illegal.add("return");
		illegal.add("end");
		illegal.add("self"); illegal.add("nonlocal");
		illegalIdentifiers = Collections.unmodifiableSet(illegal);
	}
	private IdentifierMap[] parents; // represents link to surrounding scopes,
									 // in preference order of left to right
	private HashMap<String, Value> map;
	private Set<String> nonlocal;

	/**
	 * Constructs an <code>IdentifierMap</code>.
	 *
	 * @param	prev	the IdentifierMap associated with the parent scope
	 */
	public IdentifierMap(IdentifierMap... prev)
	{
		parents = prev;
		map = new HashMap<String, Value>();
	}

	/**
	 * 
	 */
	public void setNonlocal(String name)
	{
		if (!isValidIdentifier(name))
			throw new SyntaxError("\"" + name + "\" is not a valid identifier.");
		nonlocal.add(name);
	}

	public void set(String name, Value value)
	{
		if (!isValidIdentifier(name))
			throw new SyntaxError("\"" + name + "\" is not a valid identifier.");
		if (nonlocal.contains(name))
		{
			if (parents.length == 0)
				throw new SyntaxError("Cannot set variables as nonlocal in the global scope");
			parents[0].set(name, value);
			// multiple inheritance probably not used, so this should be safe
		}
		else
			map.put(name, value);
	}

	public Value get(String name)
	{
		IdentifierMap containingScope = getContainingScope(name);
		if (containingScope == null) {
			throw new VariableNotFoundException(name);
		}
		return containingScope.map.get(name);
	}

	public static boolean isValidIdentifier(String name) {
		if (!Character.isLetter(name.charAt(0))) {
			return false;
		}
		for (int i = 1; i < name.length(); i++) {
			if (!Character.isLetterOrDigit(name.charAt(i)))
				return false;
		}
		return !illegalIdentifiers.contains(name);
	}

	// Returns the smallest IdentifierMap that contains the identifier
	// Returns null if no such Map exists
	private IdentifierMap getContainingScope(String identifier)
	{
		if (map.containsKey(identifier))
			return this;
		for (IdentifierMap parent : parents)
		{
			IdentifierMap parentScope = parent.getContainingScope(identifier);
			if (parentScope != null)
				return parentScope;
		}
		return null;
	}
	
	public String toString()
	{
		return map.toString();
	}
}
