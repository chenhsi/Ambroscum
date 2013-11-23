package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class StringLiteral extends Value
{
	private String value;
	
	public StringLiteral(String val)
	{
		value = val;
	}
	
	public String toString()
	{
		return value;
	}
}