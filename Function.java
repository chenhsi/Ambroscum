// haven't decided if global or local

package ambroscum;

import java.util.List;
import ambroscum.lines.Block;

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
		return null;
	}
}