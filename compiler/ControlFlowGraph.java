package ambroscum.compiler;

import java.util.*;

public class ControlFlowGraph
{
	private Function mainFunction;
	private Set<Function> otherFunctions;

	public ControlFlowGraph(List<String> mainCode, Set<List<String>> functionCode)
	{
		otherFunctions = new HashSet<> ();
		mainFunction = new Function(this, mainCode);
		for (List<String> function : functionCode)
			otherFunctions.add(new Function(this, function));
	}
	
	public void optimize()
	{
		mainFunction.optimize();
		for (Function f : otherFunctions)
			f.optimize();
	}
}