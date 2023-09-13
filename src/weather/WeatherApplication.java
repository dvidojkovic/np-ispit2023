package weather;

import java.util.*;
import java.util.Scanner;

interface Subscriber {
	void update(float temperature, float humidity, float pressure);
}

class WeatherDispatcher {
	HashSet<Subscriber> subscribers;
	
	public WeatherDispatcher() {
		subscribers = new HashSet<>();
	}
	
	public void setMeasurements(float temperature, float humidity, float pressure) {
		subscribers.forEach(s -> s.update(temperature, humidity, pressure));
	}
	
	public void remove(Subscriber sub) {
		subscribers.remove(sub);
	}
	
	public void register(Subscriber sub) {
		subscribers.add(sub);
	}
}

class CurrentConditionsDisplay implements Subscriber {
	
	public CurrentConditionsDisplay(WeatherDispatcher weatherDispatcher) {
		weatherDispatcher.register(this);
	}
	
	@Override
	public void update(float temperature, float humidity, float pressure) {
		System.out.printf("Temperature: %.1fF\n" +
				"Humidity: %.1f%%\n", temperature, humidity);
	}
}

class ForecastDisplay implements Subscriber {
	
	float prevPressure = (float) 0.0;
	
	public ForecastDisplay(WeatherDispatcher weatherDispatcher) {
		weatherDispatcher.register(this);
	}
	
	@Override
	public void update(float temperature, float humidity, float pressure) {
		float res = Float.compare(pressure, prevPressure);
		if (res == 1) {
			System.out.println("Forecast: Improving");
		} else if (res == 0) {
			System.out.println("Forecast: Same");
		} else {
			System.out.println("Forecast: Cooler");
		}
		System.out.printf("\n");
		prevPressure = pressure;
	}
}

public class WeatherApplication {
	
	public static void main(String[] args) {
		WeatherDispatcher weatherDispatcher = new WeatherDispatcher();
		
		CurrentConditionsDisplay currentConditions = new CurrentConditionsDisplay(weatherDispatcher);
		ForecastDisplay forecastDisplay = new ForecastDisplay(weatherDispatcher);
		
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] parts = line.split("\\s+");
			weatherDispatcher.setMeasurements(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
			if (parts.length > 3) {
				int operation = Integer.parseInt(parts[3]);
				if (operation == 1) {
					weatherDispatcher.remove(forecastDisplay);
				}
				if (operation == 2) {
					weatherDispatcher.remove(currentConditions);
				}
				if (operation == 3) {
					weatherDispatcher.register(forecastDisplay);
				}
				if (operation == 4) {
					weatherDispatcher.register(currentConditions);
				}
				
			}
		}
	}
}
