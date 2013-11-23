package ambroscum.errors;

public class FunctionNotFoundException extends RuntimeException
{
	public FunctionNotFoundException(String message)
	{
		super(message);
	}
}