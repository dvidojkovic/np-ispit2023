package airports;

import java.util.*;
import java.util.stream.Collectors;

class Flight {
	String from;
	String to;
	int time;
	int duration;
	
	public Flight(String from, String to, int time, int duration) {
		this.from = from;
		this.to = to;
		this.time = time;
		this.duration = duration;
	}
	
	public String getFrom() {
		return from;
	}
	
	public int getTotal() {
		return duration + time;
	}
	
	public String getTo() {
		return to;
	}
	
	public int getTime() {
		return time;
	}
	
	public int getDuration() {
		return duration;
	}
	
	@Override
	public String toString() {
		int totalTime = duration + time;
		
		int hoursDuration = duration / 60 % 24;
		int minutes = duration % 60;
		
		int hoursTime = time / 60 % 24;
		int minutesTime = time % 60;
		
		int hourTotal = totalTime / 60 % 24;
		int minuteTotal = totalTime % 60;
		
		if (totalTime > 1439) {
			return String.format("%s-%s %02d:%02d-%02d:%02d +1d %dh%02dm",
					from, to, hoursTime, minutesTime, hourTotal, minuteTotal, hoursDuration, minutes);
		}
		return String.format("%s-%s %02d:%02d-%02d:%02d %dh%02dm",
				from, to, hoursTime, minutesTime, hourTotal, minuteTotal, hoursDuration, minutes);
	}
}

class Airport {
	List<Flight> flights;
	String name;
	String country;
	String code;
	int passengers;
	
	public Airport(String name, String country, String code, int passengers) {
		this.name = name;
		this.country = country;
		this.code = code;
		this.passengers = passengers;
		flights = new ArrayList<>();
	}
	
	public void addFlight(Flight f) {
		flights.add(f);
	}
}

class Airports {
	Map<String, Airport> airports;
	
	public Airports() {
		airports = new HashMap<>();
	}
	
	public void addAirport(String name, String country, String code, int passengers) {
		Airport a = new Airport(name, country, code, passengers);
		airports.put(code, a);
	}
	
	public void addFlights(String from, String to, int time, int duration) {
		airports.get(from).addFlight(new Flight(from, to, time, duration));
	}
	
	public void showFlightsFromAirport(String code) {
		Airport a = airports.get(code);
		Comparator<Flight> flightComparator = Comparator.comparing(Flight::getTo).thenComparingInt(Flight::getTotal);
		
		List<Flight> var = a.flights;
		var.sort(flightComparator);
		
		System.out.printf((String.format("%s (%s)\n", a.name, a.code)));
		System.out.println(a.country);
		System.out.println(a.passengers);
		
		for (int i = 0; i < var.size(); i++) {
			System.out.printf("%d. %s\n", i + 1, var.get(i));
		}
	}
	
	public void showDirectFlightsFromTo(String from, String to) {
		List<Flight> var = airports.values().stream()
				.flatMap(i -> i.flights.stream())
				.filter(i -> i.getFrom().equals(from) && i.getTo().equals(to)).collect(Collectors.toList());
		if (var.isEmpty()) {
			System.out.printf("No flights from %s to %s\n", from, to);
		} else {
			var.forEach(System.out::println);
		}
	}
	
	public void showDirectFlightsTo(String to) {
		airports.values().stream()
				.flatMap(i -> i.flights.stream())
				.filter(i -> i.getTo().equals(to)).sorted(Comparator.comparingInt(Flight::getTime).thenComparing(Flight::getFrom))
				.forEach(System.out::println);
	}
}

public class AirportsTest {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Airports airports = new Airports();
		int n = scanner.nextInt();
		scanner.nextLine();
		String[] codes = new String[n];
		for (int i = 0; i < n; ++ i) {
			String al = scanner.nextLine();
			String[] parts = al.split(";");
			airports.addAirport(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]));
			codes[i] = parts[2];
		}
		int nn = scanner.nextInt();
		scanner.nextLine();
		for (int i = 0; i < nn; ++ i) {
			String fl = scanner.nextLine();
			String[] parts = fl.split(";");
			airports.addFlights(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
		}
		int f = scanner.nextInt();
		int t = scanner.nextInt();
		String from = codes[f];
		String to = codes[t];
		System.out.printf("===== FLIGHTS FROM %S =====\n", from);
		airports.showFlightsFromAirport(from);
		System.out.printf("===== DIRECT FLIGHTS FROM %S TO %S =====\n", from, to);
		airports.showDirectFlightsFromTo(from, to);
		t += 5;
		t = t % n;
		to = codes[t];
		System.out.printf("===== DIRECT FLIGHTS TO %S =====\n", to);
		airports.showDirectFlightsTo(to);
	}
}
