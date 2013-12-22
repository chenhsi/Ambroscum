package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class IntValue extends ObjectValue
{
	private final int value;
	
	private IntValue(int num)
	{
		value = num;
	}
	
	public static IntValue fromInt(int n)
	{
		return new IntValue(n);
	}
	
	public int getValue()
	{
		return value;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
//		System.err.println(op + " " + otherValues);
		if (otherValues.size() == 0)
		{
			if (op.toString().equals("-"))
				return IntValue.fromInt(-value);
			return super.applyOperator(op, otherValues);
		}
		if (otherValues.size() != 1)
			throw new UnsupportedOperationException("Non-binary operators are not yet supported");
		Value other = otherValues.get(0);
		switch (op.toString())
		{
			case "+":
				if (other instanceof IntValue)
					return IntValue.fromInt(value + ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '+' operator not defined with value " + other);
			case "-":
				if (other instanceof IntValue)
					return IntValue.fromInt(value - ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '-' operator not defined with value " + other);
			case "*":
				if (other instanceof IntValue)
					return IntValue.fromInt(value * ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '*' operator not defined with value " + other);
			case "/":
				if (other instanceof IntValue)
					return IntValue.fromInt(value / ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '/' operator not defined with value " + other);
			case "%":
				if (other instanceof IntValue)
					return IntValue.fromInt(value % ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '%' operator not defined with value " + other);
			case "<":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value < ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '<' operator not defined with value " + other);
			case ">":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value > ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '>' operator not defined with value " + other);
			case "<=":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value <= ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '<=' operator not defined with value " + other);
			case ">=":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value >= ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '>=' operator not defined with value " + other);
			case "=":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value == ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '=' operator not defined with value " + other);
			case "!=":
				if (other instanceof IntValue)
					return BooleanValue.fromBoolean(value != ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '!=' operator not defined with value " + other);
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof IntValue) && value == ((IntValue) o).value;
	}
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
}