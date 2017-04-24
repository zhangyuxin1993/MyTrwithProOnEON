package MainFunction;

import java.util.ArrayList;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class opWorkingGrooming {
	
	public boolean opWorkingGrooming(NodePair nodepair, Layer iplayer, Layer oplayer,LinearRoute opnewRoute,int numOfTransponder) {
		RouteSearching Dijkstra = new RouteSearching();
		boolean opworkflag=false;
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
 
		
		//debug
//		HashMap<String, Link> linklist2 = iplayer.getLinklist();
//		Iterator<String> linkitor2 = linklist2.keySet().iterator();
//		while (linkitor2.hasNext()) {
//			Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
//			System.out.println("IP LINK:"+link1.getName()+"链路上面的虚拟链路数："+link1.getVirtualLinkList().size());
//		 
//		}
		 
		System.out.println("IP层工作路由不成功，需要新建光路");
		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());
		// System.out.println("源点： " + opsrcnode.getName() + " 终点： " +
		// opdesnode.getName());

		// 在光层新建光路的时候不需要考虑容量的问题
		 
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
			Mymain spa=new Mymain();
			index_wave = spa.spectrumallocationOneRoute(opnewRoute);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
			} else {
				opworkflag=true;
				double length1 = 0;
				double cost = 0;

				for (Link link : opnewRoute.getLinklist()) {// 物理层的link
					length1 = length1 + link.getLength();
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
				Link newlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
				iplayer.addLink(newlink);

				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
				Vlink.setnature(0);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
				Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);

				System.out.println("新建的工作光路 " + newlink.getName() + " 其对应的虚拟链路上面的已用flow: "
						+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
						+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
						+"   "+"虚拟链路cost： "+ Vlink.getcost());
				numOfTransponder = numOfTransponder + 2;
				newlink.getVirtualLinkList().add(Vlink);
				System.out.println("*********工作链路在光层新建的链路：  "+newlink.getName()+"  上的虚拟链路条数： "+ newlink.getVirtualLinkList().size());
				Vlink.setPhysicallink(opnewRoute.getLinklist());
			
				
				//debug
//				HashMap<String, Link> linklist3 = iplayer.getLinklist();
//				Iterator<String> linkitor3 = linklist3.keySet().iterator();
//				while (linkitor3.hasNext()) {
//					Link link1 = (Link) (linklist3.get(linkitor3.next()));// IPlayer里面的link
//					System.out.println("IP LINK:"+link1.getName()+"链路上面的虚拟链路数："+link1.getVirtualLinkList().size());
//					for(VirtualLink link:link1.getVirtualLinkList()){
//						System.out.println(link.getSrcnode()+" "+link.getDesnode());
//					}
//				}
			}
		}
		return opworkflag;
	}
	}

 
