package payrollextension;

import java.util.*;
import java.util.stream.Collectors;

class BonusNotAllowedException extends Exception {
	public BonusNotAllowedException(String bonus) {
		super(String.format("Bonus of %s is not allowed", bonus));
	}
}

interface Employee {
	double getBonus();
	
	double getSalary();
	
	void updateBonus(double amount);
	
	double getOvertime();
	
	String getLevel();
	
	int getTicketsCount();
}

abstract class EmployeeBase implements Employee {
	String id;
	String level;
	double rate;
	double totalBonus;
	
	public EmployeeBase(String id, String level, double rate) {
		this.id = id;
		this.level = level;
		this.rate = rate;
		totalBonus = 0.0;
	}
	
	public double getBonus() {
		return totalBonus;
	}
	
	public void updateBonus(double amount) {
		totalBonus += amount;
	}
	
	public String getLevel() {
		return level;
	}
	
	@Override
	public String toString() {
		return String.format("Employee ID: %s Level: %s", id, level);
	}
}

class HourlyEmployee extends EmployeeBase {
	double hours;
	double overtime;
	double regular;
	
	public HourlyEmployee(String id, String level, double rate, double hours) {
		super(id, level, rate);
		this.hours = hours;
		this.overtime = Math.max(0, hours - 40);
		this.regular = hours - overtime;
	}
	
	@Override
	public double getSalary() {
		return regular * rate + overtime * rate * 1.5 + this.getBonus();
	}
	
	@Override
	public double getOvertime() {
		return overtime * rate * 1.5 ;
	}
	
