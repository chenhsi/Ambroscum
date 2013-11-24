// not currently dealing with function modifiers

package ambroscum.lines;

import java.util.List;
import java.util.LinkedList;
import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.expressions.ExpressionReference;
import ambroscum.expressions.Expression;
import ambroscum.values.Value;
import ambroscum.values.Function;

public class DefLine extends Line
{
	private final String name;
	private final List<Parameter> list;
	private Block block;
	
	DefLine(TokenStream stream)
	{
		name = stream.removeFirst().toString();
		if (!IdentifierMap.isValidIdentifier(name))
			throw new SyntaxError("Not a valid function name: " + name);
		Token temp = stream.removeFirst();
		if (!temp.toString().equals("("))
			throw new SyntaxError("Unexpected token in function definition: " + temp);
		boolean first = true;
		Token token = stream.removeFirst();
		list = new LinkedList<Parameter> ();
		while (!token.toString().equals(")"))
		{
			if (token == Token.NEWLINE)
				throw new SyntaxError("Unexpected end of line in function definition");
			if (first)
				first = false;
			else
			{
				if (token != Token.COMMA)
					throw new SyntaxError("Missing delimiter in function definition");
				token = stream.removeFirst();
				if (token == Token.NEWLINE)
					throw new SyntaxError("Unexpected end of line in function definition");
				if (token == Token.COMMA)
					throw new SyntaxError("Unexpected delimiter in function definition");
			}
			list.add(new Parameter(token.toString()));
			token = stream.removeFirst();
		}
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Missing colon in function definition");
	}

	public boolean expectsBlock()
	{
		return true;
	}
	public void setBlock(Block b)
	{
		block = b;
	}
	
	@Override
	public void evaluate(IdentifierMap values)
	{
		values.add(name, new Function(list, block));
	}
	
	@Override
	public String toString()
	{
		return "(def " + name + " " + list + " (" + block + "))";
	}
}