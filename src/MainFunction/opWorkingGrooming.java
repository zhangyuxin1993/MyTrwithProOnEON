package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import demand.Request;
import general.file_out_put;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class opWorkingGrooming {
	String OutFileName =Mymain.OutFileName;
	public boolean opWorkingGrooming(NodePair nodepair, Layer iplayer, Layer oplayer,LinearRoute opnewRoute,ArrayList<WorkandProtectRoute> wprlist) throws IOException {
		RouteSearching Dijkstra = new RouteSearching();
		boolean opworkflag=false;
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
		double routelength = 0;
		LinearRoute route_out=new LinearRoute(null, 0, null);
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		
		
		System.out.println("IP层工作路由不成功，需要新建光路");
		file_io.filewrite2(OutFileName,"IP层工作路由不成功，需要新建光路");
		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());
		 System.out.println("源点： " + opsrcnode.getName() + " 终点： " +opdesnode.getName());
		 file_io.filewrite2(OutFileName,"源点： " + opsrcnode.getName() + " 终点： " +opdesnode.getName());
		 
		// 在光层新建光路的时候不需要考虑容量的问题
		 
		Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opnewRoute, null);

		if (opnewRoute.getLinklist().size() == 0) {
			System.out.println("工作无路径");
			file_io.filewrite2(OutFileName,"工作无路径");
		} else {
			System.out.print("在物理层路由为：");
			file_io.filewrite_without(OutFileName,"在物理层路由为：");
			opnewRoute.OutputRoute_node(opnewRoute);
			route_out.OutputRoute_node(opnewRoute, OutFileName);
			System.out.println(); file_io.filewrite2(OutFileName,"");
			
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000，8QAM,0-500 16QAM
			
			for(Link link:opnewRoute.getLinklist()){
				routelength=routelength+link.getLength();
			}
			// System.out.println("物理路径的长度是："+routelength);
			// 通过路径的长度来变化调制格式 并且判断再生器 的使用

			if(routelength<4000){//找到的路径不需要再生器就可以直接使用
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
			System.out.println("不需要再生器 该链路所需slot数： " + slotnum);
			file_io.filewrite2(OutFileName,"不需要再生器 该链路所需slot数： " + slotnum);
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa=new Mymain();
			index_wave = spa.spectrumallocationOneRoute(true,opnewRoute,null,slotnum);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
				file_io.filewrite2(OutFileName,"路径堵塞 ，不分配频谱资源");
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
					 System.out.println("物理层为链路分配容量后，链路 " + link.getName() + "的最大slot是： " + link.getMaxslot()+" 可用频谱窗数： "+link.getSlotsindex().size());
					 file_io.filewrite2(OutFileName,"物理层为链路分配容量后，链路 " + link.getName() + "的最大slot是： " + link.getMaxslot()+" 可用频谱窗数： "+link.getSlotsindex().size());
				} // 改变物理层上的链路容量 以便于下一次新建时分配slot
			
				String name = opsrcnode.getName() + "-" + opdesnode.getName();
				int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的
															// 故这样设置index
				
				Link finlink=iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag=false;
				try{
					System.out.println("IP层中找到工作链路"+finlink.getName());
					file_io.filewrite2(OutFileName,"IP层中找到工作链路"+finlink.getName());
					findflag=true;
				}catch(java.lang.NullPointerException ex){
					System.out.println("IP 层没有该工作链路需要新建链路");
					file_io.filewrite2(OutFileName,"IP 层没有该工作链路需要新建链路");
					createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
					iplayer.addLink(createlink);
				}
				
				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
				Vlink.setnature(0);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
				Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);
				Vlink.setPhysicallink(opnewRoute.getLinklist());	
				
				if(findflag){//如果在IP层中已经找到该链路
				finlink.getVirtualLinkList().add(Vlink);
				System.out.println("IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
						+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
						+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
						+"   "+"虚拟链路cost： "+ Vlink.getcost());
				file_io.filewrite2(OutFileName,"IP层已存在的链路 " + finlink.getName() + " 加入新的保护虚拟链路 上面的已用flow: "
						+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
						+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
						+"   "+"虚拟链路cost： "+ Vlink.getcost());
				System.out.println("工作链路在光层新建的链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
				file_io.filewrite2(OutFileName,"工作链路在光层新建的链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
				}
				else{
					createlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP层上新建链路 " + createlink.getName() + " 加入新的工作虚拟链路 上面的已用flow: "
							+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
							+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
							+"   "+"虚拟链路cost： "+ Vlink.getcost());
					file_io.filewrite2(OutFileName,"IP层上新建链路 " + createlink.getName() + " 加入新的工作虚拟链路 上面的已用flow: "
							+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
							+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
							+"   "+"虚拟链路cost： "+ Vlink.getcost());
					System.out.println("*********工作链路在光层新建的链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
					file_io.filewrite2(OutFileName,"*********工作链路在光层新建的链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
				}
//				numOfTransponder = numOfTransponder + 2;
			}
			}
			if(routelength>4000){
				RegeneratorPlace  regplace=new RegeneratorPlace();
				opworkflag=regplace.regeneratorplace( IPflow,routelength, opnewRoute, oplayer,iplayer, wprlist, nodepair);
			}
		}
		if(opworkflag){
//				&&routelength<=4000) {
			System.out.println("在光层成功路由并且RSA");
			file_io.filewrite2(OutFileName,"在光层成功路由并且RSA");
			WorkandProtectRoute wpr=new WorkandProtectRoute(nodepair);
			Request re=new Request(nodepair);
			ArrayList<Link> totallink=new ArrayList<>();
			totallink=opnewRoute.getLinklist();
			wpr.setrequest(re);
			wpr.setworklinklist(totallink);
			wprlist.add(wpr);
		
		}
		if(!opworkflag) {
			System.out.println("在光层路由失败 改业务阻塞");
			file_io.filewrite2(OutFileName,"在光层路由失败 改业务阻塞");
		}
		return opworkflag;
	}
	}

 
