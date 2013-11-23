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
		if (index instanceof IntLiteral) {
			int ind = ((IntLiteral) index).getValue();
			if (ind < 0 || ind >= list.length)
				// Or do we just want to use java.lang.ArrayIndexOutOfBoundsExecption
				throw new RuntimeException("List index out of bounds: " + ind);
			return list[ind];
		}
		throw new SyntaxError("Expected int for list index");
	}
	public void set(Value index, Value value) {
		System.out.println("INDEX " + index.getClass());
		if (index instanceof IntLiteral) {
			int ind = ((IntLiteral) index).getValue();
			if (ind < 0 || ind >= list.length)
				// Or do we just want to use java.lang.ArrayIndexOutOfBoundsExecption
				throw new RuntimeException("List index out of bounds: " + ind);
			list[ind] = value;
		}
		System.out.println("lolwut");
		throw new SyntaxError("Expected int for list index");
	}
	
	public String toString() 	{
		return Arrays.toString(list);
	}
}