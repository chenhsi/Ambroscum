package ambroscum.parser;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import ambroscum.errors.SyntaxError;

public class TokenStream extends LinkedList<Token>
{
	public Token getFirst()
	{
		try
		{
			return super.getFirst();
		}
		catch (NoSuchElementException ex)
		{
			throw new SyntaxError("Unexpected end of input");
		}
	}
	
	public Token removeFirst()
	{
		try
		{
			return super.removeFirst();
		}
		catch (NoSuchElementException ex)
		{
			throw new SyntaxError("Unexpected end of input");
		}
	}
}