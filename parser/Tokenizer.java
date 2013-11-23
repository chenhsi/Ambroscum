package ambroscum.parser;

import ambroscum.error.SyntaxError;

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
				stream.offer(new Token(str.substring(j, i)));
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
				stream.offer(new Token("" + str.charAt(i)));
				continue;
			}
			if (closeParen(str.charAt(i)) || str.charAt(i) == ',' || str.charAt(i) == '.')
				throw new SyntaxError("Unexpected token: " + str.charAt(i));
			int j = i++;
			while (i < str.length() && !isSeparator(str.charAt(i)) && !isWhitespace(str.charAt(i)))
				i++;
			stream.offer(new Token(str.substring(j, i)));
			if (i == str.length())
				break;
			if (isSeparator(str.charAt(i)))
				stream.offer(getToken(str.charAt(i)));
			if (str.charAt(i) == '\n')
				stream.offer(Token.NEWLINE);
			if (closeParen(str.charAt(i)) || str.charAt(i) == ',')
			{
				if (i + 1 == str.length())
					break;
				if (str.charAt(i + 1) == '\n' || closeParen(str.charAt(i + 1)))
					continue;
				i++;
				if (str.charAt(i) != ' ')
					throw new SyntaxError("Missing whitespace");
			}
		}
		return stream;
	}
	
	private static Token getToken(char c)
	{
		switch (c)
		{
			case '.': return Token.DOT;
			case 'c': return Token.COMMA;
		}
		return new Token("" + c);
	}
	
	private static boolean isSeparator(char c)
	{
		return openParen(c) || closeParen(c) || c == ',' || c == '.';
	}
	
	private static boolean isWhitespace(char c)
	{
		return c == ' ' || c == '\n';
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