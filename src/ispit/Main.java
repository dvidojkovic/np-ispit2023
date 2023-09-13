package ispit;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Student {
	List<Character> previousGrades;
	List<Character> currentGrades;
	String id;
	
	public Student(List<Character> previousGrades, List<Character> currentGrades, String id) {
		this.previousGrades = previousGrades;
		this.currentGrades = currentGrades;
		this.id = id;
	}
	
	public Student(String line){ // 203233;A, B, C, D;A, A, A, A;
		String[] parts = line.split(";");
		this.id = parts[0];
		List<Character> list1 = Arrays.stream(parts[1].split(", "))
				.flatMapToInt(String::chars)
				.mapToObj(c -> (char) c).collect(Collectors.toList());
	}
}

class Check{
	public Map<String, Integer> process(InputStream is){
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		br.lines()
				.map(i -> new Student(i))
				.
	}
}

public class Main {
	public static void main(String[] args) {
		StudentEvaluation.process(System.in).forEach( (i, j) -> System.out.printf("%d -> %d", i, j));
	}
}
