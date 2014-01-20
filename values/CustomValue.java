package ambroscum.values;

import java.util.List;
import java.util.Map;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.VariableNotFoundException;
import ambroscum.errors.InvalidArgumentException;
import ambroscum.errors.NonassignableException;

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
		if (ref.equals("new"))
			return new FunctionValue(null, null, scope)
			{
				public Value evaluate(List<Value> arguments)
				{
					if (arguments.size() != 0)
						throw new InvalidArgumentException("wrong number of arguments");
					return new CustomValue(new IdentifierMap(scope));
				}
			};
		return scope.get(ref);
	}

	@Override
	public void setDereference(String ref, Value val)
	{
		if ("new".equals(ref))
			throw new NonassignableException(this + ".new is not assignable");
		scope.add(ref, val);
	}
	
	public Value deepClone(Map<Value, Value> alreadyCloned) {
		return this;
	}
}