package ambroscum.values;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.error.SyntaxError;
import java.util.*;

public class ExpressionReference extends Expression
{
	private String name;
	private Expression secondary;
	private ReferenceType type;

	public ExpressionReference(Token token, TokenStream stream)
	{
		name = token.toString();
		Token peek = stream.getFirst();
		if (peek.toString().equals("("))
		{
			stream.removeFirst();
			type = ReferenceType.PARENTHESIS;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals(")"))
				throw new SyntaxError("Missing close parenthesis");
		}
		if (peek.toString().equals("["))
		{
			stream.removeFirst();
			type = ReferenceType.BRACKET;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("]"))
				throw new SyntaxError("Missing close bracket");
		}
		if (peek.toString().equals("{"))
		{
			stream.removeFirst();
			type = ReferenceType.BRACE;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("}"))
				throw new SyntaxError("Missing close brace");
		}
	}

	public Value evaluate(IdentifierMap values)
	{
		switch (type) {
			case NONE:
				return values.get(name);
			case PARENTHESIS:
				return null;
			case BRACKET:
				return null;
			case BRACE:
				return null;
		}
		return null;
	}

	public void setValue(Value value, IdentifierMap values)
	{
		switch (type)
		{
			case NONE:
				values.set(name, value);
				break;
			default:
				throw new UnsupportedOperationException("Arrays and stuff don't work yet.");
		}
	}

	private static enum ReferenceType
	{
		NONE, PARENTHESIS, BRACKET, BRACE
	}
}