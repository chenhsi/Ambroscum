package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class BooleanValue extends Value
{
	public static final BooleanValue TRUE = new BooleanValue(true);
	public static final BooleanValue FALSE = new BooleanValue(false);
	private final boolean value;
	private final String str;
	
	private BooleanValue(boolean val)
	{
		value = val;
		str = value ? "True" : "False";
	}
	
	public boolean getValue()
	{
		return value;
	}
	
	@Override
	public String toString()
	{
		return str;
	}
}