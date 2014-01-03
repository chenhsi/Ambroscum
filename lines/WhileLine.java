package ambroscum.lines;

import java.util.Map;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionLiteral;

public class WhileLine extends Line
{
	private Expression condition;
	private Block block;
	private Block thenBlock;
	
	public WhileLine(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		condition = Expression.interpret(stream);
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Expected colon after while statement");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after while statement: " + temp);
		block = new Block(this, stream, indentationLevel + 1);
		if (stream.hasNext() && stream.getFirst().toString().equals("then"))
		{
			stream.removeFirst();
			if (stream.removeFirst() != Token.COLON)
				throw new SyntaxError("Expected colon after then statement");
			temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token after then statement: " + temp);
			thenBlock = new Block(this, stream, indentationLevel + 1);
		}
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		boolean normalTermination = true;
		while (true)
		{
			Value conditionValue = condition.evaluate(values);
			if (conditionValue instanceof BooleanValue)
			{
				if (conditionValue == BooleanValue.TRUE)
				{
					if (block == null)
						throw new InfiniteLoopException("Empty non-false while loop");
					Block.ExitStatus status = block.evaluate(values);
					if (status == Block.ExitStatus.BREAK)
					{
						normalTermination = false;
						break;
					}
					if (status == Block.ExitStatus.RETURN)
					{
						setReturnValue(block.getReturnValue());
						return Block.ExitStatus.RETURN;
					}
				}
				else
					break;
			}
			else
				throw new SyntaxError("Expected a boolean for while statement condition: " + condition);
		}
		if (normalTermination && thenBlock != null)
		{
			Block.ExitStatus status = thenBlock.evaluate(values);
			if (status == Block.ExitStatus.RETURN)
				setReturnValue(thenBlock.getReturnValue());
			return status;
		}
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(while ");
		sb.append(condition).append(" (").append(block).append(")");
		if (thenBlock != null)
			sb.append(" ").append(thenBlock);
		return sb.append(")").toString();
	}
	
	public Expression getCondition()
	{
		return condition;
	}
	public Block getBlock()
	{
		return block;
	}
	public Block getThenBlock()
	{
		return thenBlock;
	}
	
	@Override
	public Line localOptimize()
	{
		condition = condition.localOptimize();
		if (thenBlock != null)
			thenBlock = (Block) thenBlock.localOptimize();
		if (condition instanceof ExpressionLiteral)
		{
			Value conditionValue = ((ExpressionLiteral) condition).getValue();
			if (conditionValue instanceof BooleanValue)
			{
				if (conditionValue == BooleanValue.FALSE)
					return thenBlock;
				block = (Block) block.localOptimize();
				return this;
			}
			else
				throw new OptimizedException(new SyntaxError("Expected a boolean for while statement condition: " + conditionValue));
		}
		if (block == null)
			block = (Block) block.localOptimize();
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		if (block != null)
			block.setDeclarations(declarations, false);
		condition.setDeclarations(declarations);
		if (thenBlock != null)
			thenBlock.setDeclarations(declarations, false);
	}
}