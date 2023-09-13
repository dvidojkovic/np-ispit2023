package taskmanager;

import java.io.BufferedReader;
import java.io.*;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

class DeadlineNotValidException extends Exception {
	public DeadlineNotValidException(String message) {
		super(String.format("The deadline %s has already passed", message));
	}
}

interface ITask {
	LocalDateTime getDeadline();
	
	int getPriority();
	
	String getCategory();
}

class TaskBase implements ITask { // [категорија][име_на_задача],[oпис],[рок_за_задачата],[приоритет]
	String category;
	String name;
	String description;
	
	public TaskBase(String category, String name, String description) {
		this.category = category;
		this.name = name;
		this.description = description;
	}
	
	@Override
	public LocalDateTime getDeadline() {
		return LocalDateTime.MAX;
	}
	
	@Override
	public int getPriority() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public String getCategory() {
		return category;
	}
	
	@Override
	public String toString() {
		return "Task{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				'}';
	}
}

abstract class TaskDecorator implements ITask {
	ITask task;
	
	public TaskDecorator(ITask task) {
		this.task = task;
	}
	
	@Override
	public String getCategory() {
		return task.getCategory();
	}
}

class PriorityTask extends TaskDecorator {
	int priority;
	
	public PriorityTask(ITask task, int priority) {
		super(task);
		this.priority = priority;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(task.toString(), 0, task.toString().length() - 1);
		sb.append(String.format(", priority=%d}", priority));
		return sb.toString();
	}
	
	@Override
	public LocalDateTime getDeadline() {
		return task.getDeadline();
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
}

class TimeTask extends TaskDecorator {
	LocalDateTime time;
	
	public TimeTask(ITask task, LocalDateTime time) {
		super(task);
		this.time = time;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(task.toString(), 0, task.toString().length() - 1);
		sb.append(String.format(", deadline=%s}", time.toString()));
		
		return sb.toString();
	}
	
	@Override
	public LocalDateTime getDeadline() {
		return time;
	}
	
	@Override
	public int getPriority() {
		return task.getPriority();
	}
}

class TaskManager {
	Map<String, List<ITask>> tasks;
	
	public void readTasks(InputStream inputStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		tasks = br.lines()
				.map(line -> {
					try {
						return TaskFactory.createTask(line);
					} catch (DeadlineNotValidException e) {
						System.out.println(e.getMessage());
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(
						ITask::getCategory,
						HashMap::new,
						Collectors.toCollection(ArrayList::new)
				));
	}
	
	public void printTasks(OutputStream os, boolean includePriority, boolean includeCategory) {
		PrintWriter pw = new PrintWriter(os);
		Comparator<ITask> complexComparator = Comparator
				.comparingInt(ITask::getPriority)
				.thenComparing(task -> Duration.between(LocalDateTime.now(), task.getDeadline()));
		
		Comparator<ITask> simpleComparator = Comparator.comparing(task -> Duration.between(LocalDateTime.now(), task.getDeadline()));
		
		
		if(includeCategory){
			tasks.forEach((key, value) -> {
				pw.println(key.toUpperCase());
				value.stream().sorted(includePriority ? complexComparator : simpleComparator).forEach(pw::println);
			});
		}
		else{
			tasks.values().stream()
					.flatMap(Collection::stream)
					.sorted(includePriority ? complexComparator : simpleComparator)
					.forEach(pw::println);
		}
		pw.flush();
	}
}

class TaskFactory {
	public static ITask createTask(String line) throws DeadlineNotValidException {
		String[] parts = line.split(",");
		ITask task = createSimpleTask(line);
		
		if (parts.length == 5) {
			LocalDateTime time = LocalDateTime.parse(parts[3]);
			checkDeadline(time);
			int priority = Integer.parseInt(parts[4]);
			task = new PriorityTask((new TimeTask(task, time)), priority);
		} else if (parts.length == 4) {
			try {
				int priority = Integer.parseInt(parts[3]);
				task = new PriorityTask(task, priority);
			} catch (Exception e) {
				LocalDateTime time = LocalDateTime.parse(parts[3]);
				checkDeadline(time);
				task = new TimeTask(task, time);
			}
		}
		return task;
	}
	
	public static void checkDeadline(LocalDateTime time) throws DeadlineNotValidException {
		LocalDate date = LocalDate.of(2020, 6, 2);
		LocalDateTime ldt = LocalDateTime.of(date, LocalTime.of(0, 0, 0));
		if(time.isBefore(ldt))
			throw new DeadlineNotValidException(time.toString());
	}
	
	public static ITask createSimpleTask(String line) {
		String[] parts = line.split(",");
		String category = parts[0];
		String name = parts[1];
		String description = parts[2];
		return new TaskBase(category, name, description);
	}
}

public class TasksManagerTest {
	
	public static void main(String[] args) {
		
		TaskManager manager = new TaskManager();
		
		System.out.println("Tasks reading");
		manager.readTasks(System.in);
		System.out.println("By categories with priority");
		manager.printTasks(System.out, true, true);
		System.out.println("-------------------------");
		System.out.println("By categories without priority");
		manager.printTasks(System.out, false, true);
		System.out.println("-------------------------");
		System.out.println("All tasks without priority");
		manager.printTasks(System.out, false, false);
		System.out.println("-------------------------");
		System.out.println("All tasks with priority");
		manager.printTasks(System.out, true, false);
		System.out.println("-------------------------");
		
	}
}
