package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class opProGrooming {
	public void opprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true��ʾ����IP�㽨���Ĺ���·��
													// flag=flase��ʾ��㽨���Ĺ���·��
		RouteSearching Dijkstra = new RouteSearching();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();

		ArrayList<Link> opDelLink = new ArrayList<Link>();
		System.out.println("************����·����IP�㲻��·�ɣ���Ҫ�ڹ���½�");
		// ɾ���ýڵ�ԵĹ���·�ɾ���������������·

		for (Link LinkOnRoute : route.getLinklist()) {// ȡ������·���е���·
			System.out.println("������ϵĹ���·����·��" + LinkOnRoute.getName());
			if (flag) {//// flag=true��ʾ���� IP�㽨���Ĺ���·��
				for (VirtualLink Vlink : LinkOnRoute.getVirtualLinkList()) {

					for (Link LinkOnPhy : Vlink.getPhysicallink()) {// ȡ��ĳһ������·�϶�Ӧ��������·

						HashMap<String, Link> oplinklist = oplayer.getLinklist();
						Iterator<String> oplinkitor = oplinklist.keySet().iterator();
						while (oplinkitor.hasNext()) {
							Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
							// System.out.println("�������·������" +
							// oplink.getName());
							if (oplink.getName().equals(LinkOnPhy.getName())) {
								opDelLink.add(oplink);
								break;
							}
						}
					}
				}
			} else {// flag=false��ʾ���� ��㽨���Ĺ���·��

				HashMap<String, Link> oplinklist = oplayer.getLinklist();
				Iterator<String> oplinkitor = oplinklist.keySet().iterator();
				while (oplinkitor.hasNext()) {
					Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
					// System.out.println("�������·������" + oplink.getName());
					if (oplink.getName().equals(LinkOnRoute.getName())) {
						System.out.println("ɾ���Ĺ����·�� " + oplink.getName());
						opDelLink.add(oplink);

						break;
					}
				}
			}
		}

		for (Link opdellink : opDelLink) {
			oplayer.removeLink(opdellink.getName());
		}

		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());

		LinearRoute opPrtectRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opPrtectRoute, null);// ��iplayer������Ѱ��̱���·��

		for (Link opdellink : opDelLink) {
			oplayer.addLink(opdellink);
		} // �ָ�oplayer�����link
		opDelLink.clear();

		if (opPrtectRoute.getLinklist().size() == 0) {
			System.out.println("����·�ɹ���޷�����");
		} else {
			System.out.println("�½��Ĺ�㱣��·��Ϊ:");
			opPrtectRoute.OutputRoute_node(opPrtectRoute);
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000��8QAM,0-500 16QAM
			double routelength = opPrtectRoute.getlength();
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

			opPrtectRoute.setSlotsnum(slotnum);
			System.out.println("����·����slot���� " + slotnum);
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain mm = new Mymain();
			index_wave = mm.spectrumallocationOneRoute(opPrtectRoute);
			if (index_wave.size() == 0) {
				System.out.println("·������ ��������Ƶ����Դ");
			} else {
				double length = 0;
				double cost = 0;
				for (Link link : opPrtectRoute.getLinklist()) {
					length = length + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);

					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("��· " + link.getName() + "
					// �����slot�ǣ� " + link.getMaxslot()+
					// " ����Ƶ�״����� "+link.getSlotsindex().size());
				}
				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
				Vlink.setnature(1);
				Vlink.setlength(length);
				Vlink.setcost(cost);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
				Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setPhysicallink(opPrtectRoute.getLinklist());
                                         
				numOfTransponder = numOfTransponder + 2;

				// ��IP�����ҳ���Ҫ���������·��link
				HashMap<String, Link> linklist3 = iplayer.getLinklist();
				Iterator<String> linkitor3 = linklist3.keySet().iterator();
				while (linkitor3.hasNext()) {
					Link link = (Link) (linklist3.get(linkitor3.next()));
					if (link.getNodeA().getName().equals(Vlink.getSrcnode())
							&& link.getNodeB().getName().equals(Vlink.getDesnode())) {
						link.getVirtualLinkList().add(Vlink);

						System.out.println("�½��ı�����· " + link.getName() + " ���Ӧ��������·���������flow: "
								+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
								+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
								+"   "+"������·cost�� "+ Vlink.getcost());
					}
				}

				// debug
				// HashMap<String, Link> linklist4 = iplayer.getLinklist();
				// Iterator<String> linkitor4 = linklist4.keySet().iterator();
				// while (linkitor4.hasNext()) {
				// Link link1 = (Link) (linklist4.get(linkitor4.next()));//
				// IPlayer�����link
				// System.out.println("IP LINK:" + link1.getName() + "
				// ��·�����������·����" + link1.getVirtualLinkList().size());

				// }
			}
		}
	}
}
