package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class WorkingGrooming {
	
	public void WorkingGrooming( Network network, Layer iplayer, Layer oplayer) {
		RouteSearching Dijkstra = new RouteSearching();
		int numOfTransponder = 0;

		ArrayList<Link> DelLinklist = new ArrayList<Link>();
		ArrayList<Link> SumDelLinklist = new ArrayList<Link>();
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		// for (NodePair nodepair : demandlist) {
		// System.out.println(nodepair.getName() + " " +
		// nodepair.getTrafficdemand());
		// }
		// ����list����Ľڵ��
		for (int n = 0; n < demandlist.size(); n++) {
			NodePair nodepair = demandlist.get(n);

			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();

			System.out.println("���ڲ����Ľڵ�ԣ� " + nodepair.getName() + "  �������������ǣ� " + nodepair.getTrafficdemand());

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link link = (Link) (linklist.get(linkitor.next()));
				System.out.println("��·���֣� " + link.getName());
				if (link.getSumflow() - link.getFlow() < nodepair.getTrafficdemand()) {
					System.out.println("link�ϵ���������" + link.getSumflow());
					System.out.println("link�ϵ���ʹ��������" + link.getFlow());
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

			LinearRoute newRoute = new LinearRoute(null, 0, null);
			Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);

			// �ָ�iplayer����ɾ����link
			for (Link nowlink : SumDelLinklist) {
				iplayer.addLink(nowlink);
			}
			SumDelLinklist.clear();
			DelLinklist.clear();

			// ����dijkstra��������· ���Ҹı���Щ��·�ϵ�����
			if (newRoute.getLinklist().size() != 0) {// ����·��·�ɳɹ�
				System.out.println("********��IP���ҵ�·�ɣ�");
				newRoute.OutputRoute_node(newRoute);

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
							link1.setIpremainflow(link1.getSumflow()-link1.getFlow());
							System.out.println("��· " + link1.getName() + "���Ѿ�ʹ�õ�����" + link1.getFlow() + "  ��·��ʣ������ ="
									+ link1.getIpremainflow());
							newrouteLinklist.add(link);
						}
					}
				}
			}

			// ���Ϲ���·��·�ɳɹ�
			else {
				System.out.println("IP�㹤��·�ɲ��ɹ�����Ҫ�½���·");
				Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
				Node opdesnode = oplayer.getNodelist().get(desnode.getName());
				// System.out.println("Դ�㣺 " + opsrcnode.getName() + " �յ㣺 " +
				// opdesnode.getName());

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
					System.out.println("����·����slot���� " + slotnum);
					ArrayList<Integer> index_wave = new ArrayList<Integer>();
					index_wave = spectrumallocationOneRoute(opnewRoute);
					if (index_wave.size() == 0) {
						System.out.println("·������ ��������Ƶ����Դ");
					} else {
						double length = 0;
						double cost = 0;
						for (Link link : opnewRoute.getLinklist()) {
							length = length + link.getLength();
							cost = cost + link.getCost();
							Request request = null;
							ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
						
							link.setMaxslot(slotnum + link.getMaxslot());
							// System.out.println("��· " + link.getName() + "
							// �����slot�ǣ� " + link.getMaxslot()+
							// " ����Ƶ�״����� "+link.getSlotsindex().size());
						}
						String name = opsrcnode.getName() + "-" + opdesnode.getName();
						int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ��
																	// ����������index
						Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(newlink);
						newlink.setNature(0);
						newlink.setFlow(nodepair.getTrafficdemand());
						newlink.setSumflow(slotnum * X);// �������flow�Ǵ����������
						newlink.setIpremainflow(newlink.getSumflow()-newlink.getFlow());
						System.out.println(newlink.getName() + " ���������flow: " + newlink.getFlow() + "    ���е�flow:  "
								+ newlink.getSumflow() + "    Ԥ����flow��  " + newlink.getIpremainflow());
						numOfTransponder = numOfTransponder + 2;
						newlink.setPhysicallink(opnewRoute.getLinklist());
					}
				}
			}
		}
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
	private ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		linklistOnroute = route.getLinklist();
		route.OutputRoute_node(route);// debug
		for (Link link : linklistOnroute) {
			if (route.getSlotsnum() == 0) {
				System.out.println("·����û��slot��Ҫ����");
				break;
			}
			link.getSlotsindex().clear();
			// slotarray��slotindex�����𣿣�
			for (int start = 0; start < link.getSlotsarray().size() - route.getSlotsnum(); start++) {// ���ҿ���slot�����
				int flag = 0;
				for (int num = start; num < route.getSlotsnum() + start; num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// �ò����Ѿ���ռ��
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					link.getSlotsindex().add(start);
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
				if (otherlink.getName().equals(firstlink.getName()))
					continue;
				if (!otherlink.getSlotsindex().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag == 1) {
				sameindex.add(index);
			}
		}
		return sameindex;
	}
}
 
