package SplayList;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        SplayList spList = new SplayList(Integer.MAX_VALUE,Integer.MIN_VALUE);
        List numbers = new ArrayList<Integer>();
        for(int i=0;i<20;++i){
            Random rn = new Random();
            int num = rn.nextInt(100)+1;
            numbers.add(num);
            spList.insertIfAbsent(num);
        }
        System.out.println("ZeroLevel of the list is " + spList.zeroLevel);
        for(int i=0;i<20;++i){
            int num = (int) numbers.get(i);
            Node a = spList.contains(num);
            if(a == null){
                System.out.println("Nod with value "+ num +" is not present");
            }
            else {
                System.out.println("Node found with value : "+num);
                System.out.println("ToplLevel : "+a.topLevel);
                System.out.println("ZeroLevel : "+a.zeroLevel);
                System.out.println("Self Hits : "+a.selfHits);
            }
        }

        spList.printList();
    }
}
