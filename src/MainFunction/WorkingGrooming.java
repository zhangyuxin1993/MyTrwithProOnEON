package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.sound.midi.SysexMessage;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class WorkingGrooming {

	public void MyWorkingGrooming(Network network, Layer iplayer, Layer oplayer) {
		System.out.println("start");
		RouteSearching Dijkstra = new RouteSearching();
		int numOfTransponder = 0;

		ArrayList<VirtualLink> DelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinklist = new ArrayList<Link>();
		ArrayList<NodePair> demandlist = Rankflow(iplayer);
		ArrayList<VirtualLink> VirtualLinklist = new ArrayList<VirtualLink>();

		// 操作list里面的节点对
		for (int n = 0; n < demandlist.size(); n++) {
			NodePair nodepair = demandlist.get(n);

			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();
			System.out.println();
			System.out.println();
			System.out.println();
			
			System.out.println("正在操作的节点对： " + nodepair.getName() + "  他的流量需求是： " + nodepair.getTrafficdemand());

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
			
			int mincost=10000;
			int length=0;
			HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
			Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
			while (Dijlinkitor.hasNext()) {
				Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
				for(VirtualLink vlink:Dijlink.getVirtualLinkList()){
					System.out.println(vlink.getSrcnode()+"   "+vlink.getDesnode());
					if(vlink.getcost()<mincost){
						mincost=vlink.getcost();
						length=vlink.getlength();
					}
				}
				Dijlink.setCost(mincost);
				Dijlink.setLength(length);
			}
			LinearRoute newRoute = new LinearRoute(null, 0, null);
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

				 MyProtectionGrooming mpg = new MyProtectionGrooming();
				 mpg.myprotectiongrooming(iplayer, oplayer, nodepair,newRoute, numOfTransponder, true);
				}
			// 以上工作路由在IP层路由成功
			else {
			 //先恢复虚拟链路
				for(VirtualLink link:SumDelVirtualLinklist){
//					System.out.println("虚拟链路： "+link.getSrcnode()+"-"+link.getDesnode());
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
//						System.out.println("IP LINK:"+link1.getName());
						if(link1.getNodeA().getName().equals(link.getSrcnode())&&link1.getNodeB().getName().equals(link.getDesnode())){		
							link1.getVirtualLinkList().add(link);
						}
					}
				}
				
				//debug
//				HashMap<String, Link> linklist2 = iplayer.getLinklist();
//				Iterator<String> linkitor2 = linklist2.keySet().iterator();
//				while (linkitor2.hasNext()) {
//					Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
//					System.out.println("IP LINK:"+link1.getName()+"链路上面的虚拟链路数："+link1.getVirtualLinkList().size());
//				 
//				}
				SumDelVirtualLinklist.clear();
	
				System.out.println("IP层工作路由不成功，需要新建光路");
				Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
				Node opdesnode = oplayer.getNodelist().get(desnode.getName());
				// System.out.println("源点： " + opsrcnode.getName() + " 终点： " +
				// opdesnode.getName());

				// 在光层新建光路的时候不需要考虑容量的问题
				LinearRoute opnewRoute = new LinearRoute(null, 0, null);
				Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opnewRoute, null);

				if (opnewRoute.getLinklist().size() == 0) {
					System.out.println("工作无路径");
				} else {
					System.out.println("在物理层路由经过的节点如下：------");
					opnewRoute.OutputRoute_node(opnewRoute);

					int slotnum = 0;
					int IPflow = nodepair.getTrafficdemand();
					double X = 1;// 2000-4000 BPSK,1000-2000
									// QBSK,500-1000，8QAM,0-500 16QAM
					double routelength = opnewRoute.getlength();
					// System.out.println("物理路径的长度是："+routelength);
					// 通过路径的长度来变化调制格式
					if (routelength > 2000 && routelength <= 4000) {
						X = 12.5;
					} else if (routelength > 1000 && routelength <= 2000) {
						X = 25.0;
					} else if (routelength > 500 && routelength <= 1000) {
						X = 37.5;
					} else if (routelength > 0 && routelength <= 500) {
						X = 50.0;
					}
					slotnum = (int) Math.ceil(IPflow / X);// 向上取整

					opnewRoute.setSlotsnum(slotnum);
					System.out.println("该链路所需slot数： " + slotnum);
					ArrayList<Integer> index_wave = new ArrayList<Integer>();
					index_wave = spectrumallocationOneRoute(opnewRoute);
					if (index_wave.size() == 0) {
						System.out.println("路径堵塞 ，不分配频谱资源");
					} else {
						double length = 0;
						double cost = 0;

						for (Link link : opnewRoute.getLinklist()) {// 物理层的link
							length = length + link.getLength();
							cost = cost + link.getCost();
							Request request = null;
							ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
							link.setMaxslot(slotnum + link.getMaxslot());
							// System.out.println("链路 " + link.getName() + "
							// 的最大slot是： " + link.getMaxslot()+
							// " 可用频谱窗数： "+link.getSlotsindex().size());
						} // 改变物理层上的链路容量 以便于下一次新建时分配slot
						String name = opsrcnode.getName() + "-" + opdesnode.getName();
						int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的
																	// 故这样设置index
						Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(newlink);

						VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
						Vlink.setnature(0);
						Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
						Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
						Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());

						System.out.println("新建的工作光路 " + newlink.getName() + " 其对应的虚拟链路上面的已用flow: "
								+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
								+ "    预留的flow：  " + Vlink.getRestcapacity());
						numOfTransponder = numOfTransponder + 2;
						newlink.getVirtualLinkList().add(Vlink);
						System.out.println("*********工作链路在光层新建的链路：  "+newlink.getName()+"  上的虚拟链路条数： "+ newlink.getVirtualLinkList().size());
						Vlink.setPhysicallink(opnewRoute.getLinklist());

						 MyProtectionGrooming mpg = new MyProtectionGrooming();
						 mpg.myprotectiongrooming(iplayer, oplayer, nodepair,opnewRoute, numOfTransponder, false);
					
						
						//debug
//						HashMap<String, Link> linklist3 = iplayer.getLinklist();
//						Iterator<String> linkitor3 = linklist3.keySet().iterator();
//						while (linkitor3.hasNext()) {
//							Link link1 = (Link) (linklist3.get(linkitor3.next()));// IPlayer里面的link
//							System.out.println("IP LINK:"+link1.getName()+"链路上面的虚拟链路数："+link1.getVirtualLinkList().size());
//							for(VirtualLink link:link1.getVirtualLinkList()){
//								System.out.println(link.getSrcnode()+" "+link.getDesnode());
//							}
//						}
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

	public ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route) {
		ArrayList<Link> linklistOnroute = new ArrayList<Link>();
		linklistOnroute = route.getLinklist();
//		route.OutputRoute_node(route);// debug
		for (Link link : linklistOnroute) {
			if (route.getSlotsnum() == 0) {
				System.out.println("路径上没有slot需要分配");
				break;
			}
			link.getSlotsindex().clear();
			// slotarray和slotindex的区别？？
			for (int start = 0; start < link.getSlotsarray().size() - route.getSlotsnum(); start++) {// 查找可用slot的起点
				int flag = 0;
				for (int num = start; num < route.getSlotsnum() + start; num++) {
					if (link.getSlotsarray().get(num).getoccupiedreqlist().size() != 0) {// 该波长已经被占用
						flag = 1;
						break;
					}
				}
				if (flag == 0) {
					link.getSlotsindex().add(start);
				}
			}
		} // 以上所有的link分配完

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
		System.out.println("！！！！！！spectrumallocationOneRoute  ");
		for (Link link : linklistOnroute) {
				System.out.println("链路：  "+link.getName()+"    "+link.getSlotsindex().size());	
			}
		return sameindex;
	}
}
