package shapes;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

class InvalidDimensionException extends Exception {
	public InvalidDimensionException() {
		super("Dimension 0 is not allowed!");
	}
}

class InvalidIDException extends Exception {
	public InvalidIDException(String message) {
		super("ID " + message + " is not valid");
	}
}

abstract class Shape {
	String id;
	double dimension;
	String type;
	final static double PI = Math.PI;
	static boolean scaled;
	
	public Shape(String type, String id, double dimension) { // 1 = круг -- 2 = квадрат -- 3 = правоаголник
		this.id = id;
		this.dimension = dimension;
		this.type = type;
		scaled = false;
	}
	
	public boolean isScaled() {
		return scaled;
	}
	
	public String getId() {
		return id;
	}
	
	abstract public double getArea();
	
	abstract public double getPerimeter();
	
	abstract public String toString();
	
	abstract public void scale(double scale);
}

class Circle extends Shape {
	
	public Circle(String type, String id, double dimension) {
		super(type, id, dimension);
	}
	
	@Override
	public double getArea() {
		return PI * (dimension * dimension);
	}
	
	@Override
	public double getPerimeter() {
		return 2 * PI * dimension;
	}
	
	@Override
	public String toString() { // Circle -> Radius: 4.88 Area: 74.92 Perimeter: 30.68
		return String.format("Circle -> Radius: %.2f Area: %.2f Perimeter: %.2f", dimension, getArea(), getPerimeter());
	}
	
	@Override
	public void scale(double scale) {
		this.dimension *= scale;
		scaled = true;
	}
}

class Square extends Shape {
	
	public Square(String type, String id, double dimension) {
		super(type, id, dimension);
	}
	
	@Override
	public double getArea() {
		return Math.pow(dimension, 2);
	}
	
	@Override
	public double getPerimeter() {
		return 4 * dimension;
	}
	
	@Override
	public String toString() { // Square: -> Side: 11.68 Area: 136.44 Perimeter: 46.72
		return String.format("Square: -> Side: %.2f Area: %.2f Perimeter: %.2f", dimension, getArea(), getPerimeter());
	}
	
	@Override
	public void scale(double scale) {
		this.dimension *= scale;
		scaled = true;
		
	}
}

class Rectangle extends Shape {
	private double dimension2;
	
	public Rectangle(String type, String id, double dimension, double dimension2) {
		super(type, id, dimension);
		this.dimension = dimension;
		this.dimension2 = dimension2;
	}
	
	
	@Override
	public double getArea() {
		return dimension * dimension2;
	}
	
	@Override
	public double getPerimeter() {
		return 2 * (dimension2 + dimension);
	}
	
	@Override
	public String toString() { // Rectangle: -> Sides: 12.42, 8.74 Area: 108.53 Perimeter: 42.32
		return String.format("Rectangle: -> Sides: %.2f, %.2f Area: %.2f Perimeter: %.2f", dimension, dimension2, getArea(), getPerimeter());
	}
	
	@Override
	public void scale(double scale) {
		this.dimension *= scale;
		this.dimension2 *= scale;
		scaled = true;
	}
}

class Canvas {
	List<Shape> shapes;
	
	public Canvas() {
		shapes = new ArrayList<>();
	}
	
	public void readShapes(InputStream is) throws InvalidDimensionException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Scanner sc = new Scanner(is);
		
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			try {
				shapes.add(ShapeFactory.createShape(line));
			} catch (InvalidIDException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void sortList(List<Shape> shapes) {
		for (int i = 0; i < shapes.size() - 1; i++) {
			for (int j = 0; j < shapes.size() - i - 1; j++) {
				if (shapes.get(j).getArea() > shapes.get(j + 1).getArea()) {
					Shape tmp = shapes.get(j);
					shapes.set(j, shapes.get(j + 1));
					shapes.set(j + 1, tmp);
				}
			}
		}
	}
	
	public void printAllShapes(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		
		if (Shape.scaled) {
			shapes.forEach(pw::println);
		} else {
			sortList(shapes);
			shapes.forEach(pw::println);
		}
		pw.flush();
	}
	
	public void scaleShapes(String id, double scale) {
		shapes.stream().filter(shape -> shape.getId().equals(id)).forEach(shape -> shape.scale(scale));
	}
	
	public void printByUserId(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		
		Map<String, TreeSet<Shape>> map = shapes.stream().collect(Collectors.groupingBy(
				Shape::getId,
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingDouble(Shape::getPerimeter).thenComparing(Shape::getId)))
		));
		
		Comparator<Map.Entry<String, TreeSet<Shape>>> comparator = Comparator.comparing(entry -> entry.getValue().size());
		
		map.entrySet().stream()
				.sorted(comparator.reversed().thenComparing(entry -> entry.getValue().stream().mapToDouble(Shape::getArea).sum()))
				.forEach(entry -> {
					pw.println("Shapes of user: " + entry.getKey());
					entry.getValue().forEach(pw::println);
				});
		pw.flush();
	}
	
	public void statistics(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		DoubleSummaryStatistics dss = shapes.stream().mapToDouble(Shape::getArea).summaryStatistics();
		pw.print(String.format("count: %d\nsum: %.2f\nmin: %.2f\naverage: %.2f\nmax: %.2f", dss.getCount(), dss.getSum(), dss.getMin(), dss.getAverage(), dss.getMax()));
		pw.flush();
		
	}
}

class ShapeFactory {
	private static boolean checkSpecialCharacters(String id) {
		for (char c : id.toCharArray()) {
			if (! Character.isLetterOrDigit(c))
				return true;
		}
		return false;
	}
	
	public static Shape createShape(String line) throws InvalidIDException, InvalidDimensionException {
		String[] parts = line.trim().split("\\s+");
		String id = parts[1];
		
		if (id.length() != 6 || checkSpecialCharacters(id)) {
			throw new InvalidIDException(id);
		}
		
		double dimension1 = Double.parseDouble(parts[2]);
		
		if (dimension1 == 0) {
			throw new InvalidDimensionException();
		}
		
		if (parts.length == 4) {
			double dimension2 = Double.parseDouble(parts[3]);
			if (dimension2 == 0) {
				throw new InvalidDimensionException();
			}
			return new Rectangle(parts[0], id, dimension1, dimension2);
		} else if (parts[0].equals("1")) {
			return new Circle(parts[0], id, dimension1);
		} else {
			return new Square(parts[0], id, dimension1);
		}
	}
}

public class CanvasTest {
	
	public static void main(String[] args) {
		Canvas canvas = new Canvas();
		
		System.out.println("READ SHAPES AND EXCEPTIONS TESTING");
		try {
			canvas.readShapes(System.in);
		} catch (InvalidDimensionException e) {
			System.out.println(e.getMessage());
		}
		
		System.out.println("BEFORE SCALING");
		canvas.printAllShapes(System.out);
		canvas.scaleShapes("123456", 1.5);
		System.out.println("AFTER SCALING");
		canvas.printAllShapes(System.out);
		
		System.out.println("PRINT BY USER ID TESTING");
		canvas.printByUserId(System.out);
		
		System.out.println("PRINT STATISTICS");
		canvas.statistics(System.out);
	}
}