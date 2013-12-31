package ambroscum.errors;

// this name is unwieldy and hard to remember, maybe change?
public class NotDereferenceableException extends AmbroscumError
{
	public NotDereferenceableException(String message)
	{
		super(message);
	}
}