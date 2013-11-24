package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class BooleanValue extends Value
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