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
	
	// technically redundant with toString, but is more consistent with other Value classes
	public String getValue()
	{
		return value;
	}
	
	@Override
	public Value applyOperator(FunctionOperator op, List<Value> otherValues)
	{
		if (otherValues.size() != 1)
			throw new UnsupportedOperationException("Non-binary operators are not yet supported");
		Value other = otherValues.get(0);
		switch (op.toString())
		{
			case "+":
				if (other instanceof IntValue)
					return new StringValue(value + ((IntValue) other).getValue());
				if (other instanceof StringValue)
					return new StringValue(value + ((StringValue) other).toString());
				throw new FunctionNotFoundException("string's '+' operator not defined with value " + other);
			case "<":
				if (other instanceof StringValue)
					return BooleanValue.fromBoolean(value.compareTo(((StringValue) other).toString()) < 0);
				throw new FunctionNotFoundException("string's '<' operator not defined with value " + other);
			case ">":
				if (other instanceof StringValue)
					return BooleanValue.fromBoolean(value.compareTo(((StringValue) other).toString()) > 0);
				throw new FunctionNotFoundException("string's '>' operator not defined with value " + other);
			case "<=":
				if (other instanceof StringValue)
					return BooleanValue.fromBoolean(value.compareTo(((StringValue) other).toString()) <= 0);
				throw new FunctionNotFoundException("string's '<=' operator not defined with value " + other);
			case ">=":
				if (other instanceof StringValue)
					return BooleanValue.fromBoolean(value.compareTo(((StringValue) other).toString()) >= 0);
				throw new FunctionNotFoundException("string's '>=' operator not defined with value " + other);
			case "=":
				if (other instanceof StringValue)
					return BooleanValue.fromBoolean(value.compareTo(((StringValue) other).toString()) == 0);
				throw new FunctionNotFoundException("string's '=' operator not defined with value " + other);
		}
		return super.applyOperator(op, otherValues);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof StringValue) {
			return value.equals(((StringValue) o).value);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public String toString()
	{
		return value;
	}
	@Override
	public String repr() {
		return '\"' + value + '\"';
	}
}