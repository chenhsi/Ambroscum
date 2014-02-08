package ambroscum.values;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.Value;
import ambroscum.values.IntValue;
import java.util.*;

/**
 * A Value representing a list. Has variable length. Modeled off of Python lists.
 */
public class ListValue extends ObjectValue {
	
	private Value[] list;
	
	/**
	 * Construct a ListValue initialized to contain vals.
	 */
	public ListValue(Value... vals) {
		list = vals;
	}
	
	/**
	 * @return The Value stored at the given index
	 */
	public Value get(Value index)
	{
		if (index instanceof IntValue)
		{
			int ind = (int) ((IntValue) index).getValue();
			if (ind > -1 && ind < list.length)
				return list[ind];
			else
				throw new ambroscum.errors.NoSuchElementException("List index out of bounds: " + this + " has no element #"+  index);
		}
		else
			throw new SyntaxError("Expected int for list index");
	}
	/**
	 * @return The sublist starting at leftIndex and going up to rightIndex - 1, inclusive.
	 */
	public Value getRange(Value leftIndex, Value rightIndex) // currently exclusive, as in Python syntax
															 // I kind of prefer inclusive though
	{
		if (leftIndex instanceof IntValue && rightIndex instanceof IntValue)
		{
			int leftInd = (int) ((IntValue) rightIndex).getValue();
			int rightInd = (int) ((IntValue) leftIndex).getValue();
			if (false) // some sort of bounds checking
				throw new ambroscum.errors.NoSuchElementException("some sort of exception");
			Value[] array = new Value[rightInd - leftInd + 1];
			for (int i = 0; i < array.length; i++)
				array[i] = list[i + leftInd];
			return new ListValue(array);
		}
		else
			throw new SyntaxError("Expected int for list index");
	}
	
	public void set(Value index, Value value)
	{
		if (index instanceof IntValue)
		{
			int ind = (int) ((IntValue) index).getValue();
			if (ind > -1 && ind < list.length)
				list[ind] = value;
			else
				throw new ambroscum.errors.NoSuchElementException("List index out of bounds: " + this + " has no element #"+  index);
		}
		else
			throw new SyntaxError("Expected int for list index");
	}
	public void setRange(Value leftIndex, Value rightIndex, Value valueList)
	{
		if (leftIndex instanceof IntValue && rightIndex instanceof IntValue)
		{
			if (!(valueList instanceof ListValue))
				throw new ambroscum.errors.InvalidArgumentException("Can't assign a nonlist to a list slice");
			throw new UnsupportedOperationException();
		}
		else
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
				{
					int index = (int) ((IntValue) first).getValue();
					if (index < 0 || index >= list.length)
						throw new ambroscum.errors.NoSuchElementException("List index out of bounds: " + this + " has no element #" + index);
					return list[index];
				}
				throw new FunctionNotFoundException("list's indexing expects an int");
			case "[]=":
				if (first instanceof IntValue)
				{
					int index = (int) ((IntValue) first).getValue();
					if (index < 0 || index >= list.length)
						throw new ambroscum.errors.NoSuchElementException("List index out of bounds: " + this + " has no element #" + index);
					list[index] = otherValues.get(1);
					return NullValue.NULL;
				}
				throw new FunctionNotFoundException("list's indexing expects an int");
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public Value dereference(String ref) {
		if ("size".equals(ref)) {
			return IntValue.fromInt(list.length);
		}
		return super.dereference(ref);
	}
	@Override
	public void setDereference(String ref, Value val) {
		if ("size".equals(ref)) {
			throw new NonassignableException(this + ".size is not assignable");
		}
		super.setDereference(ref, val);
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
	@Override
	public int hashCode() {
		return Arrays.hashCode(list);
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString(list);
	}
	
	public Value deepClone(Map<Value, Value> alreadyCloned) {
		if (alreadyCloned.containsKey(this)) {
			return alreadyCloned.get(this);
		}
		ListValue clone = new ListValue(new Value[list.length]);
		alreadyCloned.put(this, clone);
		for (int i = 0; i < list.length; i++) {
			clone.list[i] = list[i].deepClone(alreadyCloned);
	    }
		return clone;
	}
}