	@Override
	public int getTicketsCount() {
		return -1;
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

class FreelanceEmployee extends EmployeeBase {
	List<Integer> points;
	
	public FreelanceEmployee(String id, String level, double rate, List<Integer> points) {
		super(id, level, rate);
		this.points = points;
	}
	
	public int getTotalPoints() {
		return points.stream().mapToInt(Integer::intValue).sum();
	}
	
	@Override
	public double getSalary() {
		return points.stream().mapToInt(Integer::intValue).sum() * rate + this.getBonus();
	}
	
	@Override
	public double getOvertime() {
		return - 1;
	}
	
	@Override
	public int getTicketsCount() {
		return points.size();
	}
	
	@Override
	public String toString() {
		return String.format("%s Salary: %.2f " + "Tickets count: %d " + "Tickets points: %d",
				super.toString(),
				this.getSalary(),
				this.points.size(),
				this.getTotalPoints());
	}
}

abstract class BonusDecorator implements Employee {
	Employee employee;
	
	public BonusDecorator(Employee employee) {
		this.employee = employee;
	}
	
	public double getOvertime() {
		return employee.getOvertime();
	}
	
	public String getLevel() {
		return employee.getLevel();
	}
	
	@Override
	public int getTicketsCount() {
		return employee.getTicketsCount();
	}
	
	@Override
	public void updateBonus(double amount) {
		employee.updateBonus(amount);
	}
	
	@Override
	public String toString() {
		return String.format("%s Bonus: %.2f", employee.toString(), employee.getBonus());
	}
}

class FixedBonusDecorator extends BonusDecorator {
	double fixedAmount;
	
	public FixedBonusDecorator(Employee employee, double fixedAmount) {
		super(employee);
		this.fixedAmount = fixedAmount;
		this.employee.updateBonus(fixedAmount);
	}
	
	@Override
	public double getBonus() {
		return fixedAmount;
	}
	
	@Override
	public double getSalary() {
		return employee.getSalary() + employee.getBonus();
	}
}

class PercentageBonusDecorator extends BonusDecorator {
	double percent;
	double bonus;
	
	public PercentageBonusDecorator(Employee employee, double percent) {
		super(employee);
		this.percent = percent;
		bonus = employee.getSalary() * percent / 100.0;
		this.employee.updateBonus(bonus);
	}
	
	@Override
	public double getBonus() {
		return bonus;
	}
	
	@Override
	public double getSalary() {
		return employee.getSalary() + employee.getBonus();
	}
}

class EmployeeFactory {
	public static Employee createEmployee(String line, Map<String, Double> hourlyRateByLevel,
	                                      Map<String, Double> ticketRateByLevel) throws BonusNotAllowedException {
		String[] parts = line.split("\\s+");
		Employee e = createSimpleEmployee(parts[0], hourlyRateByLevel, ticketRateByLevel);
		
		if (parts.length > 1) {
			if (parts[1].contains("%")) {
				double percentage = Double.parseDouble(parts[1].substring(0, parts[1].length() - 1));
				if (percentage > 20) {
					throw new BonusNotAllowedException(parts[1]);
				}
				e = new PercentageBonusDecorator(e, percentage);
			} else {
				double bonusAmount = Double.parseDouble(parts[1]);
				if (bonusAmount > 1000) {
					throw new BonusNotAllowedException(parts[1] + "$");
				}
				e = new FixedBonusDecorator(e, bonusAmount);
			}
		}
		return e;
	}
	
	public static Employee createSimpleEmployee(String line, Map<String, Double> hourlyRateByLevel,
	                                            Map<String, Double> ticketRateByLevel) {
		String[] parts = line.split(";");
		String id = parts[1];
		String level = parts[2];
		if (parts[0].equals("H")) {
			double hours = Double.parseDouble(parts[3]);
			return new HourlyEmployee(id, level, hourlyRateByLevel.get(level), hours);
		} else {
			List<Integer> points = Arrays.stream(parts).skip(3).map(Integer::parseInt).collect(Collectors.toList());
			return new FreelanceEmployee(id, level, ticketRateByLevel.get(level), points);
		}
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
	
	public Employee createEmployee(String line) throws BonusNotAllowedException {
		Employee e = EmployeeFactory.createEmployee(line, hourlyRateByLevel, ticketRateByLevel);
		employees.add(e);
		return e;
	}
	
	public Map<String, Double> getOvertimeSalaryForLevels() {
		
		Map<String, Double> result = employees.stream().collect(Collectors.groupingBy(
				Employee::getLevel,
				Collectors.summingDouble(Employee::getOvertime)
		));
		List<String> list = result.keySet().stream().filter(k -> result.get(k) == - 1).collect(Collectors.toList());
		list.forEach(result::remove);
		
		return result;
	}
	
	public void printStatisticsForOvertimeSalary() {
		DoubleSummaryStatistics dss = employees.stream().filter(i -> i.getOvertime() != -1).mapToDouble(Employee::getOvertime).summaryStatistics();
		System.out.printf("Statistics for overtime salary: " +
				"Min: %.2f Average: %.2f Max: %.2f Sum: %.2f\n",
				dss.getMin(),
				dss.getAverage(),
				dss.getMax(),
				dss.getSum());
	}
	
	public Map<String, Integer> ticketsDoneByLevel() {
		return employees.stream().filter(i -> i.getTicketsCount() != -1).collect(Collectors.groupingBy(
				Employee::getLevel,
				Collectors.summingInt(Employee::getTicketsCount)
				
		));
	}
	
	public Collection<Employee> getFirstNEmployeesByBonus(int n) {
		return employees.stream().sorted(Comparator.comparingDouble(Employee::getBonus).reversed()).limit(n).collect(Collectors.toList());
	}
}

public class PayrollSystemTest2 {
	
	public static void main(String[] args) {
		
		Map<String, Double> hourlyRateByLevel = new LinkedHashMap<>();
		Map<String, Double> ticketRateByLevel = new LinkedHashMap<>();
		for (int i = 1; i <= 10; i++) {
			hourlyRateByLevel.put("level" + i, 11 + i * 2.2);   // level 5: 22 -- level 4: 19.8 -- level 9: 30.8
			ticketRateByLevel.put("level" + i, 5.5 + i * 2.5);
		}
		
		Scanner sc = new Scanner(System.in);
		
		int employeesCount = Integer.parseInt(sc.nextLine());
		
		PayrollSystem ps = new PayrollSystem(hourlyRateByLevel, ticketRateByLevel);
		Employee emp = null;
		for (int i = 0; i < employeesCount; i++) {
			try {
				emp = ps.createEmployee(sc.nextLine());
			} catch (BonusNotAllowedException e) {
				System.out.println(e.getMessage());
			}
		}
		
		int testCase = Integer.parseInt(sc.nextLine());
		
		switch (testCase) {
			case 1: //Testing createEmployee
				if (emp != null)
					System.out.println(emp);
				break;
			case 2: //Testing getOvertimeSalaryForLevels()
				ps.getOvertimeSalaryForLevels().forEach((level, overtimeSalary) -> {
					System.out.printf("Level: %s Overtime salary: %.2f\n", level, overtimeSalary);
				});
				break;
			case 3: //Testing printStatisticsForOvertimeSalary()
				ps.printStatisticsForOvertimeSalary();
				break;
			case 4: //Testing ticketsDoneByLevel
				ps.ticketsDoneByLevel().forEach((level, overtimeSalary) -> {
					System.out.printf("Level: %s Tickets by level: %d\n", level, overtimeSalary);
				});
				break;
			case 5: //Testing getFirstNEmployeesByBonus (int n)
				ps.getFirstNEmployeesByBonus(Integer.parseInt(sc.nextLine())).forEach(System.out::println);
				break;
		}
		
	}
}