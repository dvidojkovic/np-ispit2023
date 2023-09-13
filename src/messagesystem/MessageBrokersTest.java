package messagesystem;

import java.time.LocalDateTime;
import java.util.*;

class PartitionDoesNotExistException extends Exception {
	public PartitionDoesNotExistException(String message1, int message2) {
		super(String.format("The topic %s does not have a partition with number %d", message1, message2));
	}
}

class Message implements Comparable<Message> {
	LocalDateTime timestamp;
	String message;
	Integer partition;
	String key;
	
	public Message(LocalDateTime timestamp, String message, Integer partition, String key) {
		this.timestamp = timestamp;
		this.message = message;
		this.partition = partition;
		this.key = key;
	}
	
	public Message(LocalDateTime timestamp, String message, String key) {
		this.timestamp = timestamp;
		this.message = message;
		this.key = key;
	}
	
	@Override
	public String toString() {
		return "Message{" +
				"timestamp=" + timestamp +
				", message='" + message + '\'' +
				'}' + "\n";
	}
	
	@Override
	public int compareTo(Message o) {
		return timestamp.compareTo(o.timestamp);
	}
}

class Partition {
	int number;
	TreeSet<Message> messages;
	
	public Partition(int number) {
		this.number = number;
		messages = new TreeSet<>();
	}
	
	public void addMessage(Message m) {
		if(m.timestamp.isBefore(MessageBroker.MINIMUM_DATE)){
			return;
		}
		if(messages.size() == MessageBroker.CAPACITY_PER_TOPIC){
			messages.remove(messages.first());
			messages.add(m);
		}
		messages.add(m);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("%2s : Count of messages:%6d\n", number, messages.size()));
		sb.append("Messages:\n");
		messages.forEach(sb::append);
		
		return sb.toString();
	}
}

class Topic {
	String topic;
	Map<Integer, Partition> partitions;
	int partitionsCount;
	
	public Topic(String topic, int partitionsCount) {
		this.topic = topic;
		this.partitionsCount = partitionsCount;
		partitions = new TreeMap<>();
		
		for (int i = 1; i <= partitionsCount; i++) {
			partitions.put(i, new Partition(i));
		}
	}
	
	public void addMessage(Message message) throws PartitionDoesNotExistException {
		Integer partition = message.partition;
		if (partition == null) {
			partition = PartitionAssigner.assignPartition(message, partitionsCount);
		}
		if (!partitions.containsKey(partition)) {
			throw new PartitionDoesNotExistException(topic, partition);
		}
		partitions.get(partition).addMessage(message);
	}
	
	public void changeNumberOfPartitions(int newPartitionsNumber) throws UnsupportedOperationException {
		if (newPartitionsNumber < partitionsCount) {
			throw new UnsupportedOperationException();
		}
		for (int i = partitionsCount + 1; i <= newPartitionsNumber; i++) {
			partitions.put(i, new Partition(i));
		}
		partitionsCount = newPartitionsNumber;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Topic: %10s Partitions: %5d\n", topic, partitions.size()));
		partitions.values().forEach(sb::append);
		return sb.toString();
	}
}

class MessageBroker {
	Map<String, Topic> topics;
	static LocalDateTime MINIMUM_DATE;
	static Integer CAPACITY_PER_TOPIC;
	
	public MessageBroker(LocalDateTime minimumDate, Integer capacityPerTopic) {
		MINIMUM_DATE = minimumDate;
		CAPACITY_PER_TOPIC = capacityPerTopic;
		topics = new HashMap<>();
	}
	
	public void addTopic (String topic, int partitionsCount){
		topics.putIfAbsent(topic, new Topic(topic, partitionsCount));
	}
	
	public void addMessage (String topic, Message message) throws PartitionDoesNotExistException {
		topics.get(topic).addMessage(message);
	}
	public void changeTopicSettings (String topic, int partitionsCount){
		topics.get(topic).changeNumberOfPartitions(partitionsCount);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Broker with %2d topics:\n", topics.size()));
		topics.values().forEach(sb::append);
		
		return sb.toString();
	}
}

class PartitionAssigner {
	public static Integer assignPartition(Message message, int partitionsCount) {
		return (Math.abs(message.key.hashCode()) % partitionsCount) + 1;
	}
}

public class MessageBrokersTest {
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		String date = sc.nextLine();
		LocalDateTime localDateTime = LocalDateTime.parse(date);
		Integer partitionsLimit = Integer.parseInt(sc.nextLine()); // 10
		MessageBroker broker = new MessageBroker(localDateTime, partitionsLimit);
		int topicsCount = Integer.parseInt(sc.nextLine());
		
		//Adding topics
		for (int i = 0; i < topicsCount; i++) { // 5
			String line = sc.nextLine();
			String[] parts = line.split(";");
			String topicName = parts[0];
			int partitionsCount = Integer.parseInt(parts[1]);
			broker.addTopic(topicName, partitionsCount);
		}
		
		//Reading messages
		int messagesCount = Integer.parseInt(sc.nextLine()); // 200
		
		System.out.println("===ADDING MESSAGES TO TOPICS===");
		for (int i = 0; i < messagesCount; i++) { // topic1_1;2018-05-15T19:42;Message from the system with id18313;DBDIB
			String line = sc.nextLine();
			String[] parts = line.split(";");
			String topic = parts[0];
			LocalDateTime timestamp = LocalDateTime.parse(parts[1]);
			String message = parts[2];
			if (parts.length == 4) {
				String key = parts[3];
				try {
					broker.addMessage(topic, new Message(timestamp, message, key));
				} catch (UnsupportedOperationException | PartitionDoesNotExistException e) {
					System.out.println(e.getMessage());
				}
			} else {
				Integer partition = Integer.parseInt(parts[3]);
				String key = parts[4];
				try {
					broker.addMessage(topic, new Message(timestamp, message, partition, key));
				} catch (UnsupportedOperationException | PartitionDoesNotExistException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		
		System.out.println("===BROKER STATE AFTER ADDITION OF MESSAGES===");
		System.out.println(broker);
		
		System.out.println("===CHANGE OF TOPICS CONFIGURATION===");
		//topics changes
		int changesCount = Integer.parseInt(sc.nextLine());
		for (int i = 0; i < changesCount; i++) {
			String line = sc.nextLine();
			String[] parts = line.split(";");
			String topicName = parts[0];
			Integer partitions = Integer.parseInt(parts[1]);
			try {
				broker.changeTopicSettings(topicName, partitions);
			} catch (UnsupportedOperationException e) {
				System.out.println(e.getMessage());
			}
		}
		
		System.out.println("===ADDING NEW MESSAGES TO TOPICS===");
		messagesCount = Integer.parseInt(sc.nextLine());
		for (int i = 0; i < messagesCount; i++) {
			String line = sc.nextLine();
			String[] parts = line.split(";");
			String topic = parts[0];
			LocalDateTime timestamp = LocalDateTime.parse(parts[1]);
			String message = parts[2];
			if (parts.length == 4) {
				String key = parts[3];
				try {
					broker.addMessage(topic, new Message(timestamp, message, key));
				} catch (UnsupportedOperationException | PartitionDoesNotExistException e) {
					System.out.println(e.getMessage());
				}
			} else {
				Integer partition = Integer.parseInt(parts[3]);
				String key = parts[4];
				try {
					broker.addMessage(topic, new Message(timestamp, message, partition, key));
				} catch (UnsupportedOperationException | PartitionDoesNotExistException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		
		System.out.println("===BROKER STATE AFTER CONFIGURATION CHANGE===");
		System.out.println(broker);
		
		
	}
}
