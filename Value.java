/**
 * Represents a value. Can be stored in a variable, returned by an expression, etc.
 * 
 * @author Chenhsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.util.*;

public class Value
{
	public static final Value TRUE = new Value();
	
	private Type type;
	
	private Map<String, Value> fields;
	private Map<String, Function> functions;
}
