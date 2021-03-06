package ambroscum.compiler;

import java.util.*;

public class BasicBlock
{
	List<Instruction> instructions = new LinkedList<Instruction> ();
	Set<BasicBlock> parents = new HashSet<> ();
	BasicBlock nextBlock;
	BasicBlock jumpBlock;
	
	static int counter = 0;
	int id = ++counter;
	Set<BasicBlock> dominators = new HashSet<BasicBlock> ();
	
	void add(String str)
	{
		instructions.add(new Instruction(str, this));
	}
	
	public String toString()
	{
		return "block " + id;
	}
	
	void print()
	{
		System.out.println(this);
		System.out.println("Parents: " + parents);
		for (Instruction inst : instructions)
			inst.print();
		System.out.println("Children: " + nextBlock + (jumpBlock == null ? "" : (" " + jumpBlock)));
	}
	
	void variablePropogation()
	{
		for (Instruction inst : instructions)
			inst.optimize();
		Map<String, Set<Instruction>> subExpressions = new HashMap<> ();
		outer: for (Instruction inst : instructions)
		{
			if (inst.type == InstructionType.CALCULATION)
			{
				String rightHalf = inst.line.substring(inst.line.indexOf(" = ") + 3);
				if (!subExpressions.containsKey(rightHalf))
					subExpressions.put(rightHalf, new HashSet<Instruction> ());
				else
					inner: for (Instruction prev : subExpressions.get(rightHalf))
					{
						for (String str : inst.variablesUsed)
							if (inst.preDeclarations.get(str) != prev.preDeclarations.get(str) || inst.preDeclarations.get(str) == null)
								continue inner;
						String newValue = prev.line.substring(0, prev.line.indexOf(" = "));
						inst.line = inst.line.substring(0, inst.line.indexOf(" = ")) + " = " + newValue;
						inst.type = InstructionType.ASSIGNMENT;
						inst.variablesUsed = new LinkedList<String> ();
						inst.variablesUsed.add(newValue);
						continue outer;
					}
				subExpressions.get(rightHalf).add(inst);
			}
		}
		for (Iterator<Instruction> iter = instructions.iterator(); iter.hasNext();)
		{
			Instruction inst = iter.next();
			inst.optimize();
			if (inst.line.equals("nop"))
				iter.remove();
		}
	}
}