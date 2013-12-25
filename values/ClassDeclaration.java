package ambroscum.values;

import java.util.List;
import ambroscum.lines.Block;
import ambroscum.Parameter;
import ambroscum.IdentifierMap;
import ambroscum.errors.InvalidArgumentException;
import ambroscum.errors.SyntaxError;

public class ClassDeclaration extends Value
{
	private Block code;
	
	public ClassDeclaration(Block c)
	{
		code = c;
	}
	
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return "(classdecl " + code + ")";
	}
}