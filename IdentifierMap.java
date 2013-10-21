// map of variables to types and values
// basically implemented as a Map<Identifier, Tuple<Type, Value>>

package ambroscum;

import java.util.*;

public class IdentifierMap
{
	private IdentifierMap previousblock; // represents link to previous block of code, might be null
	
	class TypeValue
	{
		Type t;
		Value = v;
	}
}