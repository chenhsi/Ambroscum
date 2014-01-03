package ambroscum.parser;

import java.util.List;
import java.util.Scanner;
import java.util.Queue;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.io.*;
import ambroscum.errors.SyntaxError;

public class TokenStream
{
	private boolean interactive;
	private Queue<Token> tokens;
	
	private TokenStream(boolean interactive)
	{
		this.interactive = interactive;
		tokens = new LinkedList<Token> ();
	}
	
	public Token getFirst()
	{
		while (tokens.isEmpty())
		{
			if (interactive)
				readInteractiveLine();
			else
				throw new SyntaxError("Unexpected end of input");
		}
		return tokens.peek();
	}
	
	public Token removeFirst()
	{
		getFirst();
		return tokens.poll();
	}
	
	private static final Scanner interactiveInput = new Scanner(System.in);
	public void readInteractiveLine()
	{
		System.out.print(">>> ");
		tokens.addAll(Tokenizer.tokenize(interactiveInput.nextLine() + "\n"));
	}
	
	public void makeInteractive()
	{
		interactive = true;
	}
	
	public boolean hasNext()
	{
		return !tokens.isEmpty();
	}
	
	public static TokenStream readFile(File fileName) throws FileNotFoundException
	{
		TokenStream stream = new TokenStream(false);
		Scanner in = new Scanner(new FileInputStream(fileName));
		StringBuilder sb = new StringBuilder();
		while (in.hasNextLine())
			sb.append(in.nextLine()).append("\n");
		stream.tokens.addAll(Tokenizer.tokenize(sb.toString()));
		stream.tokens.add(Token.EOF);
		return stream;
	}
	
	public static TokenStream interactiveInput()
	{
		return new TokenStream(true);
	}
	
	public static TokenStream readAsStream(List<Token> list)
	{
		TokenStream stream = new TokenStream(false);
		stream.tokens.addAll(list);
		return stream;
	}
	
	public String toString()
	{
		return "(stream " + interactive + " " + tokens + ")";
	}
}