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
	public String toString()
	{
		return value;
	}
}