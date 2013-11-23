package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class IntValue extends Value
{
	private int value;
	
	public IntValue(int num)
	{
		value = num;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
}