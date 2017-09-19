package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class RegeneratorPlace {
	public boolean regeneratorplace(int IPflow,double routelength,LinearRoute newRoute,Layer oplayer,Layer ipLayer){
		double length=0;
		int n=0; 
		boolean brokeflag=false,opworkflag=false,partworkflag=false,RSAflag=false;
		ArrayList<Link> linklist=new ArrayList<Link>();
		
		for(Link link:newRoute.getLinklist()){//判断route的每一段链路长度是否超过最长调制距离
			if(link.getLength()>4000) {
				System.out.println(link.getName()+" 的距离过长 业务堵塞");
				brokeflag=true;
				break;
			}
		}
	
		if(!brokeflag){
		do{
			Node nodeA=newRoute.getNodelist().get(n);
			Node nodeB=newRoute.getNodelist().get(n+1);
			System.out.println(nodeA.getName()+"-"+nodeB.getName());
			
			Link link=oplayer.findLink(nodeA, nodeB);
			length=length+link.getLength();
			if(length<=4000) {
				n=n+1;
				linklist.add(link);
				if(n==newRoute.getNodelist().size()-1)
					partworkflag=modifylinkcapacity(IPflow,length, linklist, oplayer,ipLayer);//为目的节点前的剩余链路进行RSA
			}
			if(length>4000)  {
				length=length-link.getLength();
				partworkflag=modifylinkcapacity(IPflow,length, linklist, oplayer,ipLayer);//此时在n点放置再生器
				length=0;
				RSAflag=true;
				linklist.clear();
			}
			if(!partworkflag&&RSAflag) break;
		}while(n!=newRoute.getNodelist().size()-1);  
		}
		if(partworkflag)  opworkflag=true;
		return opworkflag;

	}
	public boolean modifylinkcapacity(int IPflow,double routelength,ArrayList<Link> linklist,Layer oplayer,Layer iplayer){
		 double X=1;
		 int slotnum=0;
		 boolean opworkflag=false;
		 Node srcnode=new Node(null, 0, null, iplayer, 0, 0);
		 Node desnode=new Node(null, 0, null, iplayer, 0, 0);
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
		
		System.out.println("该链路所需slot数： " + slotnum);
		ArrayList<Integer> index_wave = new ArrayList<Integer>();
		Mymain spa=new Mymain();
		index_wave = spa.spectrumallocationOneRoute(false,null,linklist,slotnum);
		if (index_wave.size() == 0) {
			System.out.println("路径堵塞 ，不分配频谱资源");
		} 
		else{
			opworkflag=true;
			double length1 = 0;
			double cost = 0;
			for (Link link : linklist) {// 物理层的link
				length1 = length1 + link.getLength();
				cost = cost + link.getCost();
				Request request = null;
				ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
				link.setMaxslot(slotnum + link.getMaxslot());
				// System.out.println("链路 " + link.getName() + "的最大slot是： " + link.getMaxslot()+" 可用频谱窗数： "+link.getSlotsindex().size());
			}
			
			for(int num=0;num<iplayer.getNodelist().size()-1;num++){// 在IP层中寻找transparent链路的两端
				boolean srcflag=false,desflag=false;
//				System.out.println(iplayer.getNodelist()..get(0).getName());
				 HashMap<String,Node>map=iplayer.getNodelist();
			     Iterator<String>iter=map.keySet().iterator();
			       while(iter.hasNext()){
			    	   Node node=(Node)(map.get(iter.next()));
			    	   
			    	   if(node.getName().equals(linklist.get(0).getNodeA().getName())){
			    		   srcnode=node;
			    		   srcflag=true;
			    	   }
			    	   if(node.getName().equals(linklist.get(linklist.size()-1).getNodeB().getName())){
			    		   desnode=node;
			    		   desflag=true;
			    	   }
			       }
				if(srcflag&&desflag) break;
			}
			
			String name = srcnode.getName() + "-" +desnode.getName();
			System.out.println(name);
			int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的故这样设置index
		
			Link finlink=iplayer.findLink(srcnode, desnode);
			Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
			boolean findflag=false;
			try{
				System.out.println(finlink.getName());
				findflag=true;
			}catch(java.lang.NullPointerException ex){
				System.out.println("IP 层没有该链路需要新建链路");
				createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
				iplayer.addLink(createlink);
			}
			
			VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
			Vlink.setnature(0);
			Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
			Vlink.setFullcapacity(slotnum * X);// 多出来的flow是从这里产生的
			Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
			Vlink.setlength(length1);
			Vlink.setcost(cost);	
			Vlink.setPhysicallink(linklist);	
	
			if(findflag){//如果在IP层中已经找到该链路
				System.out.println(finlink.getVirtualLinkList().size());
				finlink.getVirtualLinkList().add(Vlink);
				System.out.println(finlink.getVirtualLinkList().size());
				System.out.println("IP层已存在的链路 " + finlink.getName() + " 其对应的虚拟链路上面的已用flow: "
						+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
						+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
						+"   "+"虚拟链路cost： "+ Vlink.getcost());
				System.out.println("*********工作链路在光层新建的链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
				}
				else{
					createlink.getVirtualLinkList().add(Vlink);
					System.out.println(createlink.getVirtualLinkList().size());
					System.out.println("IP层上新建链路 " + createlink.getName() + " 其对应的虚拟链路上面的已用flow: "
							+ Vlink.getUsedcapacity() + "\n "+"共有的flow:  " + Vlink.getFullcapacity()
							+ "    预留的flow：  " + Vlink.getRestcapacity()+"\n"+"虚拟链路长度："+Vlink.getlength()
							+"   "+"虚拟链路cost： "+ Vlink.getcost());
					System.out.println("*********工作链路在光层新建的链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
				}
		}
		return opworkflag;
	}
	/*
	 * 检验小程序 检验IP层里是否已经有某条链路
	 */
	public static void main(String[] args) { 
		Network network = new Network("ip over EON", 0, null);
		network.readPhysicalTopology("G:/Topology/68modifylength.csv");
		network.copyNodes();
		network.createNodepair();// 
		Layer iplayer = network.getLayerlist().get("Layer0");
		Node nodeA=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeB=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeC=new Node(null, 0, null, iplayer, 0, 0);
		Node nodeD=new Node(null, 0, null, iplayer, 0, 0);
		
		System.out.println(iplayer.getNodelist().size());
		 HashMap<String,Node>map=iplayer.getNodelist();
	     Iterator<String>iter=map.keySet().iterator();
	       while(iter.hasNext()){
	    	   Node node=(Node)(map.get(iter.next()));
	    	   if(node.getName().equals("N3"))   nodeA=node;
	    	   if(node.getName().equals("N4"))   nodeB=node;
	    	   if(node.getName().equals("N3"))   nodeC=node;
	    	   if(node.getName().equals("N4"))   nodeD=node;
	       }
	
		String name=nodeA.getName()+"-"+nodeB.getName();
		String name2=nodeC.getName()+"-"+nodeD.getName();
		System.out.println(name+"   "+name2);
		Link newlink=new Link(name, 0, null, iplayer, nodeA, nodeB, 0, 0);
		iplayer.addLink(newlink);
		Link findledink=iplayer.findLink(nodeA, nodeB);
		Link findledink2=iplayer.findLink(nodeC, nodeD);// 找不到link的时候怎么办？？？
		System.out.println(findledink.getName());
//		try{
//			System.out.println(findledink2.getName());
//		}catch(java.lang.NullPointerException ex){
//			System.out.println("IP 层没有该链路");
//		}
		
		VirtualLink Vlink = new VirtualLink(nodeA.getName(), nodeB.getName(), 0, 0);
		Vlink.setnature(0);
		Vlink.setUsedcapacity(100);
		Vlink.setFullcapacity(200);// 多出来的flow是从这里产生的
		Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
		Vlink.setlength(4500);
		Vlink.setcost(0);	

		VirtualLink Vlink2 = new VirtualLink(nodeA.getName(), nodeB.getName(), 0, 0);
		Vlink.setnature(0);
		Vlink.setUsedcapacity(100);
		Vlink.setFullcapacity(200);// 多出来的flow是从这里产生的
		Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
		Vlink.setlength(4500);
		Vlink.setcost(0);	
		
		
		findledink.getVirtualLinkList().add(Vlink);
		findledink.getVirtualLinkList().add(Vlink2);
		System.out.println("虚拟链路条数："+findledink.getVirtualLinkList().size());//一条IP 链路上可以加多个虚拟链路并且这些虚拟链路即使所有因素都一样也不会重复
		
		
	}
}


