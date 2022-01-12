package SplayList;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.random;

public class Main {
    public static void main(String[] args) {
        SplayList spList = new SplayList(Integer.MAX_VALUE,Integer.MIN_VALUE);
        List<Integer> numbers = new ArrayList<>();
        for(int i=0;i<20;++i){
            Random rn = new Random();
            int num = rn.nextInt(100)+1;
            numbers.add(num);
            spList.insertIfAbsent(num);
        }
        System.out.println("Size of the List is : "+spList.getSize());
        System.out.println("ZeroLevel of the list is " + spList.getZeroLevel());
        System.out.println("=============== SplayList After Insertion ==================");
        spList.printList();
        for(int i=0;i<20;++i) {
            int num =  numbers.get(i);
            boolean a = spList.contains(num);
            if (!a) {
                System.out.println("Node with value " + num + " is not present");
            } else {
                System.out.println("Node found with value : " + num);
                if (random() < 0.7) {
                    spList.removeIfPresent(num);
                }
            }
        }
        System.out.println("=============== SplayList After Deletion ==================");
        for(int i=0;i<20;++i) {
            int num = numbers.get(i);
            boolean a = spList.contains(num);
            if (!a) {
                System.out.println("Nod with value " + num + " is not present");
            } else {
                System.out.println("Node found with value : " + num);
            }
        }
        spList.printList();
    }
}
