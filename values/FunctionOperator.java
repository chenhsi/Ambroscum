// consider moving a lot of this code to ExpressionOperators

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
	
	public int getPriority()
	{
		return op.getPriority();
	}
	
	public int getNumOperands()
	{
		return op.getNumOperands();
	}
	
	@Override
	public Value evaluate(List<Value> arguments, IdentifierMap values)
	{
		if (!(arguments.get(0) instanceof ObjectValue))
			throw new InvalidArgumentException("Can not invoke an operator on a function");
		return ((ObjectValue) arguments.get(0)).applyOperator(this, arguments.subList(1, arguments.size()));
	}
	
	enum Operator
	{
		POST_INCREMENT("++_", 1, 0), POST_DECREMENT("--_", 1, 0),
		NOT("not", 1, 1), PRE_INCREMENT("++_", 1, 1), PRE_DECREMENT("--_", 1, 1),
		MUL("*", 2, 2), DIV("/", 2, 2), MOD("%", 2, 2),
		ADD("+", 2, 3), SUB("-", 2, 3),
		GREATER_THAN(">", 2, 4), LESS_THAN("<", 2, 4), GREATER_THAN_EQUAL(">=", 2, 4), LESS_THAN_EQUAL("<=", 2, 4),
		IS_EQUAL("=", 2, 5), NOT_EQUAL("!=", 2, 5),
		AND("and", 2, 6), OR("or", 2, 7),
		GET(".[]", 1, -1), SET("[]=", 2, -1);
		
		private String name;
		private int numOperands;
		private int priority;
		
		Operator(String name, int operands, int priority)
		{
			FunctionOperator.map.put(name, new FunctionOperator(this));
			numOperands = operands;
			this.name = name;
			this.priority = priority; // lower is grouped first (e.g. "*".priority < "+".priority)
		}
		
		public int getNumOperands()
		{
			return numOperands;
		}
		
		public int getPriority()
		{
			return priority;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static void ready() {} // exists just to be called for the static initializer
	}
}