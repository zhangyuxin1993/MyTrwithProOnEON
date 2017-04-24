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

		// 操作list里面的节点对
	
			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();
			System.out.println();
			System.out.println();
			System.out.println();
			

			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link Mlink = (Link) (linklist.get(linkitor.next()));
							
//				System.out.println("IP层上的链路：" + Mlink.getName());

				VirtualLinklist = Mlink.getVirtualLinkList();//取出IP层上的链路对应的虚拟链路 新建一个list使其本身的虚拟链路不改变						
				for (VirtualLink Vlink : VirtualLinklist) { // 取出link上对应的virtual
															// link
//					System.out.println("虚拟链路：" + Vlink.getSrcnode() + "-" + Vlink.getDesnode()
//							+ "   nature=" + Vlink.getNature());
					if (Vlink.getNature() == 1) {// 工作是0 保护是1
						DelVirtualLinklist.add(Vlink);
						continue;
					}
					if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {
						DelVirtualLinklist.add(Vlink);
						continue;
					}
				}
				for (VirtualLink nowlink : DelVirtualLinklist) {
//					System.out.println(Mlink.getName()+" 上删除的虚拟链路为："+ nowlink.getSrcnode()+"  "+nowlink.getDesnode());
					Mlink.getVirtualLinkList().remove(nowlink);
				}
				for (VirtualLink nowlink : DelVirtualLinklist) { //  统计所有删除的虚拟链路
					if (!SumDelVirtualLinklist.contains(nowlink)) {
						SumDelVirtualLinklist.add(nowlink);
					}
				}		
				DelVirtualLinklist.clear();
				
				if (Mlink.getVirtualLinkList().size() == 0)
					DelIPLinklist.add(Mlink);
			}
			for (Link link : DelIPLinklist) {
//				System.out.println("删除的IP层链路为："+link.getName());
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
				System.out.println("!!!!!!!!改变长度的链路： "+Dijlink.getName()+"   长度为："+Dijlink.getLength()+"    cost:  "+Dijlink.getCost());
			}
//			LinearRoute newRoute = new LinearRoute(null, 0, null);
			Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);

			// 恢复iplayer里面删除的link
			for (Link nowlink : DelIPLinklist) {
				iplayer.addLink(nowlink);
			}
			DelIPLinklist.clear();

			// 储存dijkstra经过的链路 并且改变这些链路上的容量
			if (newRoute.getLinklist().size() != 0) {// 工作路径路由成功
				System.out.println("********在IP层找到路由！");
				newRoute.OutputRoute_node(newRoute);
				routeFlag=true;
				
				for (int c = 0; c < newRoute.getLinklist().size(); c++) {
					Link link = newRoute.getLinklist().get(c); // 找到的路由上面的link
//					System.out.println("光层路由上的链路："+link.getName());
					/*
					 * 如果路由成功 则需要找到IP层上的link对应的虚拟链路 改变其容量
					 */
					boolean delflag=false;
					double minCapacity=100000;
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
						if(link1.getNodeA().getName().equals(link.getNodeA().getName())&&link1.getNodeB().getName().equals(link.getNodeB().getName())){			
//							System.out.println(link1.getName());
							for (VirtualLink Vlink : link1.getVirtualLinkList()) {	
								/*
								 * 若有多条virtuallink在link上面 则找到剩余容量最少的那条虚拟link使用 改变其剩余容量值
								 */
								System.out.println(Vlink.getSrcnode()+"  "+Vlink.getDesnode());
								if( Vlink.getRestcapacity()<minCapacity){//找出剩余容量最少的虚拟链路（如果有多条虚拟链路可用 可以在这里修改选择）
									minCapacity=Vlink.getRestcapacity();
//									System.out.println(minCapacity);
								}
							}
							for(VirtualLink Vlink : link1.getVirtualLinkList()) {
								if(Vlink.getRestcapacity()==minCapacity){ // 修改路由之后虚拟链路上的链路容量
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
				//恢复链路上对应的虚拟链路
				for(VirtualLink link:SumDelVirtualLinklist){
//					System.out.println(link.getSrcnode()+"-"+link.getDesnode());
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
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
					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
					if(link1.getNodeA().getName().equals(link.getSrcnode())&&link1.getNodeB().getName().equals(link.getDesnode())){		
						link1.getVirtualLinkList().add(link);
					}
				}
			}
			
		return routeFlag;
	}
}