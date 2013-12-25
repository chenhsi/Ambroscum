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
import ambroscum.values.ClassDeclaration;

public class ClassLine extends Line
{
	private final String name;
	private Block block;
	
	ClassLine(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		name = stream.removeFirst().toString();
		if (!IdentifierMap.isValidIdentifier(name))
			throw new SyntaxError("Not a valid function name: " + name);
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Missing colon in class definition");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token at end of function definition: " + temp);
		block = new Block(this, stream, indentationLevel + 1);
	}

	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		values.add(name, new ClassDeclaration(block));
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(class " + name + " " + block + ")";
	}
}