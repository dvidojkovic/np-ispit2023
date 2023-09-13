package studentskidosiea;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Student{
	private String id;
	private List<Integer> grades;
	
	public Student(String id, List<Integer> grades) {
		this.id = id;
		this.grades = grades;
	}
	
	public String getId() {
		return id;
	}
	
	public double getAverage(){
		return (double) grades.stream().mapToInt(Integer::intValue).sum() / grades.size();
	}
	
	public List<Integer> getGrades() {
		return grades;
	}
	
	@Override
	public String toString() {
		return String.format("%s %.2f\n", id, getAverage());
	}
}

class Program {
	private String name;
	private Set<Student> students;
	
	public Program(String name) {
		this.name = name;
		students = new TreeSet<>(
				Comparator
						.comparingDouble(Student::getAverage).reversed()
						.thenComparing(Student::getId)
		);
	}
	public void addStudent(Student s){
		students.add(s);
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Student> getStudents() {
		return students;
	}
	
	public int getGradeDistribution(int grade){
		return (int) students.stream().flatMap(i -> i.getGrades().stream()).filter(i -> i.equals(grade)).count();
	}
	
	public String writeGradeDistribution(int grade){
		StringBuilder sb = new StringBuilder();
		int grades = getGradeDistribution(grade);
		double asterisks = Math.ceil(getGradeDistribution(grade)/(double)10);
		
		sb.append(String.format("%2d | ", grade));
		for(int i=0; i<asterisks; i++){
			sb.append("*");
		}
		sb.append(String.format("(%d)\n", grades));
		
		return sb.toString();
	}
}

class StudentRecords {
	Map<String, Program> programMap;
	
	public StudentRecords() {
		programMap = new TreeMap<>();
	}
	
	public int readRecords(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		int count = 0;
		
		try {
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("\\s+");
				String id = parts[0];
				String major = parts[1];
				List<Integer> grades = Arrays.stream(parts).skip(2)
						.map(Integer::parseInt)
						.collect(Collectors.toList());
				Student s = new Student(id, grades);
				programMap.putIfAbsent(major, new Program(major));
				programMap.get(major).addStudent(s);
				count++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return count;
	}
	
	public void writeTable(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		for(Map.Entry<String, Program> entry : programMap.entrySet()){
			pw.println(entry.getKey());
			entry.getValue().getStudents().forEach(pw::print);
		}
		pw.flush();
	}
	
	public void writeDistribution(OutputStream os) {
		PrintWriter pw = new PrintWriter(os);
		
		programMap.values()
				.stream()
				.sorted(Comparator.comparing(s -> -s.getGradeDistribution(10)))
				.forEach(p -> {
					pw.println(p.getName());
					IntStream.range(6, 11).forEach(i -> pw.print(p.writeGradeDistribution(i)));
				});
		
		pw.flush();
	}
}

public class StudentRecordsTest {
	public static void main(String[] args) {
		System.out.println("=== READING RECORDS ===");
		StudentRecords studentRecords = new StudentRecords();
		int total = studentRecords.readRecords(System.in);
		System.out.printf("Total records: %d\n", total);
		System.out.println("=== WRITING TABLE ===");
		studentRecords.writeTable(System.out);
		System.out.println("=== WRITING DISTRIBUTION ===");
		studentRecords.writeDistribution(System.out);
	}
}