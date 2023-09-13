package faculty;

import com.sun.source.tree.Tree;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class OperationNotAllowedException extends Exception {
	public OperationNotAllowedException(String message) {
		super(message);
	}
}

class Course {
	String name;
	int grade;
	IntSummaryStatistics iss;
	
	public Course(String name, int grade) {
		this.name = name;
		this.grade = grade;
	}
	
	public Course(String name) {
		this.name = name;
		iss = new IntSummaryStatistics();
	}
	
	public void addGrade(int grade){
		iss.accept(grade);
	}
	
	public long getStudentCount(){
		return iss.getCount();
	}
	
	public double getAverageGrade(){
		return iss.getAverage();
	}
	
	@Override
	public String toString() {
		return String.format("%s %d %.2f", name, getStudentCount(), getAverageGrade());
	}
}

class Term {
	int number;
	List<Course> courses;
	
	public Term(int number) {
		this.number = number;
		courses = new ArrayList<>();
	}
	
	public void addCourse(Course c) {
		courses.add(c);
	}
	
	public double getAverageGrade() {
		return courses.stream().mapToInt(i -> i.grade).average().orElse(5.0);
	}
	
	public int getPassedCourses() {
		return (int) courses.stream().filter(i -> i.grade >= 5).mapToInt(i -> i.grade).count();
	}
}

class Student {
	String id;
	int yearsOfStudies;
	Map<Integer, Term> terms;
	
	public Student(String id, int yearsOfStudies) {
		this.id = id;
		this.yearsOfStudies = yearsOfStudies;
		terms = new HashMap<>();
		for (int i = 1; i <= yearsOfStudies * 2; i++) {
			terms.put(i, new Term(i));
		}
	}
	
	public void addGrade(String courseName, int grade, int term) throws OperationNotAllowedException {
		if (! terms.containsKey(term)) {
			throw new OperationNotAllowedException("Term " + term + " is not possible for student with ID " + id);
		}
		if (terms.get(term).courses.size() == 3) {
			throw new OperationNotAllowedException("Student " + id + " already has 3 grades in term " + term);
		}
		terms.get(term).addCourse(new Course(courseName, grade));
		
	}
	
	public double getAverageGrade() {
		return terms.values().stream().flatMap(i -> i.courses.stream()).mapToInt(i -> i.grade).average().orElse(5.0);
	}
	
	public double getAverageGrade(int term) {
		return terms.get(term).getAverageGrade();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Student: ").append(id).append("\n");
		terms.forEach((key, value) -> {
			sb.append("Term ").append(key).append("\n");
			sb.append("Courses: ").append(value.courses.size()).append("\n");
			sb.append(String.format("Average grade for term: %.2f\n", getAverageGrade(key)));
		});
		sb.append(String.format("Average grade: %.2f\n", getAverageGrade())); // > 0 ? getAverageGrade() : 5.00
		
		
		sb.append("Courses attended: ")
				.append(terms
						.values().stream()
						.flatMap(i -> i.courses.stream())
						.map(i -> i.name).sorted(Comparator.naturalOrder()).collect(Collectors.joining(",")));
		
		return sb.toString();
	}
	
	public int getAllPassedCourses() {
		return terms.values().stream().mapToInt(Term::getPassedCourses).sum();
	}
}

class Faculty {
	Map<String, Student> students;
	List<String> logs;
	Map<String, Course> courses;
	
	public Faculty() {
		students = new TreeMap<>();
		logs = new ArrayList<>();
		courses = new HashMap<>();
	}
	
	void addStudent(String id, int yearsOfStudies) {
		students.put(id, new Student(id, yearsOfStudies));
	}
	
	void addGradeToStudent(String studentId, int term, String courseName, int grade) throws OperationNotAllowedException {
		if (! students.containsKey(studentId)) {
			return;
		}
		Student student = students.get(studentId);
		student.addGrade(courseName, grade, term);
		
		courses.putIfAbsent(courseName, new Course(courseName));
		courses.get(courseName).addGrade(grade);
		
		if (checkGraduation(student)) {
			logs.add(String.format("Student with ID %s graduated with average grade %.2f in %d years.",
					student.id, student.getAverageGrade(), student.yearsOfStudies));
			students.remove(studentId);
		}
	}
	
