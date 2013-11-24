package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.lines.*;
import ambroscum.parser.*;
import java.util.*;

public class Block extends Line {
	
	public static final Block OUTER_BLOCK = new Block(null);
	static {
		OUTER_BLOCK.indentation = 0;
	}
	
	private ArrayList<Line> lines;
	private Block parent;
	private int indentation;
	
	public Block(Block p) {
		lines = new ArrayList<>();
		parent = p;
		if (parent != null)
			indentation = parent.indentation + 1;
	}
	
	public boolean expectsBlock() {
		return false;
	}
	public void setBlock(Block b) {}
	
	public Block readLines(TokenStream tokens) {
		TokenStream a = new TokenStream();
		a.addAll(tokens);
		Line lineLine;
		try {
			lineLine = Line.evalAsLine(tokens, 0);
		} catch (SyntaxError ex) {
			if (ex.getMessage().equals("Missing indentation")) {
				parent.readLines(a);
				return parent;
			}
			throw ex;
		}
		if (lineLine instanceof EmptyLine)
			return parent;
		lines.add(lineLine);
		if (lineLine.expectsBlock()) {
			Block next = new Block(this);
			return next;
		}
		return this;
	}
	
	public int getIndentation() {
		return indentation;
	}
	
	public void evaluate(IdentifierMap values) {
//		lineLine.evaluate(values);
	}
}