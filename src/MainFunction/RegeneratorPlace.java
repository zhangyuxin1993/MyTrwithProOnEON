package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import network.Layer;
import network.Link;
import network.Node;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class RegeneratorPlace {
	public int newFS=0;
	public boolean regeneratorplace(int IPflow,double routelength,LinearRoute newRoute,Layer oplayer,Layer ipLayer){
		//第二种方法先判断一条路径最少使用的再生器的个数 然后穷尽所有的情况来选择再生器 放置的位置
		 
		int minRegNum=(int) Math.floor(routelength/4000);//最少的再生器的个数
		int internode=newRoute.getNodelist().size()-2;
		int FStotal=0,n=0;
		double length=0;
		ArrayList<Link> linklist=new ArrayList<>();
		boolean partworkflag=false,RSAflag=false,regflag=false,success=false;
		ArrayList<RouteAndRegPlace> regplaceoption=new ArrayList<>();
		RouteAndRegPlace finalRoute=new RouteAndRegPlace(null, 0);
		
		for(int s=minRegNum;s<=internode;s++){
			if(partworkflag) break;
		Test nOfm = new Test(s,internode);  //在所有中间节点中随机选取m个点来放置再生器
	    while (nOfm.hasNext()) { 
	    	RSAflag=false; regflag=false;partworkflag=false;n=0;length=0;FStotal=0;linklist.clear();
	            int[] set = nOfm.next();   //随机产生的再生器放置位置
	            for (int i = 0; i < set.length+1; i++) {//RSA的次数比再生器的个数多1  
	            	if(!partworkflag&&RSAflag) break;
	            	if(i<set.length)
	            	System.out.println("****************再生器的位置为："+set[i]);  //set里面的数应该是节点的位置+1！
	                else {
	                	System.out.println("************最后一个再生器与终结点之间的RSA ");
	                	regflag=true;
	                }
	                do{//通过一个
	                	Node nodeA=newRoute.getNodelist().get(n);
	                	Node nodeB=newRoute.getNodelist().get(n+1);
	                	Link link=oplayer.findLink(nodeA, nodeB);
	                	System.out.println(link.getName());
	        			length=length+link.getLength();
	        			linklist.add(link);
	        			n=n+1;
	        			if(!regflag){//未到达最后一段路径的RSA
	                	if(n!=set[i]){
	        				if(n==newRoute.getNodelist().size()-1){
	        					partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//为目的节点前的剩余链路进行RSA
	        					FStotal=FStotal+newFS;
	        				}
	                	}
	                	if(n==set[i]){
//	                		length=length-link.getLength();
	        				partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//此时在n点放置再生器
	        				FStotal=FStotal+newFS;
	        				length=0;
	        				RSAflag=true;
	        				linklist.clear();
	        				break;
	                	}
	        			}
	                if(n==newRoute.getNodelist().size()-1){
	                	partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//此时在n点放置再生器
	                	FStotal=FStotal+newFS;
	                }
	                	if(!partworkflag&&RSAflag) break;
	                }while(n!=newRoute.getNodelist().size()-1);  
	            //如果路由成功则保存该路由对于再生器的放置
	            }  
	            if(partworkflag) {
	            	RouteAndRegPlace rarp=new RouteAndRegPlace(newRoute, 0);
	            	rarp.setnewFSnum(FStotal);
	            	ArrayList<Integer> setarray=new ArrayList<>();
	            	for(int k=0;k<set.length;k++){
	            		setarray.add(set[k]);
	            	}
	            	rarp.setregnode(setarray);
	            	rarp.setregnum(setarray.size());
	            	regplaceoption.add(rarp);
	            	System.out.println("该路径成功RSA, 已成功RSA的条数为："+regplaceoption.size());//再生器的个数加进去
	            }
	        }
		}
		//在已经产生的几条链路中选取一条使用FS最少的链路作为最终链路
		if(regplaceoption.size()!=0){
			success=true;
			int FS=10000;
			for(RouteAndRegPlace route: regplaceoption){
				if(route.getnewFSnum()<FS){
					FS=route.getnewFSnum();
					finalRoute=route;//这是最终选择的再生器放置的地点 接下来要对该条路径结合其再生器放置位置进行容量分配~
				}
			}
		finalRoute.getRoute().OutputRoute_node(finalRoute.getRoute());
		int count=0;
		double length2=0;
		boolean regflag2=false;
		ArrayList<Link> linklist2=new ArrayList<>();
		for (int i = 0; i <finalRoute.getregnum()+1; i++) {
				if(i>=finalRoute.getregnum())
					regflag2=true;
			do{
				Node nodeA=finalRoute.getRoute().getNodelist().get(count);
				Node nodeB=finalRoute.getRoute().getNodelist().get(count+1);
				Link link=oplayer.findLink(nodeA, nodeB);
				System.out.println("找到的链路名字："+link.getName());
				length2=length2+link.getLength();
				linklist2.add(link);
				count=count+1;
    			if(!regflag2){//未到达最后一段路径的RSA
            	if(count!=finalRoute.getregnode().get(i)){
    				if(count==finalRoute.getRoute().getNodelist().size()-1){
    					modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//为目的节点前的剩余链路进行RSA
    					linklist2.clear();
            	}
    			}	
            	if(count==finalRoute.getregnode().get(i)){
            		modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//此时在n点放置再生器
    				length2=0;
    				linklist2.clear();
    				break;
            	}
    			}
    			  if(count==finalRoute.getRoute().getNodelist().size()-1){
    				  modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//此时在n点放置再生器
    				  linklist2.clear();
	                }
			}while(count!=finalRoute.getRoute().getNodelist().size()-1);
		}
		}
		
		if(regplaceoption.size()==0){
			success=false;
			System.out.println("该路径被阻塞");
		}
		System.out.println();
		if(success)   {
			System.out.print("再生器放置成功并且RSA,放置的再生器个数为"+ finalRoute.getregnum()+"  位置为：");
			for(int p=0;p<finalRoute.getregnode().size();p++){
				System.out.print(finalRoute.getregnode().get(p)+"     ");
			}
		}
		
		else  System.out.println("放置再生器不成功改路径被堵塞");
		return success;
		/*
		 * 第一部分是通过距离来决定在哪里放置再生器 
		 */
		/*
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
*/
		
		
		
	}

public Boolean vertify(int IPflow,double routelength,ArrayList<Link> linklist,Layer oplayer,Layer iplayer){
	 double X=1;
	 int slotnum=0;
	 boolean opworkflag=false;
	 if(routelength>4000){
		 System.out.println("链路过长无法RSA");
	 }
	 if(routelength<4000){
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
	newFS=slotnum;
	ArrayList<Integer> index_wave = new ArrayList<Integer>();
	Mymain spa=new Mymain();
	index_wave = spa.spectrumallocationOneRoute(false,null,linklist,slotnum);
	if (index_wave.size() == 0) {
		System.out.println("路径堵塞 ，不分配频谱资源");
	} 
	else{
		opworkflag=true;
		System.out.println("可以进行RSA");
	}
	 }
	return opworkflag;
}
public boolean modifylinkcapacity(int IPflow,double routelength,ArrayList<Link> linklist,Layer oplayer,Layer iplayer ){
		 double X=1;
		 int slotnum=0;
		 boolean opworkflag=false;
		 Node srcnode=new Node(null, 0, null, iplayer, 0, 0);
		 Node desnode=new Node(null, 0, null, iplayer, 0, 0);
		 if(routelength>4000){
			 System.out.println("链路过长无法RSA");
		 }
		 if(routelength<4000){
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
		newFS=slotnum;
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
//				System.out.println(finlink.getVirtualLinkList().size());
				finlink.getVirtualLinkList().add(Vlink);
//				System.out.println(finlink.getVirtualLinkList().size());
				System.out.println("IP层已存在的链路 " + finlink.getName() +"\n "+"    预留的flow：  " + Vlink.getRestcapacity());
				System.out.println("工作链路在光层新建的链路：  "+finlink.getName()+"  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
				}
				else{
					createlink.getVirtualLinkList().add(Vlink);
//					System.out.println(createlink.getVirtualLinkList().size());
					System.out.println("IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
					System.out.println("工作链路在光层新建的链路：  "+createlink.getName()+"  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
				}
		}
	}
		 return opworkflag;
}


	
}


