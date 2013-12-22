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
import ambroscum.values.Value;
import java.util.*;

public class Block extends Line
{
	private List<Line> lines;
	private Value returnValue;
	
	/**
	 * Constructs a <code>Block</code> from the given input stream of tokens,
	 * with the specified indentation level.
	 *
	 * @param	stream				the stream to read the block lines from
	 * @param	indentationLevel	the level of indentation of the block
	 */
	public Block(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		lines = new LinkedList<Line>();
		while (true)
		{
			Line line = Line.interpret(this, stream, indentationLevel);
			if (line instanceof EndLine)
				break;
			lines.add(line);
		}
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
				return status;
		}
		return ExitStatus.NORMAL;
	}

	public Value getReturnValue()
	{
		return returnValue;
	}
	
	protected void setReturnValue(Value value)
	{
		returnValue = value;
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