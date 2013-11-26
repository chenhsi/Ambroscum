package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class IntValue extends ObjectValue
{
	private final int value;
	
	public IntValue(int num)
	{
		value = num;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, Value otherValue)
	{
		switch (op.toString())
		{
			case "+":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '+' operator not defined with value " + otherValue);
				return new IntValue(value + ((IntValue) otherValue).getValue());
			case "-":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '-' operator not defined with value " + otherValue);
				return new IntValue(value - ((IntValue) otherValue).getValue());
			case "*":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '*' operator not defined with value " + otherValue);
				return new IntValue(value * ((IntValue) otherValue).getValue());
			case "/":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '/' operator not defined with value " + otherValue);
				return new IntValue(value / ((IntValue) otherValue).getValue());
			case "%":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '%' operator not defined with value " + otherValue);
				return new IntValue(value % ((IntValue) otherValue).getValue());
			case "<":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '<' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value < ((IntValue) otherValue).getValue());
			case ">":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '>' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value > ((IntValue) otherValue).getValue());
			case "<=":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '<=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value <= ((IntValue) otherValue).getValue());
			case ">=":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '>=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value >= ((IntValue) otherValue).getValue());
			case "=":
				if (!(otherValue instanceof IntValue))
					throw new FunctionNotFoundException("int's '=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value == ((IntValue) otherValue).getValue());
		}
		return super.applyOperator(op, otherValue);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof IntValue) && value == ((IntValue) o).value;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
}