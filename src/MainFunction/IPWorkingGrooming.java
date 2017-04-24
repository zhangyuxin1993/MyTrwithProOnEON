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
import network.VirtualLink;
import subgraph.LinearRoute;

public class IPWorkingGrooming {
	
	public boolean ipWorkingGrooming(NodePair nodepair, Layer iplayer, Layer oplayer,int numOfTransponder,LinearRoute newRoute) {
		boolean routeFlag=false;
		RouteSearching Dijkstra = new RouteSearching();
	
		ArrayList<VirtualLink> DelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinklist = new ArrayList<Link>();
		
		ArrayList<VirtualLink> VirtualLinklist = new ArrayList<VirtualLink>();

		// ����list����Ľڵ��
	
			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();
			System.out.println();
			System.out.println();
			System.out.println();
			

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link Mlink = (Link) (linklist.get(linkitor.next()));
							
//				System.out.println("IP���ϵ���·��" + Mlink.getName());

				VirtualLinklist = Mlink.getVirtualLinkList();//ȡ��IP���ϵ���·��Ӧ��������· �½�һ��listʹ�䱾���������·���ı�						
				for (VirtualLink Vlink : VirtualLinklist) { // ȡ��link�϶�Ӧ��virtual
															// link
//					System.out.println("������·��" + Vlink.getSrcnode() + "-" + Vlink.getDesnode()
//							+ "   nature=" + Vlink.getNature());
					if (Vlink.getNature() == 1) {// ������0 ������1
						DelVirtualLinklist.add(Vlink);
						continue;
					}
					if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {
						DelVirtualLinklist.add(Vlink);
						continue;
					}
				}
				for (VirtualLink nowlink : DelVirtualLinklist) {
//					System.out.println(Mlink.getName()+" ��ɾ����������·Ϊ��"+ nowlink.getSrcnode()+"  "+nowlink.getDesnode());
					Mlink.getVirtualLinkList().remove(nowlink);
				}
				for (VirtualLink nowlink : DelVirtualLinklist) { //  ͳ������ɾ����������·
					if (!SumDelVirtualLinklist.contains(nowlink)) {
						SumDelVirtualLinklist.add(nowlink);
					}
				}		
				DelVirtualLinklist.clear();
				
				if (Mlink.getVirtualLinkList().size() == 0)
					DelIPLinklist.add(Mlink);
			}
			for (Link link : DelIPLinklist) {
//				System.out.println("ɾ����IP����·Ϊ��"+link.getName());
				iplayer.removeLink(link.getName());
			}
			
			
			HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
			Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
			while (Dijlinkitor.hasNext()) {
				double mincost=10000;
				double length=0;
				Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
				for(VirtualLink vlink:Dijlink.getVirtualLinkList()){
//					System.out.println(vlink.getSrcnode()+"   "+vlink.getDesnode());
					if(vlink.getcost()<mincost){
						mincost=vlink.getcost();
						length=vlink.getlength();
					}
				}
				Dijlink.setCost(mincost);
				Dijlink.setLength(length);
				System.out.println("!!!!!!!!�ı䳤�ȵ���·�� "+Dijlink.getName()+"   ����Ϊ��"+Dijlink.getLength()+"    cost:  "+Dijlink.getCost());
			}
//			LinearRoute newRoute = new LinearRoute(null, 0, null);
			Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);

			// �ָ�iplayer����ɾ����link
			for (Link nowlink : DelIPLinklist) {
				iplayer.addLink(nowlink);
			}
			DelIPLinklist.clear();

			// ����dijkstra��������· ���Ҹı���Щ��·�ϵ�����
			if (newRoute.getLinklist().size() != 0) {// ����·��·�ɳɹ�
				System.out.println("********��IP���ҵ�·�ɣ�");
				newRoute.OutputRoute_node(newRoute);
				routeFlag=true;
				
				for (int c = 0; c < newRoute.getLinklist().size(); c++) {
					Link link = newRoute.getLinklist().get(c); // �ҵ���·�������link
//					System.out.println("���·���ϵ���·��"+link.getName());
					/*
					 * ���·�ɳɹ� ����Ҫ�ҵ�IP���ϵ�link��Ӧ��������· �ı�������
					 */
					boolean delflag=false;
					double minCapacity=100000;
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
						if(link1.getNodeA().getName().equals(link.getNodeA().getName())&&link1.getNodeB().getName().equals(link.getNodeB().getName())){			
//							System.out.println(link1.getName());
							for (VirtualLink Vlink : link1.getVirtualLinkList()) {	
								/*
								 * ���ж���virtuallink��link���� ���ҵ�ʣ���������ٵ���������linkʹ�� �ı���ʣ������ֵ
								 */
								System.out.println(Vlink.getSrcnode()+"  "+Vlink.getDesnode());
								if( Vlink.getRestcapacity()<minCapacity){//�ҳ�ʣ���������ٵ�������·������ж���������·���� �����������޸�ѡ��
									minCapacity=Vlink.getRestcapacity();
//									System.out.println(minCapacity);
								}
							}
							for(VirtualLink Vlink : link1.getVirtualLinkList()) {
								if(Vlink.getRestcapacity()==minCapacity){ // �޸�·��֮��������·�ϵ���·����
//									System.out.println(Vlink.getSrcnode()+"  "+Vlink.getDesnode()+"  "+Vlink.getRestcapacity());
									Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
									Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
//									System.out.println(Vlink.getRestcapacity());
									delflag=true;
									break;
								}
								}
							if(delflag)
								break;
							}
						}
					}
				//�ָ���·�϶�Ӧ��������·
				for(VirtualLink link:SumDelVirtualLinklist){
//					System.out.println(link.getSrcnode()+"-"+link.getDesnode());
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
						if(link1.getNodeA().getName().equals(link.getSrcnode())&&link1.getNodeB().getName().equals(link.getDesnode())){		
							link1.getVirtualLinkList().add(link);
						}
					}
				}
				SumDelVirtualLinklist.clear();

				
//				 MyProtectionGrooming mpg = new MyProtectionGrooming();
//				 mpg.myprotectiongrooming(iplayer, oplayer, nodepair,newRoute, numOfTransponder, true);
				
			}
			for(VirtualLink link:SumDelVirtualLinklist){
//				System.out.println(link.getSrcnode()+"-"+link.getDesnode());
				HashMap<String, Link> linklist2 = iplayer.getLinklist();
				Iterator<String> linkitor2 = linklist2.keySet().iterator();
				while (linkitor2.hasNext()) {
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
					if(link1.getNodeA().getName().equals(link.getSrcnode())&&link1.getNodeB().getName().equals(link.getDesnode())){		
						link1.getVirtualLinkList().add(link);
					}
				}
			}
			
		return routeFlag;
	}
}