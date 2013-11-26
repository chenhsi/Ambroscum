package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;
import ambroscum.expressions.Expression;

public class WhileLine extends Line
{
	private Expression condition;
	private Block block;
	
	public WhileLine(TokenStream stream, int indentationLevel)
	{
		condition = Expression.interpret(stream);
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Expected colon after while statement");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after while statement: " + temp);
		block = new Block(stream, indentationLevel + 1);
	}
	
	@Override
	public boolean expectsBlock()
	{
		return false;
	}
	@Override
	public void setBlock(Block b)
	{
		block = b;
	}
	
	@Override
	public void evaluate(IdentifierMap values)
	{
		while (true)
		{
			Value conditionValue = condition.evaluate(values);
			if (conditionValue instanceof BooleanValue)
			{
				if (conditionValue == BooleanValue.TRUE)
					block.evaluate(values);
				else
					break;
			}
			else
				throw new SyntaxError("Expected a boolean for while statement condition: " + condition);
		}
	}
	
	@Override
	public String toString()
	{
		return "(while " + condition + " (" + block + "))";
	}
}