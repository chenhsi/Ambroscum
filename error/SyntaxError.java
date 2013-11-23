package ambroscum.error;

public class SyntaxError extends RuntimeException
{
	public SyntaxError()
	{
		super();
	}
	
	public SyntaxError(String message)
	{
		super(message);
	}
}