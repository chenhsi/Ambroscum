package ambroscum.lines;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.Expression;

public class NonlocalLine extends Line
{
	private List<String> nonlocals;
	
	NonlocalLine(Line parent, TokenStream stream)
	{
		super(parent);
		nonlocals = new LinkedList<> ();
		while (stream.hasNext())
		{
			Token temp = stream.removeFirst();
			if (IdentifierMap.isValidIdentifier(temp.toString()))
			{
				nonlocals.add(temp.toString());
				continue;
			}
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token in nonlocal declarations: " + temp);
		}
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		throw new UnsupportedOperationException("no idea what should be done here");
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		for (String var : nonlocals)
			values.setNonlocal(var);
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(nonlocal " + nonlocals + ")";
	}
}