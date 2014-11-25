package housing;

/*******************************************
 * A Node is the basic sub-unit of an agent-based model.
 * In this paradigm, an ABM is a tree with
 * the model itself being the root node and it's children
 * being sub-modules. Leaf nodes are the agents.
 * 
 * 
 * 
 * @author daniel
 *
 ******************************************/
public class Node {
	public Node(Node [] children) {
		subNodes = children;
		
		Object [] test = {2,3,"hello"};
	}
	
	
	
	public Node [] 	subNodes;
	
}
