package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;
import ambroscum.errors.InvalidArgumentException;

public class IntValue extends ObjectValue
{
	private final long value;
	
	private IntValue(long num)
	{
		value = num;
	}
	
	public static IntValue fromInt(long n)
	{
		return new IntValue(n);
	}
	
	public long getValue()
	{
		return value;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
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
			case "<<":
				if (other instanceof IntValue)
					return IntValue.fromInt(value << ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '<<' operator not defined with value " + other);
			case ">>":	// arithmetic right shift
				if (other instanceof IntValue)
					return IntValue.fromInt(value >> ((IntValue) other).getValue());
				throw new FunctionNotFoundException("int's '>>' operator not defined with value " + other);
			case "**":
				if (other instanceof IntValue)
				{
					long exp = ((IntValue) other).getValue();
					if (exp < 0)
						throw new InvalidArgumentException("int's '**' operator not defined with negative exponents");
					if (exp == 0)
						return IntValue.fromInt(1);
					long prod = value;
					while (exp > 1)
					{
						if (exp % 2 == 0)
							prod *= prod;
						else
							prod *= prod * value;
						exp /= 2;
					}
					return IntValue.fromInt(prod);
				}
				throw new FunctionNotFoundException("int's '**' operator not defined with value " + other);
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return (o instanceof IntValue) && value == ((IntValue) o).value;
	}
	@Override
	public int hashCode()
	{
		return Long.valueOf(value).hashCode();
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
	
	public Value deepClone(Map<Value, Value> alreadyCloned) {
		return this;
	}
}