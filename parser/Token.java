package ambroscum.parser;

public class Token
{
	public static final Token NEWLINE = new Token("\n");
	public static final Token TAB = new Token("\t");
	public static final Token COMMA = new Token(",");
	public static final Token DOT = new Token(".");
	
	private String str;

	public Token(String str)
	{
		this.str = str;
	}

	public String toString()
	{
		return str;
	}
}