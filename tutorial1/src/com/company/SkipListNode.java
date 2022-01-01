package com.company;


import java.util.ArrayList;
import java.util.List;

public class SkipListNode<E> {
    private E value;
    public List<SkipListNode<E> > nextNodes;

    public E getValue() {
        return value;
    }

    public SkipListNode(E value) {
        this.value = value;
        nextNodes = new ArrayList<SkipListNode<E> >();
    }
    public SkipListNode(E value, int n) {
        this.value = value;
        nextNodes = new ArrayList<SkipListNode<E> >();
        while(n--!=0){
            nextNodes.add(null);
        }
    }

    public int level() {
        return nextNodes.size()-1;
    }

    public String toString() {
        return "SLN: " + value;
    }
}