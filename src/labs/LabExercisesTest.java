package labs;

import java.util.*;
import java.util.stream.Collectors;

class Student implements Comparable<Student>{
	private String index;
	private List<Integer> points;
	
	public Student(String index, List<Integer> points) {
		this.index = index;
		this.points = points;
	}
	
	public String getIndex() {
		return index;
	}
	
    public int getYear(){
        return 20-(Integer.parseInt(index) / 10000);
    }
    
	public double getPointsAverage() {
		return (double) points.stream().mapToInt(Integer::intValue).sum() / 10;
	}
	
	public String passed() {
		boolean res = points.size() >= 8;
		return res ? "YES" : "NO";
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %.2f", index, passed(), getPointsAverage());
	}
    
    @Override
    public int compareTo(Student o) {
        int res = Double.compare(this.getPointsAverage(), o.getPointsAverage());
        if(res == 0){
            res = this.getIndex().compareTo(o.getIndex());
        }
        return res;
    }
}

class LabExercises {
	private List<Student> students;
	
	public LabExercises() {
		students = new ArrayList<>();
	}
	
	public void addStudent(Student student) {
		students.add(student);
	}
	
	public void printByAveragePoints(boolean ascending, int n) {
		Comparator<Student> comparator = Comparator.naturalOrder();
		if (!ascending) {
			comparator = Comparator.reverseOrder();
		}
		students.stream().sorted(comparator).limit(n).forEach(System.out::println);
	}
	
	public List<Student> failedStudents() {
		return students.stream()
                .filter(i -> i.passed().equals("NO"))
                .sorted(Comparator
                        .comparing(Student::getIndex)
                        .thenComparingDouble(Student::getPointsAverage))
                .collect(Collectors.toList());
	}
	
	public Map<Integer, Double> getStatisticsByYear() {
		return students.stream()
                .filter(i -> i.passed().equals("YES"))
                .collect(Collectors.groupingBy(
                    Student::getYear,
                    Collectors.averagingDouble(Student::getPointsAverage)
                ));
	}
}

public class LabExercisesTest {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		LabExercises labExercises = new LabExercises();
		while (sc.hasNextLine()) {
			String input = sc.nextLine();
			String[] parts = input.split("\\s+");
			String index = parts[0];
			List<Integer> points = Arrays.stream(parts).skip(1)
					.mapToInt(Integer::parseInt)
					.boxed()
					.collect(Collectors.toList());
			
			labExercises.addStudent(new Student(index, points));
		}
		
		System.out.println("===printByAveragePoints (ascending)===");
		labExercises.printByAveragePoints(true, 100);
		System.out.println("===printByAveragePoints (descending)===");
		labExercises.printByAveragePoints(false, 100);
		System.out.println("===failed students===");
		labExercises.failedStudents().forEach(System.out::println);
		System.out.println("===statistics by year");
		labExercises.getStatisticsByYear().entrySet().stream()
				.map(entry -> String.format("%d : %.2f", entry.getKey(), entry.getValue()))
				.forEach(System.out::println);
		
	}
}