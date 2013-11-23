// strongly consider changing this to an enum

package ambroscum.values;

import ambroscum.Function;
import ambroscum.Parameter;
import ambroscum.Expression;
import ambroscum.Value;
import ambroscum.lines.Block;
import java.util.*;

public class FunctionOperator extends Function
{
	private final List<Parameter> params = new LinkedList<Parameter> ();
	
	private FunctionOperator(String name)
	{
		super(null, null);
		params.add(new Parameter("a"));
		params.add(new Parameter("b"));
	}
	
	private static final FunctionOperator ADD = new FunctionOperator("+");
	private static final FunctionOperator SUB = new FunctionOperator("-");
	private static final FunctionOperator MUL = new FunctionOperator("*");
	private static final FunctionOperator DIV = new FunctionOperator("/");
	private static final FunctionOperator MOD = new FunctionOperator("%");
	private static final FunctionOperator AND = new FunctionOperator("and");
	private static final FunctionOperator OR = new FunctionOperator("or");
	
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