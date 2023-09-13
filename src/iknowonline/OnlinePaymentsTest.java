package iknowonline;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

class Item implements Comparable<Item>{
    private String name;
    private int price;
    
    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPrice() {
        return price;
    }
    
    @Override
    public String toString() {
        return name + " " + price + "\n";
    }
    
    @Override
    public int compareTo(Item o) {
        int res = Integer.compare(this.price, o.price);
        if(res == 0){
            res = this.getName().compareTo(o.getName());
        }
        return res;
    }
}

class Student {
    private String id;
    private List<Item> items;
    
    public Student(String id) {
        this.id = id;
        items = new ArrayList<>();
    }
    
    public void addItem(Item item){
        items.add(item);
    }
    
    private int calculateNet(){
        return items.stream().mapToInt(Item::getPrice).sum();
    }
    
    private int calculateFee(){
        int fee = (int) Math.round(calculateNet() * (1.14 / 100));
        
        if(fee > 300) fee = 300;
        else if(fee < 3) fee = 3;
        
        return fee;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Student: %s Net: %d Fee: %d Total: %d\n",
                id,
                calculateNet(),
                calculateFee(),
                calculateNet()+calculateFee()));
        
        sb.append("Items:\n");
        
        items.sort(Comparator.comparing(Item::getPrice).thenComparing(Item::getName).reversed());
        
        for(int i=0; i<items.size(); i++){
            sb.append(String.format("%d. %s", i+1, items.get(i).toString()));
        }
        
        return sb.toString();
    }
}

class OnlinePayments {
    Map<String, Student> students;
    
    public OnlinePayments() {
        students = new TreeMap<>();
    }
    
    public void readItems(InputStream is){
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        
        br.lines().forEach(i -> {
            String[] parts = i.split(";");
            students.putIfAbsent(parts[0], new Student(parts[0]));
            students.get(parts[0]).addItem(new Item(parts[1], Integer.parseInt(parts[2])));
        });
    }
    
    public void printStudentReport(String id, OutputStream os)  {
        PrintWriter pw = new PrintWriter(os);
        Student s = students.get(id);
        if(s == null){
            pw.println(String.format("Student %s not found!", id));
        }else{
            pw.print(students.get(id));
        }
        pw.flush();
    }
}

public class OnlinePaymentsTest {
    public static void main(String[] args) {
        OnlinePayments onlinePayments = new OnlinePayments();
        
        onlinePayments.readItems(System.in);
        
        IntStream.range(151020, 151025).mapToObj(String::valueOf)
                .forEach(id -> onlinePayments.printStudentReport(id, System.out));
    }
}