// haven't decided if global or local

package ambroscum;

public class Function extends Value
{
	private List<Parameter> params;
	private Block code;
	
	public Function(List<Parameter> p, Block c)
	{
		params = p;
		code = c;
	}
	
	public Value evaluate(List<Expression> arguments)
	{
		return null;
	}
}