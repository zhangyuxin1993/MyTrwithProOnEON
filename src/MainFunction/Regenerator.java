package MainFunction;

import network.Node;

public class Regenerator {
	private Node node = new Node(null, 0, null, null, 0, 0);
	private int index = 0;
//	private NodePair nodepair = new NodePair(null, 0, null, null, null, null);
	
	public Regenerator(Node node) {
		super();
		this.node = node;
	}
	
//	public HashMap<Node, Integer> getnodeandindex() {
//		return nodeandindex;
//	}
//	public void setnodeandindex(HashMap<Node, Integer> nodeandindex) {
//		this.nodeandindex = nodeandindex;
//	}
	
	public void setnode(Node node) {
		 this.node=node;
	}
	public Node getnode() {
		return node;
	}
	
	public void setindex(int index) {
		 this.index=index;
	}
	public int getindex() {
		return index;
	}
	
//	public void setnodepair(NodePair nodePair) {
//		 this.nodepair=nodePair;
//	}
//	public NodePair getnodepair() {
//		return nodepair;
//	}

}
