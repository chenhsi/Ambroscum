import java.util.*;

abstract class Value // base class for most (all?) objects
{
	public abstract Value operator(String op, Value... otherArgs);
}

abstract class Function extends Value
{
	private String[] params;
	private VariableMap map;
	
	public Function(VariableMap parentMap, String... params)
	{
		map = parentMap;
		this.params = params;
	}
	
	protected abstract Value call(VariableMap map);
	
	public Value call(Value... args)
	{
		if (args.length != params.length)
			throw new IllegalArgumentException("wrong number of arguments");
		VariableMap tempMap = new VariableMap(map);
		for (int i = 0; i < args.length; i++)
			tempMap.put(params[i], args[i]);
		return call(tempMap);
	}
	
	public Value operator(String op, Value... otherArgs)
	{
		throw new IllegalArgumentException("Functions cannot take operator " + op);
	}
}

class VariableMap
{
	private VariableMap parent;
	private Map<String, Value> values = new HashMap<> ();
	
	public VariableMap() {}
	public VariableMap(VariableMap parent)
	{
		this.parent = parent;
	}
	
	public Value put(String key, Value value)
	{
		// is this how we want to do scoping
		values.put(key, value);
		return value;
	}
	public Value get(String key)
	{
		if (values.containsKey(key))
			return values.get(key);
		if (parent == null)
			throw new RuntimeException("this should be changed to a better exception");
		return parent.get(key);
	}
}

class AmbroscumList extends Value implements Iterable
{
	private List<Value> internal = new LinkedList<> ();
	
	public void add(Value toAdd)
	{
		internal.add(toAdd);
	}
	
	public Value get(IntValue index)
	{
		return internal.get(index.value);
	}
	
	public Value set(IntValue index, Value obj)
	{
		internal.set(index.value, obj);
		return obj;
	}
	
	public Iterator iterator()
	{
		return internal.iterator();
	}
	
	public Value operator(String op, Value... otherArgs)
	{
		throw new IllegalArgumentException("Cannot apply operators to lists");
	}
	
	public String toString()
	{
		return internal.toString();
	}
}

class IntValue extends Value
{
	public final int value;
	
	private IntValue(int n)
	{
		value = n;
	}
	
	public static IntValue from(int n)
	{
		return new IntValue(n);
	}
	
	public Value operator(String op, Value... otherArgs)
	{
		switch (op)
		{
			case "+":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value + ((IntValue) otherArgs[0]).value);
				if (otherArgs[0] instanceof StringValue)
					return StringValue.from(this.value + ((StringValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '+' to an int with " + otherArgs[0]);
			case "-":
				if (otherArgs.length == 0)
					return IntValue.from(-this.value);
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value - ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '-' to an int with " + otherArgs[0]);
			case "*":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value * ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '*' to an int with " + otherArgs[0]);
			case "/":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value / ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '/' to an int with " + otherArgs[0]);
			case "%":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value % ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '%' to an int with " + otherArgs[0]);
			case "<":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value < ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '<' to an int with " + otherArgs[0]);
			case ">":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value > ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '>' to an int with " + otherArgs[0]);
			case "<=":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value <= ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '<=' to an int with " + otherArgs[0]);
			case ">=":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value >= ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '>=' to an int with " + otherArgs[0]);
			case "=":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value == ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '=' to an int with " + otherArgs[0]);
			case "!=":
				if (otherArgs[0] instanceof IntValue)
					return BooleanValue.from(this.value != ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '!=' to an int with " + otherArgs[0]);
			case "<<":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value << ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '<<' to an int with " + otherArgs[0]);
			case ">>":
				if (otherArgs[0] instanceof IntValue)
					return IntValue.from(this.value >> ((IntValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '>>' to an int with " + otherArgs[0]);
			case "**":
				throw new UnsupportedOperationException();
		}
		throw new UnsupportedOperationException();
	}
	
	public String toString()
	{
		return value + "";
	}
}

class BooleanValue extends Value
{
	private static final BooleanValue TRUE = new BooleanValue(true);
	private static final BooleanValue FALSE = new BooleanValue(false);
	public final boolean value;
	
	private BooleanValue(boolean b)
	{
		value = b;
	}
	
	public static BooleanValue from(boolean b)
	{
		return b ? TRUE : FALSE;
	}
	
	public Value operator(String op, Value... otherArgs)
	{
		switch (op)
		{
			case "not":
				return value ? FALSE : TRUE;
			case "and":
				if (otherArgs[0] instanceof BooleanValue)
					return BooleanValue.from(this.value && ((BooleanValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '&&' to an boolean with " + otherArgs[0]);
			case "or":
				if (otherArgs[0] instanceof BooleanValue)
					return BooleanValue.from(this.value || ((BooleanValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '||' to an boolean with " + otherArgs[0]);
			case "=":
				if (otherArgs[0] instanceof BooleanValue)
					return BooleanValue.from(this.value == ((BooleanValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '==' to an boolean with " + otherArgs[0]);
			
		}
		throw new UnsupportedOperationException();
	}
	
	public String toString()
	{
		return value + "";
	}
}

class StringValue extends Value
{
	public final String value;
	
	private StringValue(String s)
	{
		value = s;
	}
	
	public static StringValue from(String s)
	{
		return new StringValue(s);
	}
	
	public Value operator(String op, Value... otherArgs)
	{
		switch (op)
		{
			case "+":
				if (otherArgs[0] instanceof IntValue)
					return StringValue.from(this.value + ((IntValue) otherArgs[0]).value);
				if (otherArgs[0] instanceof BooleanValue)
					return StringValue.from(this.value + ((BooleanValue) otherArgs[0]).value);
				if (otherArgs[0] instanceof StringValue)
					return StringValue.from(this.value + ((StringValue) otherArgs[0]).value);
				throw new IllegalArgumentException("Cannot apply operator '+' to a string with " + otherArgs[0]);
			case "<":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) < 0);
				throw new IllegalArgumentException("Cannot apply operator '<' to a string with " + otherArgs[0]);
			case ">":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) > 0);
				throw new IllegalArgumentException("Cannot apply operator '>' to a string with " + otherArgs[0]);
			case "<=":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) <= 0);
				throw new IllegalArgumentException("Cannot apply operator '<=' to a string with " + otherArgs[0]);
			case ">=":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) >= 0);
				throw new IllegalArgumentException("Cannot apply operator '>=' to a string with " + otherArgs[0]);
			case "=":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) == 0);
				throw new IllegalArgumentException("Cannot apply operator '=' to a string with " + otherArgs[0]);
			case "!=":
				if (otherArgs[0] instanceof StringValue)
					return BooleanValue.from(this.value.compareTo(((StringValue) otherArgs[0]).value) != 0);
				throw new IllegalArgumentException("Cannot apply operator '!=' to a string with " + otherArgs[0]);
		}
		throw new UnsupportedOperationException();
	}
	
	public String toString()
	{
		return value;
	}
}
