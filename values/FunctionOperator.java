package ambroscum.values;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import ambroscum.IdentifierMap;
import ambroscum.Parameter;
import ambroscum.parser.Token;
import ambroscum.expressions.Expression;
import ambroscum.lines.Block;
import ambroscum.values.Value;
import ambroscum.values.FunctionDeclaration;
import ambroscum.values.IntValue;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.InvalidArgumentException;

public class FunctionOperator extends FunctionDeclaration
{
	private static Map<String, FunctionOperator> map = new HashMap<> ();
	private Operator op;
	
	static
	{
		Operator.ready();
	}
	
	private FunctionOperator(Operator op)
	{
		super(null, null, null);
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
	
	public String getOperandType()
	{
		return op.getType();
	}
	
	public static boolean isOperator(Token t)
	{
		return get(t.toString()) != null && get(t.toString()).getPriority() > -1;
	}
	
	@Override
	public Value evaluate(List<Value> arguments)
	{
		if (!(arguments.get(0) instanceof ObjectValue))
			throw new InvalidArgumentException("Can not invoke an operator on a function");
		return ((ObjectValue) arguments.get(0)).applyOperator(this, arguments.subList(1, arguments.size()));
	}
	
	enum Operator
	{
		POST_INCREMENT("_++", 1, 0, "int"), POST_DECREMENT("_--", 1, 0, "int"),
		NOT("not", 1, 1, "boolean"), PRE_INCREMENT("++_", 1, 1, "int"), PRE_DECREMENT("--_", 1, 1, "int"),
		MUL("*", 2, 2, "int"), DIV("/", 2, 2, "int"), MOD("%", 2, 2, "int"),
		ADD("+", 2, 3, "int"), SUB("-", 2, 3, "int"), POW("**", 2, 3, "int"),
		LEFTSHIFT("<<", 2, 4, "int"), RIGHTSHIFT(">>", 2, 4, "int"),
		GREATER_THAN(">", 2, 5, "int"), LESS_THAN("<", 2, 5, "int"), GREATER_THAN_EQUAL(">=", 2, 5, "int"), LESS_THAN_EQUAL("<=", 2, 5, "int"),
		IS_EQUAL("=", 2, 6, "Object"), NOT_EQUAL("!=", 2, 6, "Object"),
		AND("and", 2, 7, "boolean"), OR("or", 2, 8, "boolean"),
		GET(".[]", 1, -1, null), SET("[]=", 2, -1, null);
		
		private String name;
		private int numOperands;
		private int priority;
		private String type;
		
		Operator(String name, int operands, int priority, String type)
		{
			FunctionOperator.map.put(name, new FunctionOperator(this));
			numOperands = operands;
			this.name = name;
			this.priority = priority; // lower is grouped first (e.g. "*".priority < "+".priority)
			this.type = type;
		}
		
		public int getNumOperands()
		{
			return numOperands;
		}
		
		public int getPriority()
		{
			return priority;
		}
		
		public String getType()
		{
			return type;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static void ready() {} // exists just to be called for the static initializer
	}
}