package ambroscum.parser;

public class Token
{
	public static final Token NEWLINE = new Token("\n");
	public static final Token TAB = new Token("\t");
	public static final Token COMMA = new Token(",");
	public static final Token DOT = new Token(".");
	public static final Token COLON = new Token(":");
	
	private String str;

	private Token(String str)
	{
		this.str = str;
	}
	
	public static Token getToken(String str)
	{
		if (str.equals("\n"))
			return NEWLINE;
		if (str.equals("\t"))
			return TAB;
		if (str.equals(","))
			return COMMA;
		if (str.equals(","))
			return DOT;
		if (str.equals(":"))
			return COLON;
		return new Token(str);
	}

	public String toString()
	{
		return str;
	}
}