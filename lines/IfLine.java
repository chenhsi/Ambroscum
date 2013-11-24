package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;

public class IfLine extends Line {
	
	private Expression condition;
	private Block block;
	
	public IfLine(TokenStream stream) {
		condition = Expression.interpret(stream);
	}
	
	public boolean expectsBlock() {
		return true;
	}
	public void setBlock(Block b) {
		block = b;
	}
	
	public void evaluate(IdentifierMap values) {
		Value conditionValue = condition.evaluate(values);
		if (conditionValue instanceof BooleanValue) {
			if (((BooleanValue) conditionValue).getValue()) {
				block.evaluate(values);
			}
		}
		throw new SyntaxError("Expected a boolean for if statement condition: " + condition);
	}
}