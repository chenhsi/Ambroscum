package ambroscum.errors;

public class AssertionError extends RuntimeException
{
	public AssertionError(String message)
	{
		super(message);
	}
}