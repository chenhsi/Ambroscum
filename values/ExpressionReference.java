package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionReference extends Expression
{
	private String name;
	private Expression secondary;
	private ReferenceType type;

	public ExpressionReference(String text) {
		for (int i = 0; i < REFERENCE_DELIMITERS.length; i++) {
			char ch = REFERENCE_DELIMITERS[i];
			int index = str.indexOf(ch);
			if (index > -1) {
				name = str.substring(0, index);
				secondary = str.substring(index + 1, str.length() - 1);
				return;
			}
		}
		// It's just a plain old variable name, without any silly bracket nonsense on the end (or it's not a valid name)
		return IdentifierMap.isValidIdentifier(str);
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

/*	private Identifier primary;
	private Value secondary;
	private ReferenceType type;

	public ExpressionReference(String text)
	{
		String[] components = new String[2];
		if (text.charAt(text.length() - 1) == ']')
		{
			int count = 1, i = text.length() - 2;
			for (; count != 0; i--)
				if (text.charAt(i) == '[')
					count--;
				else if (text.charAt(i) == ']')
					count++;
			components[0] = text.substring(0, i + 1);
			components[1] = text.substring(i + 2, text.length() - 1);
			type = ReferenceType.BRACKET;
		}
		else if (text.charAt(text.length() - 1) == '}')
		{
			int count = 1, i = text.length() - 2;
			for (; count != 0; i--)
				if (text.charAt(i) == '{')
					count--;
				else if (text.charAt(i) == '}')
					count++;
			components[0] = text.substring(0, i + 1);
			components[1] = text.substring(i + 2, text.length() - 1);
			type = ReferenceType.DOT;
		}
		else if (text.contains("."))
		{
			int index = text.lastIndexOf('.');
			components[0] = text.substring(0, index);
			components[1] = text.substring(index + 1);
			type = ReferenceType.DOT;
		}
		else
			throw new IllegalArgumentException();
		primary = Expression.interpret(components[0]);
		secondary = Expression.interpret(components[1]);
	}
*/
	public Value evaluate(IdentifierMap values)
	{
//		switch (type)
//		{
//			case NONE: return primary.evaluate(values);
//			case DOT: return primary.evaluate(values).getField(secondary.evaluate(values));
//			case BRACKET: return primary.evaluate(values).call("[]", secondary.evaluate(values));
//			case BRACE: return primary.evaluate(values).call("{}", secondary.evaluate(values));
//		}
		return null;
	}

	private static enum ReferenceType
	{
		NONE, PARENTHESES, BRACKET, BRACE
	}
	private static final char[] REFERENCE_DELIMITERS = new char[] {'(', '[', '{'};
	private static final char[]    MATCHING_BRACKETS = new char[] {')', ']', '}'};
}