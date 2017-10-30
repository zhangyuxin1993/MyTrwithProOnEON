package MainFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import demand.Request;
import general.file_out_put;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import resource.ResourceOnLink;
import subgraph.LinearRoute;

public class RegeneratorPlace {
	public int newFS = 0;
	static int totalregNum = 0;
	String OutFileName =Mymain.OutFileName;
	public boolean regeneratorplace(int IPflow, double routelength, LinearRoute newRoute, Layer oplayer,
			Layer ipLayer,ArrayList<WorkandProtectRoute> wprlist,NodePair nodepair) {  //注意最少再生器个数的计算
		// /*
		// 第二种方法先判断一条路径最少使用的再生器的个数 然后穷尽所有的情况来选择再生器 放置的位置
		file_out_put file_io=new file_out_put();
		int minRegNum = (int) Math.floor(routelength / 4000);// 最少的再生器的个数
		int internode = newRoute.getNodelist().size() - 2;
		int FStotal = 0, n = 0;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();
		boolean partworkflag = false, RSAflag = false, regflag = false, success = false;
		ArrayList<RouteAndRegPlace> regplaceoption = new ArrayList<>();
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 0);
		float threshold=(float) 0.1;//在这里控制阈值
		// 找到所有可以成功路由的路径 part1
		
