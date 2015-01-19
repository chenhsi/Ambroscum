package ambroscum.compiler;

import java.util.*;

public class ControlFlowGraph
{
	private static final boolean printAfter = true;
	private Function mainFunction;
	private Map<String, Function> otherFunctions;

	public ControlFlowGraph(List<String> mainCode, Map<String, List<String>> functions)
	{
		otherFunctions = new HashMap<> ();
		mainFunction = new Function(this, mainCode, true);
		for (String functionName : functions.keySet())
			otherFunctions.put(functionName, new Function(this, functions.get(functionName), false));
	}
	
	public void optimize()
	{
		if (!printAfter)
		{
			mainFunction.printAll();
			for (Function f : otherFunctions.values())
				f.printAll();
		}
		for (int i = 0; i < 10; i++) // should be smarter about this
		{
//			System.out.println("Optimization iteration " + i);
			mainFunction.optimize();
			for (Function f : otherFunctions.values())
				f.optimize();
		}
		if (printAfter)
		{
			mainFunction.printAll();
			for (Function f : otherFunctions.values())
				f.printAll();
		}
	}
	
	public Function getFunction(String name)
	{
		return otherFunctions.get(name);
	}
	
	public Function getMain()
	{
		return mainFunction;
	}
	public Map<String, Function> getFunctions()
	{
		return otherFunctions;
	}
}