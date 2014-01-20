package ambroscum.values;

import java.util.List;
import java.util.Map;
import ambroscum.lines.Block;
import ambroscum.IdentifierMap;
import ambroscum.errors.InvalidArgumentException;
import ambroscum.errors.SyntaxError;

public class FunctionValue extends Value
{
	private IdentifierMap declaringScope;
	private List<String> params;
	private Block code;
	
	public FunctionValue(List<String> p, Block c, IdentifierMap s)
	{
		params = p;
		code = c;
		declaringScope = s;
	}
	
	public Value evaluate(List<Value> arguments)
	{
		IdentifierMap ownScope = new IdentifierMap(declaringScope);
		if (params.size() != arguments.size())
			throw new InvalidArgumentException("wrong number of arguments");
		for (int i = 0; i < params.size(); i++)
			ownScope.add(params.get(i), arguments.get(i));
		switch (code.evaluate(ownScope))
		{
			case CONTINUE:
				throw new SyntaxError("continues should not be terminating function calls");
			case BREAK:
				throw new SyntaxError("breaks should not be terminating function calls");
			case RETURN:
				return code.getReturnValue();
			default:
				return NullValue.NULL;
		}
	}
	
	@Override
	public String toString()
	{
		return "(funcdecl " + params + " " + code + ")";
	}
	
	public Value deepClone(Map<Value, Value> alreadyCloned) {
		return this;
	}
}