package ambroscum.values;

import java.util.List;
import java.util.Map;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.VariableNotFoundException;

public class ObjectValue extends Value
{
	// these should just be made abstract	
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		throw new FunctionNotFoundException(op + " is not defined");
	}
	public Value dereference(String ref)
	{
		throw new VariableNotFoundException(ref);
	}
	public void setDereference(String ref, Value val)
	{
		throw new VariableNotFoundException(ref);
	}
	
	public Value deepClone(Map<Value, Value> alreadyCloned) {
		return this;
	}
}