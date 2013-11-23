package ambroscum.values;

import ambroscum.*;
import java.util.*;

public class IntValue extends Value implements Comparable<IntValue>
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
	public boolean equals(Object o) {
		if (o instanceof IntValue) {
			return value == ((IntValue) o).value;
		}
		return false;
	}
	@Override
	public int compareTo(IntValue o) {
		return value - o.value;
	}
	
	@Override
	public String toString()
	{
		return value + "";
	}
}