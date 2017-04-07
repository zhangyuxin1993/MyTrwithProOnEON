package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class Mymain {

	public static void main(String[] args) {

		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/6.csv");
		network.copyNodes();//
		network.createNodepair();// ÿ��layer�����ɽڵ�� �����ڵ�Ե�ʱ����Զ�����nodepair֮���demand

		Layer iplayer = network.getLayerlist().get("Layer0");
		Layer oplayer = network.getLayerlist().get("Physical");

		Mymain mm = new Mymain();
		mm.grooming(network, iplayer, oplayer);
	}

	public void grooming(Network network, Layer iplayer, Layer oplayer) {

		ReadFlowFile rff = new ReadFlowFile();
		rff.Readflow(iplayer, "G:/Topology/6.csv");// Q1 �����Ǹ����õ�
		// /*
		ArrayList<Link> DelLinklist = new ArrayList<Link>();
		// ArrayList<Link> NatureDelLinklist=new ArrayList<Link>();
		ArrayList<Link> SumDelLinklist = new ArrayList<Link>();
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		// ����list����Ľڵ��
		for (int n = 0; n < demandlist.size(); n++) {
			NodePair nodepair = demandlist.get(n);
			System.out.println("���ڲ����Ľڵ�ԣ� " + nodepair.getName() + "  �������������ǣ� " + nodepair.getTrafficdemand());

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link link = (Link) (linklist.get(linkitor.next()));
				System.out.println("��·���֣� " + link.getName());
				if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
					DelLinklist.add(link);
				} // �Ƴ�������������·
				if (link.getNature() == 1) {// ������1 ������0
					DelLinklist.add(link);
				} // �Ƴ����Բ��Ե���·
			}
			for (Link nowlink : DelLinklist) {
				if (!SumDelLinklist.contains(nowlink)) {
					SumDelLinklist.add(nowlink);
				}
			}
			for (Link nowlink : SumDelLinklist) {
				iplayer.removeLink(nowlink.getName());
			}

			// ��iplayer�����link copy��copylayer����ȥ
			Layer ipcopylayer = network.getLayerlist().get("ipcopylayer");
			HashMap<String, Link> linklist1 = iplayer.getLinklist();
			Iterator<String> linkitor1 = linklist1.keySet().iterator();
			while (linkitor1.hasNext()) {
				Link link = (Link) (linklist1.get(linkitor1.next()));

				Node srcnode = ipcopylayer.getNodelist().get(link.getNodeA().getName());
				Node desnode = ipcopylayer.getNodelist().get(link.getNodeB().getName());

				String name = srcnode.getName() + "-" + desnode.getName();
				int index = iplayer.getLinkNum();
				double length = link.getLength();
				double cost = link.getCost();
				Link addlink = new Link(name, index, null, ipcopylayer, srcnode, desnode, length, cost);
				ipcopylayer.addLink(addlink);
			}

			// ��ipcopylayer������Ѱ���·��
			Node srcnode = ipcopylayer.getNodelist().get(nodepair.getSrcNode().getName());
			Node desnode = ipcopylayer.getNodelist().get(nodepair.getDesNode().getName());
			LinearRoute newRoute = new LinearRoute(null, 0, null);
			RouteSearching Dijkstra = new RouteSearching();
			Dijkstra.Dijkstras(srcnode, desnode, ipcopylayer, newRoute, null);

			// �ָ�iplayer����ɾ����link
			for (Link nowlink : SumDelLinklist) {
				iplayer.addLink(nowlink);
			}
			SumDelLinklist.clear();
			DelLinklist.clear();

			// ���ipcopylayer�����link
			ArrayList<Link> CopyDelLinklist = new ArrayList<Link>();
			HashMap<String, Link> copylinklist = ipcopylayer.getLinklist();
			Iterator<String> copylinkitor = copylinklist.keySet().iterator();
			while (copylinkitor.hasNext()) {
				Link link = (Link) (copylinklist.get(copylinkitor.next()));
				CopyDelLinklist.add(link);
			}
			for (Link nowlink : CopyDelLinklist) {
				ipcopylayer.removeLink(nowlink.getName());
			}
			CopyDelLinklist.clear();

			// ����dijkstra��������· ���Ҹı���Щ��·�ϵ�����
			if (newRoute.getLinklist().size() != 0) {// ����·��·�ɳɹ�
				ArrayList<Link> newrouteLinklist = new ArrayList<Link>();
				for (int c = 0; c < newRoute.getLinklist().size(); c++) {
					Link link = newRoute.getLinklist().get(c);

					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));
						if (link.getNodeA().getName().equals(link1.getNodeA().getName())
								&& link.getNodeB().getName().equals(link1.getNodeB().getName())
								&& link1.getNature() == 0) {
							link1.setFlow(link1.getFlow() + nodepair.getTrafficdemand());
							newrouteLinklist.add(link);
						}
					}
				}
			}
			// ���Ϲ���·��·�ɳɹ�
			else {
				System.out.println("IP�㹤��·�ɲ��ɹ�����Ҫ�½���·");
				Node opsrcnode = oplayer.getNodelist().get(nodepair.getSrcNode().getName());
				Node opdesnode = oplayer.getNodelist().get(nodepair.getDesNode().getName());
				// �ڹ���½���·��ʱ����Ҫ��������������
				LinearRoute opnewRoute = new LinearRoute(null, 0, null);
				Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opnewRoute, null);

				if (opnewRoute.getLinklist().size() == 0) {
					System.out.println("������·��");
				} else {
					System.out.println("�������·�ɾ����Ľڵ����£�------");
					opnewRoute.OutputRoute_node(opnewRoute);

					int slotnum = 0;
					int IPflow = nodepair.getTrafficdemand();
					double X = 1;// 2000-4000 BPSK,1000-2000
									// QBSK,500-1000��8QAM,0-500 16QAM
					double routelength = opnewRoute.getlength();
					// System.out.println("����·���ĳ����ǣ�"+routelength);
					// ͨ��·���ĳ������仯���Ƹ�ʽ
					if (routelength > 2000 && routelength <= 4000) {
						X = 12.5;
					} else if (routelength > 1000 && routelength <= 2000) {
						X = 25.0;
					} else if (routelength > 500 && routelength <= 1000) {
						X = 37.5;
					} else if (routelength > 0 && routelength <= 500) {
						X = 50.0;
					}
					slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��
					opnewRoute.setSlotsnum(slotnum);

					ArrayList<Integer> index_wave = new ArrayList<Integer>();
					index_wave = spectrumallocationOneRoute(opnewRoute);
				}

			}

		}
		// */
	}

	private ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		linklistOnroute = route.getLinklist();
		for (Link link : linklistOnroute) {
			if (route.getSlotsnum() == 0) {
				System.out.println("·����û��slot��Ҫ����");
				break;
			}
			link.getSlotsindex().clear();
			int flag = 0;//slotarray��slotindex�����𣿣�
			for (int start = 0; start < link.getSlotsarray().size() - route.getSlotsnum(); start++) {// ���ҿ���slot�����
				for (int num = start; num < route.getSlotsnum(); num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// �ò����Ѿ���ռ��
						flag = 1;
						break;
					}

					if (flag == 0) {
						link.getSlotsindex().add(start);
					}
				}
			}
		} // �������е�link������

		Link firstlink = linklistOnroute.get(0);
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();

		for (int s = 0; s < firstlink.getSlotsindex().size(); s++) {
			int index = firstlink.getSlotsindex().get(s);
			int flag = 1;

			for (Link otherlink : linklistOnroute) {
				if (!otherlink.getSlotsindex().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag == 1) {
				sameindex.add(s);
			}
		}
		return sameindex;
	}

	public static ArrayList<NodePair> Rankflow(Layer IPlayer) {
		ArrayList<NodePair> nodepairlist = new ArrayList<NodePair>(2000);
		HashMap<String, NodePair> map3 = IPlayer.getNodepairlist();
		Iterator<String> iter3 = map3.keySet().iterator();
		while (iter3.hasNext()) {
			NodePair np = (NodePair) (map3.get(iter3.next()));
			if (nodepairlist.size() == 0)
				nodepairlist.add(np);
			else {
				boolean insert = false;
				for (int i = 0; i < nodepairlist.size(); i++) {
					int m_flow = np.getTrafficdemand();
					int n_flow = nodepairlist.get(i).getTrafficdemand();

					if (m_flow > n_flow) {
						nodepairlist.add(i, np);
						insert = true;
						break;
					}

				}

				if (insert == false)
					nodepairlist.add(np);
			}
		}
		return nodepairlist;
	}
}
