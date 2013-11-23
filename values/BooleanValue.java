package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class BooleanValue extends Value
{
	public static final BooleanValue TRUE = new BooleanValue(true);
	public static final BooleanValue FALSE = new BooleanValue(false);
	private boolean value;
	
	private BooleanValue(boolean val)
	{
		value = val;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
}