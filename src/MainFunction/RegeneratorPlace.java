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
		//�ڶ��ַ������ж�һ��·������ʹ�õ��������ĸ��� Ȼ������е������ѡ�������� ���õ�λ��
		 
		int minRegNum=(int) Math.floor(routelength/4000);//���ٵ��������ĸ���
		int internode=newRoute.getNodelist().size()-2;
		int FStotal=0,n=0;
		double length=0;
		ArrayList<Link> linklist=new ArrayList<>();
		boolean partworkflag=false,RSAflag=false,regflag=false,success=false;
		ArrayList<RouteAndRegPlace> regplaceoption=new ArrayList<>();
		RouteAndRegPlace finalRoute=new RouteAndRegPlace(null, 0);
		
		for(int s=minRegNum;s<=internode;s++){
			if(partworkflag) break;
		Test nOfm = new Test(s,internode);  //�������м�ڵ������ѡȡm����������������
	    while (nOfm.hasNext()) { 
	    	RSAflag=false; regflag=false;partworkflag=false;n=0;length=0;FStotal=0;linklist.clear();
	            int[] set = nOfm.next();   //�������������������λ��
	            for (int i = 0; i < set.length+1; i++) {//RSA�Ĵ������������ĸ�����1  
	            	if(!partworkflag&&RSAflag) break;
	            	if(i<set.length)
	            	System.out.println("****************��������λ��Ϊ��"+set[i]);  //set�������Ӧ���ǽڵ��λ��+1��
	                else {
	                	System.out.println("************���һ�����������ս��֮���RSA ");
	                	regflag=true;
	                }
	                do{//ͨ��һ��
	                	Node nodeA=newRoute.getNodelist().get(n);
	                	Node nodeB=newRoute.getNodelist().get(n+1);
	                	Link link=oplayer.findLink(nodeA, nodeB);
	                	System.out.println(link.getName());
	        			length=length+link.getLength();
	        			linklist.add(link);
	        			n=n+1;
	        			if(!regflag){//δ�������һ��·����RSA
	                	if(n!=set[i]){
	        				if(n==newRoute.getNodelist().size()-1){
	        					partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
	        					FStotal=FStotal+newFS;
	        				}
	                	}
	                	if(n==set[i]){
//	                		length=length-link.getLength();
	        				partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//��ʱ��n�����������
	        				FStotal=FStotal+newFS;
	        				length=0;
	        				RSAflag=true;
	        				linklist.clear();
	        				break;
	                	}
	        			}
	                if(n==newRoute.getNodelist().size()-1){
	                	partworkflag=vertify(IPflow,length, linklist, oplayer,ipLayer);//��ʱ��n�����������
	                	FStotal=FStotal+newFS;
	                }
	                	if(!partworkflag&&RSAflag) break;
	                }while(n!=newRoute.getNodelist().size()-1);  
	            //���·�ɳɹ��򱣴��·�ɶ����������ķ���
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
	            	System.out.println("��·���ɹ�RSA, �ѳɹ�RSA������Ϊ��"+regplaceoption.size());//�������ĸ����ӽ�ȥ
	            }
	        }
		}
		//���Ѿ������ļ�����·��ѡȡһ��ʹ��FS���ٵ���·��Ϊ������·
		if(regplaceoption.size()!=0){
			success=true;
			int FS=10000;
			for(RouteAndRegPlace route: regplaceoption){
				if(route.getnewFSnum()<FS){
					FS=route.getnewFSnum();
					finalRoute=route;//��������ѡ������������õĵص� ������Ҫ�Ը���·�����������������λ�ý�����������~
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
				System.out.println("�ҵ�����·���֣�"+link.getName());
				length2=length2+link.getLength();
				linklist2.add(link);
				count=count+1;
    			if(!regflag2){//δ�������һ��·����RSA
            	if(count!=finalRoute.getregnode().get(i)){
    				if(count==finalRoute.getRoute().getNodelist().size()-1){
    					modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
    					linklist2.clear();
            	}
    			}	
            	if(count==finalRoute.getregnode().get(i)){
            		modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//��ʱ��n�����������
    				length2=0;
    				linklist2.clear();
    				break;
            	}
    			}
    			  if(count==finalRoute.getRoute().getNodelist().size()-1){
    				  modifylinkcapacity(IPflow,length2, linklist2, oplayer,ipLayer);//��ʱ��n�����������
    				  linklist2.clear();
	                }
			}while(count!=finalRoute.getRoute().getNodelist().size()-1);
		}
		}
		
		if(regplaceoption.size()==0){
			success=false;
			System.out.println("��·��������");
		}
		System.out.println();
		if(success)   {
			System.out.print("���������óɹ�����RSA,���õ�����������Ϊ"+ finalRoute.getregnum()+"  λ��Ϊ��");
			for(int p=0;p<finalRoute.getregnode().size();p++){
				System.out.print(finalRoute.getregnode().get(p)+"     ");
			}
		}
		
		else  System.out.println("�������������ɹ���·��������");
		return success;
		/*
		 * ��һ������ͨ��������������������������� 
		 */
		/*
		double length=0;
		int n=0; 
		boolean brokeflag=false,opworkflag=false,partworkflag=false,RSAflag=false;
		ArrayList<Link> linklist=new ArrayList<Link>();
		
		for(Link link:newRoute.getLinklist()){//�ж�route��ÿһ����·�����Ƿ񳬹�����ƾ���
			if(link.getLength()>4000) {
				System.out.println(link.getName()+" �ľ������ ҵ�����");
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
					partworkflag=modifylinkcapacity(IPflow,length, linklist, oplayer,ipLayer);//ΪĿ�Ľڵ�ǰ��ʣ����·����RSA
			}
			if(length>4000)  {
				length=length-link.getLength();
				partworkflag=modifylinkcapacity(IPflow,length, linklist, oplayer,ipLayer);//��ʱ��n�����������
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
		 System.out.println("��·�����޷�RSA");
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
	slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��
	System.out.println("����·����slot���� " + slotnum);
	newFS=slotnum;
	ArrayList<Integer> index_wave = new ArrayList<Integer>();
	Mymain spa=new Mymain();
	index_wave = spa.spectrumallocationOneRoute(false,null,linklist,slotnum);
	if (index_wave.size() == 0) {
		System.out.println("·������ ��������Ƶ����Դ");
	} 
	else{
		opworkflag=true;
		System.out.println("���Խ���RSA");
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
			 System.out.println("��·�����޷�RSA");
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
		slotnum = (int) Math.ceil(IPflow / X);// ����ȡ��
		
		System.out.println("����·����slot���� " + slotnum);
		newFS=slotnum;
		ArrayList<Integer> index_wave = new ArrayList<Integer>();
		Mymain spa=new Mymain();
		index_wave = spa.spectrumallocationOneRoute(false,null,linklist,slotnum);
		if (index_wave.size() == 0) {
			System.out.println("·������ ��������Ƶ����Դ");
		} 
		else{
			opworkflag=true;
			double length1 = 0;
			double cost = 0;
			for (Link link : linklist) {// ������link
				length1 = length1 + link.getLength();
				cost = cost + link.getCost();
				Request request = null;
				ResourceOnLink ro = new ResourceOnLink(request, link, index_wave.get(0), slotnum);
				link.setMaxslot(slotnum + link.getMaxslot());
				// System.out.println("��· " + link.getName() + "�����slot�ǣ� " + link.getMaxslot()+" ����Ƶ�״����� "+link.getSlotsindex().size());
			}
			
			for(int num=0;num<iplayer.getNodelist().size()-1;num++){// ��IP����Ѱ��transparent��·������
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
			int index = iplayer.getLinklist().size();// ��Ϊiplayer�����link��һ��һ������ȥ�Ĺ���������index
		
			Link finlink=iplayer.findLink(srcnode, desnode);
			Link createlink = new Link(null, 0, null, iplayer, null, null, 0, 0);
			boolean findflag=false;
			try{
				System.out.println(finlink.getName());
				findflag=true;
			}catch(java.lang.NullPointerException ex){
				System.out.println("IP ��û�и���·��Ҫ�½���·");
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
	
			if(findflag){//�����IP�����Ѿ��ҵ�����·
//				System.out.println(finlink.getVirtualLinkList().size());
				finlink.getVirtualLinkList().add(Vlink);
//				System.out.println(finlink.getVirtualLinkList().size());
				System.out.println("IP���Ѵ��ڵ���· " + finlink.getName() +"\n "+"    Ԥ����flow��  " + Vlink.getRestcapacity());
				System.out.println("������·�ڹ���½�����·��  "+finlink.getName()+"  �ϵ�������·������ "+ finlink.getVirtualLinkList().size());
				}
				else{
					createlink.getVirtualLinkList().add(Vlink);
//					System.out.println(createlink.getVirtualLinkList().size());
					System.out.println("IP�����½���· " + createlink.getName() + "    Ԥ����flow��  " + Vlink.getRestcapacity());
					System.out.println("������·�ڹ���½�����·��  "+createlink.getName()+"  �ϵ�������·������ "+ createlink.getVirtualLinkList().size());
				}
		}
	}
		 return opworkflag;
}


	
}


