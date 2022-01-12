package SplayList;

import static java.lang.Math.random;

public class SplayList {
    private Node head;
    private Node tail;
    /**
     * The base Level of the Splay List . This is maintained for the re-balancing/expansion of the Splay List
     * This is basically the lowest level visited by any of the nodes in the Splay List.
     */
    private int zeroLevel = GlobalVariables.MAX_LEVEL-1;
    /**
     * This basically tells us the total number of hits of all the elements of the Splay List
     */
    private long accessCounter = 0;
    /**
     * The total number of hits of the logically deleted elements
     */
    private long deleted_hits = 0;
    /**
     * Number of elements currently logically present in the Splay List
     */
    private int size = 0;

    /**
     * Probability of updating/balancing the splayList
     */
    private static final double p = 0.5;

    public int getZeroLevel(){
        return zeroLevel;
    }

    public Node getHead(){
        return head;
    }

    public int getSize(){
        return size;
    }

    /**
     * Update the zeroLevel and perform required actions
     * @param curr node to be updated
     */
    private void updateZeroLevel(Node curr){
        if(curr == null) return;
        // This function should be used only if we have lock on curr
        int currZeroLevel = zeroLevel;
        if(curr.zeroLevel > currZeroLevel){
            curr.hits.set(curr.zeroLevel-1,0);
            curr.next.set(curr.zeroLevel-1,curr.next.get(curr.zeroLevel));
            curr.zeroLevel--;
        }
    }

    /**
     * update the zeroLevel of the provided node up to a given level
     * @param curr node to be updated
     * @param level level up to which update is to be done
     */
    private void updateUpToLevel(Node curr, int level){
        if(curr == null) return;
        // Lock the node and then proceed
        while(curr.zeroLevel > level){
            updateZeroLevel(curr);
        }
        // unlock the node
    }

    /**
     * Create a new node with the given parameters
     * @param value Value with which the node is initialized
     * @param level TopLevel of the node
     * @return A new Node initialized with the provided parameters.
     */
    public Node createNode(int value,int level){
        return new Node(value,level);
    }

    public SplayList(int maxKey,int minKey){
        head = createNode(minKey,zeroLevel);
        tail = createNode(maxKey,zeroLevel);
        head.topLevel = GlobalVariables.MAX_LEVEL;
        tail.topLevel = GlobalVariables.MAX_LEVEL;
        for(int i=zeroLevel; i<= GlobalVariables.MAX_LEVEL ; ++i){
            head.next.set(i,tail);
            tail.next.set(i,null);
        }
    }

    /**
     * Return the hits of the provided node at level h
     * @param node node for which hits are to be found
     * @param h level at which hits are to be calculated or to be found
     * @return hits of node at level h
     */
    private int getHits(Node node,int h){
        if(node.zeroLevel > h) return node.selfHits;
        return node.selfHits + node.hits.get(h);
    }

    /**
     * find the provided value in the SplayList and returns a boolean Value accordingly
     * @param value Value to be found
     * @param np node pairs consisting of predecessor and successor nodes which will be updated in this function
     * @return true if the SplayList has the provided value or false otherwise
     */
    private boolean find(int value, NodePairs np){
        Node suc = np.b;
        Node pre = head;
        for(int level = GlobalVariables.MAX_LEVEL-1; level >= zeroLevel ;level--) {
            updateUpToLevel(pre, level);

            suc = pre.next.get(level);

            updateUpToLevel(suc, level);

            while (suc != null && value > suc.value) {
                pre = suc;
                suc = pre.next.get(level);
                updateUpToLevel(suc, level);
            }
            if (suc != null && value == suc.value) {
                np.a = pre;
                np.b = suc;
                return true;
            }
        }
        np.a = pre;
        np.b = suc;
        return false;
    }

