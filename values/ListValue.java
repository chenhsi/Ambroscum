package ambroscum.values;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import java.util.*;

public class ListValue extends ObjectValue {
	
	private Value[] list;
	
	public ListValue(Value... vals) {
		list = vals;
	}
	
	public Value get(Value index) {
		if (index instanceof IntValue) {
			int ind = ((IntValue) index).getValue();
			return list[ind];
		}
		throw new SyntaxError("Expected int for list index");
	}
	public void set(Value index, Value value) {
		if (index instanceof IntValue) {
			int ind = ((IntValue) index).getValue();
			list[ind] = value;
		} else
			throw new SyntaxError("Expected int for list index");
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		switch (op.toString())
		{
			// should decide how these work
		}
		return super.applyOperator(op, otherValues);
	}

	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ListValue) {
			Value[] oList = ((ListValue) o).list;
			if (oList.length != list.length) {
				return false;
			}
			for (int i = 0; i < list.length; i++) {
				if (!list[i].equals(oList[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public String toString() 	{
		return Arrays.toString(list);
	}
}