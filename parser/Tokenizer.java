package ambroscum.parser;

import java.util.LinkedList;
import ambroscum.errors.SyntaxError;

public class Tokenizer
{
	private static String str;
	private static LinkedList<Token> stream;
	
	public static LinkedList<Token> tokenize(String s)
	{
		str = s;
		stream = new LinkedList<Token>();
		int i = 0;
		while (i < str.length())
		{
			switch (str.charAt(i))
			{
				case ')': case ']': case '}': case ' ': case '.': case ',':
					throw new SyntaxError("Unexpected character: " + str.charAt(i));
				
				case ';':
					i = comment(i);
					break;
				case '\t':
					stream.offer(Token.TAB);
					i++;
					break;
				case '(': case '[': case '{':
					i = openParen(i);
					break;
				case '\n':
					i = newline(i);
					break;
				case '"':
					i = string(i);
					if (!isWhitespace(str.charAt(i)))
						throw new SyntaxError("Expecting whitespace after quotes");
					if (str.charAt(i) == ' ')
						i++;
					continue;
				default:
					i = normalToken(i);
			}
		}
//		System.out.println(stream);
		return stream;
	}
	
	private static int newline(int i)
	{
		stream.offer(Token.NEWLINE);
		i++;
		while (i < str.length() && str.charAt(i) == '\t')
		{
			stream.offer(Token.TAB);
			i++;
		}
		return i;
	}
	
	private static int comment(int i)
	{
		while (i < str.length() && str.charAt(i) != '\n')
			i++;
		stream.offer(Token.NEWLINE);
		return i + 1;
	}
	
	private static int openParen(int i)
	{
		char initial = str.charAt(i);
		stream.offer(Token.getToken(initial + ""));
		i++;
		outer: while (i < str.length())
		{
			switch (str.charAt(i))
			{
				case ')': case ']': case '}':
					if (Math.abs(str.charAt(i) - initial) > 2)
						throw new SyntaxError("Mismatched grouping characters");
					else
						break outer;
				
				case '(': case '[': case '{': i = openParen(i); break;
				
				case '\n': throw new SyntaxError("Unexpected end of line in grouping");
				case ';': throw new SyntaxError("Unexpected end of line in grouping");
				case '\t': throw new SyntaxError("Unexpected tab");
				case ' ': throw new SyntaxError("Unexpected whitespace");
				case '.': throw new SyntaxError("Unexpected period");
				case ',': throw new SyntaxError("Unexpected comma");
				
				case '"':
					i = string(i);
					if (!closeParen(str.charAt(i)) && !isWhitespace(str.charAt(i)))
						throw new SyntaxError("Expecting whitespace after quotes");
					continue;
				default:
					i = normalToken(i);
			}
		}
		if (i == str.length())
			throw new SyntaxError("Nonterminating grouping");
		stream.offer(Token.getToken("" + str.charAt(i++)));
		switch (str.charAt(i))
		{
			case ')': case ']': case '}': case '(': case '[': case '{':
				return i;
			case '.':
				if (str.charAt(i + 1) == '\n')
					throw new SyntaxError("Trailing dot at end of line");
				stream.offer(Token.DOT);
				return i + 1;
			case ',':
				stream.offer(Token.COMMA);
				if (str.charAt(i + 1) == '\n' || str.charAt(i + 2) == '\n')
					throw new SyntaxError("Trailing comma at end of line");
				return i + 2;
			case ':':
				stream.offer(Token.COLON);
				return i + 1;
			case ' ':
				if (str.charAt(i + 1) == '\n')
					throw new SyntaxError("Trailing space at end of line");
				return i + 1;
			case ';':
				return comment(i);
			case '\n':
				return newline(i);
			default:
				throw new SyntaxError("Expected whitespace after grouping, found " + str.charAt(i));
		}
	}
	
	private static int string(int i)
	{
		int start = i++;
		while (i < str.length() && str.charAt(i) != '\n' && str.charAt(i) != '"')
			i++;
		if (i == str.length() || str.charAt(i) == '\n')
			throw new SyntaxError("Nonterminating string");
		stream.offer(Token.getToken(str.substring(start, i + 1)));
		return i + 1;
	}
	
	private static int normalToken(int i)
	{
		boolean alphanumeric = Character.isLetterOrDigit(str.charAt(i));
		int start = i++;
		outer: while (i < str.length())
		{
			switch (str.charAt(i))
			{
				case '\t': throw new SyntaxError("Unexpected tab");
				case '"': throw new SyntaxError("Unexpected string");
				
				case '(': case '[': case '{': case ')': case ']': case '}': case '\n': case ' ': case '.': case ',': case ';':
					break outer;
			}
			if (alphanumeric != Character.isLetterOrDigit(str.charAt(i)))
				break;
			i++;
		}
		stream.offer(Token.getToken(str.substring(start, i)));
		if (i == str.length())
			return i;
		switch (str.charAt(i))
		{
			case '(': case '[': case '{':
				return openParen(i);
			case ')': case ']': case '}': 
				return i;
			case ';':
				return comment(i);
			case '\n':
				return newline(i);
			case ' ':
				if (str.charAt(i + 1) != '\n')
					return i + 1;
				else
					throw new SyntaxError("Extra space at end of line");
			case '.':
				stream.offer(Token.DOT);
				return normalToken(i + 1);
			case ',':
				stream.offer(Token.COMMA);
				if (str.charAt(i + 1) == '\n' || str.charAt(i + 2) == '\n')
					throw new SyntaxError("Trailing comma at end of line");
				else if (str.charAt(i + 1) != ' ')
					throw new SyntaxError("Whitespace expected");
				return i + 2;
			default:
				return normalToken(i);
		}
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