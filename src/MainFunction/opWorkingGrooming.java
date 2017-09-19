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
//			Link link1 = (Link) (linklist2.get(linkitor2.next()));// IPlayer�����link
//			System.out.println("IP LINK:"+link1.getName()+"��·�����������·����"+link1.getVirtualLinkList().size());
//		 
//		}
		 
		System.out.println("IP�㹤��·�ɲ��ɹ�����Ҫ�½���·");
		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());
		 System.out.println("Դ�㣺 " + opsrcnode.getName() + " �յ㣺 " +opdesnode.getName());

		// �ڹ���½���·��ʱ����Ҫ��������������
		 
		Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opnewRoute, null);

		if (opnewRoute.getLinklist().size() == 0) {
			System.out.println("������·��");
		} else {
			System.out.print("�������·��Ϊ��------");
			opnewRoute.OutputRoute_node(opnewRoute);

			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000��8QAM,0-500 16QAM
			double routelength = opnewRoute.getlength();
			// System.out.println("����·���ĳ����ǣ�"+routelength);
			// ͨ��·���ĳ������仯���Ƹ�ʽ �����ж������� ��ʹ��

			if(routelength<4000){//�ҵ���·������Ҫ�������Ϳ���ֱ��ʹ��
			if (routelength > 2000 && routelength <= 4000) {
				X = 12.5;
			} else if (routelength > 1000 && routelength <= 2000) {
				X = 25.0;
			} else if (routelength > 500 && routelength <= 1000) {
				X = 37.5;
			} else if (routelength > 0 && routelength <= 500) {
				X = 50.0;
			}
			slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��

			opnewRoute.setSlotsnum(slotnum);
			System.out.println("����·����slot���� " + slotnum);
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa=new Mymain();
			index_wave = spa.spectrumallocationOneRoute(true,opnewRoute,null,slotnum);
			if (index_wave.size() == 0) {
				System.out.println("·������ ��������Ƶ����Դ");
			} else {
				opworkflag=true;
				double length1 = 0;
				double cost = 0;

				for (Link link : opnewRoute.getLinklist()) {// ������link
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("��· " + link.getName() + "�����slot�ǣ� " + link.getMaxslot()+" ����Ƶ�״����� "+link.getSlotsindex().size());
				} // �ı�������ϵ���·���� �Ա�����һ���½�ʱ����slot
				String name = opsrcnode.getName() + "-" + opdesnode.getName();
				int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ��
															// ����������index
				
				Link finlink=iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag=false;
				try{
					System.out.println("IP�����ҵ���·"+finlink.getName());
					findflag=true;
				}catch(java.lang.NullPointerException ex){
					System.out.println("IP ��û�и���·��Ҫ�½���·");
					createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
					iplayer.addLink(createlink);
				}
				
				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
				Vlink.setnature(0);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
				Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);
				Vlink.setPhysicallink(opnewRoute.getLinklist());	
				
				if(findflag){//�����IP�����Ѿ��ҵ�����·
				System.out.println(finlink.getVirtualLinkList().size());
				finlink.getVirtualLinkList().add(Vlink);
				System.out.println(finlink.getVirtualLinkList().size());
				System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() + " ���Ӧ��������·���������flow: "
						+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
						+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
						+"   "+"������·cost�� "+ Vlink.getcost());
				System.out.println("*********������·�ڹ���½�����·��  "+finlink.getName()+"  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
				}
				else{
					createlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP�����½���· " + createlink.getName() + " ���Ӧ��������·���������flow: "
							+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
							+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
							+"   "+"������·cost�� "+ Vlink.getcost());
					System.out.println("*********������·�ڹ���½�����·��  "+createlink.getName()+"  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
				
				}
				
				
//				numOfTransponder = numOfTransponder + 2;
			
			}
				
				//debug
//				HashMap<String, Link> linklist3 = iplayer.getLinklist();
//				Iterator<String> linkitor3 = linklist3.keySet().iterator();
//				while (linkitor3.hasNext()) {
//					Link link1 = (Link) (linklist3.get(linkitor3.next()));// IPlayer�����link
//					System.out.println("IP LINK:"+link1.getName()+"��·�����������·����"+link1.getVirtualLinkList().size());
//					for(VirtualLink link:link1.getVirtualLinkList()){
//						System.out.println(link.getSrcnode()+" "+link.getDesnode());
//					}
//				}
			}
			if(routelength>4000){
				RegeneratorPlace  regplace=new RegeneratorPlace();
				opworkflag=regplace.regeneratorplace( IPflow,routelength, opnewRoute, oplayer,iplayer);
			}
		}
		return opworkflag;
	}
	}

 
