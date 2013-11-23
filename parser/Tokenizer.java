package ambroscum.parser;

import ambroscum.error.SyntaxError;

public class Tokenizer
{
	public static TokenStream tokenize(String str)
	{
		TokenStream stream = new TokenStream();
		
		char[] array = str.toCharArray();
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == ' ')
				throw new SyntaxError("Unexpected whitespace");
			if (array[i] == '"')
			{
				int j = i++;
				while (i < array.length && array[i] != '"')
					i++;
				if (i == array.length)
					throw new SyntaxError("Nonterminating string");
				stream.offer(new Token(str.substring(j, i)));
				i++;
				if (array[i] != ' ')
					throw new SyntaxError("Missing whitespace");
				continue;
			}
			if (isParen(array[i]))
			{
				stream.offer(new Token("" + array[i]));
				continue;
			}
			int j = i++;
			while (i < array.length && !isParen(array[i]) && i != ' ')
				i++;
			stream.offer(new Token(str.substring(j, i)));
			if (i == array.length)
				break;
			if (isParen(array[i]))
				stream.offer(new Token("" + array[i]));
			if (closeParen(array[i]))
			{
				i++;
				if (i < array.length && array[i] != ' ')
					throw new SyntaxError("Missing whitespace");
			}
		}
		return stream;
	}
	
	private static boolean isParen(char c)
	{
		return openParen(c) || closeParen(c);
	}
	
	private static boolean openParen(char c)
	{
		return c == '(' || c == '[' || c == '{';
	}
	
	private static boolean closeParen(char c)
	{
		return c == ')' || c == ']' || c == '}';
	}
}