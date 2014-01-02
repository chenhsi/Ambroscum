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
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty) {}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(end)";
	}
}