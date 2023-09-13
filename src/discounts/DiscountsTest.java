package discounts;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

class Item implements Comparable<Item>{
	private int price;
	private int discountPrice;
	
	public Item(int discountPrice, int price) {
		this.price = price;
		this.discountPrice = discountPrice;
	}
	
	public static Item createItem(String line) {
		String[] parts = line.split(":");
		return new Item(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
	}
	
	public int getPrice() {
		return price;
	}
	
	public int getDiscountPrice() {
		return discountPrice;
	}
	
	public int getTotalDiscount() {
		return price - discountPrice;
	}
	
	public int getDiscountPercentage() {
		return ((price - discountPrice) * 100) / price;
	}
	
	@Override
	public String toString() {
		return String.format("%2d%% %d/%d", getDiscountPercentage(), getDiscountPrice(), getPrice());
	}
	
	@Override
	public int compareTo(Item o) {
		int res = Integer.compare(this.getDiscountPercentage(), o.getDiscountPercentage());
		if(res == 0){
			res = Integer.compare(this.getTotalDiscount(), o.getTotalDiscount());
		}
		return res;
	}
}

class Store {
	private String name;
	private List<Item> items;
	
	public Store(String name, List<Item> items) {
		this.name = name;
		this.items = items;
	}
	
	public static Store createStore(String line) {
		String[] parts = line.split("\\s+");
		String name = parts[0];
		List<Item> var = Arrays.stream(parts).skip(1).map(Item::createItem).collect(Collectors.toList());
		return new Store(name, var);
	}
	
	public String getName() {
		return name;
	}
	
	public int getTotalDiscountSum() {
		return items.stream().mapToInt(Item::getTotalDiscount).sum();
	}
	
	public double getAverageDiscount() {
		return (double) items.stream().mapToInt(Item::getDiscountPercentage).sum() / items.size(); // da se isproba so .average().orElse(0)
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(name).append("\n");
		sb.append(String.format("Average discount: %.1f%%\n", getAverageDiscount()));
		sb.append(String.format("Total discount: %d\n", getTotalDiscountSum()));
		
		items.sort(Comparator.reverseOrder());
		
		for (int i = 0; i < items.size() - 1; i++) {
			sb.append(items.get(i)).append("\n");
		}
		sb.append(items.get(items.size() - 1));
		
		return sb.toString();
	}
}

class Discounts {
	private List<Store> stores;
	
	public int readStores(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		stores = br.lines().takeWhile(i -> ! i.equals("done"))
				.map(Store::createStore)
				.collect(Collectors.toList());
		
		return stores.size();
	}
	
	public List<Store> byAverageDiscount() {
		return stores.stream()
				.sorted(Comparator
						.comparingDouble(Store::getAverageDiscount).reversed()
						.thenComparing(Store::getName))
				.limit(3)
				.collect(Collectors.toList());
	}
	
	public List<Store> byTotalDiscount() {
		return stores.stream()
				.sorted(Comparator
						.comparingInt(Store::getTotalDiscountSum)
						.thenComparing(Store::getName))
				.limit(3)
				.collect(Collectors.toList());
	}
}

public class DiscountsTest {
	public static void main(String[] args) {
		Discounts discounts = new Discounts();
		int stores = discounts.readStores(System.in);
		System.out.println("Stores read: " + stores);
		System.out.println("=== By average discount ===");
		discounts.byAverageDiscount().forEach(System.out::println);
		System.out.println("=== By total discount ===");
		discounts.byTotalDiscount().forEach(System.out::println);
	}
}