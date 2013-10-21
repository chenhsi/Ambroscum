package ambroscum.lines;

import ambroscum.*;

public class PrintLine extends Line
{
	private Expression[] expressions;
	
	protected PrintLine(String line)
	{
		assignIDs = (Expression[]) left.split(", ");
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