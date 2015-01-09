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
				connect(curr, next, true);
				labels.put(str.substring(6), next);
				curr = next;
				continue;
			}
			else if (str.startsWith("jump")) // jump or jumpunless
			{
				hasJump.add(curr);
				curr.add(str);
				BasicBlock next = new BasicBlock();
				if (str.startsWith("jumpunless"))
				{
					connect(curr, next, true);
					curr.nextBlock = next;
				}
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
				connect(curr, endingBlock, false);
				curr = new BasicBlock();
			}
			else
				curr.add(str);
		}
		connect(curr, endingBlock, true);
		
		// Now that we have a list of all the labels,
		// we can connect jump statements to targets
		for (BasicBlock block : hasJump)
			for (ListIterator<Instruction> iter = block.instructions.listIterator(); iter.hasNext();)
			{
				String str = iter.next().line;
				if (str.startsWith("jump")) // jump or jumpunless
				{
					String jumpTarget = str.substring(str.lastIndexOf(" ") + 1);
					connect(block, labels.get(jumpTarget), false);
					
					// I had this here, but I don't remember why anymore,
					// and I don't think it should be here
					// If my intent was that jump statements could be converted to direct connections
					// I'm now doing that somewhere else
//					if (str.startsWith("jump "))
//						iter.remove();
				}
			}
	}
	
	public void optimize()
	{
		// No particular sense to this ordering
//		System.out.println("Optimizing part 1: ");
		simplifyBlockStructure();
		complicateBlockStructure();
//		System.out.println("Optimizing part 2: ");
		propogateVariableDeclarations();
//		System.out.println("Optimizing part 3: ");
		variablePropogation();
//		System.out.println("Optimizing part 4: ");
		propogateVariableDeclarations();
//		System.out.println("Optimizing part 5: ");
		removeUnneededDeclarations();
//		System.out.println("Optimizing part 6: ");
		propogateVariableDeclarations();
//		System.out.println("Optimizing part 7: ");
		variablePropogation();
//		System.out.println("Optimizing part 8: ");
		// Currently causes infinite loops, haven't figured out why
//		loopAnalysis();
//		System.out.println("Optimizing Done");
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
			if (curr.nextBlock != null)
				frontier.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				frontier.add(curr.jumpBlock);
		}
	}

	private static void connect(BasicBlock parent, BasicBlock child, boolean normalTransition)
	{
		if (normalTransition)
			parent.nextBlock = child;
		else
			parent.jumpBlock = child;
		child.parents.add(parent);
	}
	
	private void simplifyBlockStructure()
	{
		// remove empty blocks by directly connecting their parents/children
		Set<BasicBlock> explored = new HashSet<> ();
		Queue<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove();
			if (explored.contains(curr))
				continue;
			explored.add(curr);
			if (curr.instructions.size() == 0 && curr != startingBlock && curr != endingBlock)
			{
				// If a block is empty, then it has to directly lead to the next block
				if (curr.nextBlock == null || curr.jumpBlock != null)
					throw new AssertionError();
				for (BasicBlock parent : curr.parents)
					connect(parent, curr.nextBlock, parent.nextBlock == curr);
				curr.nextBlock.parents.remove(curr);
				// The following block might also be empty, so search that too
				frontier.add(curr.nextBlock);
				continue;
			}
			// If a block's conditional jump just goes to the next block anyway
			else if (curr.nextBlock == curr.jumpBlock && curr != endingBlock)
			{
				if (curr.nextBlock == null)
					throw new AssertionError();
				curr.jumpBlock = null;
//				curr.print();
				curr.instructions.remove(curr.instructions.size() - 1);
			}
			if (curr.nextBlock != null)
				frontier.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				frontier.add(curr.jumpBlock);
		}

		// remove unreachable blocks from structure
		for (BasicBlock block : explored)
		{
			for (Iterator<BasicBlock> iter = block.parents.iterator(); iter.hasNext(); )
			{
				BasicBlock parent = iter.next();
				if (!explored.contains(parent))
					iter.remove();
			}
		}

		// combine block pairs that must be connected
		// (i.e. parent only links to one child and child only comes from one parent)
		explored = new HashSet<> ();
		frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		outer: while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove();
			if (explored.contains(curr))
				continue;
			explored.add(curr);
			if (curr.nextBlock == null ^ curr.jumpBlock == null) // If exactly 1 child
			{
				BasicBlock child = curr.nextBlock == null ? curr.jumpBlock : curr.nextBlock;
				// can't remove ending block from graph, but need to rethink how to do this
				if (child == endingBlock)
					continue;
				if (child.parents.size() == 1)
				{
					if (curr.jumpBlock != null)
					// if curr had terminated in a unconditioned jump
					// cut out the jump statement, since now merging
					if (curr.jumpBlock != null)
						curr.instructions.remove(curr.instructions.size() - 1);
					// Move the instructions to curr
					for (Instruction inst : child.instructions)
						inst.block = curr;
					curr.instructions.addAll(child.instructions);
					
					// Make curr's children = child's children's parents
					curr.nextBlock = null;
					curr.jumpBlock = null;
					if (child.nextBlock != null)
					{
						connect(curr, child.nextBlock, true);
						child.nextBlock.parents.remove(child);
					}
					if (child.jumpBlock != null)
					{
						connect(curr, child.jumpBlock, false);
						child.jumpBlock.parents.remove(child);
					}

					// Push curr back onto the search queue,
					// since this optimization might work again
					frontier.add(curr);
					continue outer;
				}
			}
			
			// If curr is a jump block, and the jump target isn't directly
			// connected to any of it's parents, then directly connect them
			// Choice of which of curr.child's parents becomes a direct
			// connection is currently completely arbitrary (random loop order)
			if (curr.nextBlock == null && curr != endingBlock)
			{
				for (BasicBlock childParents : curr.jumpBlock.parents)
				{
					if (childParents.nextBlock == curr.jumpBlock)
						continue outer; // This optimization doesn't work, skip
				}
				curr.nextBlock = curr.jumpBlock;
				curr.jumpBlock = null;
				curr.instructions.remove(curr.instructions.size() - 1);
			}
			
			if (curr.nextBlock != null)
				frontier.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				frontier.add(curr.jumpBlock);
		}
	}

	// if a block is small enough, you can just copy/merge it into its parents
	private static final int SMALL_BLOCK = 5; // 5 is an arbitarily chosen number
	private void complicateBlockStructure()
	{
		Set<BasicBlock> explored = new HashSet<> ();
		Queue<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove();
			if (explored.contains(curr))
				continue;
			explored.add(curr);

			if (curr.nextBlock != null)
				frontier.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				frontier.add(curr.jumpBlock);
			
			if (curr == startingBlock || curr == endingBlock)
				continue;
			if (curr.instructions.size() > SMALL_BLOCK)
				continue;
			
			// all the parents that are to be removed
			Set<BasicBlock> toRemove = new HashSet<> ();
			for (BasicBlock parent : curr.parents)
			{
				// can't merge if parent ends with a branch
				if (parent.nextBlock != null && parent.jumpBlock != null)
					continue;
				if (parent.jumpBlock != null) // delete jump statement, readd later maybe
					parent.instructions.remove(parent.instructions.size() - 1);
				for (Instruction inst : curr.instructions)
					parent.instructions.add(new Instruction(inst.line, parent));
				toRemove.add(parent);
			}

			// If curr doesn't branch, our work is a bit simpler
			// just have the parent jump to curr's child
			if (curr.nextBlock == null)
			{ 
				for (BasicBlock parent : toRemove)
				{
					parent.nextBlock = null;
					connect(parent, curr.jumpBlock, false);
				}
			}
			else if (curr.jumpBlock == null)
			{ 
				for (BasicBlock parent : toRemove)
				{
					parent.nextBlock = null;
					connect(parent, curr.nextBlock, false);
					parent.instructions.add(new Instruction("jump " + "somewhereidk", parent));
				}
			}
			// Otherwise, we need to create a new block to be parent.nextBlock
			// And have that new block then jump to curr.nextBlock
			else
			{
				for (BasicBlock parent : toRemove)
				{
					BasicBlock newChild = new BasicBlock();
					newChild.instructions.add(new Instruction("jump " + "somewhereidk", newChild));
					connect(parent, newChild, true);
					connect(newChild, curr.nextBlock, false);
					connect(parent, curr.jumpBlock, false);
				}
			}

			curr.parents.removeAll(toRemove);
			if (curr.parents.size() == 0)
			{
				if (curr.nextBlock != null)
					curr.nextBlock.parents.remove(curr);
				if (curr.jumpBlock != null)
					curr.jumpBlock.parents.remove(curr);
			}
		}
	}
	
	private void propogateVariableDeclarations()
	{
		// Maps each basic block to the set of instruction declarations entering the block
		//// something that might be better to put within each basic block, as a field?
		Map<BasicBlock, Map<String, Instruction>> analyzed = new HashMap<> ();
		analyzed.put(startingBlock, new HashMap<String, Instruction> ());
		
		Set<BasicBlock> exploredAtAll = new HashSet<BasicBlock> ();
		Queue<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(startingBlock);
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove();
			exploredAtAll.add(curr);
			// the current map of variables to the declaration lines (or null, if can't be determined)
			Map<String, Instruction> currMap = new HashMap<> (analyzed.get(curr));
			for (Instruction inst : curr.instructions)
			{
				inst.preDeclarations = new HashMap<> (currMap);
				if (inst.type.isAssignment()) // adds the declaration
					currMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), inst);
				else if (inst.type == InstructionType.FUNCTIONCALL) //// haven't debugged yet
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
						// _all corresponds to having gone to an unknown function call,
						// so any or all variables may have been changed
						// and any number of new variables may be present
						if (noInfo)
						{
							currMap.clear();
							currMap.put("_all", null);
						}
					}
				}
				inst.postDeclarations = new HashMap<> (currMap);
			}
			
			List<BasicBlock> children = new ArrayList<> (2);
			if (curr.nextBlock != null)
				children.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				children.add(curr.jumpBlock);
			for (BasicBlock child : children)
			{
				if (!analyzed.containsKey(child))
				{
					analyzed.put(child, new HashMap<> (currMap));
					toProcess.add(child);
				}
				else // already have some info on child, now supplementing
				{
					Map<String, Instruction> orig = analyzed.get(child);
					boolean changed = false;
					// If either orig or currMap has "_all", then just keep the
					// values they share in common
					if (orig.containsKey("_all") || currMap.containsKey("_all"))
					{
						Map<String, Instruction> keep = new HashMap<> ();
						for (String key : currMap.keySet())
							if (orig.get(key) == currMap.get(key))
								keep.put(key, currMap.get(key));
						keep.put("_all", null);
						changed = orig.equals(keep);
						analyzed.put(child, keep);
					}
					else // otherwise, keep track of exactly which values conflict
					{
						for (String key : currMap.keySet())
							// if child didn't have info on this variable before
							if (!orig.containsKey(key))
							{
								changed = true;
								orig.put(key, currMap.get(key));
							}
							// if child did have info, but conflicting
							else if (orig.get(key) != currMap.get(key) && orig.get(key) != null)
							{
								changed = true;
								orig.put(key, null);
							}
					}
					if (changed)
						toProcess.add(child);
				}
			}
		}

		// Note which values have changed in this function
		variablesModified = new HashSet<String> ();
		Map<String, Instruction> currMap = analyzed.get(endingBlock);
		if (currMap.containsKey("_all"))
			variablesModified.add("_all");
		else
			for (String key : currMap.keySet())
				if (key.charAt(0) != '_')
					variablesModified.add(key);
	}

	// includes a liveless analysis
	private void removeUnneededDeclarations()
	{
		nonlocalVariablesUsed = new HashSet<String> ();
		Set<BasicBlock> exploredAtAll = new HashSet<BasicBlock> ();
		Map<BasicBlock, Set<String>> analyzed = new HashMap<> ();
		Queue<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(endingBlock);
		if (isMain)
			analyzed.put(endingBlock, new HashSet<String> ());
		else
			analyzed.put(endingBlock, new HashSet<String> (variablesModified));
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove();
			if (!exploredAtAll.contains(curr))
				for (Instruction inst : curr.instructions)
				{
					inst.preLiveVariables = new HashSet<> ();
					inst.postLiveVariables = new HashSet<> ();
				}
			else
				exploredAtAll.add(curr);
			Set<String> currSet = new HashSet<> (analyzed.get(curr));
			// Iterating through the instructions in reverse order
			for (ListIterator<Instruction> iter = curr.instructions.listIterator(curr.instructions.size()); iter.hasPrevious();)
			{
				Instruction inst = iter.previous();
				inst.postLiveVariables.addAll(currSet);
				if (inst.type.isAssignment()) // assigned variable not alive immediately before it
					currSet.remove(inst.line.substring(0, inst.line.indexOf(" = ")));
				else if (inst.type == InstructionType.FUNCTIONCALL) //// not debugging right now
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
						{
							//// or something like this? _all is not supported right now and being completely ignored
							currSet.clear();
							currSet.add("_all");
						}
					}
				}
				for (String str : inst.variablesUsed)
					if (!str.equals("print")) //// don't want to deal with memory accesses right now
