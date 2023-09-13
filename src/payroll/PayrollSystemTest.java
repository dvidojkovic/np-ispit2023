package payroll;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

abstract class Employee implements Comparable<Employee> {
	String type;
	String id;
	String level;
	double rate;
	
	public Employee(String type, String id, String level, double rate) {
		this.type = type;
		this.id = id;
		this.level = level;
		this.rate = rate;
	}
	
	public static Employee createEmployee(String line, Map<String, Double> hourlyRateByLevel, Map<String, Double> ticketRateByLevel) {
		String[] parts = line.split(";");
		String type = parts[0];
		String id = parts[1];
		String level = parts[2];
		
		if (parts.length > 4) {
			List<Integer> list = Arrays.stream(parts).skip(3).map(Integer::parseInt).collect(Collectors.toList());
			return new FreelanceEmployee(type, id, level, list, ticketRateByLevel.get(level));
		} else {
			double hours = Double.parseDouble(parts[3]);
			return new HourlyEmployee(type, id, level, hours, hourlyRateByLevel.get(level));
		}
	}
	
	abstract double getSalary();
	
	abstract public String getId();
	
	abstract public String getLevel();
	
	@Override
	public String toString() {
		return String.format("Employee ID: %s Level: %s", id, level);
	}
	
	@Override
	public int compareTo(Employee o) {
		int res = Double.compare(o.getSalary(), this.getSalary());
		if (res == 0)
			res = o.level.compareTo(this.level);
		return res;
	}
}

class HourlyEmployee extends Employee {
	double hours;
	double overtime;
	double regular;
	
	public HourlyEmployee(String type, String id, String level, double hours, double rate) {
		super(type, id, level, rate);
		this.hours = hours;
		this.overtime = Math.max(0, hours - 40);
		this.regular = hours - overtime;
	}
	
	@Override
	public double getSalary() {
		return regular * rate + overtime * rate * 1.5;
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public String getLevel() {
		return this.level;
	}
	
	@Override
	public String toString() {
		return String.format("%s Salary: %.2f " +
						"Regular hours: %.2f " +
						"Overtime hours: %.2f",
				super.toString(),
				this.getSalary(),
				regular,
				overtime);
	}
}

class FreelanceEmployee extends Employee {
	List<Integer> points;
	
	public FreelanceEmployee(String type, String id, String level, List<Integer> points, double rate) {
		super(type, id, level, rate);
		this.points = points;
	}
	
	@Override
	double getSalary() {
		return getTotalPoints() * rate;
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public String getLevel() {
		return this.level;
	}
	
	private int getTotalPoints() {
		return points.stream().mapToInt(i -> i).sum();
	}
	
	@Override
	public String toString() {
		return String.format("%s Salary: %.2f Tickets count: %d Tickets points: %d", super.toString(), this.getSalary(), this.points.size(), this.getTotalPoints());
	}
}

class PayrollSystem {
	List<Employee> employees;
	Map<String, Double> hourlyRateByLevel;
	Map<String, Double> ticketRateByLevel;
	
	public PayrollSystem(Map<String, Double> hourlyRateByLevel, Map<String, Double> ticketRateByLevel) {
		this.hourlyRateByLevel = hourlyRateByLevel;
		this.ticketRateByLevel = ticketRateByLevel;
		employees = new ArrayList<>();
	}
	
	public void readEmployees(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		employees = br.lines()
				.map(line -> Employee.createEmployee(line, hourlyRateByLevel, ticketRateByLevel))
				.collect(Collectors.toList());
	}
	
	public Map<String, Collection<Employee>> printEmployeesByLevels(OutputStream os, Set<String> levels) {
		Map<String, Collection<Employee>> map = new TreeMap<>();
		
		for (String level : levels) {
			List<Employee> list = employees.stream()
					.filter(i -> i.getLevel().equals(level))
					.sorted()
					.collect(Collectors.toList());
			
			if (! list.isEmpty())
				map.put(level, list);
		}
		
		return map;
	}
}

public class PayrollSystemTest {
	
	public static void main(String[] args) {
		
		Map<String, Double> hourlyRateByLevel = new LinkedHashMap<>();
		Map<String, Double> ticketRateByLevel = new LinkedHashMap<>();
		for (int i = 1; i <= 10; i++) {
			hourlyRateByLevel.put("level" + i, 10 + i * 2.2);
			ticketRateByLevel.put("level" + i, 5 + i * 2.5);
		}
		
		PayrollSystem payrollSystem = new PayrollSystem(hourlyRateByLevel, ticketRateByLevel);
		
		System.out.println("READING OF THE EMPLOYEES DATA");
		payrollSystem.readEmployees(System.in);
		
		System.out.println("PRINTING EMPLOYEES BY LEVEL");
		Set<String> levels = new LinkedHashSet<>();
		for (int i = 5; i <= 10; i++) {
			levels.add("level" + i);
		}
		Map<String, Collection<Employee>> result = payrollSystem.printEmployeesByLevels(System.out, levels);
		result.forEach((level, employees) -> {
			System.out.println("LEVEL: " + level);
			System.out.println("Employees: ");
			employees.forEach(System.out::println);
			System.out.println("------------");
		});
		
		
	}
}