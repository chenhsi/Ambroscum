package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.NullReferenceException;

public class NullValue extends ObjectValue
{
	public static final NullValue NULL = new NullValue();
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		// should this support equality and nonequality?
		throw new NullReferenceException("No operations can be performed on a null value");
	}
	
	@Override
	public String toString()
	{
		return "null";
	}
}