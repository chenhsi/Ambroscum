package ambroscum.error;

public class AssertionError extends RuntimeException
{
	public AssertionError()
	{
		super();
	}
	
	public AssertionError(String message)
	{
		super(message);
	}
}