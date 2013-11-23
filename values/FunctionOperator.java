// strongly consider changing this to an enum

package ambroscum.values;

import ambroscum.Function;
import ambroscum.Parameter;
import ambroscum.Expression;
import ambroscum.Value;
import ambroscum.lines.Block;

public class FunctionOperator extends Function
{
	private enum 
	
	private final List<Parameter> params = new LinkedList<Parameter> ();
	
	private FunctionOperator(String name)
	{
		super(params, null);
		params.add(new Parameter("a"));
		params.add(new Parameter("b"));
	}
	
	private static final ADD = new FunctionOperator("+");
	private static final SUB = new FunctionOperator("-");
	private static final MUL = new FunctionOperator("*");
	private static final DIV = new FunctionOperator("/");
	private static final MOD = new FunctionOperator("%");
	private static final AND = new FunctionOperator("and");
	private static final OR = new FunctionOperator("or");
	
	public static FunctionOperator get(String name)
	{
		switch (name)
		{
			case "_+": return ADD;
			case "_-": return SUB;
			case "_*": return MUL;
			case "_/": return DIV;
			case "_%": return MOD;
			case "_and": return AND;
			case "_or": return OR;
		}
		throw new UnsupportedOperationException();
	}
	
	public Value evaluate(List<Expression> arguments)
	{
		throw new UnsupportedOperationException();
	}
}