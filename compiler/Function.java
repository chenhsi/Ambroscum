package ambroscum.compiler;

import java.util.*;

public class Function
{
	private ControlFlowGraph graph;
	
	BasicBlock startingBlock;
	BasicBlock endingBlock;
	
	private Set<String> nonlocalVariablesUsed;
	private Set<String> variablesModified;

	private boolean isMain;

	public Function(ControlFlowGraph graph, List<String> code, boolean isMain)
	{
		this.graph = graph;
		this.isMain = isMain;
		Map<String, BasicBlock> labels = new HashMap<> ();
		Set<BasicBlock> hasJump = new HashSet<> ();
		startingBlock = new BasicBlock();
		endingBlock = new BasicBlock(); // should include function cleanup maybe
		BasicBlock curr = startingBlock;
		for (String str : code)
		{
			if (str.startsWith("label "))
			{
				BasicBlock next = new BasicBlock();
				connect(curr, next);
				curr = next;
				labels.put(str.substring(6), curr);
				continue;
			}
			else if (str.startsWith("jump")) // jump or jumpunless
			{
				hasJump.add(curr);
				BasicBlock next = new BasicBlock();
				curr.add(str);
				if (str.startsWith("jumpunless"))
					connect(curr, next);
				curr = next;
				continue;
			}
			else if (str.startsWith("call "))
			{
				curr.add(str);
				// not actually supported
			}
			else if (str.startsWith("return "))
			{
				curr.add(str);
				connect(curr, endingBlock);
				curr = new BasicBlock();
			}
			else
				curr.add(str);
		}
		connect(curr, endingBlock);
		for (BasicBlock block : hasJump)
			for (ListIterator<Instruction> iter = block.instructions.listIterator(); iter.hasNext();)
			{
				String str = iter.next().line;
				if (str.startsWith("jump")) // jump or jumpunless
				{
					String jumpTarget = str.substring(str.lastIndexOf(" ") + 1);
					connect(block, labels.get(jumpTarget));
					if (str.startsWith("jump "))
						iter.remove();
				}
			}
	}
	
	public void optimize()
	{
		simplifyBlockStructure();
		propogateVariableDeclarations();
		variablePropogation();
		propogateVariableDeclarations();
		removeUnneededDeclarations();
		propogateVariableDeclarations();
		variablePropogation();
		loopAnalysis();
	}
	
