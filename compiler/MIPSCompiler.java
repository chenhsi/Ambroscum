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
	private static Map<String, Integer> stringLiterals;
	
	public static void compile(File input, PrintWriter out) throws IOException
	{
		ControlFlowGraph graph = ILCompiler.compile(input);
		MIPSCompiler.out = out;
		
		out.println(".data");
		out.println("  stringTrue: .asciiz \"true\"");
		out.println("  stringFalse: .asciiz \"false\"");
//		out.println("  stringSpace: .asciiz \" \"");
		out.println("  stringNewline: .asciiz \"\\n\"");
		stringLiterals = new HashMap<> ();
		printLiterals(graph.getMain());
		out.println();
		out.println(".text");
		
		compile(graph.getMain(), true);
		
		out.flush();
		out.close();
	}
	
	private static void printLiterals(Function func)
	{
		Set<BasicBlock> explored = new HashSet<> ();
		Queue<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(func.startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock block = frontier.remove();
			if (explored.contains(block))
				continue;
			explored.add(block);
			for (Instruction inst : block.instructions)
			{
				for (String part : inst.line.split(" "))
					if (part.charAt(0) == '"' && !stringLiterals.containsKey(part) && !part.equals("\"\\n\""))
					{
						out.println("  string" + stringLiterals.size() + ": .asciiz " + part);
						stringLiterals.put(part, stringLiterals.size());
					}
			}
			if (block.nextBlock != null)
				frontier.add(block.nextBlock);
			if (block.jumpBlock != null)
				frontier.add(block.jumpBlock);
		}
	}
	
	private static Map<String, String> registerAssignment(Function func)
	{
		Map<String, Set<String>> conflicts = new HashMap<> ();
		
		Set<BasicBlock> explored = new HashSet<> ();
		Queue<BasicBlock> frontier = new LinkedList<> ();
		frontier.add(func.startingBlock);
		while (!frontier.isEmpty())
		{
			BasicBlock block = frontier.remove();
			if (explored.contains(block))
				continue;
			explored.add(block);
			for (Instruction inst : block.instructions)
			{
				for (String var1 : inst.postLiveVariables)
				{
					if (!conflicts.containsKey(var1))
						conflicts.put(var1, new HashSet<String> ());
					for (String var2 : inst.postLiveVariables)
					{
						if (var1 == var2)
							continue;
						if (!conflicts.containsKey(var2))
							conflicts.put(var2, new HashSet<String> ());
						conflicts.get(var1).add(var2);
						conflicts.get(var2).add(var1);
					}
				}
			}
			if (block.nextBlock != null)
				frontier.add(block.nextBlock);
			if (block.jumpBlock != null)
				frontier.add(block.jumpBlock);
		}
		System.out.println(conflicts);
		
		Map<String, Integer> colorings = new HashMap<String, Integer> ();
		for (String variable : conflicts.keySet()) // should iterate more cleverly
		{
			Set<Integer> invalid = new HashSet<> ();
			for (String adj : conflicts.get(variable))
				if (colorings.containsKey(adj))
					invalid.add(colorings.get(adj));
			for (int poss = 8; ; poss++)
			{
				if (poss == 26)
					throw new UnsupportedOperationException("too many variables - can't support yet");
				if (invalid.contains(poss))
					continue;
				colorings.put(variable, poss);
				break;
			}
		}
		System.out.println(colorings);
		
		Map<String, String> registersMap = new HashMap<String, String> ();
		for (String str : colorings.keySet())
			registersMap.put(str, colorings.get(str) + "");
		
		return registersMap;
	}
	
	private static void compile(Function func, boolean mainFunction)
	{
		Map<String, String> registersMap = registerAssignment(func);
		
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
				
				List<String> freeRegisters = new LinkedList<String> ();
				for (int i = 8; i < 26; i++)
					freeRegisters.add("" + i);
				if (block.instructions.size() > 0)
					for (String initiallyAlive : block.instructions.get(0).preLiveVariables)
						freeRegisters.remove(registersMap.get(initiallyAlive));
				
				for (Instruction line : block.instructions)
					compile(line, registersMap, freeRegisters, stringLiterals);
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
	
	private static void compile(Instruction inst, Map<String, String> registersMap, List<String> freeRegisters, Map<String, Integer> stringLiterals)
	{
//		out.println(inst.line);
		out.flush();
		outer: switch (inst.type)
		{
			case CALCULATION:
			{
				String[] parts = inst.line.split(" ");
				String assignTarget = registersMap.get(parts[0]);
				String leftReg = registersMap.get(parts[2]);
				if (leftReg == null)
				{
					leftReg = freeRegisters.remove(0);
					setPrimitiveValue(parts[2], "$" + leftReg, false);
				}
				String rightReg = registersMap.get(parts[4]);
				if (rightReg == null)
				{
					rightReg = freeRegisters.remove(0);
					setPrimitiveValue(parts[4], "$" + rightReg, false);
				}
				switch (parts[3])
				{
					case "+":
						out.println("  add $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case "-":
						out.println("  sub $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case "*":
						out.println("  mult $" + leftReg + ", $" + rightReg);
						out.println("  mflo $" + assignTarget);
						break;
					case "<":
						out.println("  slt $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case ">":
						//// this is wrong, but don't currently have a better solution
						out.println("  slt $" + assignTarget + ", $" + rightReg + ", $" + leftReg);
						break;
					case ">=":
						out.println("  slt $" + assignTarget + ", $" + rightReg + ", $" + leftReg);
						break;
					case "=": // complicated thing based on checking if (a & b = a) & (a & b = b) and using that (a & b > a) never
							  // all to avoid jumps - is this actually better?
						String temp0 = freeRegisters.remove(0);
						String temp1 = freeRegisters.remove(0);
						out.println("  and $" + temp0 + ", $" + leftReg + ", $" + rightReg);
						out.println("  sub $" + temp1 + ", $" + temp0 + ", $" + leftReg);
						out.println("  slti $" + temp1 + ", $" + temp1 + ", 0");
						out.println("  sub $" + temp0 + ", $" + temp0 + ", $" + rightReg);
						out.println("  slti $" + temp0 + ", $" + temp0 + ", 0");
						out.println("  and $" + assignTarget + ", $" + temp0 + ", $" + temp1);
						freeRegisters.add(temp0);
						freeRegisters.add(temp1);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported op: " + parts[3]);
				}
				// If it was a temp use or if it was a real variable but dead after this line
				if (registersMap.get(parts[2]) == null || !inst.postLiveVariables.contains(parts[2]))
					freeRegisters.add(leftReg); // free up the space
				if (registersMap.get(parts[4]) == null || !inst.postLiveVariables.contains(parts[4]))
					freeRegisters.add(rightReg);
				freeRegisters.remove(assignTarget);
				break;
			}
			case ASSIGNMENT:
				String[] parts = inst.line.split(" = ");
				String assignTarget = registersMap.get(parts[0]);
				if (Instruction.identifier(parts[1]))
				{
					String source = registersMap.get(parts[1]);
					out.println("  move $" + assignTarget + ", $" + source);
					if (!inst.postLiveVariables.contains(parts[1]))
						freeRegisters.add(source);
				}
				else
					setPrimitiveValue(parts[1], "$" + assignTarget, false);
				freeRegisters.remove(assignTarget);
				break;
			case FUNCTIONCALL:
				String funcName = inst.variablesUsed.get(0);
				if (funcName.equals("print"))
					out.println("  syscall");
				else
					throw new UnsupportedOperationException();
//				for (int i = 0; i < 4; i++)
//					if (!registersUsed.contains("a" + i))
//						break;
//					else
//						registersUsed.remove("a" + i);
				break;
			case FUNCTIONPARAM:
				if (inst.variablesUsed.size() > 0)
				{
					//// what if it isn't $a0? and what if we're not printing right now?
					out.println("  li $v0 1");
					out.println("  move $a0, $" + registersMap.get(inst.variablesUsed.get(0)));
				}
				else
					setPrimitiveValue(inst.line.substring(6), "$a0", true);
				break;
//				for (int i = 0; i < 4; i++)
//					if (!registersUsed.contains("a" + i))
//					{
//						String register = "$a" + i;
//						if (inst.variablesUsed.size() > 0)
//						{
//							out.println("  li $v0 1");
//							out.println("  move " + register + ", $" + registersMap.get(inst.variablesUsed.get(0)));
//						}
//						else
//							setPrimitiveValue(inst.line.substring(6), register, true);
//						registersUsed.add(register);
//						break outer;
//					}
			case FUNCTIONRETURN:
				throw new UnsupportedOperationException();
//				out.println("  move $v0, " + registersMap.get(inst.variablesUsed.get(0)));
			case SPECIALASSIGNMENT:
				throw new UnsupportedOperationException();
			case JUMP:
				String jumpTarget = "_b" + inst.block.jumpBlock.id;
				if (inst.line.startsWith("jumpunless"))
				{
					String jumpCond = inst.line.substring(11, inst.line.lastIndexOf(" "));
					out.println("  beq $0, $" + registersMap.get(jumpCond) + ", " + jumpTarget);
					if (!inst.postLiveVariables.contains(jumpCond))
						freeRegisters.add(jumpCond);
				}
				else
					out.println("  j " + jumpTarget);
				break;
		}
//		out.println("(from " + inst.line);
//		out.println(registersUsed + ", " + registersMap);
		out.flush();
	}
	
	private static void setPrimitiveValue(String str, String register, boolean printing)
	{
		if (str.charAt(0) == '\"')
		{
			if (printing)
				out.println("  li $v0 4");
//			if (str.equals("\" \""))
//				out.println("  la " + register + ", stringSpace");
			if (str.equals("\"\\\\n\""))
				out.println("  la " + register + ", stringNewline");
			else
				out.println("  la " + register + ", string" + stringLiterals.get(str));
		}
		else
		{
			if (printing)
				out.println("  li $v0 1");
			if (str.equals("true"))
				out.println("  li " + register + ", 1");
			else if (str.equals("false"))
				out.println("  li " + register + ", 0");
			else // if not boolean or string, probably an int (until we add support for other stuff)
			{
				try
				{
					int value = Integer.parseInt(str);
					out.println("  li " + register + ", " + value);
				}
				catch (NumberFormatException ex)
				{
					throw new AssertionError("printing something weird: " + str);
				}
			}
		}
	}
}