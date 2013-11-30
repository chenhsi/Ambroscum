// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.*;
import ambroscum.parser.*;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionReference;
import ambroscum.expressions.ExpressionOperator;
import ambroscum.expressions.ExpressionCall;

public class AssignmentLine extends Line
{
	private List<ExpressionReference> assignIDs; // list of the ids being assigned to
	private List<Expression> exprs;
	private ExpressionOperator operator;

	AssignmentLine(TokenStream idStream, TokenStream valueStream)
	{
		assignIDs = new LinkedList<ExpressionReference>();
		assignIDs.add(ExpressionReference.createExpressionReference(idStream.removeFirst(), idStream));
		while (!idStream.getFirst().toString().endsWith("="))
		{
			if (idStream.removeFirst() != Token.COMMA)
				throw new SyntaxError("Expected a comma delimiter in assignment");
			assignIDs.add(ExpressionReference.createExpressionReference(idStream.removeFirst(), idStream));
		}
		String assignOp = idStream.getFirst().toString();
		if (assignOp.length() > 1)
			operator = new ExpressionOperator(assignOp.substring(0, assignOp.length() - 1));
		
		exprs = new LinkedList<Expression>();
		exprs.add(Expression.interpret(valueStream));
		while (valueStream.getFirst() != Token.NEWLINE)
		{
			if (valueStream.removeFirst() != Token.COMMA)
				throw new SyntaxError("Expected a comma delimiter in assignment");
			exprs.add(Expression.interpret(valueStream));
		}
		valueStream.removeFirst();

		if (assignIDs.size() != exprs.size())
			throw new SyntaxError("Assignment targets and values differ in number: " + assignIDs + " = " + exprs);
	}
	
	@Override
	public Block.ExitStatus evaluate(IdentifierMap values)
	{
		List<Value> targetVals = new LinkedList<Value> ();
		for (int i = 0; i < exprs.size(); i++)
		{
			Expression expr = exprs.get(i);
			if (operator != null)
				expr = new ExpressionCall(operator, assignIDs.get(i), expr);
			targetVals.add(expr.evaluate(values));
		}
		for (int i = 0; i < exprs.size(); i++)
			assignIDs.get(i).setValue(targetVals.get(i), values);
		return Block.ExitStatus.NORMAL;
	}
	
	@Override
	public String toString()
	{
		return "(assign " + assignIDs + " " + exprs + ")";
	}
}
