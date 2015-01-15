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
		
		for (String funcName : graph.getFunctions().keySet())
		{
			out.println(funcName + ":");
			compile(graph.getFunctions().get(funcName), false);
		}
		
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
				int index = inst.line.indexOf("\"");
				while (index != -1)
				{
					int index2 = inst.line.indexOf("\"", index + 1);
					String str = inst.line.substring(index, index2 + 1);
					if (!stringLiterals.containsKey(str) && !str.equals("\"\\n\""))
					{
						out.println("  string" + stringLiterals.size() + ": .asciiz " + str);
						stringLiterals.put(str, stringLiterals.size());
					}
					index = inst.line.indexOf("\"", index2 + 1);
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
						out.println("  jr $ra");
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
					case "<=":
						out.println("  sle $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
						break;
					case ">":
						out.println("  slt $" + assignTarget + ", $" + rightReg + ", $" + leftReg);
						break;
					case ">=":
						out.println("  sle $" + assignTarget + ", $" + rightReg + ", $" + leftReg);
						break;
					case "=":
						out.println("  seq $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
//																		  // assignTarget if left = right | left != right
//						out.println("  xor $" + assignTarget + ", $" + leftReg + ", $" + rightReg); // 0 | positive
//						out.println("  slt $" + assignTarget + ", $0, $" + assignTarget); 			 // 0 | 1
//						out.println("  addi $" + assignTarget + ", $" + assignTarget + ", -1"); 	// -1 | 0
//						out.println("  sub $" + assignTarget + ", $0, $" + assignTarget); 			 // 1 | 0
						break;
					case "!=":
						out.println("  sne $" + assignTarget + ", $" + leftReg + ", $" + rightReg);
//																		  // assignTarget if left = right | left != right
//						out.println("  xor $" + assignTarget + ", $" + leftReg + ", $" + rightReg); // 0 | positive
//						out.println("  slt $" + assignTarget + ", $0, $" + assignTarget); 			 // 0 | 1
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
				if (parts[0].charAt(0) == '*') // Assigning to memory
				{
					String assignTarget = registersMap.get(parts[0].substring(1));
					if (Instruction.identifier(parts[1]))
					{
						String source = registersMap.get(parts[1]);
						out.println("  sw $" + source + ", 4($" + assignTarget + ")");
					}
					else // Store the literal in a temporary variable first
					{
						String temp = freeRegisters.remove(0);
						setPrimitiveValue(parts[1], "$" + temp, false);
						out.println("  sw $" + temp + ", 4($" + assignTarget + ")");
						freeRegisters.add(temp);
					}
					freeRegisters.remove(assignTarget);
				}
				else // Assigning to a regular variable
				{
					String assignTarget = registersMap.get(parts[0]);
					if (Instruction.identifier(parts[1]))
					{
						if (parts[1].startsWith("*_func")) // function declaration
						{
							String source = registersMap.get(parts[1].substring(1));
							out.println("  li $" + assignTarget + ", " + source);
						}
						else if (parts[1].charAt(0) == '*') // Loading value from memory
						{
							String source = registersMap.get(parts[1].substring(1));
							out.println("  lw $" + assignTarget + ", 4($" + source + ")");
						}
						else // Regular assignment of one variable to another - this should be optimized away
						{
							String source = registersMap.get(parts[1]);
							out.println("  move $" + assignTarget + ", $" + source);
							if (!inst.postLiveVariables.contains(parts[1]))
								freeRegisters.add(source);
						}
					}
					else // Assigning a literal to a regular variable
						setPrimitiveValue(parts[1], "$" + assignTarget, false);
					freeRegisters.remove(assignTarget);
				}
				break;
			case FUNCTIONCALL:
				String funcName = inst.line.substring(5, inst.line.lastIndexOf(" "));
				if (funcName.equals("print"))
					out.println("  syscall");
				else if (funcName.equals("malloc"))
				{
					out.println("  li $v0, 9");
					out.println("  li $a0, " + inst.line.substring(inst.line.indexOf("malloc") + 7));
					out.println("  syscall");
				}
				else
				{
					out.println("  jal " + funcName);
					for (int i = 0; i < 4; i++)
						if (freeRegisters.contains("a" + i))
							break;
						else
							freeRegisters.add("a" + i);
				}
				break;
			case FUNCTIONPARAM:
				if (inst.variablesUsed.size() > 0)
				{
					//// what if it isn't $a0? and what if we're not printing right now?
					out.println("  li $v0, 1");
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
				String returnValue = inst.line.substring(7);
				if (returnValue.equals("null"))
					break;
				else if (Instruction.identifier(returnValue))
					out.println("  move $v0, " + registersMap.get(returnValue));
				else
					setPrimitiveValue(returnValue, "$v0", false);
				break;
			case SPECIALASSIGNMENT:
				if (inst.line.endsWith("paramvalue"))
					throw new UnsupportedOperationException();
				else if (inst.line.endsWith("returnvalue"))
				{
					String register = registersMap.get(inst.line.substring(0, inst.line.indexOf(" = ")));
					out.println("  move $" + register + ", $v0");
					freeRegisters.remove(register);
					break;
				}
				else
					throw new AssertionError();
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
				out.println("  li $v0, 4");
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
				out.println("  li $v0, 1");
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