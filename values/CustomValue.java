package ambroscum.values;

import java.util.List;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.VariableNotFoundException;

public class CustomValue extends ObjectValue
{
	private IdentifierMap scope;
	
	public CustomValue(IdentifierMap map)
	{
		scope = map;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		throw new FunctionNotFoundException(op + " is not defined");
	}
	
	@Override
	public Value dereference(String ref)
	{
		return scope.get(ref);
	}
	@Override
	public void setDereference(String ref, Value val)
	{
		scope.add(ref, val);
	}
}