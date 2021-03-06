/**
 * Represents assignment lines, that sets references to values.
 * <p>
 * Can represent normal assignments (e.g. <code>x = 3</code>),
 * multi-assignments (e.g. <code>x, y = 3, 4</code>), and compound assignment
 * (e.g. <code>x += 5</code>. Multi-compound assignments, however, are not
 * supported.
 * <p>
 * It is undefined behavior to assign to the same variable or reference twice
 * in a single line, or to cause side effects in referencing the assignment
 * target (e.g. <code>f()[0] = 3</code>, when <code>f</code> has side effects.
 * 
 * @author Chen-Hsi Steven Bi, Jing-Lun Edward Gao
 * @version 1.0
 */

package ambroscum.lines;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.*;
import ambroscum.values.*;
import ambroscum.parser.*;
import ambroscum.expressions.Expression;
import ambroscum.expressions.ExpressionIdentifier;
import ambroscum.expressions.ExpressionReference;
import ambroscum.expressions.ExpressionOperator;
import ambroscum.expressions.ExpressionCall;

public class AssignmentLine extends Line
{
	private List<Expression> assignIDs;
	private List<Expression> exprs;
	private ExpressionOperator operator;

	/**
	 * Constructs an <code>AssignmentLine</code> from the two input streams.
	 * 
	 * @param	idStream	the stream to read assignment references from. This
	 *						stream is expected to have no other tokens in it.
	 *						The last token is used to determine if the
	 *						assignment line represents an compound assignment
	 *						line.
	 * @param	valueStream	the stream to read assignment values from. This
	 *						stream should be the same stream as the rest of the
	 *						input.
	 */

	AssignmentLine(Line parent, TokenStream idStream, TokenStream valueStream)
	{
		super(parent);
		assignIDs = new LinkedList<Expression>();
		assignIDs.add(Expression.extend(new ExpressionIdentifier(idStream.removeFirst()), idStream));
		while (!idStream.getFirst().toString().endsWith("="))
		{
			if (idStream.removeFirst() != Token.COMMA)
				throw new SyntaxError("Expected a comma delimiter in assignment");
			assignIDs.add(Expression.extend(new ExpressionIdentifier(idStream.removeFirst()), idStream));
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
	
	/**
	 * Calculates the values of the assignment expressions using the given
	 * <code>IdentifierMap</code>, and sets them to the specified expressions.
	 *
	 * @param	values	the <code>IdentifierMap</code> referenced to determine
	 *					the assigned values, and where the assigned values are
	 *					set to.
	 * @return	should always return <code>ExitStatus.NORMAL</code>
	 */
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
		{
			Expression target = assignIDs.get(i);
			if (target instanceof ExpressionIdentifier)
				((ExpressionIdentifier) target).setValue(targetVals.get(i), values);
			else if (target instanceof ExpressionReference)
				((ExpressionReference) target).setValue(targetVals.get(i), values);
			else
				throw new NonassignableException(target + " cannot be assigned to");
		}
		return Block.ExitStatus.NORMAL;
	}
	
	/**
	 * Returns a string either in the form "(assign [references] [values])" for
	 * normal assignments or "(assign [references] operator [values])" for
	 * compound assignments.
	 *
	 * @return	a string representation of the line
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("(assign ");
		sb.append(assignIDs).append(" ");
		if (operator != null)
			sb.append(operator).append(" ");
		return sb.append(exprs).append(")").toString();
	}
	
	public List<Expression> getAssignTargets()
	{
		return assignIDs;
	}
	public List<Expression> getAssignValues()
	{
		return exprs;
	}
	public ExpressionOperator getAssignType()
	{
		return operator;
	}
	
	@Override
	public Line localOptimize()
	{
		for (int i = 0; i < exprs.size(); i++)
			exprs.set(i, exprs.get(i).localOptimize());
		return this;
	}
	
	@Override
	public void setDeclarations(Map<String, Expression> declarations, boolean certainty)
	{
		for (Expression expr : exprs)
			expr.setDeclarations(declarations);
		for (int i = 0; i < assignIDs.size(); i++)
		{
			Expression expr = assignIDs.get(i);
			if (expr instanceof ExpressionReference || ((ExpressionIdentifier) expr).getParent() != null)
				throw new UnsupportedOperationException("can't deal with complicated declarations yet");
			String name = ((ExpressionIdentifier) expr).getReference();
			declarations.put(name, certainty ? exprs.get(i) : null);
		}
	}
}