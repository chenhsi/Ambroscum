// strongly consider changing this to an enum

package ambroscum.values;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
	private static Map<String, FunctionOperator> map = new HashMap<> ();
	private Operator op;
	
	static
	{
		Operator.ready();
	}
	
	private FunctionOperator(Operator op)
	{
		super(null, null);
		this.op = op;
	}
	
	@Override
	public String toString()
	{
		return op.toString();
	}
	
	public static FunctionOperator get(String name)
	{
		return map.get(name);
	}
	
	@Override
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		if (!(arguments.get(0) instanceof ObjectValue))
			throw new InvalidArgumentException("Can not invoke an operator on a function");
		return ((ObjectValue) arguments.get(0)).applyOperator(this, arguments.get(1));
	}
	
	enum Operator
	{
		ADD("+", 2), SUB("-", 2), MUL("*", 2), DIV("/", 2), MOD("%", 2), AND("and", 2), OR("or", 2),
		IS_EQUAL("=", 2), GREATER_THAN(">", 2), LESS_THAN("<", 2), GREATER_THAN_EQUAL(">=", 2), LESS_THAN_EQUAL("<=", 2);
		
		private int numOperands;
		private String name;
		
		Operator(String name, int operands)
		{
			FunctionOperator.map.put(name, new FunctionOperator(this));
			numOperands = operands;
			this.name = name;
		}
		
		public int getNumOperands()
		{
			return numOperands;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static void ready()
		{
			
		}
	}
}