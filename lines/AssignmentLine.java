// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import ambroscum.*;

public class AssignmentLine extends Line
{
	private String[] assignIDs; // list of the ids being set to
	private Expression[] expressions;
	
	AssignmentLine(String left, String right)
	{
		assignIDs = left.split(", ");
		String[] ids = right.split(", ");
		if (ids.length != assignIDs.length)
			throw new RuntimeException("need to find better exceptions"); // comment to be easier to find
		expressions = new Expression[ids.length];
		for (int i = 0; i < ids.length; i++)
			expressions[i] = (Expression) (Object) ids[i]; // again, will add this later
	}
	
	public void evaluate(IdentifierMap values)
	{
		for (int i = 0; i < expressions.length; i++)
			values.set(assignIDs[i], expressions[i].evaluate(values));
	}
}
