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
		out.println("  stringTrue: .asciiz \"true\"");
		out.println("  stringFalse: .asciiz \"false\"");
		out.println("  stringSpace: .asciiz \" \"");
		out.println("  stringNewline: .asciiz \"\\n\"");
		out.println();
		out.println(".text");
		
		compile(graph.getMain(), true);
		
		out.flush();
		out.close();
	}
	
	private static void compile(Function func, boolean mainFunction)
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
				out.println("_b" + block.id + ":");
				for (Instruction line : block.instructions)
					compile(line, registersUsed, registersMap);
				if (block == func.endingBlock)
				{
					if (mainFunction)
					{
						out.println("  li $v0 10");
						out.println("  syscall");
					}
					else
						throw new UnsupportedOperationException();
					break;
				}
				if (block.jumpBlock != null)
					toExplore.add(block.jumpBlock);
				block = block.nextBlock;
			} while (block != null);
		}
	}
	
	private static void compile(Instruction inst, Set<String> registersUsed, Map<String, String> registersMap)
	{
//		out.println(inst.line);
		out.flush();
		outer: switch (inst.type)
		{
			case CALCULATION:
			{
				String assignTarget = getEmptyRegister(registersUsed);
				registersUsed.add(assignTarget);
				registersMap.put(inst.line.substring(0, inst.line.indexOf(" = ")), assignTarget);
				String[] parts = inst.line.split(" ");
				String leftReg = registersMap.get(parts[2]);
				if (leftReg == null)
				{
					leftReg = getEmptyRegister(registersUsed);
					registersUsed.add(leftReg);
					setPrimitiveValue(parts[2], "$" + leftReg);
				}
				String rightReg = registersMap.get(parts[4]);
				if (rightReg == null)
				{
					rightReg = getEmptyRegister(registersUsed);
					registersUsed.add(rightReg);
					setPrimitiveValue(parts[4], "$" + rightReg);
				}
				switch (parts[3])
				{
					case "+":
						out.println("  add $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case "<":
						out.println("  slt $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case ">":
						out.println("  slt $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					default:
						throw new UnsupportedOperationException();
				}
				if (registersMap.get(parts[2]) == null)
					registersUsed.remove(leftReg);
				else if (!inst.postLiveVariables.contains(parts[2]))
				{
					registersUsed.remove(registersMap.get(parts[2]));
					registersMap.remove(parts[2]);
				}
				if (registersMap.get(parts[4]) == null)
					registersUsed.remove(rightReg);
				else if (!inst.postLiveVariables.contains(parts[4]))
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
						String register = "$a" + i;
						if (inst.variablesUsed.size() > 0)
							out.println("  move " + register + ", $" + registersMap.get(inst.variablesUsed.get(0)));
						else
							setPrimitiveValue(inst.line.substring(6), register);
						registersUsed.add(register);
						break outer;
					}
				throw new UnsupportedOperationException();
			case FUNCTIONRETURN:
				out.println("  move $v0, " + registersMap.get(inst.variablesUsed.get(0)));
				break;
			case SPECIALASSIGNMENT:
				throw new UnsupportedOperationException();
			case JUMP:
				String jumpTarget = "_b" + inst.block.jumpBlock.id;
				if (inst.line.startsWith("jumpunless"))
				{
					String jumpCond = inst.line.substring(11, inst.line.lastIndexOf(" "));
					out.println("  bne $0, $" + registersMap.get(jumpCond) + ", " + jumpTarget);
					if (!inst.postLiveVariables.contains(jumpCond))
					{
						registersUsed.remove(registersMap.get(jumpCond));
						registersMap.remove(jumpCond);
					}
				}
				else
					out.println("  j " + jumpTarget);
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
	
	private static void setPrimitiveValue(String str, String register)
	{
		if (str.equals("true"))
			out.println("  la " + register + ", stringTrue");
		else if (str.equals("false"))
			out.println("  la " + register + ", stringFalse");
		else if (str.equals("\" \""))
			out.println("  la " + register + ", stringSpace");
		else if (str.equals("\"\\\\n\""))
			out.println("  la " + register + ", stringNewline");
		else if (str.charAt(0) == '\"') // idk what to do if string
			throw new UnsupportedOperationException(str);
		else // if not boolean or string, probably an int (until we add support for other stuff)
		{
			try
			{
				int value = Integer.parseInt(str);
				out.println("  addi " + register + ", $0, " + value);
			}
			catch (NumberFormatException ex)
			{
				throw new AssertionError("printing something weird: " + str);
			}
		}
	}
}