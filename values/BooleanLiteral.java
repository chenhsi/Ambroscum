package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class BooleanLiteral extends Value
{
	public static final BooleanLiteral TRUE = new BooleanLiteral(true);
	public static final BooleanLiteral FALSE = new BooleanLiteral(false);
	private boolean value;
	
	private BooleanLiteral(boolean val)
	{
		value = val;
	}
	
	public String toString()
	{
		return value + "";
	}
}