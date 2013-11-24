package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;

public class WhileLine extends Line
{
	private Expression condition;
	private Block block;
	
	public WhileLine(TokenStream stream)
	{
		condition = Expression.interpret(stream);
		System.out.println(condition + " " + stream);
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Expected colon after while statement");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after while statement: " + temp);
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
		while (true)
		{
			Value conditionValue = condition.evaluate(values);
			if (conditionValue instanceof BooleanValue)
			{
				if (conditionValue == BooleanValue.True)
					block.evaluate(values);
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