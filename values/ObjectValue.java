package ambroscum.values;

import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class ObjectValue extends Value
{
	// more stuff will be implemented later
	
	public Value applyOperator(FunctionOperator op, Value otherValue)
	{
		throw new FunctionNotFoundException(op + " is not defined");
	}
}