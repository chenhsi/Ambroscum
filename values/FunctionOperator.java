// strongly consider changing this to an enum

package ambroscum.values;

import java.util.List;
import java.util.LinkedList;
import ambroscum.IdentifierMap;
import ambroscum.Parameter;
import ambroscum.expressions.Expression;
import ambroscum.lines.Block;
import ambroscum.values.Value;
import ambroscum.values.Function;
import ambroscum.values.IntValue;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.InvalidArgumentException;

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
	private static final FunctionOperator IS_EQUAL = new FunctionOperator("=");
	private static final FunctionOperator GREATER_THAN = new FunctionOperator(">");
	private static final FunctionOperator LESS_THAN = new FunctionOperator("<");
	private static final FunctionOperator GREATER_THAN_EQUAL = new FunctionOperator(">=");
	private static final FunctionOperator LESS_THAN_EQUAL = new FunctionOperator("<=");
	
	public String toString()
	{
		return name;
	}
	
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
			case "_=": return IS_EQUAL;
			case "_>": return GREATER_THAN;
			case "_<": return LESS_THAN;
			case "_>=": return GREATER_THAN_EQUAL;
			case "_<=": return LESS_THAN_EQUAL;
		}
		throw new UnsupportedOperationException();
	}
	
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		if (!(arguments.get(0) instanceof ObjectValue))
			throw new InvalidArgumentException("Can not invoke an operator on a function");
		return ((ObjectValue) arguments.get(0)).applyOperator(this, arguments.get(1));
	}
}