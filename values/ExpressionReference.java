package ambroscum.values;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import java.util.*;

public class ExpressionReference extends Expression
{
	private String name;
	private Expression secondary;
	private ReferenceType type;

	public ExpressionReference(TokenStream stream)
	{
		name = stream.removeFirst().toString();
		String delimiter = (stream.size() > 0) ? stream.getFirst().toString() : null;
		if ("(".equals(delimiter)) {
			stream.removeFirst();
			type = ReferenceType.PARENTHESIS;
			secondary = Expression.interpret(stream);
		} else if ("[".equals(delimiter)) {
			stream.removeFirst();
			type = ReferenceType.BRACKET;
			secondary = Expression.interpret(stream);
		} else if ("{".equals(delimiter)) {
			stream.removeFirst();
			type = ReferenceType.BRACE;
			secondary = Expression.interpret(stream);
		} else { // No special stuff afterward
			type = ReferenceType.NONE;
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
	public void setValue(Value value, IdentifierMap values) {
		switch (type) {
			case NONE:
				values.set(name, value);
				break;
			default:
				throw new UnsupportedOperationException("Arrays and stuff don't work yet.");
		}
	}

	public static boolean isValidReference(String str) {
		for (int i = 0; i < REFERENCE_DELIMITERS.length; i++) {
			char ch = REFERENCE_DELIMITERS[i];
			int index = str.indexOf(ch);
			if (index > -1) {
				// It's something like variableName[expression stuff that we won't check here]
				if (IdentifierMap.isValidIdentifier(str.substring(0, index)) && str.charAt(str.length() - 1) == MATCHING_BRACKETS[i]) {
					return true;
				}
			}
		}
		// It's just a plain old variable name, without any silly bracket nonsense on the end (or it's not a valid name)
		return IdentifierMap.isValidIdentifier(str);
	}

	private static enum ReferenceType
	{
		NONE, PARENTHESIS, BRACKET, BRACE
	}
	private static final char[] REFERENCE_DELIMITERS = new char[] {'(', '[', '{'};
	private static final char[]    MATCHING_BRACKETS = new char[] {')', ']', '}'};
}