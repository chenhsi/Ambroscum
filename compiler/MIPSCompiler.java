package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class MIPSCompiler
{
	public static void compile(File input, PrintWriter out) throws IOException
	{
		ControlFlowGraph graph = ILCompiler.compile(input);
		
		out.println(".data");
		out.println();
		out.println(".text");
		
		compile(graph.getMain());
		
		out.println("  li $v0 10");
		out.println("  syscall");
		
		out.flush();
		out.close();
	}
	
	private static void compile(Function func)
	{
		Set<String> registersUsed = new HashSet<> ();
		Map<String, String> registersMap = new HashMap<> ();
		
		Set<BasicBlock> compiled = new HashSet<> ();
		Queue<BasicBlock> toExplore = new LinkedList<> ();
		toExplore.add(func.startingBlock);
		while (!toExplore.isEmpty())
		{
			BasicBlock block = toExplore.remove();
			if (compiled.contains(block))
				continue;
			toExplore.addAll(block.children);
			for (Line line : block.instructions)
				compile(line, registersUsed, registersMap);
		}
	}
	
	private static void compile(Instruction inst, Set<String> registersUsed, Map<String, String> registersMap)
	{
		outer: switch (inst.type)
		{
			case InstructionType.CALCULATION:
				throw new UnsupportedOperationException();
			case InstructionType.ASSIGNMENT:
				String assignTarget = null;
				for (int i = 0; i < 8; i++)
					if (!registersUsed.contains("s" + i))
					{
						assignTarget = "s" + i;
						break;
					}
				for (int i = 0; assignTarget == null && i < 10; i++)
					if (!registersUsed.contains("t" + i))
					{
						assignTarget = "t" + i;
						break;
					}
				if (assignTarget == null)
					throw new UnsupportedOperationException();
				
				String assignValue = inst.variablesUsed.get(0);
				if (Instruction.identifier(assignValue))
				{
					String register = registersMap.get(assignValue);
					out.println("  move $" + assignTarget + ", $" + register);
					if (!inst.postLiveVariables.contains(assignValue))
					{
						registersMap.removeKey(assignValue);
						registersUed.remove(register);
					}
				}
				else
					out.println("  addi $" + assignTarget + ", $0, " + assignValue);
				registersUsed.add(assignTarget);
				break;
			case InstructionType.FUNCTIONCALL:
				String funcName = inst.variablesUsed.get(0);
				if (funcName.equals("print"))
				{
					out.println("  li $v0 4");
					out.println("  syscall");
				}
				else
					throw new UnsupportedOperationException();
				break;
			case InstructionType.FUNCTIONPARAM:
				for (int i = 0; i < 4; i++)
					if (!registersUsed.contains("a" + i))
					{
						out.println("  move $a" + i + ", $" + registerMap.get(inst.variablesUsed.get(0)));
						registersUsed.add("a" + i);
						break outer;
					}
				throw new UnsupportedOperationException();
			case InstructionType.FUNCTIONRETURN:
				out.println("  move $v0, " + registerMap.get(inst.variablesUsed.get(0)));
				break;
			case InstructionType.SPECIALASSIGNMENT:
				throw new UnsupportedOperationException();
			case InstructionType.JUMP:
				throw new UnsupportedOperationException();
		}
	}
}