package ambroscum.compiler;

import java.util.*;

public class ControlFlowGraph
{
	private BasicBlock startingBlock;
	private BasicBlock endingBlock;

	private ControlFlowGraph(List<String> instructions)
	{
		Map<String, BasicBlock> labels = new HashMap<> ();
		Set<BasicBlock> hasJump = new HashSet<> ();
		startingBlock = new BasicBlock();
		BasicBlock curr = startingBlock;
		for (String str : instructions)
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
			else if (str.startsWith("call ") || str.startsWith("return "))
			{
				curr.add(str);
				// not actually supported
			}
			else
				curr.add(str);
		}
		endingBlock = curr;
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
	
	public static void analyze(List<String> instructions)
	{
		ControlFlowGraph graph = new ControlFlowGraph(instructions);
		graph.simplifyBlockStructure();
		graph.propogateVariableDeclarations();
		graph.variablePropogation();
		graph.propogateVariableDeclarations();
		graph.removeUnneededDeclarations();
		graph.loopAnalysis();
		graph.printAll();
	}
	
	private void printAll()
	{
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
					if (child == endingBlock) // can't remove ending block from graph
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
		Map<BasicBlock, Map<String, Instruction>> analyzed = new HashMap<> ();
		List<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(startingBlock);
		analyzed.put(startingBlock, new HashMap<String, Instruction> ());
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove(0);
			Map<String, Instruction> currMap = new HashMap<> (analyzed.get(curr));
			for (Instruction inst : curr.instructions)
			{
				inst.preDeclarations = new HashMap<> (currMap);
				if (inst.type.isAssignment())
					currMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), inst);
				inst.postDeclarations = new HashMap<> (currMap);
			}
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
		Map<BasicBlock, Set<String>> analyzed = new HashMap<> ();
		List<BasicBlock> toProcess = new LinkedList<> ();
		toProcess.add(endingBlock);
		analyzed.put(endingBlock, new HashSet<String> ());
		while (!toProcess.isEmpty())
		{
			BasicBlock curr = toProcess.remove(0);
			Set<String> currSet = new HashSet<> (analyzed.get(curr));
			for (ListIterator<Instruction> iter = curr.instructions.listIterator(curr.instructions.size()); iter.hasPrevious();)
			{
				Instruction inst = iter.previous();
				inst.postLiveVariables.addAll(currSet);
				int index = inst.line.indexOf(" = ");
				if (index != -1)
					currSet.remove(inst.line.substring(0, index));
				for (String str : inst.variablesUsed)
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
					int currSize = analyzed.get(parent).size();
					analyzed.get(parent).addAll(currSet);
					if (currSize != analyzed.get(parent).size())
						toProcess.add(parent);
				}
			}
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
				inst.preDeclarations.keySet().retainAll(inst.preLiveVariables);
				inst.postDeclarations.keySet().retainAll(inst.postLiveVariables);
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
	
	private static boolean identifier(String str)
	{
		char c = str.charAt(0);
		if (c == '_')
			return true;
		if (!Character.isLetter(c))
			return false;
		if (str.equals("true") || str.equals("false") || str.equals("paramvalue") || str.equals("returnvalue") || str.equals("print"))
			return false;
		return true;
	}
	
	static class BasicBlock
	{
		List<Instruction> instructions = new LinkedList<Instruction> ();
		Set<BasicBlock> parents = new HashSet<> ();
		Set<BasicBlock> children = new HashSet<> ();
		
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
			System.out.println("Children: " + children);
		}
		
		void variablePropogation()
		{
			for (Instruction inst : instructions)
				inst.optimize();
			Map<String, Set<Instruction>> subExpressions = new HashMap<> ();
			outer: for (Instruction inst : instructions)
			{
				if (inst.type != InstructionType.CALCULATION)
					continue;
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
			for (Instruction inst : instructions)
				inst.optimize();
		}
	}
	
	static class Instruction
	{
		BasicBlock block;
		String line;
		List<String> variablesUsed;
		InstructionType type;
		
		Map<String, Instruction> preDeclarations = new HashMap<> ();
		Map<String, Instruction> postDeclarations = new HashMap<> ();
		Set<String> preLiveVariables = new HashSet<> ();
		Set<String> postLiveVariables = new HashSet<> ();
		
		public Instruction(String str, BasicBlock block)
		{
			this.block = block;
			line = str;
			variablesUsed = new LinkedList<String> ();
			int index = line.indexOf(" = ");
			if (index != -1)
			{
				String[] substrs = line.substring(index + 3).split(" ");
				if (substrs.length > 1)
					type = InstructionType.CALCULATION;
				else if (substrs[0].equals("paramvalue") || substrs[0].equals("returnvalue"))
					type = InstructionType.SPECIALASSIGNMENT;
				else
					type = InstructionType.ASSIGNMENT;
				for (String substr : substrs)
					if (ControlFlowGraph.identifier(substr))
						variablesUsed.add(substr);
			}
			else if (line.startsWith("jumpunless"))
			{
				variablesUsed.add(line.substring(11, line.lastIndexOf(" ")));
				type = InstructionType.JUMP;
			}
			else if (line.startsWith("param"))
			{
				variablesUsed.add(line.substring(6));
				type = InstructionType.FUNCTIONCALL;
			}
			else if (line.startsWith("call"))
			{
				String substr = line.substring(5, line.lastIndexOf(" "));
				if (!substr.equals("print"))
					variablesUsed.add(substr);
				type = InstructionType.FUNCTIONCALL;
			}
		}
		
		void print()
		{
//			System.out.println("\t\tPre-Declarations: " + preDeclarations);
//			System.out.println("\t\tPre-Live Variables: " + preLiveVariables);
//			System.out.println("\t\tReferenced Variables: " + variablesUsed);
			System.out.println("\t" + line);
//			System.out.println("\t\tPost-Declarations: " + postDeclarations);
		}
		
		void optimize()
		{
			boolean optimized = false;
			if (type == InstructionType.ASSIGNMENT || type == InstructionType.CALCULATION)
				for (String str : variablesUsed)
				{
					Instruction decl = preDeclarations.get(str);
					if (decl == null || decl.block != this.block || decl.type != InstructionType.ASSIGNMENT)
						continue;
					String rest = decl.line.substring(decl.line.indexOf(" = ") + 3);
					line = line.replaceAll(str, rest);
					variablesUsed.remove(str);
					if (identifier(rest))
						variablesUsed.add(rest);
					optimized = true;
					break;
				}
	// this "optimization" reverses other optimizations done; I don't remember why I thought it would be useful
/*			if (!optimized && line.indexOf(" = ") != -1 && variablesUsed.size() == 1 && line.endsWith(" = " + variablesUsed.get(0)))
			{
				Instruction decl = preDeclarations.get(variablesUsed.get(0));
				if (decl != null && decl.block == this.block && decl.line.split(" ").length == 5)
				{
					line = line.substring(0, line.indexOf(" = ")) + decl.line.substring(decl.line.indexOf(" = "));
					variablesUsed.remove(0);
					optimized = true;
				}
			}*/
			if (optimized)
				optimize();
		}
		
		public String toString()
		{
			return "inst:\"" + line + "\"";
		}
	}
	
	enum InstructionType
	{
		CALCULATION(true), ASSIGNMENT(true), FUNCTIONCALL(false), SPECIALASSIGNMENT(true), JUMP(false);
		
		private boolean assignmentType;
		
		private InstructionType(boolean assignmentType)
		{
			this.assignmentType = assignmentType;
		}
		
		public boolean isAssignment()
		{
			return assignmentType;
		}
	}
}