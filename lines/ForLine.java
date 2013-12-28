package ambroscum.lines;

import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.parser.*;
import ambroscum.values.*;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionReference;

public class ForLine extends Line
{
	private ExpressionReference variable;
	private Expression iterable;
	private Block block;
	private Block thenBlock;
	
	public ForLine(Line parent, TokenStream stream, int indentationLevel)
	{
		super(parent);
		Token variableName = stream.removeFirst();
		if (!IdentifierMap.isValidIdentifier(variableName.toString()))
			throw new SyntaxError("Invalid variable name in for statement: " + variableName);
		variable = ExpressionReference.createExpressionReference(variableName);
		Token temp = stream.removeFirst();
		if (!temp.toString().equals("in"))
			throw new SyntaxError("Unexpected token in reading for statement: " + temp);
		iterable = Expression.interpret(stream);
		temp = stream.removeFirst();
		if (temp != Token.COLON)
			throw new SyntaxError("Expected colon after for statement, found " + temp);
		temp = stream.removeFirst();
		if (temp != Token.NEWLINE)
			throw new SyntaxError("Unexpected token after for statement: " + temp);
		block = new Block(this, stream, indentationLevel + 1);
		if (stream.hasNext() && stream.getFirst().toString().equals("then"))
		{
			stream.removeFirst();
			temp = stream.removeFirst();
			if (temp != Token.COLON)
				throw new SyntaxError("Expected colon after then statement, found " + temp);
			temp = stream.removeFirst();
			if (temp != Token.NEWLINE)
				throw new SyntaxError("Unexpected token after then statement: " + temp);
			thenBlock = new Block(this, stream, indentationLevel + 1);
		}
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		Value evalIter = iterable.evaluate(values);
		if (!(evalIter  instanceof ListValue))	// this will probably be changed
			throw new InvalidArgumentException(evalIter + " does not evaluate to a list value"); // is this a good exception?
		ListValue asList = (ListValue) evalIter;
		boolean normalTermination = true;
		for (int i = 0; i < ((IntValue) asList.dereference("size")).getValue(); i++)
		{
			variable.setValue(asList.get(IntValue.fromInt(i)), values);
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
		StringBuilder sb = new StringBuilder("(for ");
		sb.append(variable).append(" ").append(iterable).append(" ").append(block);
		if (thenBlock != null)
			sb.append(" ").append(thenBlock);
		return sb.append(")").toString();
	}
}