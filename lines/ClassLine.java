package ambroscum.lines;

import java.util.List;
import java.util.LinkedList;
import ambroscum.IdentifierMap;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import ambroscum.errors.ObjectInstantiationException;
import ambroscum.expressions.ExpressionReference;
import ambroscum.expressions.Expression;
import ambroscum.values.Value;
import ambroscum.values.FunctionValue;
import ambroscum.values.ObjectValue;

public class ClassLine extends Line
{
	private final String name;
	private Expression parentObj;
	private Block block;
	
	ClassLine(Line parentLine, TokenStream stream, int indentationLevel)
	{
		super(parentLine);
		name = stream.removeFirst().toString();
		if (!IdentifierMap.isValidIdentifier(name))
			throw new SyntaxError("Not a valid function name: " + name);
		Token temp = stream.removeFirst();
		if (temp != Token.COLON)
		{
			if (!temp.equals("from"))
				throw new SyntaxError("Expecting \"from\" in class definition, found " + temp);
			parentObj = Expression.interpret(stream);
			temp = stream.removeFirst();
		}
		else
			parentObj = null;
		if (temp != Token.COLON)
			throw new SyntaxError("Expecting colon in class definition, found " + temp);
		temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token at end of function definition: " + temp);
		block = new Block(this, stream, indentationLevel + 1);
	}

	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		Value parentValue = parentObj.evaluate(values); // needs to deal with default objects
		if (parentValue instanceof FunctionValue)
			throw new ObjectInstantiationException("New objects cannot be cloned from functions");
		ObjectValue prototype = null; // this line should be modified once cloning is implemented
		values.add(name, prototype);
		return Block.ExitStatus.NORMAL;
		
//		IdentifierMap scope = new IdentifierMap(values);
//		block.evaluate(scope);
//		values.add(name, new ObjectValue(scope));
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(class ");
		sb.append(name);
		if (parentObj != null)
			sb.append(" ").append(parentObj);
		return sb.append(" ").append(block).append(")").toString();
	}
}