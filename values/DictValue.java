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
	public Value applyOperator(FunctionOperator op, List<Value> otherValues) {
		switch (op.toString()) 	{
			// should decide how these work
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