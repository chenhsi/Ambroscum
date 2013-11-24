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
		System.out.println(condition + " " + stream);
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Expected colon after if statement");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after if statement: " + temp);
		
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
	
	public String toString() {
		return "(if " + condition + " " + block + " end)";
	}
}