package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class IntLiteral extends Value
{
	private int value;
	
	public IntLiteral(int num)
	{
		value = num;
	}
	
	@Override
	public int getValue() {
		return value;
	}
	
	public String toString()
	{
		return value + "";
	}
}