package MainFunction;

import java.util.HashMap;
import java.util.Iterator;

import network.Layer;
import network.Link;
import network.Node;

public class Onlyfortest {
	public void onlyfortest(Layer layer) {
		HashMap<String, Link> linklist2 = layer.getLinklist();
		Iterator<String> linkitor2 = linklist2.keySet().iterator();
		while (linkitor2.hasNext()) {
			Link link1 = (Link) (linklist2.get(linkitor2.next()));
			System.out.println(link1.getName()+"    链路长度为："+link1.getLength());
		}
		HashMap<String, Node> linklist = layer.getNodelist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {
			Node link1 = (Node) (linklist.get(linkitor.next()));
			System.out.println(link1.getName());
		}
	}
}
