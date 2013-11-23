// strongly consider changing this to an enum

package ambroscum.values;

import java.util.List;
import java.util.LinkedList;
import ambroscum.Expression;
import ambroscum.Function;
import ambroscum.IdentifierMap;
import ambroscum.Parameter;
import ambroscum.Value;
import ambroscum.lines.Block;
import ambroscum.values.IntValue;
import ambroscum.errors.FunctionNotFoundException;

public class FunctionOperator extends Function
{
	private String name;
	
	private FunctionOperator(String name)
	{
		super(null, null);
		this.name = name;
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
	
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		switch (name)
		{
			case "+":
				if (arguments.size() != 2)
					throw new AssertionError("how did we even get to functionoperator without 2 arguments");
				if (!(arguments.get(0) instanceof IntValue) || !(arguments.get(1) instanceof IntValue))
					throw new FunctionNotFoundException("+ not defined for non-integer inputs");
				return new IntValue(((IntValue) arguments.get(0)).getValue() + ((IntValue) arguments.get(1)).getValue());
		}
		throw new UnsupportedOperationException();
	}
}