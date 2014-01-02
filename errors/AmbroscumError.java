package ambroscum.errors;

public class AmbroscumError extends RuntimeException {
	
	public AmbroscumError(String message) {
		super(message);
	}
	public AmbroscumError(AmbroscumError exception) {
		super(exception);
	}
}