package MainFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class ipProGrooming {
	public boolean ipprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag) {// flag=true��ʾ����IP�㽨���Ĺ���·��
													// flag=flase��ʾ��㽨���Ĺ���·��
		RouteSearching Dijkstra = new RouteSearching();
		boolean ipproflag=false;
		boolean delflag = false;

		System.out.println("�ڵ�ԣ�" + nodepair.getName() + "   flag=" + flag);
		ArrayList<VirtualLink> DelLinkList = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelLinkList = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinkList = new ArrayList<Link>();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
//		System.out.println("IP�����·������ " + iplayer.getLinklist().size());

		HashMap<String, Link> linklist = iplayer.getLinklist();
		Iterator<String> linkitor = linklist.keySet().iterator();
		while (linkitor.hasNext()) {// ��һ���� ��һ��
			Link link = (Link) (linklist.get(linkitor.next()));
//			System.out.println("��ǰIP��·��" + link.getName());
			for (VirtualLink Vlink : link.getVirtualLinkList()) {// ��һ���� �ڶ��� // ȡ��������·
//				System.out.println("������·���� ��" + link.getVirtualLinkList().size());												
				if (Vlink.getNature() == 0) {// ɾȥ����Ϊ��������·
					DelLinkList.add(Vlink);
					continue;
				}
				if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {// ɾȥ������������·
					DelLinkList.add(Vlink);
					continue;
				}
				/*
				 * ��IP�㱣��·���빤��·����Ӧ������·�����غ� ��Ҫ��ip��·�ɱ���ʱӦ��ɾ�� ����·�ɶ�Ӧ��������·
				 */
				delflag = false;
//				System.out.println("��IPlink��������·: "+Vlink.getSrcnode()+"  "+Vlink.getDesnode());
				for (Link linkOnphy : Vlink.getPhysicallink()) {// ��һ���� ������
																// ȡ��������·��Ӧ�Ĺ�·
//					System.out.println("��IPlink��������·��Ӧ�Ĺ����·�� "+linkOnphy.getName());
					if (delflag)
						break;
					for (Link LinkOnRoute : route.getLinklist()) {// �ڶ������� ���� ȡ������·���е���·
					
						if (delflag)
							break;
//						System.out.println("����·���ϵ���·�� " + LinkOnRoute.getName());

						if (flag) {// flagΪtrue���ʾ���� IP�㽨���Ĺ���·��
//							System.out.println("����·���ϵ�IPlink��Ӧ��������·������ "+LinkOnRoute.getVirtualLinkList().size());
							for (VirtualLink WorkLinkVritual : LinkOnRoute.getVirtualLinkList()) {// �ڶ����ֵڶ���
																									// ȡ����IP��·�ɵĹ���·������·��Ӧ��������·����ʱֻʣ����������·��

//								System.out.println("����·���ϵ�������·�� " + WorkLinkVritual.getSrcnode() + "  "
//										+ WorkLinkVritual.getDesnode() + "   nature:  " + WorkLinkVritual.getNature());
								if (delflag)
									break;
								for (Link WorkLinkOnPhy : WorkLinkVritual.getPhysicallink()) {// �ڶ����ֵ�����
//									System.out.println("��IP�㽨���Ĺ���·���ϵ�������·��Ӧ�Ĺ����·�� " + WorkLinkOnPhy.getName());
//									System.out.println("��IP��·��������·������������·��  "+linkOnphy.getName());
									if (linkOnphy.getName().equals(WorkLinkOnPhy.getName())) {
										DelLinkList.add(Vlink);
										delflag = true;
									}
									if (delflag)
										break;
								}
							}
						} else {// flagΪfalse���ʾ���� ��㽨���Ĺ���·��
							System.out.println("����IP��·��������·��Ӧ�Ĺ�·��   " + linkOnphy.getName());
							System.out.println("����·����Ӧ�Ĺ�·��   " + LinkOnRoute.getName());
							if (linkOnphy.getName().equals(LinkOnRoute.getName())) {
								DelLinkList.add(Vlink);
								delflag = true;
							}
						}
					}
				}
			} // �����ѭ��Ϊ��ɾ�������õ�������·
			
			for (VirtualLink dellink : DelLinkList) {
				if (!SumDelLinkList.contains(dellink))
					SumDelLinkList.add(dellink);
			}
			for (VirtualLink dellink2 : SumDelLinkList) {
				link.getVirtualLinkList().remove(dellink2);
			} // �Ƴ�ȥ���в�����Ҫ���link
			if (link.getVirtualLinkList().size() == 0) {
				DelIPLinkList.add(link);
			}
		}

		for (Link link : DelIPLinkList) {
//			 System.out.println("ɾ����IP����·Ϊ��"+link.getName());
			iplayer.removeLink(link.getName());
		}
   //����Ϊ�ж�ip���е���·��Щ��Ҫɾ��
		
		HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
		Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
		while (Dijlinkitor.hasNext()) {
			double mincost=10000;
			double Dijlength=0;
			Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
			for(VirtualLink vlink:Dijlink.getVirtualLinkList()){
//				System.out.println(vlink.getSrcnode()+"   "+vlink.getDesnode());
				if(vlink.getcost()<mincost){
					mincost=vlink.getcost();
					Dijlength=vlink.getlength();
				}
			}
			Dijlink.setCost(mincost);
			Dijlink.setLength(Dijlength);
			System.out.println("!!!!!!!!�ı䳤�ȵ���·�� "+Dijlink.getName()+"   ����Ϊ��"+Dijlink.getLength()+"    cost:  "+Dijlink.getCost());
		}
	 
		LinearRoute newRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);// ��iplayer������Ѱ��̱���·��

		for (Link addlink : DelIPLinkList) {// �ָ�iplayer
			iplayer.addLink(addlink);
		}
		DelIPLinkList.clear();
		
		if (newRoute.getNodelist().size() != 0) {
			ipproflag=true;
			System.out.println("**************����·����IP����·�ɳɹ�  ");
			newRoute.OutputRoute_node(newRoute);

			for (int c = 0; c < newRoute.getLinklist().size(); c++) {
				Link link = newRoute.getLinklist().get(c); // �ҵ���·�������link
				System.out.println("���·���ϵ���·��" + link.getName());
				/*
				 * ���·�ɳɹ� ����Ҫ�ҵ�IP���ϵ�link��Ӧ��������· �ı�������
				 */
				boolean delflag_pro = false;
				double minCapacity = 100000;
				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
					if (link1.getNodeA().getName().equals(link.getNodeA().getName())
							&& link1.getNodeB().getName().equals(link.getNodeB().getName())) {
						System.out.println("�ҵ�·�ɾ�������·�� " + link1.getName());
						for (VirtualLink Vlink : link1.getVirtualLinkList()) {
							if (Vlink.getNature() == 1) {
								/*
								 * ���ж���virtuallink��link���� ���ҵ�ʣ���������ٵ���������linkʹ��
								 * �ı���ʣ������ֵ
								 */
//								System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode());
								if (Vlink.getRestcapacity() < minCapacity) {// �ҳ�ʣ���������ٵ�������·������ж���������·����
																			// �����������޸�ѡ��
									minCapacity = Vlink.getRestcapacity();
									System.out.println(minCapacity);
								}
							}
						}
						for (VirtualLink Vlink : link1.getVirtualLinkList()) {
							if (Vlink.getNature() == 1) {
								if (Vlink.getRestcapacity() == minCapacity) { // �޸�·��֮��������·�ϵ���·����
//									System.out.println(Vlink.getSrcnode() + "  " + Vlink.getDesnode() + "  "
//											+ Vlink.getRestcapacity());
									Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
									Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
//									System.out.println(Vlink.getRestcapacity());
									delflag_pro = true;
									break;
								}
							}
						}
						if (delflag_pro)
							break;
					}
				}
			}

			// �ָ���·�϶�Ӧ��������·
			for (VirtualLink link : SumDelLinkList) {
				// System.out.println(link.getSrcnode()+"-"+link.getDesnode());
				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
					if (link1.getNodeA().getName().equals(link.getSrcnode())
							&& link1.getNodeB().getName().equals(link.getDesnode())) {
						link1.getVirtualLinkList().add(link);
					}
				}
			}
			SumDelLinkList.clear();
		}
		for (VirtualLink link : SumDelLinkList) {
//			 System.out.println(link.getSrcnode()+"-"+link.getDesnode());
			HashMap<String, Link> linklist2 = iplayer.getLinklist();
			Iterator<String> linkitor2 = linklist2.keySet().iterator();
			while (linkitor2.hasNext()) {
				Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
				if (link1.getNodeA().getName().equals(link.getSrcnode())
						&& link1.getNodeB().getName().equals(link.getDesnode())) {
					link1.getVirtualLinkList().add(link);
				}
			}
		}
		SumDelLinkList.clear();
		return ipproflag;	
	}

}
