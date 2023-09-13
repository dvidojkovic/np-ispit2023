package onlineshop;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum COMPARATOR_TYPE {
	NEWEST_FIRST,
	OLDEST_FIRST,
	LOWEST_PRICE_FIRST,
	HIGHEST_PRICE_FIRST,
	MOST_SOLD_FIRST,
	LEAST_SOLD_FIRST
}

class ProductNotFoundException extends Exception {
	ProductNotFoundException(String message) {
		super(message);
	}
}


class Product {
	String category;
	String id;
	String name;
	LocalDateTime createdAt;
	double price;
	int quantitySold;
	
	public Product(String category, String id, String name, LocalDateTime createdAt, double price) {
		this.category = category;
		this.id = id;
		this.name = name;
		this.createdAt = createdAt;
		this.price = price;
		quantitySold = 0;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public double getPrice() {
		return price;
	}
	
	public int getQuantitySold() {
		return quantitySold;
	}
	
	public void increaseQuantity(int value) {
		quantitySold += value;
	}
	
	@Override
	public String toString() {
		return "Product{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", createdAt=" + createdAt +
				", price=" + price +
				", quantitySold=" + quantitySold +
				'}';
	}
	
	public String getCategory() {
		return category;
	}
}


class OnlineShop {
	Map<String, Product> products;
	double totalRevenue;
	
	public OnlineShop() {
		products = new HashMap<>();
		totalRevenue = 0;
	}
	
	void addProduct(String category, String id, String name, LocalDateTime createdAt, double price) {
		products.put(id, new Product(category, id, name, createdAt, price));
	}
	
	public void increaseRevenue(double value) {
		totalRevenue += value;
	}
	
	double buyProduct(String id, int quantity) throws ProductNotFoundException {
		Product product = products.get(id);
		if (product == null) {
			throw new ProductNotFoundException("Product with id 1718a7753 does not exist in the online shop!");
		}
		
		product.increaseQuantity(quantity);
		double total = product.price * quantity;
		increaseRevenue(total);
		
		return total;
	}
	
	List<List<Product>> listProducts(String category, COMPARATOR_TYPE comparatorType, int pageSize) {
		List<List<Product>> result = new ArrayList<>();
		
		Comparator<Product> timeComparator = Comparator.comparing(Product::getCreatedAt);
		Comparator<Product> quantitySoldComparator = Comparator.comparing(Product::getQuantitySold);
		Comparator<Product> priceComparator = Comparator.comparing(Product::getPrice);
		
		Comparator<Product> productComparator;
		
		switch (comparatorType) {
			case NEWEST_FIRST:
				productComparator = timeComparator.reversed();
				break;
			case OLDEST_FIRST:
				productComparator = timeComparator;
				break;
			case LEAST_SOLD_FIRST:
				productComparator = quantitySoldComparator;
				break;
			case LOWEST_PRICE_FIRST:
				productComparator = priceComparator;
				break;
			case HIGHEST_PRICE_FIRST:
				productComparator = priceComparator.reversed();
				break;
			default:
				productComparator = quantitySoldComparator.reversed();
		}
		
		List<Product> page = new ArrayList<>();
		if (category == null) {
			page = products.values().stream().sorted(productComparator).collect(Collectors.toList());
		} else {
			page = products.values().stream().filter(i -> i.getCategory().equals(category)).sorted(productComparator).collect(Collectors.toList());
		}
		
		int counter = 0;
		for (int i = 0; counter < page.size(); i++) { //
			result.add(i, new ArrayList<>());
			for (int j = 0; j < pageSize && counter < page.size(); j++, counter++) { //
				result.get(i).add(page.get(counter));
			}
			if (counter + i == page.size())
				break;
		}
		
		return result;
	}
	
}

public class OnlineShopTest {
	
	public static void main(String[] args) {
		OnlineShop onlineShop = new OnlineShop();
		double totalAmount = 0.0;
		Scanner sc = new Scanner(System.in);
		String line;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			String[] parts = line.split("\\s+");
			if (parts[0].equalsIgnoreCase("addproduct")) {
				String category = parts[1];
				String id = parts[2];
				String name = parts[3];
				LocalDateTime createdAt = LocalDateTime.parse(parts[4]);
				double price = Double.parseDouble(parts[5]);
				onlineShop.addProduct(category, id, name, createdAt, price);
			} else if (parts[0].equalsIgnoreCase("buyproduct")) {
				String id = parts[1];
				int quantity = Integer.parseInt(parts[2]);
				try {
					totalAmount += onlineShop.buyProduct(id, quantity);
				} catch (ProductNotFoundException e) {
					System.out.println(e.getMessage());
				}
			} else {
				String category = parts[1];
				if (category.equalsIgnoreCase("null"))
					category = null;
				String comparatorString = parts[2];
				int pageSize = Integer.parseInt(parts[3]);
				COMPARATOR_TYPE comparatorType = COMPARATOR_TYPE.valueOf(comparatorString);
				printPages(onlineShop.listProducts(category, comparatorType, pageSize));
			}
		}
		System.out.println("Total revenue of the online shop is: " + totalAmount);
		
	}
	
	private static void printPages(List<List<Product>> listProducts) {
		for (int i = 0; i < listProducts.size(); i++) {
			System.out.println("PAGE " + (i + 1));
			listProducts.get(i).forEach(System.out::println);
		}
	}
}

