import java.util.*;


public class Main {
	public static void main(String[] args) {
		VariableMap map = new VariableMap();
		Map<String, VariableMap> scopes = new HashMap<>();
		System.out.print(IntValue.from(1));
		System.out.println();
		AmbroscumList _e2 = new AmbroscumList();
		_e2.add(IntValue.from(1));
		_e2.add(IntValue.from(2));
		_e2.add(IntValue.from(3));
		System.out.print(_e2);
		System.out.println();
		AmbroscumList _e6 = new AmbroscumList();
		System.out.print(_e6);
		System.out.println();
		AmbroscumList _e7 = new AmbroscumList();
		_e7.add(IntValue.from(1));
		_e7.add(IntValue.from(2));
		_e7.add(IntValue.from(3));
		System.out.print(((AmbroscumList) _e7).get((IntValue) IntValue.from(0)));
		System.out.println();
		System.out.print((IntValue.from(2).operator("+", (IntValue.from(3).operator("*", (IntValue.from(4).operator("-")))))));
		System.out.println();
		System.out.print(((IntValue.from(5).operator("*", IntValue.from(6))).operator("-", IntValue.from(7))));
		System.out.println();
		System.out.print(((IntValue.from(8).operator("/", IntValue.from(2))).operator("%", IntValue.from(3))));
		System.out.println();
		map.put("x", IntValue.from(3));
		System.out.print(map.get("x"));
		System.out.print(" " + (map.get("x").operator("-")));
		System.out.println();
		System.out.print((map.get("x").operator("+", (map.get("x").operator("-")))));
		System.out.println();
		System.out.print((BooleanValue.from(true).operator("and", (IntValue.from(5).operator(">", (IntValue.from(6).operator("-", IntValue.from(3))))))));
		System.out.println();
		System.out.print(((IntValue.from(5).operator("<", IntValue.from(6))).operator("and", BooleanValue.from(true))));
		System.out.println();
		System.out.print(((IntValue.from(3).operator(">", IntValue.from(4))).operator("or", (BooleanValue.from(true).operator("not")))));
		System.out.println();
	}
}
