package MainFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

public class opProGrooming {// ���·�ɱ���
	String OutFileName =Mymain.OutFileName;
	public boolean opprotectiongrooming(Layer iplayer, Layer oplayer, NodePair nodepair, LinearRoute route,
			int numOfTransponder, boolean flag,ArrayList<WorkandProtectRoute> wprlist) throws IOException {// flag=true��ʾ����IP�㽨���Ĺ���·��
													// flag=flase��ʾ��㽨���Ĺ���·��
		RouteSearching Dijkstra = new RouteSearching();
		Node srcnode = nodepair.getSrcNode();
		Node desnode = nodepair.getDesNode();
		boolean success=false;
		double routelength = 0;
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		ArrayList<VirtualLink> provirtuallinklist=new ArrayList<>();
		HashMap<Link, Integer> FSuseOnlink=new  HashMap<Link, Integer>();
		ArrayList<Link> opDelLink = new ArrayList<Link>();
		System.out.println("************����·����IP�㲻��·�ɣ���Ҫ�ڹ���½�");
		file_io.filewrite2(OutFileName,"************����·����IP�㲻��·�ɣ���Ҫ�ڹ���½�");
		
		// ɾ���ýڵ�ԵĹ���·�ɾ���������������·
		for (Link LinkOnRoute : route.getLinklist()) {// ȡ������·���е���·
//			System.out.println("����·����·��" + LinkOnRoute.getName());
			if (flag) {//// flag=true��ʾ���� IP�㽨���Ĺ���·��
				for (VirtualLink Vlink : LinkOnRoute.getVirtualLinkList()) {
					for (Link LinkOnPhy : Vlink.getPhysicallink()) {// ȡ��ĳһ������·�϶�Ӧ��������·

						HashMap<String, Link> oplinklist = oplayer.getLinklist();
						Iterator<String> oplinkitor = oplinklist.keySet().iterator();
						while (oplinkitor.hasNext()) {
							Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
							// System.out.println("�������·������" +oplink.getName());
							if (oplink.getName().equals(LinkOnPhy.getName())) {
								if (!opDelLink.contains(oplink))
									opDelLink.add(oplink);
								break;
							}
						}
					}
				}
			} else {// flag=false��ʾ���� ��㽨���Ĺ���·��
				HashMap<String, Link> oplinklist = oplayer.getLinklist();
				Iterator<String> oplinkitor = oplinklist.keySet().iterator();
				while (oplinkitor.hasNext()) {
					Link oplink = (Link) (oplinklist.get(oplinkitor.next()));
					// System.out.println("�������·������" + oplink.getName());
					if (oplink.getName().equals(LinkOnRoute.getName())) {
//						System.out.println("ɾ���Ĺ����·�� " + oplink.getName());
						opDelLink.add(oplink);
						break;
					}
				}
			}
		}
		// ����Ϊ��һ���� ɾ����������й�����·������������·

		for (Link opdellink : opDelLink) {
			oplayer.removeLink(opdellink.getName());
		}

		Node opsrcnode = oplayer.getNodelist().get(srcnode.getName());
		Node opdesnode = oplayer.getNodelist().get(desnode.getName());

		LinearRoute opPrtectRoute = new LinearRoute(null, 0, null);
		Dijkstra.Dijkstras(opsrcnode, opdesnode, oplayer, opPrtectRoute, null);// ��iplayer������Ѱ��̱���·��

		for (Link opdellink : opDelLink) {
			oplayer.addLink(opdellink);
		} // �ָ�oplayer�����link
		opDelLink.clear();

		if (opPrtectRoute.getLinklist().size() == 0) {
			System.out.println("����·�ɹ���޷�����");
			file_io.filewrite2(OutFileName,"����·�ɹ���޷�����");
		} else {
			System.out.println("����ҵ�·��:");
			file_io.filewrite2(OutFileName,"����ҵ�·��:");
			opPrtectRoute.OutputRoute_node(opPrtectRoute);
			LinearRoute route_out=new LinearRoute(null, 0, null);
			route_out.OutputRoute_node(opPrtectRoute, OutFileName);
			int slotnum = 0;
			int IPflow = nodepair.getTrafficdemand();
			double X = 1;// 2000-4000 BPSK,1000-2000
							// QBSK,500-1000��8QAM,0-500 16QAM
			
			for(Link link:opPrtectRoute.getLinklist()){
				routelength=routelength+link.getLength();
			}
			// System.out.println("����·���ĳ����ǣ�"+routelength);
			// ͨ��·���ĳ������仯���Ƹ�ʽ
			if (routelength <= 4000) {
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

				opPrtectRoute.setSlotsnum(slotnum);
				System.out.println("����·����slot���� " + slotnum);
				file_io.filewrite2(OutFileName,"����·����slot���� " + slotnum);
				ArrayList<Integer> index_wave = new ArrayList<Integer>();
				Mymain mm = new Mymain();
				index_wave = mm.spectrumallocationOneRoute(true, opPrtectRoute, null, slotnum);
				if (index_wave.size() == 0) {
					System.out.println("·������ ��������Ƶ����Դ");
					file_io.filewrite2(OutFileName,"·������ ��������Ƶ����Դ");
				} else {
					success=true;
					double length = 0;
					double cost = 0;
					for (Link link : opPrtectRoute.getLinklist()) {
						length = length + link.getLength();
						cost = cost + link.getCost();
						Request request = null;
						ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
						FSuseOnlink.put(link, slotnum);
						link.setMaxslot(slotnum + link.getMaxslot());
						// System.out.println("��· " + link.getName() + " �����slot�ǣ� " + link.getMaxslot()+" ����Ƶ�״�����
						// "+link.getSlotsindex().size());
					}
					
					String name = opsrcnode.getName() + "-" + opdesnode.getName();
					int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ��
																// ����������index
					Link finlink=iplayer.findLink(srcnode, desnode);
					Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
					boolean findflag=false;
					try{
						System.out.println("IP�����ҵ���·"+finlink.getName());
						file_io.filewrite2(OutFileName,"IP�����ҵ���·"+finlink.getName());
						findflag=true;
					}catch(java.lang.NullPointerException ex){
						System.out.println("IP ��û�и���·��Ҫ�½���·");
						file_io.filewrite2(OutFileName,"IP ��û�и���·��Ҫ�½���·");
						createlink = new Link(name, index, null, iplayer, srcnode, desnode, length, cost);
						iplayer.addLink(createlink);
					}
					
					VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 1, 0);
					Vlink.setnature(1);
					Vlink.setlength(length);
					Vlink.setcost(cost);
					Vlink.setUsedcapacity(Vlink.getUsedcapacity() + nodepair.getTrafficdemand());
					Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
					Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
					Vlink.setPhysicallink(opPrtectRoute.getLinklist());
					provirtuallinklist.add(Vlink);
//					numOfTransponder = numOfTransponder + 2;

					if(findflag){//�����IP�����Ѿ��ҵ�����·
						System.out.println("������·������"+finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName,"������·������"+finlink.getVirtualLinkList().size());
						finlink.getVirtualLinkList().add(Vlink);
						System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() + " �����µı���������· ���������flow: "
								+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
								+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
								+"   "+"������·cost�� "+ Vlink.getcost());
						file_io.filewrite2(OutFileName,"IP���Ѵ��ڵ���· " + finlink.getName() + " �����µı���������· ���������flow: "
								+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
								+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
								+"   "+"������·cost�� "+ Vlink.getcost());
						System.out.println("*********�Ѵ���IP����·��  "+finlink.getName()+"  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
						file_io.filewrite2(OutFileName,"*********�Ѵ���IP����·��  "+finlink.getName()+"  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
					}
						else{
							System.out.println("������·������"+createlink.getVirtualLinkList().size());
							file_io.filewrite2(OutFileName,"������·������"+createlink.getVirtualLinkList().size());
							createlink.getVirtualLinkList().add(Vlink);
							System.out.println("IP�����½���· " + createlink.getName() + " �����µı���������· ���������flow: "
									+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
									+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
									+"   "+"������·cost�� "+ Vlink.getcost());
							file_io.filewrite2(OutFileName,"IP�����½���· " + createlink.getName() + " �����µı���������· ���������flow: "
									+ Vlink.getUsedcapacity() + "\n "+"���е�flow:  " + Vlink.getFullcapacity()
									+ "    Ԥ����flow��  " + Vlink.getRestcapacity()+"\n"+"������·���ȣ�"+Vlink.getlength()
									+"   "+"������·cost�� "+ Vlink.getcost());
							System.out.println("*********�½�IP��·��  "+createlink.getName()+"  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
							file_io.filewrite2(OutFileName,"*********�½�IP��·��  "+createlink.getName()+"  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
						}
				}
			}
			if (routelength > 4000) {
				ProregeneratorPlace rgp=new ProregeneratorPlace();
				success=rgp.proregeneratorplace(nodepair, opPrtectRoute, wprlist, routelength, oplayer, iplayer, IPflow);
			}
		}
		 for(WorkandProtectRoute wpr0:wprlist){
			 if(wpr0.getdemand().equals(nodepair)){
				wpr0.setproroute(opPrtectRoute);  
			 }
		 }
		if(success&&routelength<4000) {
		 for(WorkandProtectRoute wpr:wprlist){
			 if(wpr.getdemand().equals(nodepair)){
				 ArrayList<Link> totallink=new ArrayList<>();
				totallink=opPrtectRoute.getLinklist();
				wpr.setprolinklist(totallink);
				wpr.setFSuseOnlink(FSuseOnlink);
				wpr.setprovirtuallinklist(provirtuallinklist);
				wpr.setregthinglist(null);
			 }
		 }
		}
		return success;
	}
}
