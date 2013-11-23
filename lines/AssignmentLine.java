// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import ambroscum.*;

public class AssignmentLine extends Line
{
	private String[] assignIDs; // list of the ids being set to
	private Expression[] expressions;

	AssignmentLine(TokenStream stream)
	{
		assignIDs = left.split(", ");
		String[] exprs = right.split(", ");
		if (exprs.length != assignIDs.length)
			throw new RuntimeException("need to find better exceptions"); // comment to be easier to find
		expressions = new Expression[exprs.length];
		for (int i = 0; i < exprs.length; i++)
			expressions[i] = Expression.interpret(exprs[i]); // again, will add this later
	}

	public void evaluate(IdentifierMap values)
	{
		for (int i = 0; i < expressions.length; i++)
			values.set(assignIDs[i], expressions[i].evaluate(values));
	}
}
