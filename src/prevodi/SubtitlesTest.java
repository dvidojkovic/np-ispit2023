package prevodi;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

class Element {
	private int id;
	private String timeStart;
	private String timeEnd;
	private List<String> description;
	
	public Element(int id, String timeStart, String timeEnd, List<String> description) {
		this.id = id;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
		this.description = description;
	}
	
	public int timeInMs(String time, int shift) {
		String[] parts = time.split(":|,");
		int hour = Integer.parseInt(parts[0]) * 3600000; // hour -> minute = hour * 60
		int minute = Integer.parseInt(parts[1]) * 60000; // minute to second = minute * 60
		int second = Integer.parseInt(parts[2]) * 1000; // second to milisecond = second * 1000
		int milisecond = Integer.parseInt(parts[3]);
		
		return hour + minute + second + milisecond + shift;
	}
	
	public void shift(int shift) {
		
		int totalStartTimeIn = timeInMs(timeStart, shift);
		
		int shiftedHour = totalStartTimeIn / 3600000;
		totalStartTimeIn %= 3600000;
		int shiftedMinute = totalStartTimeIn / 60000; // 1
		totalStartTimeIn %= 60000;
		int shiftedSecond = totalStartTimeIn / 1000;
		int shiftedMilisecond = totalStartTimeIn % 1000;
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%02d:%02d:%02d,%03d", shiftedHour, shiftedMinute, shiftedSecond, shiftedMilisecond));
		
		this.timeStart = sb.toString();
		
		int totalEndTime = timeInMs(timeEnd, shift);
		sb = new StringBuilder();
		
		 shiftedHour = totalEndTime / 3600000;
		totalEndTime %= 3600000;
		 shiftedMinute = totalEndTime / 60000; // 1
		totalEndTime %= 60000;
		 shiftedSecond = totalEndTime / 1000;
		 shiftedMilisecond = totalEndTime % 1000;
		
		sb = new StringBuilder();
		sb.append(String.format("%02d:%02d:%02d,%03d", shiftedHour, shiftedMinute, shiftedSecond, shiftedMilisecond));
		this.timeEnd = sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(id).append("\n");
		sb.append(timeStart).append(" --> ").append(timeEnd).append("\n");
		description.forEach(i -> sb.append(i).append("\n"));
		
		return sb.toString();
	}
}

class Subtitles {
	private List<Element> subtitles;
	
	public Subtitles() {
		subtitles = new ArrayList<>();
	}
	
	public int loadSubtitles(InputStream in) throws IOException {
		Scanner sc = new Scanner(in);
		StringBuilder sb = new StringBuilder();
		String line;
		
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (line.equals("done")) break;
			int id = Integer.parseInt(line);
			
			line = sc.nextLine();
			String[] time = line.split(" --> ");
			String timeStart = time[0];
			String timeEnd = time[1];
			
			List<String> description = new ArrayList<>();
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				if (line.equals("")) break;
				description.add(line);
			}
			subtitles.add(new Element(id, timeStart, timeEnd, description));
		}
		
		return subtitles.size();
	}
	
	public void print() {
		subtitles.forEach(System.out::println);
	}
	
	public void shift(int shift) {
		subtitles.forEach(i -> i.shift(shift));
	}
}

public class SubtitlesTest {
	public static void main(String[] args) {
		Subtitles subtitles = new Subtitles();
		int n = 0;
		try {
			n = subtitles.loadSubtitles(System.in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("+++++ ORIGINIAL SUBTITLES +++++");
		subtitles.print();
		int shift = n * 37;
		shift = (shift % 2 == 1) ? - shift : shift;
		System.out.println(String.format("SHIFT FOR %d ms", shift));
		subtitles.shift(shift);
		System.out.println("+++++ SHIFTED SUBTITLES +++++");
		subtitles.print();
	}
}