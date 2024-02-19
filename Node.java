/** Represents a node in a linked list of character data objects.
 *  A node has a pointer to a CharData object, and a pointer to another node. */
public class Node {
   
    // a pointer to a CharData object
    CharData cd;
 
    // a pointer to a Node object
    Node next;  // pointer
 
    /** Constructs a node with the given CharData object.
     *  The new node will point to the given next node. */
    public Node(CharData cd, Node next) {
       this.cd = cd;
       this.next = next;
    }
         
    /** Constructs a node with the given CharData object.
     *  The new node will point to null. */
    public Node(CharData cd) {
       this(cd, null);
    }
 
    /** Textual representation of this node. */
    public String toString() {
       return "" + cd;
    }
 }