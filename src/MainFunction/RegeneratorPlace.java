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
			Layer ipLayer,ArrayList<WorkandProtectRoute> wprlist,NodePair nodepair) {
		// /*
		// �ڶ��ַ������ж�һ��·������ʹ�õ��������ĸ��� Ȼ������е������ѡ�������� ���õ�λ��
		// int totalregNum=Mymain.totalregNum;
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		int minRegNum = (int) Math.floor(routelength / 4000);// ���ٵ��������ĸ���
		int internode = newRoute.getNodelist().size() - 2;
		int FStotal = 0, n = 0;
		double length = 0;
		ArrayList<Link> linklist = new ArrayList<>();
		boolean partworkflag = false, RSAflag = false, regflag = false, success = false;
		ArrayList<RouteAndRegPlace> regplaceoption = new ArrayList<>();
		RouteAndRegPlace finalRoute = new RouteAndRegPlace(null, 0);
		
		// �ҵ����п��Գɹ�·�ɵ�·�� part1
		for (int s = minRegNum; s <= internode; s++) {
			if (partworkflag)
				break;
			Test nOfm = new Test(s, internode); // �������м�ڵ������ѡȡm����������������
			while (nOfm.hasNext()) {
				RSAflag = false;
				regflag = false;
				partworkflag = false;
				n = 0;
				length = 0;
				FStotal = 0;
				linklist.clear();
				int[] set = nOfm.next(); // �������������������λ��
				for (int i = 0; i < set.length + 1; i++) {// RSA�Ĵ������������ĸ�����1
					if (!partworkflag && RSAflag)
						break;
					if (i < set.length){
						System.out.println("****************��������λ��Ϊ��" + set[i]); // set�������Ӧ���ǽڵ��λ��+1��
						file_io.filewrite2(OutFileName,"****************��������λ��Ϊ��" + set[i]); 
					}
					else {
						System.out.println("************���һ�����������ս��֮���RSA ");
						file_io.filewrite2(OutFileName,"************���һ�����������ս��֮���RSA ");
						regflag = true;
					}
					do {// ͨ��һ��
						Node nodeA = newRoute.getNodelist().get(n);
						Node nodeB = newRoute.getNodelist().get(n + 1);
						Link link = oplayer.findLink(nodeA, nodeB);
						System.out.println(link.getName());
						file_io.filewrite2(OutFileName,link.getName());
						length = length + link.getLength();
						linklist.add(link);
						n = n + 1;
						if (!regflag) {// δ�������һ��·����RSA
							if (n != set[i]) {
								if (n == newRoute.getNodelist().size() - 1) {
									partworkflag = vertify(IPflow, length, linklist, oplayer, ipLayer, true, wprlist, nodepair);// ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
									FStotal = FStotal + newFS;
								}
							}
							if (n == set[i]) {
								// length=length-link.getLength();
								partworkflag = vertify(IPflow, length, linklist, oplayer, ipLayer, true, wprlist, nodepair);// ��ʱ��n�����������
								FStotal = FStotal + newFS;
								length = 0;
								RSAflag = true;
								linklist.clear();
								break;
							}
						}
						if (n == newRoute.getNodelist().size() - 1) {
							partworkflag = vertify(IPflow, length, linklist, oplayer, ipLayer, true, wprlist, nodepair);// ��ʱ��n�����������
							FStotal = FStotal + newFS;
						}
						if (!partworkflag && RSAflag)
							break;
					} while (n != newRoute.getNodelist().size() - 1);
					// ���·�ɳɹ��򱣴��·�ɶ����������ķ���
				}
				if (partworkflag) {
					RouteAndRegPlace rarp = new RouteAndRegPlace(newRoute, 0);
					rarp.setnewFSnum(FStotal);
					ArrayList<Integer> setarray = new ArrayList<>();
					for (int k = 0; k < set.length; k++) {
						setarray.add(set[k]);
					}
					rarp.setregnode(setarray);
					rarp.setregnum(setarray.size());
					regplaceoption.add(rarp);
					System.out.println("��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());// �������ĸ����ӽ�ȥ
					file_io.filewrite2(OutFileName,"��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��" + regplaceoption.size());
				}
			}
		}
		// part1 finish

		// ���Ѿ������ļ�����·��ѡȡһ��ʹ��FS���ٵ���·��Ϊ������·
		if (regplaceoption.size() != 0) {
			success = true;
			int FS = 10000;
			for (RouteAndRegPlace route : regplaceoption) {
				if (route.getnewFSnum() < FS) {
					FS = route.getnewFSnum();
					finalRoute = route;// ��������ѡ������������õĵص�
										// ������Ҫ�Ը���·�����������������λ�ý�����������~
				}
			}
			RegeneratorPlace regp = new RegeneratorPlace();
			regp.FinalRouteRSA(finalRoute, oplayer, ipLayer, IPflow);
			
		}
		if (regplaceoption.size() == 0) {
			success = false;
			System.out.println("��·��������");
			file_io.filewrite2(OutFileName,"��·��������");
		}
		System.out.println();
		if (success) {
			System.out.print("���������óɹ�����RSA,���õ�����������Ϊ" + finalRoute.getregnum() + "  λ��Ϊ��");
			file_io.filewrite_without(OutFileName,"���������óɹ�����RSA,���õ�����������Ϊ" + finalRoute.getregnum() + "  λ��Ϊ��");
			for (int p = 0; p < finalRoute.getregnode().size(); p++) {
				System.out.print(finalRoute.getregnode().get(p) + "     ");
				file_io.filewrite_without(OutFileName,finalRoute.getregnode().get(p) + "     ");
			}
			totalregNum = totalregNum + finalRoute.getregnum();
			System.out.println("����·��һ����Ҫ������������" + totalregNum);
			file_io.filewrite2(OutFileName,"����·��һ����Ҫ������������" + totalregNum);
		} else{
			System.out.println("�������������ɹ���·��������");
			file_io.filewrite2(OutFileName,"�������������ɹ���·��������");
		}
		return success;
		// */
		/*
		 * ��һ������ͨ��������������������������� //
		 */
		/*
		 * double length=0; int n=0; boolean
		 * brokeflag=false,opworkflag=false,partworkflag=false,RSAflag=false;
		 * ArrayList<Link> linklist=new ArrayList<Link>();
		 * 
		 * for(Link link:newRoute.getLinklist()){//�ж�route��ÿһ����·�����Ƿ񳬹�����ƾ���
		 * if(link.getLength()>4000) { System.out.println(link.getName()+
		 * " �ľ������ ҵ�����"); brokeflag=true; break; } }
		 * 
		 * if(!brokeflag){ do{ Node nodeA=newRoute.getNodelist().get(n); Node
		 * nodeB=newRoute.getNodelist().get(n+1);
		 * System.out.println(nodeA.getName()+"-"+nodeB.getName());
		 * 
		 * Link link=oplayer.findLink(nodeA, nodeB);
		 * length=length+link.getLength(); if(length<=4000) { n=n+1;
		 * linklist.add(link); if(n==newRoute.getNodelist().size()-1)
		 * partworkflag=modifylinkcapacity(IPflow,length, linklist,
		 * oplayer,ipLayer);//ΪĿ�Ľڵ�ǰ��ʣ����·����RSA totalregNum++; } if(length>4000)
		 * { length=length-link.getLength();
		 * partworkflag=modifylinkcapacity(IPflow,length, linklist,
		 * oplayer,ipLayer);//��ʱ��n����������� totalregNum++; length=0; RSAflag=true;
		 * linklist.clear(); } if(!partworkflag&&RSAflag) break;
		 * }while(n!=newRoute.getNodelist().size()-1); }
		 * System.out.println("һ����Ҫ������������Ϊ��"+totalregNum); if(partworkflag)
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
				System.out.println("�ҵ�����·���֣�" + link.getName());
				file_io.filewrite2(OutFileName,"�ҵ�����·���֣�" + link.getName());
				length2 = length2 + link.getLength();
				linklist2.add(link);
				count = count + 1;
				if (!regflag2) {// δ�������һ��·����RSA
					if (count == finalRoute.getregnode().get(i)) {
						modifylinkcapacity(IPflow, length2, linklist2, oplayer, ipLayer);// ��ʱ��n�����������
						length2 = 0;
						linklist2.clear();
						break;
					}
				}
				if (count == finalRoute.getRoute().getNodelist().size() - 1) {
					modifylinkcapacity(IPflow, length2, linklist2, oplayer, ipLayer);// ��ʱ��n�����������
					linklist2.clear();
				}
			} while (count != finalRoute.getRoute().getNodelist().size() - 1);
		}
	
	}

	public Boolean vertify(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer, Layer iplayer,
			boolean workOrproflag, ArrayList<WorkandProtectRoute> wprlist, NodePair nodepair) {
	//�ж�ĳһ��transparent��·�Ƿ��ܹ��ɹ�RSA ���Ҽ�¼��ʹ�õ�FS����
		// workOrproflag=true��ʱ���ʾ�ǹ��� false��ʱ���ʾ����
		double X = 1;
		int slotnum = 0;
//		String OutFileName = "F:\\programFile\\RegwithProandTrgro\\NSFNET.dat";
		file_out_put file_io=new file_out_put();
		boolean opworkflag = false;
		if (routelength > 4000) {
			System.out.println("��·�����޷�RSA");
			file_io.filewrite2(OutFileName,"��·�����޷�RSA");
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
			slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��
//			System.out.println("����·����slot���� " + slotnum);

			WorkandProtectRoute nowdemand = new WorkandProtectRoute(null);
			Test t = new Test();
			
				newFS = slotnum * linklist.size();
				if (!workOrproflag) {// ��ʱ����������������·
				for (WorkandProtectRoute wpr : wprlist) {
					if (wpr.getdemand().equals(nodepair)) {
						nowdemand = wpr;
						continue;
					}
				}

				for (Link nowlink : linklist) {
					for (WorkandProtectRoute wpr : wprlist) {
						if (wpr.getdemand().equals(nodepair)) {
							continue;
						}
						if (wpr.getworklinklist().contains(nowlink)) {
							int cross = t.linklistcompare(wpr.getworklinklist(), nowdemand.getworklinklist());
							if (cross == 0) {// ����������·�ཻ��ʾ����·��FS�����Թ���
								newFS=newFS-slotnum; //���ĳһ����·�ϵ�Ƶ�׿��Թ��� ����Ҫ�ڼ�ȥ���FS
								break;
							}
						}
					}
				}
			}
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("·������ ��������Ƶ����Դ");
				file_io.filewrite2(OutFileName,"·������ ��������Ƶ����Դ");
			} else {
				opworkflag = true;
				System.out.println("���Խ���RSA");
				file_io.filewrite2(OutFileName,"���Խ���RSA");
			}
		}
		return opworkflag;
	}

	public boolean modifylinkcapacity(int IPflow, double routelength, ArrayList<Link> linklist, Layer oplayer,
			Layer iplayer) {
		double X = 1;
		int slotnum = 0;
		boolean opworkflag = false;
		file_out_put file_io=new file_out_put();
		Node srcnode = new Node(null, 0, null, iplayer, 0, 0);
		Node desnode = new Node(null, 0, null, iplayer, 0, 0);
		if (routelength > 4000) {
			System.out.println("��·�����޷�RSA");
			file_io.filewrite2(OutFileName,"��·�����޷�RSA");
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
			slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��

			System.out.println("����·����slot���� " + slotnum);
			file_io.filewrite2(OutFileName,"����·����slot���� " + slotnum);
			ArrayList<Integer> index_wave = new ArrayList<Integer>();
			Mymain spa = new Mymain();
			index_wave = spa.spectrumallocationOneRoute(false, null, linklist, slotnum);
			if (index_wave.size() == 0) {
				System.out.println("·������ ��������Ƶ����Դ");
				file_io.filewrite2(OutFileName,"·������ ��������Ƶ����Դ");
			} else {
				opworkflag = true;
				double length1 = 0;
				double cost = 0;
				for (Link link : linklist) {// ������link
					length1 = length1 + link.getLength();
					cost = cost + link.getCost();
					Request request = null;
					ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
					link.setMaxslot(slotnum + link.getMaxslot());
					// System.out.println("��· " + link.getName() + "�����slot�ǣ� "
					// + link.getMaxslot()+" ����Ƶ�״�����
					// "+link.getSlotsindex().size());
				}

				Node startnode=new Node(null, 0, null, iplayer, 0, 0);
				Node endnode=new Node(null, 0, null, iplayer, 0, 0);
				if(linklist.size()!=1){
					Link link1=linklist.get(0);  Link link2=linklist.get(1);
					Link link3=linklist.get(linklist.size()-2);  Link link4=linklist.get(linklist.size()-1);
					Node nodeA=link1.getNodeA(); Node nodeB=link1.getNodeB();
					Node nodeC=link4.getNodeA(); Node nodeD=link4.getNodeB();
					file_io.filewrite2(OutFileName,"ȡ������·Ϊ"+link1.getName()+"  "+link2.getName()+"   "+link3.getName()+"  "+link4.getName());
					
					if(link2.getNodeA().equals(nodeA)||link2.getNodeB().equals(nodeA)) startnode=nodeB;
					if(link2.getNodeA().equals(nodeB)||link2.getNodeB().equals(nodeB)) startnode=nodeA;//�ҵ���ʼ�˵�
					if(link3.getNodeA().equals(nodeC)||link3.getNodeB().equals(nodeC)) endnode=nodeD;
					if(link3.getNodeA().equals(nodeD)||link3.getNodeB().equals(nodeD)) endnode=nodeC;//�ҵ���ֹ�˵�
					file_io.filewrite2(OutFileName,"�ҵ��Ľڵ㣺"+startnode.getName()+"  "+endnode.getName());
				}
				if(linklist.size()==1){
					startnode=linklist.get(0).getNodeA();
					endnode=linklist.get(0).getNodeB();
				}
				
				for (int num = 0; num < iplayer.getNodelist().size() - 1; num++) {// ��IP����Ѱ��transparent��·������
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
				file_io.filewrite2(OutFileName,"src�Ľڵ��:"+srcnode.getIndex()+"  des�ڵ�ȣ�"+desnode.getIndex());
				if(srcnode.getIndex()>desnode.getIndex()){
					Node internode=srcnode;
					srcnode=desnode;
					desnode=internode;
				}
				file_io.filewrite2(OutFileName,"��ʱ��ԭ�ڵ�Ϊ:"+srcnode.getName()+"  �ս��Ϊ"+desnode.getName());
				String name = srcnode.getName() + "-" + desnode.getName();
				int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ�Ĺ���������index

				Link finlink = iplayer.findLink(srcnode, desnode);
				Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
				boolean findflag = false;
				try {
					System.out.println(finlink.getName());
					file_io.filewrite2(OutFileName,finlink.getName());
					findflag = true;
				} catch (java.lang.NullPointerException ex) {
					System.out.println("IP ��û�и���·��Ҫ�½���·");
					file_io.filewrite2(OutFileName,"IP ��û�и���·��Ҫ�½���·");
					file_io.filewrite2(OutFileName,"����������·Ϊ:"+srcnode.getName()+" "+desnode.getName());
					createlink = new Link(name, index, null, iplayer, srcnode, desnode, length1, cost);
					iplayer.addLink(createlink);
				}

				VirtualLink Vlink = new VirtualLink(srcnode.getName(), desnode.getName(), 0, 0);
				Vlink.setnature(0);
				Vlink.setUsedcapacity(Vlink.getUsedcapacity() + IPflow);
				Vlink.setFullcapacity(slotnum * X);// �������flow�Ǵ����������
				Vlink.setRestcapacity(Vlink.getFullcapacity() - Vlink.getUsedcapacity());
				Vlink.setlength(length1);
				Vlink.setcost(cost);
				Vlink.setPhysicallink(linklist);

				if (findflag) {// �����IP�����Ѿ��ҵ�����·
					finlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					file_io.filewrite2(OutFileName,"IP���Ѵ��ڵ���· " + finlink.getName() + "\n " + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					System.out.println("������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
					file_io.filewrite2(OutFileName,"������·�ڹ���½�����·��  " + finlink.getName() + "  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
				} else {
					createlink.getVirtualLinkList().add(Vlink);
					System.out.println("IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					System.out.println("������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
					file_io.filewrite2(OutFileName,"IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					file_io.filewrite2(OutFileName,"������·�ڹ���½�����·��  " + createlink.getName() + "  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
				}
			}
		}
		return opworkflag;
	}

}
