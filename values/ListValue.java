package ambroscum.values;

import ambroscum.*;
import ambroscum.error.*;
import java.util.*;

public class ListValue extends Value {
	
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
		System.out.println(index.getClass() + " " + (index instanceof IntValue));
		if (index instanceof IntValue) {
			int ind = ((IntValue) index).getValue();
			list[ind] = value;
		} else
			throw new SyntaxError("Expected int for list index");
	}
	
	public String toString() 	{
		return Arrays.toString(list);
	}
}