	String getFacultyLogs() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.join("\n", logs));
		
		return sb.toString();
	}
	
	String getDetailedReportForStudent(String id) {
		return students.get(id).toString();
	}
	
	void printFirstNStudents(int n) {
		students.values().stream()
				.sorted(Comparator
						.comparingInt(Student::getAllPassedCourses)
						.thenComparingDouble(Student::getAverageGrade)
						.thenComparing(s -> s.id).reversed())
				.limit(n)
				.forEach(s -> {
					System.out.printf("Student: %s Courses passed: %d Average grade: %.2f\n",
							s.id, s.getAllPassedCourses(), s.getAverageGrade());
				});
	}
	
	void printCourses() { // [course_name] [count_of_students] [average_grade]
		Comparator<Course> courseComparator = Comparator
				.comparingLong(Course::getStudentCount)
				.thenComparingDouble(Course::getAverageGrade).thenComparing(i -> i.name);
		
		TreeSet<Course> courseTreeSet = new TreeSet<>(courseComparator);
		
		courseTreeSet.addAll(courses.values());
		courseTreeSet.forEach(System.out::println);
	}
	
	public boolean checkGraduation(Student student) {
		if (student.yearsOfStudies == 3) {
			return student.terms.values().stream().mapToInt(i -> i.courses.size()).sum() == 18;
		} else {
			return student.terms.values().stream().mapToInt(i -> i.courses.size()).sum() == 24;
		}
	}
}

public class FacultyTest {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int testCase = sc.nextInt();
		
