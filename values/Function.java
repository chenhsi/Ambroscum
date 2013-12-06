package ambroscum.values;

import java.util.List;
import ambroscum.lines.Block;
import ambroscum.Parameter;
import ambroscum.IdentifierMap;
import ambroscum.errors.InvalidArgumentException;
import ambroscum.errors.SyntaxError;

public class Function extends Value
{
	private List<Parameter> params;
	private Block code;
	
	public Function(List<Parameter> p, Block c)
	{
		params = p;
		code = c;
	}
	
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		IdentifierMap ownScope = new IdentifierMap(values);
		if (params.size() != arguments.size())
			throw new InvalidArgumentException("wrong number of arguments");
		for (int i = 0; i < params.size(); i++)
			ownScope.add(params.get(i).toString(), arguments.get(i));
		switch (code.evaluate(ownScope))
		{
			case CONTINUE:
				throw new SyntaxError("continues should not be terminating function calls");
			case BREAK:
				throw new SyntaxError("breaks should not be terminating function calls");
			case RETURN:
				return (Value) code.getAssociatedValue();
			default:
				return NullValue.NULL;
		}
	}
}