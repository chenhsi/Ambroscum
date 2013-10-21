// need to make ids be able to be non-simple (e.g. a[3] or b.c), but that takes effort
// not sure how to represent them, since don't want to re-evaluate every time

package ambroscum.lines;

import ambroscum.*;

public class AssignmentLine extends Line
{
	private Type declareAs; // null if no declaration in this line
	private String[] assignIDs; // list of the ids being set to
	private Expression[] expressions;
	
	public AssignmentLine(String left, String right)
	{
		String[] ids = left.split(", ");
		if (ids[0].contains(" "))
		{
			declareAs = (Type) (Object) ids[0].substring(ids[0].indexOf(" ")); // this so doesn't work
			ids[0] = ids[0].substring(ids[0].indexOf(" ") + 1);
		}
		assignIDs = ids;
		
		ids = right.split(", ");
		if (ids.length != assignIDs.length)
			throw new RuntimeException("need to find better exceptions") // comment to be easier to find
		expressions = new Expression[ids.length];
		for (int i = 0; i < ids.length; i++)
			expressions[i] = (Expression) (Object) ids[i]; // again, will add this later
	}
	
	public void evaluate(IdentifierMap values)
	{
		if (declareAs == null)
			for (int i = 0; i < expressions.length; i++)
				values.add(assignIDs[i], declareAs, expressions[i].evaluate(values));
		else
			for (int i = 0; i < expressions.length; i++)
				values.set(assignIDs[i], expressions[i].evaluate(values));
	}
}