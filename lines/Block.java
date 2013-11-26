package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.lines.*;
import ambroscum.parser.*;
import java.util.*;

public class Block extends Line {
	
	private List<Line> lines;
	
	public Block(TokenStream stream, int indentationLevel)
	{
		lines = new LinkedList<Line>();
		while (true)
		{
			Line line = Line.interpret(stream, indentationLevel);
			if (line instanceof EndLine)
				break;
			if (line.expectsBlock())
				line.setBlock(new Block(stream, indentationLevel + 1));
			lines.add(line);
		}
	}

	public Block(ArrayList<Line> lines) {
		this.lines = lines;
	}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	public void evaluate(IdentifierMap values) {
		for (Line l : lines) {
			l.evaluate(values);
		}
	}
	
	public String toString()
	{
		return lines.toString();
	}
}