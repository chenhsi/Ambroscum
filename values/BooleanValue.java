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
	public Value applyOperator(FunctionOperator op, Value otherValue)
	{
		switch (op.toString())
		{
			case "and":
				if (!(otherValue instanceof BooleanValue))
					throw new FunctionNotFoundException("bool's 'and' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value && ((BooleanValue) otherValue).getValue());
			case "or":
				if (!(otherValue instanceof BooleanValue))
					throw new FunctionNotFoundException("bool's 'or' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value || ((BooleanValue) otherValue).getValue());
		}
		return super.applyOperator(op, otherValue);
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