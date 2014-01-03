package ambroscum.lines;

import java.util.Map;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;

public class EndLine extends Line
{
	EndLine(Line parent)
	{
		super(parent);
	}
	
	@Override
	public Line localOptimize()
	{
		throw new AssertionError("End lines should not be stored, and optimizations should thus never be attempted");
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		throw new AssertionError("End lines should not be stored, and optimizations should thus never be attempted");
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		throw new AssertionError("End lines should not be stored, and should thus never be evaluated");
	}
	
	@Override
	public String toString()
	{
		return "(end)";
	}
}