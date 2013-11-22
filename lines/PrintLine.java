package ambroscum.lines;

import ambroscum.*;

public class PrintLine extends Line
{
	private Expression[] expressions;
	
	protected PrintLine(String line)
	{
		assignIDs = (Expression[]) line.split(", ");
	}
	
	public void evaluate(IdentifierMap values)
	{
		for (Expression expr : expressions)
			System.out.print(expr.evaluate())
		System.out.println();
	}
}