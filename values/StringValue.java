package ambroscum.values;

import java.util.*;
import ambroscum.*;
import ambroscum.errors.FunctionNotFoundException;

public class StringValue extends ObjectValue
{
	private String value;
	
	public StringValue(String val)
	{
		value = val;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, Value otherValue)
	{
		switch (op.toString())
		{
			case "+":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '+' operator not defined with value " + otherValue);
				return new StringValue(value + ((StringValue) otherValue).toString());
			case "<":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '<' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value.compareTo(((StringValue) otherValue).toString()) < 0);
			case ">":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '>' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value.compareTo(((StringValue) otherValue).toString()) > 0);
			case "<=":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '<=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value.compareTo(((StringValue) otherValue).toString()) <= 0);
			case ">=":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '>=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value.compareTo(((StringValue) otherValue).toString()) >= 0);
			case "=":
				if (!(otherValue instanceof StringValue))
					throw new FunctionNotFoundException("string's '=' operator not defined with value " + otherValue);
				return BooleanValue.fromBoolean(value.compareTo(((StringValue) otherValue).toString()) == 0);
		}
		return super.applyOperator(op, otherValue);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StringValue) {
			return value.equals(((StringValue) o).value);
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return value;
	}
}