		for (int s = minRegNum; s <= internode; s++) {
			if (partworkflag||regplaceoption.size()!=0)//如果再生器个数较少的时候已经可以RSA那么就不需要增加再生器的个数
				break;
			Test nOfm = new Test(s, internode); // 在所有中间节点中随机选取m个点来放置再生器
			while (nOfm.hasNext()) {
				RSAflag = false;
				regflag = false;
				partworkflag = false;
				n = 0;
				length = 0;
				FStotal = 0;
				linklist.clear();
				int[] set = nOfm.next(); // 随机产生的再生器放置位置
				ArrayList<Float> RemainRatio=new ArrayList<>();//记录每段链路上剩余的flow
				float NumRemainFlow=0;
				
				for (int i = 0; i < set.length + 1; i++) {// RSA的次数比再生器的个数多1 对某一段链路在某个set再生器下进行RSA
					if (!partworkflag && RSAflag)
						break;
					if (i < set.length){
						System.out.println("****************工作再生器的位置为：" + set[i]); // set里面的数应该是节点的位置+1！
						file_io.filewrite2(OutFileName,"****************工作再生器的位置为：" + set[i]); 
					}
					else {
						System.out.println("************最后一个再生器与终结点之间的RSA ");
						file_io.filewrite2(OutFileName,"************最后一个再生器与终结点之间的RSA ");
						regflag = true;
					}
					do {// 通过一个
						Node nodeA = newRoute.getNodelist().get(n);
						Node nodeB = newRoute.getNodelist().get(n + 1);
						Link link = oplayer.findLink(nodeA, nodeB);
						System.out.println(link.getName());
						file_io.filewrite2(OutFileName,link.getName());
						length = length + link.getLength();
						linklist.add(link);
						n = n + 1;
						if (!regflag) {// 未到达最后一段路径的RSA
							if (n == set[i]) {
								ParameterTransfer pt=new ParameterTransfer();
//								float remainFlow=pt.remainFlow;
								partworkflag = vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,pt);// 此时在n点放置再生器
								RemainRatio.add(pt.getRemainFlowRatio());
								NumRemainFlow=NumRemainFlow+pt.getNumremainFlow();
//								file_io.filewrite2(OutFileName,"链路上剩余的容量比例 "+pt.getRemainFlow());
								FStotal = FStotal + newFS;
								length = 0;
								RSAflag = true;
								linklist.clear();
								break;
							}
						}
						if (n == newRoute.getNodelist().size() - 1) {
							ParameterTransfer pt=new ParameterTransfer();
							partworkflag = vertify(IPflow, length, linklist, oplayer, ipLayer, wprlist, nodepair,pt);// 此时在n点放置再生器
							RemainRatio.add(pt.getRemainFlowRatio());
							NumRemainFlow=NumRemainFlow+pt.getNumremainFlow();
							file_io.filewrite2(OutFileName,"链路上剩余的容量比例 "+pt.getRemainFlowRatio());
							FStotal = FStotal + newFS;
						}
						if (!partworkflag && RSAflag)
							break;
					} while (n != newRoute.getNodelist().size() - 1);
					// 如果路由成功则保存该路由对于再生器的放置
				}
				if (partworkflag) {//在一个特定的set下进行路由结束
					RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 0);
					rarp.setnewFSnum(FStotal);
					ArrayList<Integer> setarray = new ArrayList<>();
					ArrayList<Integer> IPRegarray = new ArrayList<>();
					file_io.filewrite2(OutFileName,"");
					file_io.filewrite_without(OutFileName," 工作链路IP再生器的位置为" );
					file_io.filewrite2(OutFileName," " );
					for (int k = 0; k < set.length; k++) {
						setarray.add(set[k]);
						if(RemainRatio.get(k)>threshold||RemainRatio.get(k+1)>threshold){// 只要再生器前面或者后面有一段未充分使用则放置IP再生器
							IPRegarray.add(set[k]);//存储IP再生器放置节点
							file_io.filewrite_without(OutFileName,IPRegarray.get(k)+"    ");
						}
					}
					file_io.filewrite2(OutFileName," " );
					rarp.setIPRegnode(IPRegarray);
					rarp.setregnode(setarray);
					rarp.setregnum(setarray.size());
					rarp.setNumRemainFlow(NumRemainFlow);
					regplaceoption.add(rarp);
					System.out.println("该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());// 再生器的个数加进去
					file_io.filewrite2(OutFileName,"该路径成功RSA, 已成功RSA的条数为：" + regplaceoption.size());
				}
			}
		}
		// part1 finish

		// 在已经产生的几条链路中选取一条使用FS最少的链路作为最终链路
		if (regplaceoption.size() != 0) {
			success = true;
			ArrayList<RouteAndRegPlace> RemoveRoute= new ArrayList<>(); 
			//确定final route
			//第一层选择 选择IP再生器少的路由
			if(regplaceoption.size()==1){
				finalRoute=regplaceoption.get(0);
			}
			else{
				RouteAndRegPlace StandardRoute=regplaceoption.get(0);
				for(int k=1;k<regplaceoption.size();k++){
					RouteAndRegPlace CompareRoute=regplaceoption.get(k);
					if(StandardRoute.getIPRegnode().size()>CompareRoute.getIPRegnode().size()){
						RemoveRoute.add(StandardRoute);//删去IP再生器多的路径
						StandardRoute=CompareRoute;//如果比较的route比标准的好 则将标准定为比较route
					}
					if(StandardRoute.getIPRegnode().size()<CompareRoute.getIPRegnode().size()){
						RemoveRoute.add(CompareRoute);//比较的没有标准好
					}
				}
			for(RouteAndRegPlace rag:RemoveRoute){
				regplaceoption.remove(rag);
			}
			RemoveRoute.clear();
			//第二层选择 选择IP再生器少的路由
			if(regplaceoption.size()==1){
				finalRoute=regplaceoption.get(0);
			}
			else{
				RouteAndRegPlace StandardRoute_2=regplaceoption.get(0);
				for(int k=1;k<regplaceoption.size();k++){
					RouteAndRegPlace CompareRoute_2=regplaceoption.get(k);
					if(StandardRoute_2.getNumRemainFlow()<CompareRoute_2.getNumRemainFlow()){
						RemoveRoute.add(StandardRoute_2);//删去剩余流量少的路由
						StandardRoute_2=CompareRoute_2;//如果比较的route比标准的好 则将标准定为比较route
					}
					if(StandardRoute_2.getNumRemainFlow()>CompareRoute_2.getNumRemainFlow()){
						RemoveRoute.add(CompareRoute_2);//比较的没有标准好
					}
				}
				for(RouteAndRegPlace rag:RemoveRoute){
					regplaceoption.remove(rag);
				}
				RemoveRoute.clear();
			
				//第三层选择 选择新使用FS较少的路由
				if(regplaceoption.size()==1){
					finalRoute=regplaceoption.get(0);
				}
				else{
					RouteAndRegPlace StandardRoute_3=regplaceoption.get(0);
					for(int k=1;k<regplaceoption.size();k++){
						RouteAndRegPlace CompareRoute_3=regplaceoption.get(k);
						if(StandardRoute_3.getnewFSnum()>CompareRoute_3.getnewFSnum()){
							RemoveRoute.add(StandardRoute_3);//删去使用FS较多的路由
							StandardRoute_3=CompareRoute_3;//如果比较的route比标准的好 则将标准定为比较route
						}
						if(StandardRoute_3.getnewFSnum()<CompareRoute_3.getnewFSnum()){
							RemoveRoute.add(CompareRoute_3);//比较的没有标准好
						}
					}
					for(RouteAndRegPlace rag:RemoveRoute){
						regplaceoption.remove(rag);
					}
					RemoveRoute.clear();
			}
				finalRoute=regplaceoption.get(0);//最终不管是否只剩一条链路 都选择第一条作为最终链路
			
			}
			}
			RegeneratorPlace regp = new RegeneratorPlace();
			regp.FinalRouteRSA(finalRoute, oplayer, ipLayer, IPflow);
		}
		
		if (regplaceoption.size() == 0) {
			success = false;
			System.out.println("该路径被阻塞");
			file_io.filewrite2(OutFileName,"该路径被阻塞");
		}
		System.out.println();
		if (success) {
			System.out.print("再生器放置成功并且RSA,放置的再生器个数为" + finalRoute.getregnum() + "  位置为：");
			file_io.filewrite_without(OutFileName,"再生器放置成功并且RSA,放置的再生器个数为" + finalRoute.getregnum() + "  位置为：");
			for (int p = 0; p < finalRoute.getregnode().size(); p++) {
				System.out.print(finalRoute.getregnode().get(p) + "     ");
				file_io.filewrite_without(OutFileName,finalRoute.getregnode().get(p) + "     ");
			}
			System.out.println();
			totalregNum = totalregNum + finalRoute.getregnum();
			System.out.println("*******工作路径一共需要再生器个数：" + totalregNum);
			file_io.filewrite2(OutFileName,"");
			file_io.filewrite2(OutFileName,"******工作路径一共需要再生器个数：" + totalregNum);
		} else{
			System.out.println("放置再生器不成功改路径被堵塞");
			file_io.filewrite2(OutFileName,"放置再生器不成功改路径被堵塞");
		}
		return success;
		// */
		/*
		 * 第一部分是通过距离来决定在哪里放置再生器 //
		 */
		/*
		 * double length=0; int n=0; boolean
		 * brokeflag=false,opworkflag=false,partworkflag=false,RSAflag=false;
		 * ArrayList<Link> linklist=new ArrayList<Link>();
		 * 
		 * for(Link link:newRoute.getLinklist()){//判断route的每一段链路长度是否超过最长调制距离
		 * if(link.getLength()>4000) { System.out.println(link.getName()+
		 * " 的距离过长 业务堵塞"); brokeflag=true; break; } }
		 * 
		 * if(!brokeflag){ do{ Node nodeA=newRoute.getNodelist().get(n); Node
		 * nodeB=newRoute.getNodelist().get(n+1);
		 * System.out.println(nodeA.getName()+"-"+nodeB.getName());
		 * 
		 * Link link=oplayer.findLink(nodeA, nodeB);
		 * length=length+link.getLength(); if(length<=4000) { n=n+1;
		 * linklist.add(link); if(n==newRoute.getNodelist().size()-1)
		 * partworkflag=modifylinkcapacity(IPflow,length, linklist,
		 * oplayer,ipLayer);//为目的节点前的剩余链路进行RSA totalregNum++; } if(length>4000)
		 * { length=length-link.getLength();
		 * partworkflag=modifylinkcapacity(IPflow,length, linklist,
		 * oplayer,ipLayer);//此时在n点放置再生器 totalregNum++; length=0; RSAflag=true;
		 * linklist.clear(); } if(!partworkflag&&RSAflag) break;
		 * }while(n!=newRoute.getNodelist().size()-1); }
		 * System.out.println("一共需要的再生器个数为："+totalregNum); if(partworkflag)
		 * opworkflag=true; return opworkflag;
		 */

	}
	 
	
	public void FinalRouteRSA(RouteAndRegPlace finalRoute, Layer oplayer, Layer ipLayer, int IPflow) {

		finalRoute.getRoute().OutputRoute_node(finalRoute.getRoute());
		int count = 0;
		double length2 = 0;
		boolean regflag2 = false;
		ArrayList<Link> linklist2 = new ArrayList<>();
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		
		for (int i = 0; i < finalRoute.getregnum() + 1; i++) {
			if (i >= finalRoute.getregnum())
				regflag2 = true;
			do {
				Node nodeA = finalRoute.getRoute().getNodelist().get(count);
				Node nodeB = finalRoute.getRoute().getNodelist().get(count + 1);
				Link link = oplayer.findLink(nodeA, nodeB);
				System.out.println();
				file_io.filewrite2(OutFileName,"");
				System.out.println("新建工作链路RSA：" + link.getName());
				file_io.filewrite2(OutFileName,"新建工作链路RSA：" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// 未到达最后一段路径的RSA
					if (count == finalRoute.getregnode().get(i)) {
						modifylinkcapacity(IPflow, length2, linklist2, oplayer, ipLayer);// 此时在n点放置再生器
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					modifylinkcapacity(IPflow, length2, linklist2, oplayer, ipLayer);// 此时在n点放置再生器
					linklist2.clear();
				}
			} while (count != finalRoute.getRoute().getNodelist().size() - 1);
		}
	
	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			 ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair,ParameterTransfer RemainRatio) {
	//判断某一段transparent链路是否能够成功RSA 并且记录新使用的FS数量
 
		double X = 1;
		int slotnum = 0;
		file_out_put file_io=new file_out_put();
		boolean opworkflag = false;
		if (routelength > 4000) {
			System.out.println("链路过长无法RSA");
			file_io.filewrite2(OutFileName,"链路过长无法RSA");
		}
		if (routelength < 4000) {
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

			newFS = slotnum * linklist.size();
	 
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
				file_io.filewrite2(OutFileName,"路径堵塞 ，不分配频谱资源");
			} else {
				RemainRatio.setRemainFlowRatio(  (float) ((slotnum*X-IPflow) / (slotnum*X)) );
				RemainRatio.setNumremainFlow((float) (slotnum*X-IPflow));
				file_io.filewrite2(OutFileName,"建立通道的总容量 "+slotnum*X+" 需要的容量 "+ IPflow+ "剩余的容量比例 "+ RemainRatio.getRemainFlowRatio() );
				opworkflag = true;
				System.out.println("可以进行RSA ");
				file_io.filewrite2(OutFileName,"可以进行RSA");
			}
		}
		return opworkflag;
	}

	public boolean modifylinkcapacity(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer,Layer iplayer) {
		double X = 1;
		int slotnum = 0;
		boolean opworkflag = false;
		file_out_put file_io=new file_out_put();
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		if (routelength > 4000) {
			System.out.println("链路过长无法RSA");
			file_io.filewrite2(OutFileName,"链路过长无法RSA");
		}
		if (routelength < 4000) {
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
			file_io.filewrite2(OutFileName,"该链路所需slot数： " + slotnum);
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("路径堵塞 ，不分配频谱资源");
				file_io.filewrite2(OutFileName,"路径堵塞 ，不分配频谱资源");
			} else {
				opworkflag = true;
				double length1 = 0;
				double cost = 0;
				file_io.filewrite2(OutFileName,"");
				file_io.filewrite2(OutFileName,"工作链路放置再生器时在光层分配频谱：");
				file_io.filewrite2(OutFileName,"FS起始值："+index_wave.get(0)+"  长度"+slotnum);
				for (Link link : linklist) {// 物理层的link
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
					link.setMaxslot(slotnum + link.getMaxslot());
					 System.out.println();
					 System.out.println("链路 " + link.getName() + "的最大slot是： "+ link.getMaxslot()+" 可用频谱窗数："
					+link.getSlotsindex().size());
				}

				Node startnode=new Node(null, 0, null, iplayer, 0, 0);
				Node endnode=new Node(null, 0, null, iplayer, 0, 0);
				if(linklist.size()!=1){
					Link link1=linklist.get(0);  Link link2=linklist.get(1);
					Link link3=linklist.get(linklist.size()-2);  Link link4=linklist.get(linklist.size()-1);
					Node nodeA=link1.getNodeA(); Node nodeB=link1.getNodeB();
					Node nodeC=link4.getNodeA(); Node nodeD=link4.getNodeB();
					file_io.filewrite2(OutFileName,"取出的链路为"+link1.getName()+"  "+link2.getName()+"   "+link3.getName()+"  "+link4.getName());
					
					if(link2.getNodeA().equals(nodeA)||link2.getNodeB().equals(nodeA)) startnode=nodeB;
					if(link2.getNodeA().equals(nodeB)||link2.getNodeB().equals(nodeB)) startnode=nodeA;//找到起始端点
					if(link3.getNodeA().equals(nodeC)||link3.getNodeB().equals(nodeC)) endnode=nodeD;
					if(link3.getNodeA().equals(nodeD)||link3.getNodeB().equals(nodeD)) endnode=nodeC;//找到终止端点
					file_io.filewrite2(OutFileName,"找到的节点："+startnode.getName()+"  "+endnode.getName());
				}
				if(linklist.size()==1){
					startnode=linklist.get(0).getNodeA();
					endnode=linklist.get(0).getNodeB();
				}
				
				for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// 在IP层中寻找transparent链路的两端
					boolean srcflag = false, desflag = false;
					// System.out.println(iplayer.getNodelist()..get(0).getName());
					HashMap<String, Node> map = iplayer.getNodelist();
					Iterator<String> iter = map.keySet().iterator();
					while (iter.hasNext()) {
						Node node = (Node) (map.get(iter.next()));

						if (node.getName().equals(startnode.getName())) {
							srcnode = node;
							srcflag = true;
						}
						if (node.getName().equals(endnode.getName())) {
							desnode = node;
							desflag = true;
						}
					}
					if (srcflag && desflag)
						break;
				}
//				file_io.filewrite2(OutFileName,"src的节点度:"+srcnode.getIndex()+"  des节点度："+desnode.getIndex());
				if(srcnode.getIndex()>desnode.getIndex()){
					Node internode=srcnode;
					srcnode=desnode;
					desnode=internode;
				}
//				file_io.filewrite2(OutFileName,"此时的原节点为:"+srcnode.getName()+"  终结点为"+desnode.getName());
				String name = srcnode.getName() + "-" + desnode.getName();
				int index = iplayer.getLinklist().size();// 因为iplayer里面的link是一条一条加上去的故这样设置index

				Link finlink = iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag = false;
				try {
					System.out.println(finlink.getName());
					file_io.filewrite2(OutFileName,finlink.getName());
					findflag = true;
				} catch (java.lang.NullPointerException ex) {
					System.out.println("IP 层没有该链路需要新建链路");
					file_io.filewrite2(OutFileName,"IP 层没有该链路需要新建链路");
					file_io.filewrite2(OutFileName,"建立的新链路为:"+srcnode.getName()+" "+desnode.getName());
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

				if (findflag) {// 如果在IP层中已经找到该链路
					finlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP层已存在的链路 " + finlink.getName() + "\n " + "    预留的flow：  " + Vlink.getRestcapacity());
					file_io.filewrite2(OutFileName,"IP层已存在的链路 " + finlink.getName() + "\n " + "    预留的flow：  " + Vlink.getRestcapacity());
					System.out.println("工作链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
					file_io.filewrite2(OutFileName,"工作链路在光层新建的链路：  " + finlink.getName() + "  上的虚拟链路条数： "+ finlink.getVirtualLinkList().size());
				} else {
					createlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
					System.out.println("工作链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
					file_io.filewrite2(OutFileName,"IP层上新建链路 " + createlink.getName() + "    预留的flow：  " + Vlink.getRestcapacity());
					file_io.filewrite2(OutFileName,"工作链路在光层新建的链路：  " + createlink.getName() + "  上的虚拟链路条数： "+ createlink.getVirtualLinkList().size());
				}
			}
		}
		return opworkflag;
	}

}
