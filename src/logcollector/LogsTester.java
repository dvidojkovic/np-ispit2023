package logcollector;

import java.util.*;
import java.util.stream.Collectors;

abstract class Log {
	String microservice;
	String service;
	String message;
	long timestamp;
	String type;
	
	public Log(String service, String microservice, String type, String message, long timestamp) {
		this.microservice = microservice;
		this.service = service;
		this.message = message;
		this.timestamp = timestamp;
		this.type = type;
	}
	
	public static Log createLog(String line) {
		String[] parts = line.split("\\s+");
		String service = parts[0];
		String microservice = parts[1];
		String type = parts[2];
		String message = Arrays.stream(parts).skip(3).limit(parts.length - 4).collect(Collectors.joining(" "));
		long timestamp = Long.parseLong(parts[parts.length - 1]);
		
		if (type.equals("ERROR"))
			return new ELog(service, microservice, type, message, timestamp);
		else if (type.equals("WARN"))
			return new WLog(service, microservice, type, message, timestamp);
		else
			return new ILog(service, microservice, type, message, timestamp);
	}
	
	@Override
	public String toString() { // service2|microservice2 [INFO] Log message 9. 953 T:953
		return String.format("%s|%s [%s] %s %s T:%s", service, microservice, type, message, timestamp, timestamp);
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	abstract int getSeverity();
}

class WLog extends Log {
	
	public WLog(String service, String microservice, String type, String message, long timestamp) {
		super(service, microservice, type, message, timestamp);
	}
	
	@Override
	int getSeverity() {
		return message.contains("might cause error") ? 2 : 1;
	}
}

class ELog extends Log {
	
	public ELog(String service, String microservice, String type, String message, long timestamp) {
		super(service, microservice, type, message, timestamp);
	}
	
	@Override
	int getSeverity() {
		int severity = 3;
		if (message.contains("fatal"))
			severity += 2;
		if (message.contains("exception"))
			severity += 3;
		return severity;
	}
}

class ILog extends Log {
	
	public ILog(String service, String microservice, String type, String message, long timestamp) {
		super(service, microservice, type, message, timestamp);
	}
	
	@Override
	int getSeverity() {
		return 0;
	}
}

class Microservice {
	String name;
	List<Log> logs;
	
	public Microservice(String name) {
		this.name = name;
		logs = new ArrayList<>();
	}
	
	public void addLog(Log log) {
		logs.add(log);
	}
}

class Service {
	String name;
	Map<String, Microservice> microservices;
	
	public Service(String name) {
		this.name = name;
		microservices = new HashMap<>();
	}
	
	public void addLog(Log log) {
		microservices.putIfAbsent(log.microservice, new Microservice(log.microservice));
		microservices.get(log.microservice).addLog(log);
	}
	
	public double getAverageSeverity() {
		return microservices.values().stream().flatMap(i -> i.logs.stream()).mapToInt(Log::getSeverity).average().orElse(0.0);
	}
	
	@Override
	public String toString() {
		
		IntSummaryStatistics iss = microservices.values()
				.stream()
				.flatMap(i -> i.logs.stream())
				.mapToInt(Log::getSeverity)
				.summaryStatistics();
		
		return String.format("Service name: %s " +
						"Count of microservices: %d " +
						"Total logs in service: %d " +
						"Average severity for all logs: %.2f " +
						"Average number of logs per microservice: %.2f",
				this.name,
				microservices.size(),
				iss.getCount(),
				iss.getAverage(),
				iss.getCount() / (double) microservices.size());
	}
	
	public void displayLogs(String microservice, Comparator<Log> comparator) {
		List<Log> logs;
		
		if (microservice == null) {
			logs = microservices.values().stream().flatMap(i -> i.logs.stream()).collect(Collectors.toList());
		} else {
			logs = microservices.get(microservice).logs;
		}
		
		logs.stream().sorted(comparator).forEach(System.out::println);
	}
}

class LogCollector {
	Map<String, Service> services;
	
	public LogCollector() {
		services = new HashMap<>();
	}
	
	public void addLog(String line) {
		Log log = Log.createLog(line);
		services.putIfAbsent(log.service, new Service(log.service));
		services.get(log.service).addLog(log);
	}
	
	public void printServicesBySeverity() {
		services.values()
				.stream()
				.sorted(Comparator.comparingDouble(Service::getAverageSeverity).reversed())
				.forEach(System.out::println);
	}
	
	public Map<Integer, Integer> getSeverityDistribution(String service, String microservice) {
		List<Log> severities;
		if (microservice == null) {
			severities = services.get(service).microservices.values().stream().flatMap(i -> i.logs.stream()).collect(Collectors.toList());
		} else {
			severities = new ArrayList<>(services.get(service).microservices.get(microservice).logs);
		}
		
		return severities.stream().collect(Collectors.groupingBy(
				Log::getSeverity,
				Collectors.summingInt(i -> 1)
		));
	}
	
	public void displayLogs(String service, String microservice, String order) {
		Comparator<Log> comparatorSeverity = Comparator.comparingInt(Log::getSeverity).thenComparingLong(Log::getTimestamp);
		Comparator<Log> comparatorTime = Comparator.comparingLong(Log::getTimestamp);
		
		Comparator<Log> logComparator;
		
		switch (order) {
			case "NEWEST_FIRST":
				logComparator = comparatorTime.reversed();
				break;
			case "OLDEST_FIRST":
				logComparator = comparatorTime;
				break;
			case "MOST_SEVERE_FIRST":
				logComparator = comparatorSeverity.reversed();
				break;
			default:
				logComparator = comparatorSeverity;
		}
		
		services.get(service).displayLogs(microservice, logComparator);
		
	}
}

public class LogsTester { // sekoj servis chuva mikroservis, sekoj mikroservis chuva logovi
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		LogCollector collector = new LogCollector();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.startsWith("addLog")) {
				collector.addLog(line.replace("addLog ", ""));
			} else if (line.startsWith("printServicesBySeverity")) {
				collector.printServicesBySeverity();
			} else if (line.startsWith("getSeverityDistribution")) {
				String[] parts = line.split("\\s+");
				String service = parts[1];
				String microservice = null;
				if (parts.length == 3) {
					microservice = parts[2];
				}
				collector.getSeverityDistribution(service, microservice).forEach((k, v) -> System.out.printf("%d -> %d%n", k, v));
			} else if (line.startsWith("displayLogs")) {
				String[] parts = line.split("\\s+");
				String service = parts[1];
				String microservice = null;
				String order = null;
				if (parts.length == 4) {
					microservice = parts[2];
					order = parts[3];
				} else {
					order = parts[2];
				}
				System.out.println(line);
				collector.displayLogs(service, microservice, order);
			}
		}
	}
}
