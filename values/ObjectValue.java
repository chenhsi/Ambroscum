package ambroscum.values;

import java.util.List;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class ObjectValue extends Value
{
	// more stuff will be implemented later
	
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		throw new FunctionNotFoundException(op + " is not defined");
	}
}