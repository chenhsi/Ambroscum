package ambroscum.values;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import java.util.*;
import java.lang.reflect.*;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class DictValue extends ObjectValue {
	private Map<Value, Value> map = new HashMap<> ();
	
	public DictValue(List<Value> origKeys, List<Value> origValues) {
		for (int i = 0; i < origKeys.size(); i++)
			set(origKeys.get(i), origValues.get(i));
	}
	
	public Value get(Value key) {
		if (map.containsKey(key))
			return map.get(key);
		throw new ambroscum.errors.NoSuchElementException(this + " has no element " + key);
	}
	public void set(Value key, Value value) {
		// Cast is guaranteed to be safe because clone() is called on a Value
		map.put((Value) clone(key, new HashMap<Object, Object>()), value);
	}
	// Deep-clones o. Has to deal with nonsense like circular references. Sigh.
	public static Object clone(Object o, Map<Object, Object> alreadyCloned) {
		// lolk. Pretty much, if it's a built-in thing, don't clone it. Because bad things will happen.
		// I don't think we'll ever encounter a situation where we try to clone something dangerous
		// that isn't in java built-in. Cross that bridge if we get to it, I guess.
		if (o == null)
			return null;
		String packageName = o.getClass().getPackage().getName();
		if (packageName.indexOf("java") == 0) {
			alreadyCloned.put(o, o);
			return o;
		}
		// If already cloned, then get the clone
		if (alreadyCloned.containsKey(o)) {
			return alreadyCloned.get(o);
		}
		
		// Special handling for arrays, because they're annoying. Not sure this is actually necessary.
		// Commented out for now, since I don't think this case will ever happen.
		if (o.getClass().isArray()) {
			Object newArray = cloneArray(o, alreadyCloned);
			alreadyCloned.put(o, newArray);
			return newArray;
		}
		
		// Create an empty copy of o
		// Using Objenesis to get "blank" instances of o
		ObjenesisStd objenesis = new ObjenesisStd();
		ObjectInstantiator instantiator = objenesis.getInstantiatorOf(o.getClass());
		Object clone = instantiator.newInstance();
		alreadyCloned.put(o, clone);
		// Recursively copy the fields of o
		for (Field field : o.getClass().getDeclaredFields()) {
			boolean isAccessible = field.isAccessible();
			try {
				field.setAccessible(true);
				Object fieldVal = field.get(o);
				
				// Ewwwww. Why don't arrays have fields? >.>
				if (fieldVal.getClass().isArray()) {
					// HAHAHAHAHAHAHAHAHA I HATE REFLECTION WHY DID I EVER THINK THIS WAS A GOOD IDEA
					Object newArray = cloneArray(fieldVal, alreadyCloned);
					field.set(clone, newArray);
				} else {
					field.set(clone, clone(fieldVal, alreadyCloned));
				}
			} catch (IllegalAccessException ex) {
				// IDK how to handle this
				// Although hopefully, Java Security Manager won't actually care about our classes
				// Which is probably a reasonable assumption
			} finally {
				field.setAccessible(isAccessible);
			}
		}
		return clone;
	}
	private static Object cloneArray(Object o, Map<Object, Object> alreadyCloned) {
		int length = Array.getLength(o);
		Object newArray = Array.newInstance(o.getClass().getComponentType(), length);
		for (int i = 0; i < length; i++) {
		    Object element = Array.get(o, i);
		    Object elementClone = clone(element, alreadyCloned);
		    alreadyCloned.put(element, elementClone);
		    Array.set(newArray, i, elementClone);
		}
		alreadyCloned.put(o, newArray);
		return newArray;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		Value first = otherValues.get(0);
		switch (op.toString())
		{
			case ".[]":
				return get(first); // Errors if the map doesn't contain the key.
			case "[]=":
				set(first, otherValues.get(1));
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
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	public String toString() {
		Set<Value> keys = map.keySet();
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		
		Iterator<Value> iter = keys.iterator();
		if (iter.hasNext()) {
			Value key = iter.next();
			builder.append(key.repr() + ": " + map.get(key).repr());
			
			while (iter.hasNext()) {
				key = iter.next();
				builder.append(", ");
				builder.append(key.repr());
				builder.append(": ");
				builder.append(map.get(key).repr());
			}
		}
		
		builder.append('}');
		return builder.toString();
	}
}