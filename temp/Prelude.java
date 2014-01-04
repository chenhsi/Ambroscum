import java.util.*;

abstract class Function
{
	private String[] params;
	private VariableMap map;
	
	public Function(VariableMap parentMap, String... params)
	{
		map = parentMap;
		this.params = params;
	}
	
	protected abstract Object call(VariableMap map);
	
	public Object call(Object... args)
	{
		if (args.length != params.length)
			throw new IllegalArgumentException("wrong number of arguments");
		VariableMap tempMap = new VariableMap(map);
		for (int i = 0; i < args.length; i++)
			tempMap.put(params[i], args[i]);
		return call(tempMap);
	}
}

class VariableMap
{
	private VariableMap parent;
	private Map<String, Object> values = new HashMap<> ();
	
	public VariableMap() {}
	public VariableMap(VariableMap parent)
	{
		this.parent = parent;
	}
	
	public void put(String key, Object value)
	{
		values.put(key, value);
	}
	public Object get(String key)
	{
		if (values.containsKey(key))
			return values.get(key);
		if (parent == null)
			throw new RuntimeException("this should be changed to a better exception");
		return parent.get(key);
	}
}