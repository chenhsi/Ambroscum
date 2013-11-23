package ambroscum.values;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.SyntaxError;
import java.util.*;

public class ExpressionReference extends Expression
{
	private Token primary;
	private Expression secondary;
	private ReferenceType type;

	public ExpressionReference(Token token, TokenStream stream)
	{
		primary = token;
		Token peek = stream.size() > 0 ? stream.getFirst() : null;
		String peekStr = peek != null ? peek.toString() : null;
		if ("[".equals(peekStr))
		{
			stream.removeFirst();
			type = ReferenceType.BRACKET;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("]"))
				throw new SyntaxError("Missing close bracket");
		}
		else if ("{".equals(peekStr))
		{
			stream.removeFirst();
			type = ReferenceType.BRACE;
			secondary = Expression.interpret(stream);
			Token close = stream.removeFirst();
			if (!close.toString().equals("}"))
				throw new SyntaxError("Missing close brace");
		} else {
			type = ReferenceType.NONE;
		}
	}

	@Override
	public Value evaluate(IdentifierMap values)
	{
		switch (type) {
			case NONE:
				return values.get(primary.toString());
			case BRACKET:
				Value outerList = values.get(primary.toString());
				if (outerList instanceof ListValue) {
					return ((ListValue) outerList).get(secondary.evaluate(values));
				}
				throw new SyntaxError("Cannot use brackets to index a non-list: " + outerList);
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
				values.set(primary.toString(), value);
				break;
			case BRACKET:
				Value outerList = values.get(primary.toString());
				if (outerList instanceof ListValue) {
					((ListValue) outerList).set(secondary.evaluate(values), value);
				} else
					throw new SyntaxError("Cannot use brackets to index a non-list: " + outerList);
				break;
			default:
				throw new UnsupportedOperationException("Arrays and stuff don't work yet.");
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(primary.toString());
		if (type != ReferenceType.NONE)
			sb.append(type.open).append(secondary.toString()).append(type.close);
		return sb.toString();
	}

	private static enum ReferenceType
	{
		NONE("", ""), BRACKET("[", "]"), BRACE("{", "}");
		
		String open;
		String close;
		
		ReferenceType(String o, String c)
		{
			open = o;
			close = c;
		}
	}
}