package MainFunction;

import network.Node;

/*
 * 为了实现方法中值的传递
 */
public class ParameterTransfer {

	float remainFlowRatio=100;
	float NumremainFlow=0;
	Node StartNode=new Node(null, 0, null, null, 0, 0);
	Node EndNode=new Node(null, 0, null, null, 0, 0);
	float MinRemainFlowRSA=0;  //在每条链路RSA时 记录每条链路上剩余的最小容量
	
	public Node getEndNode() {
		return EndNode;
	}
	public void setEndNode(Node EndNode) {
		 this.EndNode=EndNode;
	}
	
	public Node getStartNode() {
		return StartNode;
	}
	public void setStartNode(Node StartNode) {
		 this.StartNode=StartNode;
	}

	public float getMinRemainFlowRSA() {
		return MinRemainFlowRSA;
	}
	public void setMinRemainFlowRSA(float MinRemainFlowRSA) {
		 this.MinRemainFlowRSA=MinRemainFlowRSA;
	}
	
	
	public float getRemainFlowRatio() {
		return remainFlowRatio;
	}
	public void setRemainFlowRatio(float remainFlow) {
		 this.remainFlowRatio=remainFlow;
	}
	
	public float getNumremainFlow() {
		return NumremainFlow;
	}
	public void setNumremainFlow(float NumremainFlow) {
		 this.NumremainFlow=NumremainFlow;
	}
}