//					if (str.charAt(0) != '*' && !str.equals("print")) // or any other built-in function
						currSet.add(str);
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
					// i.e. if there are any additions, then readd parent into the search queue
					// does not deal with _all or anything complicated like that
					int currSize = analyzed.get(parent).size();
					analyzed.get(parent).addAll(currSet);
					if (currSize != analyzed.get(parent).size())
						toProcess.add(parent);
				}
			}
			if (curr == startingBlock)
				nonlocalVariablesUsed.addAll(currSet);
		}
		
		// now for the actual removal
		for (BasicBlock block : analyzed.keySet())
			for (ListIterator<Instruction> iter = block.instructions.listIterator(); iter.hasNext();)
			{
				Instruction inst = iter.next();
				if (inst.type.isAssignment())
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
			explored.add(curr);
			curr.variablePropogation();
			if (curr.nextBlock != null)
				frontier.add(curr.nextBlock);
			if (curr.jumpBlock != null)
				frontier.add(curr.jumpBlock);
		}
	}
	
	private void loopAnalysis()
	{
		Set<BasicBlock> explored = new HashSet<> ();
		
		// First, calculate dominators
		Queue<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock curr = frontier.remove();
			if (!explored.contains(curr))
				curr.dominators = new HashSet<BasicBlock> ();
			explored.add(curr);
			Set<BasicBlock> temp = new HashSet<> ();
			for (BasicBlock parent : curr.parents)
				if (!explored.contains(parent)) // Haven't analyzed parent yet, skip for now
					continue;
				else if (temp.isEmpty()) // Special case to initialize temp
					temp.addAll(parent.dominators);
				else // Usual case of finding intersection of parent.dominators
					temp.retainAll(parent.dominators);
			temp.add(curr);
			if (!temp.equals(curr.dominators)) // If there are any changes
			{
				if (curr.nextBlock != null)
					frontier.add(curr.nextBlock);
				if (curr.jumpBlock != null)
					frontier.add(curr.jumpBlock);
			}
		}
		
		// Using dominators, determine all loops, as well as their header
		Map<BasicBlock, Set<BasicBlock>> loops = new HashMap<> ();
		for (BasicBlock block : explored)
		{
			List<BasicBlock> children = new ArrayList<> (2);
			if (block.nextBlock != null)
				children.add(block.nextBlock);
			if (block.jumpBlock != null)
				children.add(block.jumpBlock);
			
			//// This is probably wrong
			for (BasicBlock child : children)
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
		}
		
		// insert an empty block in front of each loop header
		Map<BasicBlock, BasicBlock> loopPreHeaders = new HashMap<> ();
		for (BasicBlock header : loops.keySet())
		{
			BasicBlock insert = new BasicBlock();
			for (BasicBlock loopParent : header.parents)
				if (!loopParent.dominators.contains(header)) // If the parent is a dominator of the loop
				{
					if (loopParent.nextBlock == header)
						loopParent.nextBlock = insert;
					else if (loopParent.jumpBlock == header)
						loopParent.jumpBlock = insert;
					insert.parents.add(loopParent);
				}
			header.parents.removeAll(insert.parents);
			connect(insert, header, true);
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
						// If the source line is unknown or within the loop, not considered invariant
						//// Future work: could be within the loop but still invariant
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
						{
							if (loopBlock2.nextBlock != null && !loops.get(header).contains(loopBlock2.nextBlock))
								continue outer;
							if (loopBlock2.jumpBlock != null && !loops.get(header).contains(loopBlock2.jumpBlock))
								continue outer;
						}
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