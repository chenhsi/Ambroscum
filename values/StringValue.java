package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class StringValue extends Value
{
	private String value;
	
	public StringValue(String val)
	{
		value = val;
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