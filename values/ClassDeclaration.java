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
	
	public Value evaluate(IdentifierMap values)
	{
		IdentifierMap ownScope = new IdentifierMap(values);
		switch (code.evaluate(ownScope))
		{
			case CONTINUE:
				throw new SyntaxError("continues should not be terminating class declarations");
			case BREAK:
				throw new SyntaxError("breaks should not be terminating class declarations");
			case RETURN:
				throw new SyntaxError("returns should not be terminating class declarations");
			default:
				return NullValue.NULL;
		}
	}
	
	@Override
	public String toString()
	{
		return "(classdecl " + code + ")";
	}
}