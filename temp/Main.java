import java.util.*;

class _tl438 extends Function {
	public _tl438(VariableMap parentMap) {
		super(parentMap);
	}
	protected Value call(VariableMap map) {
		Value _te931 = StringValue.from("1 ");
		System.out.print(_te931);
		Value _te932 = IntValue.from(2);
		return _te932;
	}
}

public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		Map<String, VariableMap> scopes = new HashMap<>();
		map.put("f", new _tl438(map));
		Value _te933 = IntValue.from(0);
		System.out.print(_te933);
		System.out.print(" " + ((Function) map.get("f")).call());
	}
}
