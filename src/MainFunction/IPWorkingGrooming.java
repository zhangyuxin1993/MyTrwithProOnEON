package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import general.file_out_put;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class IPWorkingGrooming {
	String OutFileName =Mymain.OutFileName;
	public boolean ipWorkingGrooming(NodePair nodepair, Layer iplayer, Layer oplayer,int numOfTransponder,LinearRoute newRoute, ArrayList<WorkandProtectRoute> wprlist) {
		boolean routeFlag=false;
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		RouteSearching Dijkstra = new RouteSearching();
		ArrayList<VirtualLink> DelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<VirtualLink> SumDelVirtualLinklist = new ArrayList<VirtualLink>();
		ArrayList<Link> DelIPLinklist = new ArrayList<Link>();
		
		ArrayList<VirtualLink> VirtualLinklist = new ArrayList<VirtualLink>();
		// 操作list里面的节点对
			Node srcnode = nodepair.getSrcNode();
			Node desnode = nodepair.getDesNode();
	
			//test
//			System.out.println("IP层上的链路条数为：" +  iplayer.getLinklist().size());
//			file_io.filewrite2(OutFileName,"IP层上的链路条数为：" +  iplayer.getLinklist().size());
//			HashMap<String, Link> linklisttest = iplayer.getLinklist();
//			Iterator<String> linkitortest = linklisttest.keySet().iterator();
//			while (linkitortest.hasNext()) {
//				Link Mlink = (Link) (linklisttest.get(linkitortest.next()));
//				file_io.filewrite2(OutFileName,"IP层上的链路为：" +  Mlink.getName());
//				VirtualLinklist = Mlink.getVirtualLinkList();//取出IP层上的链路对应的虚拟链路 新建一个list使其本身的虚拟链路不改变						
//				for (VirtualLink Vlink : VirtualLinklist) { // 取出link上对应的virtua
//				file_io.filewrite2(OutFileName,"该IP链路上的虚拟链路为：" +  Vlink.getSrcnode()+"-"+Vlink.getDesnode()+"   性质为："+Vlink.getNature());
//				}
//			}
			
			HashMap<String, Link> linklist = iplayer.getLinklist();
			Iterator<String> linkitor = linklist.keySet().iterator();
			while (linkitor.hasNext()) {
				Link Mlink = (Link) (linklist.get(linkitor.next()));
				VirtualLinklist = Mlink.getVirtualLinkList();//取出IP层上的链路对应的虚拟链路 新建一个list使其本身的虚拟链路不改变						
				for (VirtualLink Vlink : VirtualLinklist) { // 取出link上对应的virtual
															// link
//					System.out.println("IP层上链路"+Mlink.getName()+"    对应的虚拟链路：" + Vlink.getSrcnode() + "-" + Vlink.getDesnode()+ "   nature=" + Vlink.getNature()+
//							"    该虚拟链路上对应的剩余容量为："+Vlink.getRestcapacity());
					if (Vlink.getNature() == 1) {// 工作是0 保护是1
						DelVirtualLinklist.add(Vlink);
						continue;
					}
					if (Vlink.getRestcapacity() < nodepair.getTrafficdemand()) {
						DelVirtualLinklist.add(Vlink);
						continue;
					}
				}
				for (VirtualLink nowlink : DelVirtualLinklist) { //  统计所有删除的虚拟链路
					if (!SumDelVirtualLinklist.contains(nowlink)) {
						SumDelVirtualLinklist.add(nowlink);
					}
				}
				
				for (VirtualLink nowlink : SumDelVirtualLinklist) {
//					System.out.println(Mlink.getName()+" 上删除的虚拟链路为："+ nowlink.getSrcnode()+"  "+nowlink.getDesnode());
					Mlink.getVirtualLinkList().remove(nowlink);
				}
				
				DelVirtualLinklist.clear();
				if (Mlink.getVirtualLinkList().size() == 0)
					DelIPLinklist.add(Mlink);
			}
			
			for (Link link : DelIPLinklist) {
//				System.out.println("删除的IP层链路为："+link.getName());
				iplayer.removeLink(link.getName());
			}
			//以上为第一部分===删除IP层上容量不够的链路
			
//         因为一条IP链路可能会对应多条虚拟链路 所以这里使用剩余容量最少的路径 一条IP链路上进留下剩余容量最少的链路 
//			其余的虚拟链路均删除	
		   ArrayList<VirtualLink> DelhighcapVlink=new ArrayList<>();
			HashMap<String, Link> Dijlinklist = iplayer.getLinklist();
			Iterator<String> Dijlinkitor = Dijlinklist.keySet().iterator();
			while (Dijlinkitor.hasNext()) {
				double minrescapacity=10000;
				Link Dijlink = (Link) (Dijlinklist.get(Dijlinkitor.next()));
				for(VirtualLink vlink:Dijlink.getVirtualLinkList()){
//					System.out.println(vlink.getSrcnode()+"   "+vlink.getDesnode());
					if(vlink.getRestcapacity()<minrescapacity){
						minrescapacity=vlink.getRestcapacity();
					}
				}//找出一条IP链路上所有虚拟链路中剩余容量最少的数量
				
				for(VirtualLink vlink: Dijlink.getVirtualLinkList()){
					if(vlink.getRestcapacity()>minrescapacity){
						DelhighcapVlink.add(vlink);
					}
				}
				for(VirtualLink vlink:DelhighcapVlink){
					Dijlink.getVirtualLinkList().remove(vlink);
				}//将所有剩余容量大于最小剩余容量的链路全部放入集合 并删除
			}
			
			WorkandProtectRoute wpr=new WorkandProtectRoute(nodepair);
			Request re=new Request(nodepair);
			ArrayList<Link> totallink=new ArrayList<>();
			Dijkstra.Dijkstras(srcnode, desnode, iplayer, newRoute, null);

			// 恢复iplayer里面删除的link
			for (Link nowlink : DelIPLinklist) {
				iplayer.addLink(nowlink);
			}
			DelIPLinklist.clear();

			// 储存dijkstra经过的链路 并且改变这些链路上的容量
			if (newRoute.getLinklist().size() != 0) {// 工作路径路由成功
				System.out.print("part2==在IP层找到路由:");
				file_io.filewrite_without(OutFileName,"part2==工作路径在IP层找到路由:");
				newRoute.OutputRoute_node(newRoute);
				routeFlag=true;
				for (int c = 0; c < newRoute.getLinklist().size(); c++) {
					Link link = newRoute.getLinklist().get(c); // 找到的路由上面的link
					System.out.println("ip层上路由链路："+link.getName());
					file_io.filewrite2(OutFileName,"ip层上路由链路："+link.getName());
					HashMap<String, Link> linklist2 = iplayer.getLinklist();
					Iterator<String> linkitor2 = linklist2.keySet().iterator();
					while (linkitor2.hasNext()) {
						Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer里面的link
						if(link1.getNodeA().getName().equals(link.getNodeA().getName())&&link1.getNodeB().getName().equals(link.getNodeB().getName())){			
						//在IP层找到该路径 修改虚拟链路容量的值此时每条IP连路上只有一条虚拟链路
								VirtualLink Vlink=link1.getVirtualLinkList().get(0);
								Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
								Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
//								System.out.println("IP层链路 "+ link1.getName()+"上虚拟链路剩余容量为："+ Vlink.getRestcapacity());
								for(Link worklink:Vlink.getPhysicallink()){//将业务虚拟链路上对应的物理链路全部放在totallink中
									totallink.add(worklink);
								}
						}
						}
					}
				wpr.setrequest(re);
				wpr.setworklinklist(totallink);
				wprlist.add(wpr);
			}
				//恢复链路上对应的虚拟链路
				for(VirtualLink link:SumDelVirtualLinklist){//恢复因为容量不够删除的虚拟链路
//					System.out.println("删除容量不足或属性不对的虚拟链路： "+link.getRestcapacity());
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
				
				for(VirtualLink link:DelhighcapVlink){//恢复因为容量过剩删除的虚拟链路
//					System.out.println("删除容量过剩的虚拟链路： "+link.getRestcapacity());
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
				DelhighcapVlink.clear();
			
			
			if(routeFlag) {
				System.out.println("在IP层成功路由");
				file_io.filewrite2(OutFileName,"在IP层成功路由");
			}
			if(!routeFlag){ 
				System.out.println("IP层路由失败");
				file_io.filewrite2(OutFileName,"IP层路由失败");
			}
		return routeFlag;
	}
}