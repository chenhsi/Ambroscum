package ambroscum.values;

import java.util.List;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.VariableNotFoundException;

public class ObjectValue extends Value
{
	// more stuff will be implemented later
	
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
}