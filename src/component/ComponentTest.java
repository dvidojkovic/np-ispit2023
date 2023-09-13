package component;

import java.util.*;

class InvalidPositionException extends Exception{
	public InvalidPositionException(String message) {
		super(message);
	}
}

class Component implements Comparable<Component> {
	String color;
	int weight;
	Set<Component> components;
	
	public Component(String color, int weight) {
		this.color = color;
		this.weight = weight;
		components = new TreeSet<>();
	}
	
	public void addComponent(Component component){
		components.add(component);
	}
	
	public String getColor() {
		return color;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void changeColor(String color, int weight){
		if(this.weight < weight){
			this.color = color;
		}
		components.forEach(i -> i.changeColor(color, weight));
	}
	
	public String format(String crti) {
		StringBuilder sb = new StringBuilder(String.format("%s%d:%s\n", crti, weight, color));
		
		for(Component c : components){
			sb.append(c.format(crti+"---"));
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return format("");
	}
	
	@Override
	public int compareTo(Component o) {
		return Comparator.comparingInt(Component::getWeight)
				.thenComparing(Component::getColor)
				.compare(this, o);
	}
}

class Window {
	String name;
	Map<Integer, Component> components;
	
	public Window(String name) {
		this.name = name;
		components = new TreeMap<>();
	}
	
	public void addComponent(int position, Component component) throws InvalidPositionException {
		if(components.containsKey(position)){
			throw new InvalidPositionException("Invalid position " + position + ", alredy taken!");
		}
		components.put(position, component);
	}
	
	public void changeColor(int weight, String color){
		components.values().forEach(c -> c.changeColor(color, weight));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WINDOW ").append(name).append("\n");
		
		components.forEach((key, value) -> {
			sb.append(key).append(":").append(value.toString());
		});
		
		return sb.toString();
	}
	
	public void swichComponents(int pos1, int pos2){
		Component component = components.remove(pos1);
		components.put(pos1, components.get(pos2));
		components.put(pos2, component);
	}
}

public class ComponentTest {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();
		Window window = new Window(name);
		Component prev = null;
		while (true) {
            try {
				int what = scanner.nextInt();
				scanner.nextLine();
				if (what == 0) {
					int position = scanner.nextInt();
					window.addComponent(position, prev);
				} else if (what == 1) {
					String color = scanner.nextLine();
					int weight = scanner.nextInt();
					Component component = new Component(color, weight);
					prev = component;
				} else if (what == 2) {
					String color = scanner.nextLine();
					int weight = scanner.nextInt();
					Component component = new Component(color, weight);
					prev.addComponent(component);
                    prev = component;
				} else if (what == 3) {
					String color = scanner.nextLine();
					int weight = scanner.nextInt();
					Component component = new Component(color, weight);
					prev.addComponent(component);
				} else if(what == 4) {
                	break;
                }
                
            } catch (InvalidPositionException e) {
				System.out.println(e.getMessage());
			}
            scanner.nextLine();			
		}
		
        System.out.println("=== ORIGINAL WINDOW ===");
		System.out.println(window);
		int weight = scanner.nextInt();
		scanner.nextLine();
		String color = scanner.nextLine();
		window.changeColor(weight, color);
        System.out.println(String.format("=== CHANGED COLOR (%d, %s) ===", weight, color));
		System.out.println(window);
		int pos1 = scanner.nextInt();
		int pos2 = scanner.nextInt();
        System.out.println(String.format("=== SWITCHED COMPONENTS %d <-> %d ===", pos1, pos2));
		window.swichComponents(pos1, pos2);
		System.out.println(window);
	}
}