/**
 * Represents assert lines, that raises an error if the asserted condition is
 * false.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import ambroscum.*;
import ambroscum.parser.TokenStream;
import ambroscum.parser.Token;
import ambroscum.errors.AssertionError;
import ambroscum.errors.SyntaxError;
import ambroscum.values.BooleanValue;
import ambroscum.values.Value;
import ambroscum.expressions.Expression;

public class AssertLine extends Line
{
	private Expression test, errorMessage;

	/**
	 * Constructs a new <code>AssertLine</code> object, based on the suppied
	 * <code>InputStream</code>.
	 * <p>
	 * Expects the next tokens in the stream to either be an expression and
	 * then a newline; or an expression, colon, expression, and then a newline.
	 *
	 * @param	stream	the <code>InputStream</code> to read tokens from
	 */
	AssertLine(Line parent, TokenStream stream)
	{
		super(parent);
		test = Expression.interpret(stream);
		Token token = stream.removeFirst();
		if (token != Token.NEWLINE)
		{
			if (!token.toString().equals(":"))
				throw new SyntaxError("Expecting ':' token in assert line");
			errorMessage = Expression.interpret(stream);
			Token temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token in assert line: " + temp);
		}
	}
	
	/**
	 * Raises an error if the assert condition is true.
	 *
	 * @param	values	the <code>IdentifierMap</code> referenced to both check
	 *					if the asserted condition is true, as well as to
	 *					evaluate the error message
	 * @return	<code>ExitStatus.NORMAL</code> if the assert condition is
	 *			false, and errors with no return value otherwise
	 * @see		ambroscum.errors.AssertionError
	 */
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		Value testVal = test.evaluate(values);
		if (!(testVal.equals(BooleanValue.TRUE)))
		{
			if (errorMessage == null)
				throw new AssertionError("Assertion failed" + ": " + errorMessage.evaluate(values).toString());
			else
				throw new AssertionError("Assertion failed");
		}
		return Block.ExitStatus.NORMAL;
	}
	
	/**
	 * Returns a string either in the form "(assert condition)" or in the form
	 * "(assert condition : errorMessage)".
	 *
	 * @return	a string representation of the line
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(assert ").append(test.toString());
		if (errorMessage != null)
			sb.append(" : ").append(errorMessage.toString());
		return sb.append(")").toString();
	}
	
	public Expression getTest()
	{
		return test;
	}
	public Expression getErrorMessage()
	{
		return errorMessage;
	}
}