// why am I so bad at coming up with names

/*
 * represents an exception found during compiling that will always occur during
 * runtime if the relevant part of code is executed, e.g. if (1) will always
 * throw an exception
 *
 * while there may be legitimate occurences for having this, I'm going to have
 * an exception be thrown for now while a compromising solution is found
 */


package ambroscum.errors;

public class OptimizedException extends AmbroscumError
{
	public OptimizedException(AmbroscumError foundException)
	{
		super(foundException);
	}
}