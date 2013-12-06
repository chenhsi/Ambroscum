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
		return map.get(key);
	}
	public void set(Value key, Value value) {
		map.put(key, value);
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
	public boolean equals(Object other) {
		return other instanceof DictValue && map.equals(((DictValue) other).map);
	}
	
	public String toString() {
		return map.toString();
	}
}