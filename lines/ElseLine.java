package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;

public class ElseLine extends Line {
	
	private Block block;
	
	public ElseLine() {
	}
	
	@Override
	public boolean expectsBlock() {
		return true;
	}
	@Override
	public void setBlock(Block b) {
		block = b;
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		return block.evaluate(values);
	}
	
	@Override
	public String toString() {
		return "(else " + block + ")";
	}
}