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
import ambroscum.values.FunctionValue;

public class DefLine extends Line
{
//	An idea for validating lines
/*	private static final Set<Class<? extends Line>> validDefLines;
	private static final Set<Class<? extends Line>> validEndLines;
	static
	{
		Set<Class<? extends Line>> set = new HashSet<> ();
		set.add(AssertLine.class);
		validDefLines = Collections.unmodifiableSet(set);
	}*/
	
	private final String name;
	private final List<String> list;
	private Block block;
	
	DefLine(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		name = stream.removeFirst().toString();
		if (!IdentifierMap.isValidIdentifier(name))
			throw new SyntaxError("Not a valid function name: " + name);
		Token temp = stream.removeFirst();
		if (!temp.toString().equals("("))
			throw new SyntaxError("Unexpected token in function definition: " + temp);
		boolean first = true;
		Token token = stream.removeFirst();
		list = new LinkedList<String> ();
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
			list.add(token.toString());
			token = stream.removeFirst();
		}
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Missing colon in function definition");
		temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token at end of function definition: " + temp);
		block = new Block(this, stream, indentationLevel + 1);
	}

	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		values.set(name, new FunctionValue(list, block, values));
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(def " + name + " " + list + " " + block + ")";
	}
	
	@Override
	public Line localOptimize()
	{
		block = (Block) block.localOptimize();
		return this;
	}
	
	public String getName()
	{
		return name;
	}
	public List<String> getParams()
	{
		return list;
	}
	public Block getBlock()
	{
		return block;
	}
}