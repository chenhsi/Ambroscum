package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.lines.*;
import ambroscum.parser.*;
import java.util.*;

public class Block extends Line {
	
	private ArrayList<Line> lines;
	
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