<<<<<<< HEAD
import java.util.*;


public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		Map<String, VariableMap> scopes = new HashMap<>();
		map.put("a", ((Function) scopes.get(map.get("Test1")).get("new")).call());
		System.out.print(((Function) scopes.get(map.get("a")).get("get")).call());
		System.out.println();
		((Function) scopes.get(map.get("a")).get("set")).call(IntValue.from(3));
		System.out.print(((Function) scopes.get(map.get("a")).get("get")).call());
		System.out.println();
		map.put("b", scopes.get(map.get("a")).get("get"));
		System.out.print(((Function) map.get("b")).call());
		System.out.println();
		System.out.print(((Function) scopes.get(((Function) scopes.get(map.get("Test2")).get("new")).call()).get("get")).call());
		System.out.println();
	}
}
=======
>>>>>>> Silly github