	public void printAll()
	{
		if (isMain)
			System.out.println("Main:");
		else
		{
			System.out.println("Function:");
			System.out.println("\tNonlocal variables used: " + nonlocalVariablesUsed);
			System.out.println("\tVariables modified used: " + variablesModified);
		}
		System.out.println();
		Set<BasicBlock> explored = new HashSet<> ();
		List<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove(0);
			if (explored.contains(curr))
				continue;
			curr.print();
			System.out.println();
			explored.add(curr);
			for (BasicBlock child : curr.children)
				frontier.add(child);
		}
		System.out.println();
	}

	private static void connect(BasicBlock parent, BasicBlock child)
	{
		parent.children.add(child);
		child.parents.add(parent);
	}
	
	private void simplifyBlockStructure()
	{
		// remove empty blocks by directly connecting their parents/children
		Set<BasicBlock> explored = new HashSet<> ();
		List<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove(0);
			if (explored.contains(curr))
				continue;
			if (curr.instructions.size() == 0 && curr != startingBlock && curr != endingBlock)
			{
				for (BasicBlock parent : curr.parents)
					for (BasicBlock child : curr.children)
						connect(parent, child);
				for (BasicBlock parent : curr.parents)
					parent.children.remove(curr);
				for (BasicBlock child : curr.children)
				{
					frontier.add(child);
					child.parents.remove(curr);
				}
				continue;
			}
			explored.add(curr);
			for (BasicBlock child : curr.children)
				frontier.add(child);
		}

		// remove unreachable blocks from structure
		for (BasicBlock block : explored)
		{
			outer: while (true)
			{
				for (BasicBlock parent : block.parents)
					if (!explored.contains(parent))
					{
						block.parents.remove(parent);
						continue outer;
					}
				break;
			}
		}

		// combine block pairs that must be connected
		// (i.e. parent only links to one child and child only comes from one parent)
		explored = new HashSet<> ();
		frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove(0);
			if (explored.contains(curr))
				continue;
			if (curr.children.size() == 1)
				for (BasicBlock child : curr.children)
					if (child == endingBlock && curr != startingBlock) // can't remove ending block from graph
					{
						child.instructions.addAll(curr.instructions);
						for (Instruction inst : curr.instructions)
							inst.block = curr;
						child.parents = curr.parents;
						for (BasicBlock parent : curr.parents)
						{
							parent.children.remove(curr);
							parent.children.add(child);
						}
						break;
					}
					else if (child.parents.size() == 1)
					{
						curr.instructions.addAll(child.instructions);
						for (Instruction inst : curr.instructions)
							inst.block = curr;
						curr.children = child.children;
						for (BasicBlock newChild : child.children)
						{
							newChild.parents.remove(child);
							newChild.parents.add(curr);
						}
						frontier.add(curr);
						break;
					}
			else
				explored.add(curr);
		}
	}
	
	private void propogateVariableDeclarations()
	{
		Set<BasicBlock> exploredAtAll = new HashSet<BasicBlock> ();
		variablesModified = new HashSet<String> ();
		Map<BasicBlock, Map<String, Instruction>> analyzed = new HashMap<> ();
		List<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(startingBlock);
		analyzed.put(startingBlock, new HashMap<String, Instruction> ());
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove(0);
			if (!exploredAtAll.contains(curr))
				for (Instruction inst : curr.instructions)
				{
					inst.preDeclarations = new HashMap<> ();
					inst.postDeclarations = new HashMap<> ();
				}
			else
				exploredAtAll.add(curr);
			Map<String, Instruction> currMap = new HashMap<> (analyzed.get(curr));
			for (Instruction inst : curr.instructions)
			{
				inst.preDeclarations = new HashMap<> (currMap);
				if (inst.type.isAssignment())
					currMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), inst);
				else if (inst.type == InstructionType.FUNCTIONCALL)
				{
					String funcName = inst.variablesUsed.get(0);
					if (!funcName.equals("print")) // or any other built-in function
					{
						Instruction funcDecl = inst.preDeclarations.get(funcName);
						while (funcDecl != null && !funcDecl.line.contains("*")) // temp fix, should be changed
							if (funcDecl.type == InstructionType.ASSIGNMENT)
								funcDecl = funcDecl.preDeclarations.get(funcDecl.variablesUsed.get(0));
							else
								break;
						boolean noInfo = (funcDecl == null);
						if (!noInfo && funcDecl.line.contains("*"))
						{
							funcName = funcDecl.variablesUsed.get(0);
							Function calledFunction = graph.getFunction(funcName.substring(1));
							if (calledFunction.variablesModified != null)
							{
								for (String str : calledFunction.variablesModified)
									currMap.put(str, null);
							}
							else
								noInfo = true;
						}
						else
							noInfo = true;
						if (noInfo)
							currMap.put("_all", null);
					}
				}
				if (currMap.containsKey("_all"))
					for (String key : currMap.keySet())
						if (key.charAt(0) != '_')
							currMap.put(key, null);
				inst.postDeclarations = new HashMap<> (currMap);
			}
			if (currMap.containsKey("_all"))
				variablesModified.add("_all");
			else
				for (String key : currMap.keySet())
					if (key.charAt(0) != '_')
						variablesModified.add(key);
			for (BasicBlock child : curr.children)
			{
				if (!analyzed.containsKey(child))
				{
					analyzed.put(child, new HashMap<> (currMap));
					toProcess.add(child);
				}
				else
				{
					Map<String, Instruction> orig = analyzed.get(child);
					boolean changed = false;
					for (String key : currMap.keySet())
						if (!orig.containsKey(key) || orig.get(key) != currMap.get(key) && orig.get(key) != null)
						{
							changed = true;
							if (orig.containsKey(key))
								orig.put(key, null);
							else
								orig.put(key, currMap.get(key));
						}
					if (changed)
						toProcess.add(child);
				}
			}
		}
	}
	
	private void removeUnneededDeclarations()
	{
		Set<BasicBlock> exploredAtAll = new HashSet<BasicBlock> ();
		Map<BasicBlock, Set<String>> analyzed = new HashMap<> ();
		List<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(endingBlock);
		if (isMain)
			analyzed.put(endingBlock, new HashSet<String> ());
		else
			analyzed.put(endingBlock, new HashSet<String> (variablesModified));
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove(0);
			if (!exploredAtAll.contains(curr))
				for (Instruction inst : curr.instructions)
				{
					inst.preLiveVariables = new HashSet<> ();
					inst.postLiveVariables = new HashSet<> ();
				}
			else
				exploredAtAll.add(curr);
			Set<String> currSet = new HashSet<> (analyzed.get(curr));
			for (ListIterator<Instruction> iter = curr.instructions.listIterator(curr.instructions.size()); iter.hasPrevious();)
			{
				Instruction inst = iter.previous();
				inst.postLiveVariables.addAll(currSet);
				if (inst.type.isAssignment())
				{
					int index = inst.line.indexOf(" = ");
					if (index != -1)
						currSet.remove(inst.line.substring(0, index));
				}
				else if (inst.type == InstructionType.FUNCTIONCALL)
				{
					String funcName = inst.variablesUsed.get(0);
					if (!funcName.equals("print")) // or any other built-in function
					{
						Instruction funcDecl = inst.preDeclarations.get(funcName);
						while (funcDecl != null && !funcDecl.line.contains("*")) // temp fix, should be changed
							if (funcDecl.type == InstructionType.ASSIGNMENT)
								funcDecl = funcDecl.preDeclarations.get(funcDecl.variablesUsed.get(0));
						boolean noInfo = (funcDecl == null);
						if (!noInfo && funcDecl.line.contains("*"))
						{
							funcName = funcDecl.variablesUsed.get(0);
							Function calledFunction = graph.getFunction(funcName.substring(1));
							if (calledFunction.variablesModified != null)
							{
								for (String str : calledFunction.nonlocalVariablesUsed)
									currSet.add(str);
							}
							else
								noInfo = true;
						}
						else
							noInfo = true;
						if (noInfo)
							currSet.add("_all");
					}
				}
				for (String str : inst.variablesUsed)
					if (str.charAt(0) != '*' && !str.equals("print")) // or any other built-in
						currSet.add(str);
				if (currSet.contains("_all"))
					for (String var : inst.preDeclarations.keySet())
						if (var.charAt(0) != '_')
							currSet.add(var);
					
				inst.preLiveVariables.addAll(currSet);
			}
			for (BasicBlock parent : curr.parents)
			{
				if (!analyzed.containsKey(parent))
				{
					analyzed.put(parent, new HashSet<> (currSet));
					toProcess.add(parent);
				}
				else
				{
					int currSize = analyzed.get(parent).size();
					analyzed.get(parent).addAll(currSet);
					if (currSize != analyzed.get(parent).size())
						toProcess.add(parent);
				}
			}
			if (curr == startingBlock)
				nonlocalVariablesUsed = currSet;
		}
		for (BasicBlock block : analyzed.keySet())
			for (ListIterator<Instruction> iter = block.instructions.listIterator(); iter.hasNext();)
			{
				Instruction inst = iter.next();
				if (inst.type == InstructionType.ASSIGNMENT || inst.type == InstructionType.CALCULATION)
				{
					String assigned = inst.line.substring(0, inst.line.indexOf(" = "));
					if (!inst.postLiveVariables.contains(assigned))
						iter.remove();
				}
			}
	}
	
	private void variablePropogation()
	{
		Set<BasicBlock> explored = new HashSet<> ();
		List<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove(0);
			if (explored.contains(curr))
				continue;
			curr.variablePropogation();
			explored.add(curr);
			for (BasicBlock child : curr.children)
				frontier.add(child);
		}
	}
	
	private void loopAnalysis()
	{
		Set<BasicBlock> explored = new HashSet<> ();
		
		// First, calculate dominators
		List<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove(0);
			if (!explored.contains(curr))
				curr.dominators = new HashSet<BasicBlock> ();
			Set<BasicBlock> temp = new HashSet<> (curr.dominators);
			for (BasicBlock parent : curr.parents)
				if (parent.dominators.size() == 0)
					continue;
				else if (!explored.contains(curr))
					curr.dominators.addAll(parent.dominators);
				else
					curr.dominators.retainAll(parent.dominators);
			curr.dominators.add(curr);
			explored.add(curr);
			if (!temp.equals(curr.dominators))
				for (BasicBlock block : curr.children)
					frontier.add(block);
		}
		
		// Using dominators, determine all loops, as well as their header
		Map<BasicBlock, Set<BasicBlock>> loops = new HashMap<> ();
		for (BasicBlock block : explored)
			for (BasicBlock child : block.children)
				if (block.dominators.contains(child) && block != child)
				{
					BasicBlock header = child;
					Set<BasicBlock> loopActual = new HashSet<> ();
					List<BasicBlock> loopPotential = new LinkedList<> ();
					loopPotential.add(block);
					while (!loopPotential.isEmpty())
					{
						BasicBlock curr = loopPotential.remove(0);
						if (loopActual.contains(curr) || curr == header)
							continue;
						loopActual.add(curr);
						for (BasicBlock currParent : curr.parents)
							loopPotential.add(currParent);
					}
					if (loops.containsKey(header))
						loops.get(header).addAll(loopActual);
					else
						loops.put(header, loopActual);
				}
		
		// insert an empty block in front of each loop header
		Map<BasicBlock, BasicBlock> loopPreHeaders = new HashMap<> ();
		for (BasicBlock header : loops.keySet())
		{
			BasicBlock insert = new BasicBlock();
			for (BasicBlock loopParent : header.parents)
				if (!loopParent.dominators.contains(header))
				{
					loopParent.children.remove(header);
					loopParent.children.add(insert);
					insert.parents.add(loopParent);
				}
			header.parents.removeAll(insert.parents);
			connect(insert, header);
			loopPreHeaders.put(header, insert);
		}
		
		// push all loop invariant calculations into the pre-headers
		for (BasicBlock header : loops.keySet())
		{
			BasicBlock preHeader = loopPreHeaders.get(header);
			for (BasicBlock loopBlock : loops.get(header))
				outer: for (ListIterator<Instruction> iter = loopBlock.instructions.listIterator(0); iter.hasNext();)
				{
					Instruction inst = iter.next();
					if (inst.type != InstructionType.CALCULATION)
						continue;
					
					// first, check to make sure that this is invariant
					for (String str : inst.variablesUsed)
					{
						Instruction decl = inst.preDeclarations.get(str);
						if (decl == null || loops.get(header).contains(decl.block))
							continue outer;
					}
					
					// now, check to make sure this is safe to pull out
					String target = inst.line.substring(inst.line.indexOf(" = "));
					for (BasicBlock loopBlock2 : loops.get(header))
					{
						for (Instruction inst2 : loopBlock2.instructions)
						{
							// if there is another declaration
							if (inst2.type.isAssignment() && inst2.line.substring(inst2.line.indexOf(" = ")).equals(target) && inst2 != inst)
								continue outer;
							// if instruction is not guaranteed to reach all uses
							if (inst2.variablesUsed.contains(target) && inst2.preDeclarations.get(target) != inst)
								continue outer;
						}
						// if instruction does not dominate all loop exits
						if (!loopBlock2.dominators.contains(loopBlock))
							for (BasicBlock potentialExit : loopBlock2.children)
								if (!loops.get(header).contains(potentialExit))
									continue outer;
					}
					// invariant and safe, so go ahead and pull out
					iter.remove();
					preHeader.instructions.add(inst);
					inst.block = preHeader;
				}
		}
	}
	
	public Set<String> nonlocalVariablesUsed()
	{
		return nonlocalVariablesUsed;
	}
	public Set<String> variablesModified()
	{
		return variablesModified;
	}
}