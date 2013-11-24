package ambroscum.values;

import java.util.List;
import ambroscum.lines.Block;
import ambroscum.Parameter;
import ambroscum.IdentifierMap;
import ambroscum.errors.InvalidArgumentException;

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
		code.evaluate(ownScope);
		return null;
	}
}