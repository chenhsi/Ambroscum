package ambroscum.compiler;

import java.io.*;
import java.util.*;
import ambroscum.expressions.*;
import ambroscum.lines.*;
import ambroscum.parser.TokenStream;
import ambroscum.values.*;

public class MIPSCompiler
{
	private static PrintWriter out;
	
	public static void compile(File input, PrintWriter out) throws IOException
	{
		ControlFlowGraph graph = ILCompiler.compile(input);
		MIPSCompiler.out = out;
		
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
			do
			{
				if (compiled.contains(block))
					break;
				compiled.add(block);
				toExplore.addAll(block.children);
				if (block.name != null)
					out.println(block.name + ":");
				for (Instruction line : block.instructions)
					compile(line, registersUsed, registersMap);
				block = block.nextBlock;
			} while (block != null);
		}
	}
	
	private static void compile(Instruction inst, Set<String> registersUsed, Map<String, String> registersMap)
	{
		outer: switch (inst.type)
		{
			case CALCULATION:
			{
				String assignTarget = getEmptyRegister(registersUsed);
				registersUsed.add(assignTarget);
				registersMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), assignTarget);
				String[] parts = inst.line.split(" ");
				switch (parts[3])
				{
					case "+":
						out.println("  add $" + assignTarget + ", $" + registersMap.get(parts[2]) + ", " + registersMap.get(parts[4]));
						break;
					case "<":
						out.println("  slt $" + assignTarget + ", $" + registersMap.get(parts[2]) + ", " + registersMap.get(parts[4]));
						break;
					case ">":
						out.println("  slt $" + assignTarget + ", $" + registersMap.get(parts[4]) + ", " + registersMap.get(parts[2]));
						break;
					default:
						throw new UnsupportedOperationException();
				}
				if (!inst.postLiveVariables.contains(parts[2]))
				{
					registersUsed.remove(registersMap.get(parts[2]));
					registersMap.remove(parts[2]);
				}
				if (!inst.postLiveVariables.contains(parts[4]))
				{
					registersUsed.remove(registersMap.get(parts[4]));
					registersMap.remove(parts[4]);
				}
				break;
			}
			case ASSIGNMENT:
				String assignTarget = getEmptyRegister(registersUsed);
				registersUsed.add(assignTarget);
				String assignValue = inst.line.substring(inst.line.indexOf(" = ") + 3);
				if (assignValue.equals("true"))
					assignValue = "1";
				if (assignValue.equals("false"))
					assignValue = "0";
				if (Instruction.identifier(assignValue))
				{
					String register = registersMap.get(assignValue);
					out.println("  move $" + assignTarget + ", $" + register);
					if (!inst.postLiveVariables.contains(assignValue))
					{
						registersMap.remove(assignValue);
						registersUsed.remove(register);
					}
				}
				else
					out.println("  addi $" + assignTarget + ", $0, " + assignValue);
				registersMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), assignTarget);
				registersUsed.add(assignTarget);
				break;
			case FUNCTIONCALL:
				String funcName = inst.variablesUsed.get(0);
				if (funcName.equals("print"))
				{
					out.println("  li $v0 4");
					out.println("  syscall");
				}
				else
					throw new UnsupportedOperationException();
				for (int i = 0; i < 4; i++)
					if (!registersUsed.contains("a" + i))
						break;
					else
						registersUsed.remove("a" + i);
				break;
			case FUNCTIONPARAM:
				for (int i = 0; i < 4; i++)
					if (!registersUsed.contains("a" + i))
					{
						out.println("  move $a" + i + ", $" + registersMap.get(inst.variablesUsed.get(0)));
						registersUsed.add("a" + i);
						break outer;
					}
				throw new UnsupportedOperationException();
			case FUNCTIONRETURN:
				out.println("  move $v0, " + registersMap.get(inst.variablesUsed.get(0)));
				break;
			case SPECIALASSIGNMENT:
				throw new UnsupportedOperationException();
			case JUMP:
				if (inst.line.startsWith("jumpunless"))
				{
					String jumpCond = inst.line.substring(11, inst.line.lastIndexOf(" "));
					String jumpTarget = inst.line.substring(inst.line.lastIndexOf(" ") + 1);
					out.println("  bne $0, $" + registersMap.get(jumpCond) + ", " + jumpTarget);
					if (!inst.postLiveVariables.contains(jumpCond))
					{
						registersUsed.remove(registersMap.get(jumpCond));
						registersMap.remove(jumpCond);
					}
				}
				else
					out.println("  j " + inst.line.substring(5));
				break;
		}
//		out.println("(from " + inst.line);
//		out.println(registersUsed + ", " + registersMap);
		out.flush();
	}
	
	private static String getEmptyRegister(Set<String> registersUsed)
	{
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
		return assignTarget;
	}
}