    /**
     * Performs the re-balancing of the SplayList after an operation
     * @param val value associated with the operation
     */
    private void update(int val){
        accessCounter++;

        int head_hits = head.hits.get(GlobalVariables.MAX_LEVEL);
        head.hits.set(GlobalVariables.MAX_LEVEL,head_hits+1);

        Node pre = head;

        for(int h=GlobalVariables.MAX_LEVEL-1 ; h >= zeroLevel ; h--){
            updateUpToLevel(pre,h);

            Node predpred = pre;
            Node curr = pre.next.get(h);

            updateUpToLevel(curr,h);

            if(curr.value > val){
                pre.hits.set( h , pre.hits.get(h) + 1);
                continue;
            }

            boolean ok = false;
            while(curr.value <= val){
                updateUpToLevel(curr,h);

                if(curr.next.get(h).value > val ){
                    if(curr.value == val){
                        curr.selfHits++;
                        ok = true;
                    }
                    else{
                        curr.hits.set(h,curr.hits.get(h)+1);
                    }
                }
                // Check for ascent and descent conditions and take the required action
                // Ascent Condition
                if(h+1 < GlobalVariables.MAX_LEVEL && (h< predpred.topLevel) && predpred.next.get(h) == curr
                        && (predpred.hits.get(h+1) - predpred.hits.get(h)) > (accessCounter/(1 << (GlobalVariables.MAX_LEVEL - 2 - h)))){
                    int current_height = curr.topLevel;
                    while(current_height + 1 <GlobalVariables.MAX_LEVEL && (current_height< predpred.topLevel) && predpred.next.get(h) == curr
                            && (predpred.hits.get(current_height+1) - predpred.hits.get(current_height)) > (accessCounter/(1 << (GlobalVariables.MAX_LEVEL - 2 - current_height)))){
                        curr.topLevel++;
                        current_height++;
                        curr.hits.set(current_height,predpred.hits.get(current_height) - predpred.hits.get(current_height-1) - curr.selfHits);
                        curr.next.set(current_height,predpred.next.get(current_height));
                        predpred.next.set(current_height,curr);
                        predpred.hits.set(current_height,predpred.hits.get(current_height-1));
                    }
                    predpred = curr;
                    pre = curr;
                    curr = curr.next.get(h);
                    continue;
                }
                // Descent Condition
                else if(curr.topLevel == h && curr.next.get(h).value <= val
                        && (getHits(curr,h) + getHits(pre,h) <= (accessCounter/(1L << (GlobalVariables.MAX_LEVEL-1-h))))){
                    int currZeroLevel = zeroLevel;
                    if (h == currZeroLevel) {
                        zeroLevel = currZeroLevel - 1;
                    }
                    if(curr.zeroLevel > h-1){
                        updateZeroLevel(curr);
                    }
                    if(pre.zeroLevel > h-1){
                        updateZeroLevel(pre);
                    }
                    pre.hits.set(h,pre.hits.get(h) + getHits(curr,h));
                    curr.hits.set(h,0);
                    pre.next.set(h,curr.next.get(h));
                    curr.next.set(h,null);
                    curr.topLevel--;
                    curr = pre.next.get(h);
                    continue;
                }
                pre = curr;
                curr = pre.next.get(h);
            }
            if(ok){
                return;
            }
        }
    }

    /**
     * Provides the node/null depending on whether the element is contained by the splayList or not
     * @param val value to be found
     * @return Node if found else null
     */
    public boolean contains(int val){

        NodePairs np = new NodePairs(null,null);
        if(find(val,np)){
            if(random() < p){
                update(val);
            }
            return !np.b.deleted ;
        }
        return false;
    }

    /**
     * Insert an element in the splayList if not already inserted
     * @param val value to be inserted
     */
    public void insertIfAbsent(int val){
        while(true){
            NodePairs np = new NodePairs(null,null);
            if(find(val,np)){
                // return as the value is already inserted
                return;
            }
            Node pre = np.a;
            Node suc = np.b;
            int currZeroLevel = pre.zeroLevel;
            Node newNode = createNode(val,currZeroLevel);
            newNode.next.set(currZeroLevel,suc);

            size++;

            if(pre.next.get(currZeroLevel) == suc){
                pre.next.set(currZeroLevel,newNode);
                update(val);
                return;
            }
        }
    }

    /**
     * printing will be done in ascending order in the format of
     *  ( value : SelfHits : hits[zeroLevel] , hits[zeroLevel+1] .... , hits[topLevel] )
     */
    public void printList(){
        Node node = head;
        System.out.println("ZeroLevel of the List is level : "+zeroLevel+" and topLevel is level: "+ head.topLevel);
        while(node != null){
//            updateUpToLevel(node,zeroLevel);
            System.out.print(node.value + " : " + node.selfHits + " : ");
            for(int i = zeroLevel; i <= node.topLevel ; ++i){
                System.out.print(node.hits.get(i)+ " , ");
            }
            System.out.println();
            node = node.next.get(node.zeroLevel);
        }
    }

    /**
     * Removes the element of the provided value if present from the Splay List
     * @param val value ot the element to be removed
     */
    public void removeIfPresent(int val){
        NodePairs np = new NodePairs(null,null);
        if(find(val,np)){
            if(np.b.deleted) return;
            if(random() < p){
                update(val);
            }

            np.b.deleted = true;
            size--;
            deleted_hits += np.b.selfHits;

            // ToDo : if total_deleted_hits >= (total_hits/2)
            // Move all the logically present elements to a new Splay List
            // ie, rebuild this splay List
        }
    }
}
