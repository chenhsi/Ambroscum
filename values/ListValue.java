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
			if (ind > -1 && ind < list.length)
				return list[ind];
			else
				throw new ambroscum.errors.NoSuchElementException("List index out of bounds: " + this + " has no element #"+  index);
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
		Value first = otherValues.get(0);
		switch (op.toString()) // the two current operators aren't actually supported/ever used
		{
			case ".[]":
				if (first instanceof IntValue)
					return list[((IntValue) first).getValue()];
				throw new FunctionNotFoundException("list's indexing expects an int");
			case "[]=":
				if (first instanceof IntValue)
				{
					list[((IntValue) first).getValue()] = otherValues.get(1);
					return NullValue.NULL;
				}
				throw new FunctionNotFoundException("list's indexing expects an int");
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public Value dereference(String ref) {
		if ("length".equals(ref)) {
			return IntValue.fromInt(list.length);
		}
		throw new VariableNotFoundException(ref);
	}
	@Override
	public void setDereference(String ref, Value val) {
		if ("length".equals(ref)) {
			throw new NonassignableException(this + "." + ref + " is not assignable");
		}
		throw new VariableNotFoundException(ref);
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