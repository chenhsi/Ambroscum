package ambroscum.lines;

import java.util.Map;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;

public class ContinueLine extends Line
{
	ContinueLine(Line parent, TokenStream stream)
	{
		super(parent);
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after continue: " + temp);
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty) {}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return Block.ExitStatus.CONTINUE;
	}
	
	@Override
	public String toString()
	{
		return "(continue)";
	}
}