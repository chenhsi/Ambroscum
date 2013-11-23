package ambroscum.lines;

import ambroscum.*;

public class PrintLine extends Line
{
	private Expression[] toPrint;
	
	PrintLine(String line)
	{
		String[] strs = line.split(", ");
		toPrint = new Expression[strs.length];
		for (int i = 0; i < strs.length; i++)
			toPrint[i] = Expression.interpret(strs[i]);
	}
	
	public void evaluate(IdentifierMap values)
	{
		for (Expression expr : toPrint)
			System.out.print(expr.evaluate(values).toString() + " ");
		System.out.println();
	}
}