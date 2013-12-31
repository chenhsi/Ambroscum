package ambroscum.values;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import java.util.*;

public class DictValue extends ObjectValue {
	private Map<Value, Value> map = new HashMap<> ();
	
	public DictValue(List<Value> origKeys, List<Value> origValues) {
		for (int i = 0; i < origKeys.size(); i++)
			map.put(origKeys.get(i), origValues.get(i));
	}
	
	public Value get(Value key) {
		if (map.containsKey(key))
			return map.get(key);
		throw new ambroscum.errors.NoSuchElementException(this + " has no element " + key);
	}
	public void set(Value key, Value value) {
		map.put(clone(key, new HashSet<Value>()), value);
	}
	// Deep-clones v. Has to deal with nonsense like circular references. Sigh.
	private static Value clone(Value v, Set<Value> alreadyCloned) {
		// If already cloned, then get the clone
		if (alreadyCloned.contains(v)) {
			Iterator<Value> iter = alreadyCloned.iterator();
			while (iter.hasNext()) {
				Value next = iter.next();
				if (v.equals(next)) {
					return next;
				}
			}
		}
		// Create an empty copy of v; add it to the set of already cloned objects
		// Recursivly copy the fields of v
		return null;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		Value first = otherValues.get(0);
		switch (op.toString())
		{
			case ".[]":
				return map.get(first); // should this error if map doesn't contain the key?
			case "[]=":
				map.put(first, otherValues.get(1));
				return NullValue.NULL;
		}
		return super.applyOperator(op, otherValues);
	}

	@Override
	public Value dereference(String ref) {
		if ("size".equals(ref)) {
			return IntValue.fromInt(map.size());
		}
		throw new VariableNotFoundException(ref);
	}
	@Override
	public void setDereference(String ref, Value val) {
		if ("size".equals(ref)) {
			throw new NonassignableException(this + "." + ref + " is not assignable");
		}
		throw new VariableNotFoundException(ref);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof DictValue && map.equals(((DictValue) other).map);
	}
	
	public String toString() {
		Set<Value> keys = map.keySet();
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		
		Iterator<Value> iter = keys.iterator();
		if (iter.hasNext()) {
			Value key = iter.next();
			builder.append(key.repr() + ": " + map.get(key));
			
			while (iter.hasNext()) {
				key = iter.next();
				builder.append(", ");
				builder.append(key.repr());
				builder.append(": ");
				builder.append(map.get(key));
			}
		}
		
		builder.append('}');
		return builder.toString();
	}
}