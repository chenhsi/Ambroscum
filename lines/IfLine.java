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
	
	@Override
	public boolean expectsBlock()
	{
		return true;
	}
	@Override
	public void setBlock(Block b)
	{
		block = b;
	}
	
	@Override
	public void evaluate(IdentifierMap values)
	{
		Value conditionValue = condition.evaluate(values);
		if (conditionValue instanceof BooleanValue)
		{
			if (conditionValue == BooleanValue.TRUE)
				block.evaluate(values);
		}
		throw new SyntaxError("Expected a boolean for if statement condition: " + condition);
	}
	
	@Override
	public String toString()
	{
		return "(if " + condition + " (" + block + "))";
	}
}