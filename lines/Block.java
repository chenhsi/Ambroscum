/**
 * Represents a group of lines in the same scope. Currently, there are blocks
 * associated with each conditional, loop, and function declaration line.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.lines.*;
import ambroscum.parser.*;
import java.util.*;

public class Block extends Line
{
	private List<Line> lines;
	private Object value;
	
	/**
	 * Constructs a <code>Block</code> from the given input stream of tokens,
	 * with the specified indentation level.
	 *
	 * @param	stream				the stream to read the block lines from
	 * @param	indentationLevel	the level of indentation of the block
	 */
	public Block(TokenStream stream, int indentationLevel)
	{
		lines = new LinkedList<Line>();
		while (true)
		{
			Line line = Line.interpret(stream, indentationLevel);
			if (line instanceof EndLine)
				break;
			lines.add(line);
		}
	}

	// holy crap this is such a hacky solution
	// seriously is there no other way of dealing with this
	// currently using/planning to use this for:
	//    return values, break/continue labels
	// I guess I could break them into separate methods
	// but that creates baggage and makes other lines qq
	// alternatively, a wrapper around ExitStatus?
	/**
	 * Returns an extra value stored from the last call to <code>evaluate</code>.
	 * <p>
	 * If the last call to <code>evaluate</code> returned an <code>ExitStatus.RETURN</code>
	 * the stored value is the return value. If the last call to <code>evaluate</code>
	 * returned an <code>ExitStatus.BREAK</code> or <code>ExitStatus.CONTINUE</code>,
	 * the stored value is the label of the loop that the return status refers
	 * to.
	 *
	 * @return	the stored value
	 */
	public Object getAssociatedValue()
	{
		return value;
	}

	/**
	 * Evaluates each line of the block.
	 *
	 * @param	values	the <code>IdentifierMap</code> used to evaluate each
	 *					line of the block
	 * @return			the termination status of the block
	 */
	public ExitStatus evaluate(IdentifierMap values)
	{
		for (Line line : lines)
		{
			ExitStatus status = line.evaluate(values);
			if (status != ExitStatus.NORMAL)
			{
				if (status == ExitStatus.RETURN)
					value = ((ReturnLine) line).getValue();
				return status;
			}
		}
		return ExitStatus.NORMAL;
	}
	
	/**
	 * Returns a representation of the list of statements in the block, in the
	 * form <code>[line, line, etc...]</code>.
	 *
	 * @return	the string representation of the block
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(");
		boolean first = true;
		for (Line line : lines)
		{
			if (!first)
				sb.append(" ");
			sb.append(line);
			first = false;
		}
		return sb.append(")").toString();
	}
	
	/**
	 * Represents the termination status of a block.
	 */
	public enum ExitStatus
	{
		NORMAL, RETURN, CONTINUE, BREAK
	}
}