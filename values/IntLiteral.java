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
	
	public String toString()
	{
		return value + "";
	}
}