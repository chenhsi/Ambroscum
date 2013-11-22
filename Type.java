// now representing a class in its entirety

/**
 * Represents a type (of a value).
 * 
 * @author Chen-Hsi Steven Bi, Jinglun Edward Gao
 * @version 1.0
 */

package ambroscum;

import java.util.*;

public class Type
{
	private String name;
	private Map<String, Value> fields;
	private Map<String, Function> functions;
	
	static class Builder
	{
		private Type toBuild;
		
		public Builder(String name)
		{
			toBuild = new Type();
			toBuild.name = name;
			toBuild.fields = new HashMap<String, Value>();
			toBuild.functions = new HashMap<String, Function>();
		}
		
		public void addField(String name, Value value)
		{
			toBuild.field.put(name, code);
		}
		
		public void addFunction(String name, Function code)
		{
			toBuild.functions.put(name, code);
		}
		
		public Type toType()
		{
			if (name == null)
				throw new RuntimeException("object not properly built");
			return toBuild;
		}
	}
}
