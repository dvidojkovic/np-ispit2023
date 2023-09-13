package imenik;

import java.util.*;

class DuplicateNumberException extends Exception {
	public DuplicateNumberException(String message) {
		super(String.format("Duplicate number: %s.", message));
	}
}

class Contact implements Comparable<Contact>{
	private String phone;
	private String name;
	
	public Contact(String name, String phone) {
		this.phone = phone;
		this.name = name;
	}
	
	@Override
	public int compareTo(Contact o) {
		int res = this.name.compareTo(o.name);
		if(res == 0){
			res = this.phone.compareTo(o.phone);
		}
		return res;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s", name, phone);
	}
}

class PhoneBook {
	Set<String> allPhoneNumbers;
	Map<String, Set<Contact>> numbersBySubstring;
	Map<String, Set<Contact>> numbersByName;
	
	public PhoneBook() {
		allPhoneNumbers = new HashSet<>();
		numbersBySubstring = new HashMap<>();
		numbersByName = new HashMap<>();
	}
	
	public void addContact(String name, String number) throws DuplicateNumberException {
		if (allPhoneNumbers.contains(number)) {
			throw new DuplicateNumberException(number);
		} else {
			allPhoneNumbers.add(number);
			Contact c = new Contact(name, number);
			List<String> substrings = getSubstrings(number);
			for(String sub : substrings){
				numbersBySubstring.putIfAbsent(sub, new TreeSet<>());
				numbersBySubstring.get(sub).add(c);
			}
			numbersByName.putIfAbsent(name, new TreeSet<>());
			numbersByName.get(name).add(c);
		}
	}
	
	private List<String> getSubstrings(String phone){
		List<String> substrings = new ArrayList<>();
		for(int i=3; i<=phone.length(); i++){
			for(int j=0; j<=phone.length()-i; j++){
				substrings.add(phone.substring(j, j+i));
			}
		}
		return substrings;
	}
	
	public void contactsByNumber(String phone) {
		Set<Contact> var = numbersBySubstring.get(phone);
		if(var == null){
			System.out.println("NOT FOUND");
			return;
		}
		numbersBySubstring.get(phone).forEach(System.out::println);
	}
	
	public void contactsByName(String name) {
		Set<Contact> var = numbersByName.get(name);
		if(var == null){
			System.out.println("NOT FOUND");
			return;
		}
		var.forEach(System.out::println);
	}
}

public class PhoneBookTest {
	
	public static void main(String[] args) {
		PhoneBook phoneBook = new PhoneBook();
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		scanner.nextLine();
		for (int i = 0; i < n; ++i) {
			String line = scanner.nextLine();
			String[] parts = line.split(":");
			try {
				phoneBook.addContact(parts[0], parts[1]);
			} catch (DuplicateNumberException e) {
				System.out.println(e.getMessage());
			}
		}
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			System.out.println(line);
			String[] parts = line.split(":");
			if (parts[0].equals("NUM")) {
				phoneBook.contactsByNumber(parts[1]);
			} else {
				phoneBook.contactsByName(parts[1]);
			}
		}
	}
	
}

// Вашиот код овде