		if (testCase == 1) {
			System.out.println("TESTING addStudent AND printFirstNStudents");
			Faculty faculty = new Faculty();
			for (int i = 0; i < 10; i++) {
				faculty.addStudent("student" + i, (i % 2 == 0) ? 3 : 4);
			}
			faculty.printFirstNStudents(10);
			
		} else if (testCase == 2) {
			System.out.println("TESTING addGrade and exception");
			Faculty faculty = new Faculty();
			faculty.addStudent("123", 3);
			faculty.addStudent("1234", 4);
			try {
				faculty.addGradeToStudent("123", 7, "NP", 10);
			} catch (OperationNotAllowedException e) {
				System.out.println(e.getMessage());
			}
			try {
				faculty.addGradeToStudent("1234", 9, "NP", 8);
			} catch (OperationNotAllowedException e) {
				System.out.println(e.getMessage());
			}
		} else if (testCase == 3) {
			System.out.println("TESTING addGrade and exception");
			Faculty faculty = new Faculty();
			faculty.addStudent("123", 3);
			faculty.addStudent("1234", 4);
			for (int i = 0; i < 4; i++) {
				try {
					faculty.addGradeToStudent("123", 1, "course" + i, 10);
				} catch (OperationNotAllowedException e) {
					System.out.println(e.getMessage());
				}
			}
			for (int i = 0; i < 4; i++) {
				try {
					faculty.addGradeToStudent("1234", 1, "course" + i, 10);
				} catch (OperationNotAllowedException e) {
					System.out.println(e.getMessage());
				}
			}
		} else if (testCase == 4) {
			System.out.println("Testing addGrade for graduation");
			Faculty faculty = new Faculty();
			faculty.addStudent("123", 3);
			faculty.addStudent("1234", 4);
			int counter = 1;
			for (int i = 1; i <= 6; i++) {
				for (int j = 1; j <= 3; j++) {
					try {
						faculty.addGradeToStudent("123", i, "course" + counter, (i % 2 == 0) ? 7 : 8);
					} catch (OperationNotAllowedException e) {
						System.out.println(e.getMessage());
					}
					++ counter;
				}
			}
			counter = 1;
			for (int i = 1; i <= 8; i++) {
				for (int j = 1; j <= 3; j++) {
					try {
						faculty.addGradeToStudent("1234", i, "course" + counter, (j % 2 == 0) ? 7 : 10);
					} catch (OperationNotAllowedException e) {
						System.out.println(e.getMessage());
					}
					++ counter;
				}
			}
			System.out.println("LOGS");
			System.out.println(faculty.getFacultyLogs());
			System.out.println("PRINT STUDENTS (there shouldn't be anything after this line!");
			faculty.printFirstNStudents(2);
		} else if (testCase == 5 || testCase == 6 || testCase == 7) {
			System.out.println("Testing addGrade and printFirstNStudents (not graduated student)");
			Faculty faculty = new Faculty();
			for (int i = 1; i <= 10; i++) {
				faculty.addStudent("student" + i, ((i % 2) == 1 ? 3 : 4));
				int courseCounter = 1;
				for (int j = 1; j < ((i % 2 == 1) ? 6 : 8); j++) {
					for (int k = 1; k <= ((j % 2 == 1) ? 3 : 2); k++) {
						try {
							faculty.addGradeToStudent("student" + i, j, ("course" + courseCounter), i % 5 + 6);
						} catch (OperationNotAllowedException e) {
							System.out.println(e.getMessage());
						}
						++ courseCounter;
					}
				}
			}
			if (testCase == 5)
				faculty.printFirstNStudents(10);
			else if (testCase == 6)
				faculty.printFirstNStudents(3);
			else
				faculty.printFirstNStudents(20);
		} else if (testCase == 8 || testCase == 9) {
			System.out.println("TESTING DETAILED REPORT");
			Faculty faculty = new Faculty();
			faculty.addStudent("student1", ((testCase == 8) ? 3 : 4));
			int grade = 6;
			int counterCounter = 1;
			for (int i = 1; i < ((testCase == 8) ? 6 : 8); i++) {
				for (int j = 1; j < 3; j++) {
					try {
						faculty.addGradeToStudent("student1", i, "course" + counterCounter, grade);
					} catch (OperationNotAllowedException e) {
						e.printStackTrace();
					}
					grade++;
					if (grade == 10)
						grade = 5;
					++ counterCounter;
				}
			}
			System.out.println(faculty.getDetailedReportForStudent("student1"));
		} else if (testCase == 10) {
			System.out.println("TESTING PRINT COURSES");
			Faculty faculty = new Faculty();
			for (int i = 1; i <= 10; i++) {
				faculty.addStudent("student" + i, ((i % 2) == 1 ? 3 : 4));
				int courseCounter = 1;
				for (int j = 1; j < ((i % 2 == 1) ? 6 : 8); j++) {
					for (int k = 1; k <= ((j % 2 == 1) ? 3 : 2); k++) {
						int grade = sc.nextInt();
						try {
							faculty.addGradeToStudent("student" + i, j, ("course" + courseCounter), grade);
						} catch (OperationNotAllowedException e) {
							System.out.println(e.getMessage());
						}
						++ courseCounter;
					}
				}
			}
			faculty.printCourses();
		} else if (testCase == 11) {
			System.out.println("INTEGRATION TEST");
			Faculty faculty = new Faculty();
			for (int i = 1; i <= 10; i++) {
				faculty.addStudent("student" + i, ((i % 2) == 1 ? 3 : 4));
				int courseCounter = 1;
				for (int j = 1; j <= ((i % 2 == 1) ? 6 : 8); j++) {
					for (int k = 1; k <= ((j % 2 == 1) ? 2 : 3); k++) {
						int grade = sc.nextInt();
						try {
							faculty.addGradeToStudent("student" + i, j, ("course" + courseCounter), grade);
						} catch (OperationNotAllowedException e) {
							System.out.println(e.getMessage());
						}
						++ courseCounter;
					}
				}
				
			}
			
			for (int i = 11; i < 15; i++) {
				faculty.addStudent("student" + i, ((i % 2) == 1 ? 3 : 4));
				int courseCounter = 1;
				for (int j = 1; j <= ((i % 2 == 1) ? 6 : 8); j++) {
					for (int k = 1; k <= 3; k++) {
						int grade = sc.nextInt();
						try {
							faculty.addGradeToStudent("student" + i, j, ("course" + courseCounter), grade);
						} catch (OperationNotAllowedException e) {
							System.out.println(e.getMessage());
						}
						++ courseCounter;
					}
				}
			}
			System.out.println("LOGS");
			System.out.println(faculty.getFacultyLogs());
			System.out.println("DETAILED REPORT FOR STUDENT");
			System.out.println(faculty.getDetailedReportForStudent("student2"));
			try {
				System.out.println(faculty.getDetailedReportForStudent("student11"));
				System.out.println("The graduated students should be deleted!!!");
			} catch (NullPointerException e) {
				System.out.println("The graduated students are really deleted");
			}
			System.out.println("FIRST N STUDENTS");
			faculty.printFirstNStudents(10);
			System.out.println("COURSES");
			faculty.printCourses();
		}
	}
}
