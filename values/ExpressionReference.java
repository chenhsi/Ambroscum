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
		String text = "";
		for (char ch : REFERENCE_DELIMITERS)
		{
			int index = text.indexOf(ch);
			if (index > -1) {
				name = text.substring(0, index);
				secondary = null; //Expression.interpret(text.substring(index + 1, text.length() - 1));
				switch (ch) {
					case '(':
						type = ReferenceType.PARENTHESIS;
						break;
					case '[':
						type = ReferenceType.BRACKET;
						break;
					case '{':
						type = ReferenceType.BRACE;
						break;
				}
				return;
			}
		}

		name = text;
		type = ReferenceType.NONE;
	}
	ExpressionReference(String text) {
		for (char ch : REFERENCE_DELIMITERS) {
			int index = text.indexOf(ch);
			if (index > -1) {
				name = text.substring(0, index);
				secondary = null; //Expression.interpret(text.substring(index + 1, text.length() - 1));
				switch (ch) {
					case '(':
						type = ReferenceType.PARENTHESIS;
						break;
					case '[':
						type = ReferenceType.BRACKET;
						break;
					case '{':
						type = ReferenceType.BRACE;
						break;
				}
				return;
			}
		}

		name = text;
		type = ReferenceType.NONE;
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