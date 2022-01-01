package SplayList;

import static java.lang.Math.random;

public class SplayList {
    public Node head;
    public Node tail;
    public int zeroLevel = GlobalVariables.MAX_LEVEL-1;
    public long accessCounter = 0;

    //probability of updating/balancing the splayList
    private static final double p = 0.5;

    /**
     * Update the zeroLevel and perform required actions
     * @param curr node to be updated
     */
    public void updateZeroLevel(Node curr){
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
    public void updateUpToLevel(Node curr, int level){
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
    public int getHits(Node node,int h){
        if(node.zeroLevel > h) return node.selfHits;
        return node.selfHits + node.hits.get(h);
    }

    /**
     * find the provided value in the SplayList and returns a boolean Value accordingly
     * @param value Value to be found
     * @param np node pairs consisting of predecessor and successor nodes which will be updated in this function
     * @return true if the SplayList has the provided value or false otherwise
     */
    public boolean find(int value, NodePairs np){
        Node succ = np.b;
        Node pred = head;
        for(int level = GlobalVariables.MAX_LEVEL-1; level >= zeroLevel ;level--) {
            updateUpToLevel(pred, level);

            succ = pred.next.get(level);

            updateUpToLevel(succ, level);

            while (succ != null && value > succ.value) {
                pred = succ;
                succ = pred.next.get(level);
                updateUpToLevel(succ, level);
            }
            if (succ != null && value == succ.value) {
                np.a = pred;
                np.b = succ;
                return true;
            }
        }
        np.a = pred;
        np.b = succ;
        return false;
    }

    /**
     * Performs the re-balancing of the SplayList after an operation
     * @param val value associated with the operation
     */
    public void update(int val){
        accessCounter++;

        int head_hits = head.hits.get(GlobalVariables.MAX_LEVEL);
        head.hits.set(GlobalVariables.MAX_LEVEL,head_hits+1);

        Node pred = head;

        for(int h=GlobalVariables.MAX_LEVEL-1 ; h >= zeroLevel ; h--){
            updateUpToLevel(pred,h);

            Node predpred = pred;
            Node curr = pred.next.get(h);

            updateUpToLevel(curr,h);

            if(curr.value > val){
                pred.hits.set( h , pred.hits.get(h) + 1);
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
                    int curh = curr.topLevel;
                    while(curh + 1 <GlobalVariables.MAX_LEVEL && (curh< predpred.topLevel) && predpred.next.get(h) == curr
                            && (predpred.hits.get(curh+1) - predpred.hits.get(curh)) > (accessCounter/(1 << (GlobalVariables.MAX_LEVEL - 2 - curh)))){
                        curr.topLevel++;
                        curh++;
                        curr.hits.set(curh,predpred.hits.get(curh) - predpred.hits.get(curh-1) - curr.selfHits);
                        curr.next.set(curh,predpred.next.get(curh));
                        predpred.next.set(curh,curr);
                        predpred.hits.set(curh,predpred.hits.get(curh-1));
                    }
                    predpred = curr;
                    pred = curr;
                    curr = curr.next.get(h);
                    continue;
                }
                // Descent Condition
                else if(curr.topLevel == h && curr.next.get(h).value <= val
                        && (getHits(curr,h) + getHits(pred,h) <= (accessCounter/(1 << (GlobalVariables.MAX_LEVEL-1-h))))){
                    int currZeroLevel = zeroLevel;
                    if (h == currZeroLevel) {
                        zeroLevel = currZeroLevel - 1;
                    }
                    if(curr.zeroLevel > h-1){
                        updateZeroLevel(curr);
                    }
                    if(pred.zeroLevel > h-1){
                        updateZeroLevel(pred);
                    }
                    pred.hits.set(h,pred.hits.get(h) + getHits(curr,h));
                    curr.hits.set(h,0);
                    pred.next.set(h,curr.next.get(h));
                    curr.next.set(h,null);
                    curr.topLevel--;
                    curr = pred.next.get(h);
                    continue;
                }
                pred = curr;
                curr = pred.next.get(h);
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
    public Node contains(int val){

        NodePairs np = new NodePairs(null,null);
        if(find(val,np)){
            if(random() < p){
                update(val);
            }
            return np.b;
        }
        return null;
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
            Node pred = np.a;
            Node succ = np.b;
            int currZeroLevel = pred.zeroLevel;
            Node newNode = createNode(val,currZeroLevel);
            newNode.next.set(currZeroLevel,succ);

            if(pred.next.get(currZeroLevel) == succ){
                pred.next.set(currZeroLevel,newNode);
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
            updateUpToLevel(node,zeroLevel);
            System.out.print(node.value + " : " + node.selfHits + " : ");
            for(int i = node.zeroLevel; i <= node.topLevel ; ++i){
                System.out.print(node.hits.get(i)+ " , ");
            }
            System.out.println();
            node = node.next.get(zeroLevel);
        }
    }

    // TODO
    public void removeIfPresent(){

    }
}
