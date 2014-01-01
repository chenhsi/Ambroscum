package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class BooleanValue extends ObjectValue
{
	public static final BooleanValue TRUE = new BooleanValue(true);
	public static final BooleanValue FALSE = new BooleanValue(false);
	private final boolean value;
	
	private BooleanValue(boolean val)
	{
		value = val;
	}
	
	public boolean getValue()
	{
		return value;
	}
	
	public static BooleanValue fromBoolean(boolean bool)
	{
		return bool ? TRUE : FALSE;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		if (otherValues.size() == 0)
		{
			if (op.toString().equals("not"))
				return BooleanValue.fromBoolean(!value);
			return super.applyOperator(op, otherValues);
		}
		if (otherValues.size() > 1)
			throw new UnsupportedOperationException("ternary+ operators are not yet supported");
		Value other = otherValues.get(0);
		switch (op.toString())
		{
			case "and":
				if (other instanceof BooleanValue)
					return BooleanValue.fromBoolean(value && ((BooleanValue) other).value);
				throw new FunctionNotFoundException("bool's 'and' operator not defined with value " + other);
			case "or":
				if (other instanceof BooleanValue)
					return BooleanValue.fromBoolean(value || ((BooleanValue) other).value);
				throw new FunctionNotFoundException("bool's 'or' operator not defined with value " + other);
			case "=":
				if (other instanceof BooleanValue)
					return BooleanValue.fromBoolean(value == ((BooleanValue) other).value);
				throw new FunctionNotFoundException("bool's '=' operator not defined with value " + other);
			case "!=":
				if (other instanceof BooleanValue)
					return BooleanValue.fromBoolean(value != ((BooleanValue) other).value);
				throw new FunctionNotFoundException("bool's '!=' operator not defined with value " + other);
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BooleanValue) {
			return value == ((BooleanValue) o).value;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Boolean.toString(value);
	}
}