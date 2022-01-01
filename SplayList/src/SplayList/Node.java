package SplayList;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public int value;
    public int zeroLevel;
    public int topLevel;
    public int selfHits;
    public List<Node> next;
    public List<Integer> hits;
    public boolean deleted;

    public Node(int value,int h){
        this.value = value;
        zeroLevel = h;
        topLevel = h;
        selfHits = 0;
        deleted = false;
        next = new ArrayList<Node>();
        hits = new ArrayList<Integer>();
        for(int i=0 ; i<= GlobalVariables.MAX_LEVEL ; ++i ){
            next.add(null);
            hits.add(0);
        }
    }
}
