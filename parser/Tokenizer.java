package ambroscum.parser;

import ambroscum.errors.SyntaxError;

public class Tokenizer
{
	public static TokenStream tokenize(String str)
	{
		TokenStream stream = new TokenStream();
		
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '\n')
			{
				stream.offer(Token.NEWLINE);
				while (i + 1 < str.length() && str.charAt(i + 1) == '\t')
				{
					i++;
					stream.offer(Token.TAB);
				}
				continue;
			}
			if (str.charAt(i) == ' ')
				throw new SyntaxError("Unexpected whitespace");
			if (str.charAt(i) == '"')
			{
				int j = i++;
				while (i < str.length() && str.charAt(i) != '\n' && str.charAt(i) != '"')
					i++;
				if (i == str.length() || str.charAt(i) == '\n')
					throw new SyntaxError("Nonterminating string");
				stream.offer(new Token(str.substring(j, i + 1)));
				if (i + 1 == str.length())
					break;
				if (str.charAt(i + 1) == '\n')
					continue;
				i++;
				if (str.charAt(i) != ' ')
					throw new SyntaxError("Missing whitespace");
				continue;
			}
			if (openParen(str.charAt(i)))
			{
				stream.offer(getToken(str.charAt(i)));
				if (i + 1 == str.length())
					throw new SyntaxError("Unclosed grouping");
				if (closeParen(str.charAt(i + 1)))
				{
					if (matching(str.charAt(i), str.charAt(i + 1)))
						stream.offer(getToken(str.charAt(i + 1)));
					else
						throw new SyntaxError("non-matching grouping");
					i++;
				}
				continue;
			}
			if (closeParen(str.charAt(i)) || str.charAt(i) == ',' || str.charAt(i) == '.')
				throw new SyntaxError("Unexpected token: " + str.charAt(i));
			int j = i++;
			while (i < str.length() && !isSeparator(str.charAt(i)) && !isWhitespace(str.charAt(i)))
				i++;
			stream.offer(new Token(str.substring(j, i)));
			while (i < str.length() && closeParen(str.charAt(i)))
				stream.offer(getToken(str.charAt(i++)));
			if (i == str.length())
				break;
			if (str.charAt(i) == ',')
			{
				stream.offer(Token.COMMA);
				i++;
			}
			if (str.charAt(i) == ':')
			{
				stream.offer(Token.COLON);
				continue;
			}
			if (!isWhitespace(str.charAt(i)) && !openParen(str.charAt(i)))
				throw new SyntaxError("Whitespace expected: " + i);
			if (str.charAt(i) == '\t')
				throw new SyntaxError("Unexpected tab");
			if (str.charAt(i) == '\n' || openParen(str.charAt(i)))
				i--;
		}
		System.out.println(stream);
		return stream;
	}
	
	private static Token getToken(char c)
	{
		switch (c)
		{
			case '\n': return Token.NEWLINE;
			case '\t': return Token.TAB;
			case '.': return Token.DOT;
			case ',': return Token.COMMA;
			case ':': return Token.COLON;
		}
		return new Token("" + c);
	}
	
	private static boolean isSeparator(char c)
	{
		return openParen(c) || closeParen(c) || c == ',' || c == '.';
	}
	
	private static boolean isWhitespace(char c)
	{
		return c == ' ' || c == '\n' || c == '\t';
	}
	
	private static boolean openParen(char c)
	{
		return c == '(' || c == '[' || c == '{';
	}
	
	private static boolean closeParen(char c)
	{
		return c == ')' || c == ']' || c == '}';
	}
	
	private static boolean matching(char a, char b)
	{
		return a == '(' && b == ')' || a == '[' && b == ']' || a == '{' && b == '}';
	}
}