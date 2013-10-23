package ambroscum.lines;

import ambroscum.*;

public class AssertLine extends Line {
	
	private Expression test, error;
	
	protected AssertLine(String line) {
		// Get the test string (7 = index of first char of test, i.e. after "assert ")
		StringBuilder tBuild = new StringBuilder();
		boolean isCode = true;
		int i;
		for (i = 7; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '\"')
				isCode = !isCode;
			else {
				// If we are reading actual code (i.e. not a string literal)
				// and we hit the border of life and death
				// AKA " : ", which delimits the test expression and the error expression
				if (isCode && ch == ' ' && line.charAt(i - 1) == ':' && line.charAt(i - 2) == ' ') {
					// Remove the " :" from the error expression
					tBuild.remove(tBuild.length() - 2, tBuild.length(), "");
					break;
				}
			}
			tBuild.append(ch);
			
		}
		String t = tBuild.toString();
		String e = line.substring(i + 1);
		
		test = Expression.interpret(t);
		error = Expression.interpret(e);
	}
	
	public void evaluate(IdentifierMap values) {
		Value testVal = test.evaluate(values);
		Value errVal = error.evaluate(values);
		if (!((boolean) testVal)) {
			// Raise assertion error. Seriously, how does Value work?
			// And how are we passing strings to the terminal?
		}
	}
}
