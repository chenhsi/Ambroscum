package ambroscum.lines;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionLiteral;

public class IfLine extends Line
{
	private List<Expression> conditions;
	private List<Block> blocks;
	private Block elseBlock;
	
	public IfLine(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		conditions = new ArrayList<Expression> ();
		blocks = new ArrayList<Block> ();
		conditions.add(Expression.interpret(stream));
		if (stream.removeFirst() != Token.COLON)
			throw new SyntaxError("Expected colon after if statement");
		Token temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after if statement: " + temp);
		blocks.add(new Block(this, stream, indentationLevel + 1));
		while (stream.hasNext() && stream.getFirst().toString().equals("elif"))
		{
			stream.removeFirst();
			conditions.add(Expression.interpret(stream));
			temp = stream.removeFirst();
			if (temp != Token.COLON)
				throw new SyntaxError("Expected colon after elif");
			temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token after elif:" + temp);
			blocks.add(new Block(this, stream, indentationLevel + 1));
		}
		if (stream.hasNext() && stream.getFirst().toString().equals("else"))
		{
			stream.removeFirst();
			temp = stream.removeFirst();
			if (temp != Token.COLON)
				throw new SyntaxError("Expected colon after else");
			temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token after else:" + temp);
			elseBlock = new Block(this, stream, indentationLevel + 1);
		}
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		for (int i = 0; i < conditions.size(); i++)
		{
			Value conditionValue = conditions.get(i).evaluate(values);
			if (conditionValue instanceof BooleanValue)
			{
				if (conditionValue == BooleanValue.TRUE)
				{
					if (blocks.get(i) == null)
						return Block.ExitStatus.NORMAL;
					Block.ExitStatus evaled = blocks.get(i).evaluate(values);
					if (evaled == Block.ExitStatus.RETURN)
						setReturnValue(blocks.get(i).getReturnValue());
					return evaled;
				}
			}
			else
				// should this really be a syntax error?
				throw new SyntaxError("Expected a boolean for if statement condition: " + conditionValue);
		}
		if (elseBlock != null)
		{
			Block.ExitStatus evaled = elseBlock.evaluate(values);
			if (evaled == Block.ExitStatus.RETURN)
				setReturnValue(elseBlock.getReturnValue());
			return evaled;
		}
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(if");
		for (int i = 0; i < conditions.size(); i++)
			sb.append(" (").append(conditions.get(i)).append(" " ).append(blocks.get(i)).append(")");
		if (elseBlock != null)
			sb.append(" ").append(elseBlock);
		return sb.append(")").toString();
	}
	
	public List<Expression> getConditions()
	{
		return conditions;
	}
	public List<Block> getClauses()
	{
		List<Block> clauses = new ArrayList<Block> (blocks);
		clauses.add(elseBlock);
		return clauses;
	}
	
	@Override
	public Line localOptimize()
	{
		for (int i = 0; i < conditions.size();)
		{
			conditions.set(i, conditions.get(i).localOptimize());
			if (conditions.get(i) instanceof ExpressionLiteral)
			{
				Value conditionValue = ((ExpressionLiteral) conditions.get(i)).getValue();
				if (conditionValue instanceof BooleanValue)
				{
					if (conditionValue == BooleanValue.TRUE)
						return blocks.get(i).localOptimize();
					else
					{
						conditions.remove(i);
						blocks.remove(i);
						continue;
					}
				}
				else
					throw new OptimizedException(new SyntaxError("Expected a boolean for if statement condition: " + conditionValue));
			}
			if (blocks.get(i) != null)
				blocks.set(i, (Block) blocks.get(i).localOptimize());
			i++;
		}
		if (elseBlock != null)
			elseBlock = (Block) elseBlock.localOptimize();
		if (conditions.size() == 0)
			return elseBlock;
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		for (int i = 0; i < conditions.size(); i++)
		{
			conditions.get(i).setDeclarations(declarations);
			if (blocks.get(i) != null)
				blocks.get(i).setDeclarations(declarations, false);
		}
		if (elseBlock != null)
			elseBlock.setDeclarations(declarations, false);
	}
	
	@Override
	protected boolean endsWithReturn()
	{
		for (Block block : blocks)
			if (block != null && !block.endsWithReturn())
				return false;
		if (elseBlock != null)
			return elseBlock.endsWithReturn();
		return true;
	}
}