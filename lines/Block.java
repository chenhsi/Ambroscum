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
	public Object getAssociatedValue()
	{
		return value;
	}
	
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
	
	public String toString()
	{
		return lines.toString();
	}
	
	public enum ExitStatus
	{
		NORMAL, RETURN, CONTINUE, BREAK
	}
}