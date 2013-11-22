package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class ExpressionReference extends Expression
{
	private Identifier primary;
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
	
	public Value evaluate(IdentifierMap values)
	{
		switch (type)
		{
			case ReferenceType.NONE: return primary.evaluate(values);
			case ReferenceType.DOT: return primary.evaluate(values).getField(secondary.evaluate(values));
			case ReferenceType.BRACKET: return primary.evaluate(values).call("[]", secondary.evaluate(values));
			case ReferenceType.BRACE: return primary.evaluate(values).call("{}", secondary.evaluate(values));
		}
		assert false;
	}
	
	private enum ReferenceType
	{
		NONE, DOT, BRACKET, BRACE
	}
}