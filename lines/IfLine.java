package ambroscum.lines;

import java.util.List;
import java.util.ArrayList;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;
import ambroscum.expressions.Expression;

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
					Block.ExitStatus evaled = blocks.get(i).evaluate(values);
					if (evaled == Block.ExitStatus.RETURN)
						setReturnValue(blocks.get(i).getReturnValue());
					return evaled;
				}
			}
			else
				throw new SyntaxError("Expected a boolean for if statement condition: " + conditions.get(i));
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
}