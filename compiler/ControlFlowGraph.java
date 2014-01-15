package ambroscum.compiler;

import java.util.*;

public class ControlFlowGraph
{
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
		mainFunction.optimize();
		for (Function f : otherFunctions.values())
			f.optimize();
//		mainFunction.optimize();
		mainFunction.printAll();
		for (Function f : otherFunctions.values())
			f.printAll();
	}
	
	public Function getFunction(String name)
	{
		return otherFunctions.get(name);
	}
}