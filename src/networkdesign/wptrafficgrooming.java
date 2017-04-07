package networkdesign;

import graphalgorithms.RouteSearching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import resource.ResourceOnLink;
import subgraph.LinearRoute;
import demand.Request;
import MainFunction.ReadFlowFile;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;

public class wptrafficgrooming {//����ı�����IP��worklink��protectionlink���Թ����grooming


	public void grooming(Network network ,Layer optlayer, Layer iplayer){
	 Layer ipcopylayer=network.getLayerlist().get("ipcopylayer");
		ReadFlowFile myfile=new ReadFlowFile();
		myfile.Readflow(iplayer, "f:/Data/Node6.csv");
		 ArrayList<NodePair> demandlist=Rankflow(iplayer);
		 int num_of_transponders=0;
	     int sumslotnum=0;
		 ArrayList<Link> tempdel_LinkList=new ArrayList<Link>();	
		 ArrayList<Link> tempdel_LinkList_1 =new ArrayList<Link>();
		 ArrayList<Link> tempdel_LinkList_2 =new ArrayList<Link>(); 
		 ArrayList<Link> tempdel_LinkList_4 =new ArrayList<Link>(); 
		 ArrayList<Link> tempdel_LinkList_5 =new ArrayList<Link>(); 
	    // tempdel_LinkList.clear();
	     ArrayList<Link> optempdel_LinkList=new ArrayList<Link>();			
	    // optempdel_LinkList.clear();
	     
			for(int i=0;i<iplayer.getNodepairNum();i++){
				NodePair nodepair=demandlist.get(i);
				
				System.out.println("�������ڲ����Ľڵ���ǣ�"+nodepair.getName()+"�������������ǣ�"+nodepair.getTrafficdemand());				
				HashMap<String, Link> map6 = iplayer.getLinklist();
				Iterator<String> iter6 = map6.keySet().iterator();
				while(iter6.hasNext()){
					Link link=(Link)(map6.get(iter6.next()));
					//��ͨ������������ȥ���õ�flowС��trafficdemand
					if((link.getSumflow()-link.getFlow())<nodepair.getTrafficdemand())
					{
						tempdel_LinkList.add(link);	
						System.out.println("******�������������ɾ����ǰ��·"+link.getName());
					}
				}					
				  for(Link link : tempdel_LinkList){
				  //  System.out.println("1����ɾ������·�ǣ�"+link.getName());//ɾ����������������Ϊ������link
					iplayer.removeLink(link.getName());
				  }
			
			  ArrayList<Link> tempdel_LinkList_3 =new ArrayList<Link>();//�洢iplayer��link	   	  
			
			  HashMap<String, Link> map = iplayer.getLinklist();
					Iterator<String> iter = map.keySet().iterator();
					while(iter.hasNext()){
						Link link=(Link)(map.get(iter.next()));
						tempdel_LinkList_3.add(link);
					}
	//�ҳ�w p����������������£������������Ϊ��ѡ	
					System.out.println("iplayer��link:"+tempdel_LinkList_3.size());	
					if(tempdel_LinkList_3.size()>1)
					{
						for(int h=0;h<tempdel_LinkList_3.size();h++){
						for(int h1=h+1;h1<tempdel_LinkList_3.size();h1++){
							Link link=tempdel_LinkList_3.get(h);
							Link linka=tempdel_LinkList_3.get(h1);
							if(link.getNodeA().getName().equals(linka.getNodeA().getName())&&link.getNodeB().getName().equals(linka.getNodeB().getName())){
							if((linka.getSumflow()-linka.getFlow())<=(link.getSumflow()-link.getFlow()))
							{	
							tempdel_LinkList_5.add(linka);//�洢û�е�link
							}
							else
							{	
							tempdel_LinkList_5.add(link);
							}
							}
						}
					}
					}
					for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
						iplayer.removeLink(link.getName());
					}					
					tempdel_LinkList_3.clear();	
	
					
					HashMap<String, Link> mapaa = iplayer.getLinklist();
					Iterator<String> iteraa = mapaa.keySet().iterator();
					while(iteraa.hasNext()){
						Link link=(Link)(mapaa.get(iteraa.next()));
						System.out.println("iplayer ʣ��link:"+link.getName());
						Node srcnodec=link.getNodeA();
						Node desnodec=link.getNodeB();
						
						Node srcnodec1=ipcopylayer.getNodelist().get(srcnodec.getName());
						Node desnodec1=ipcopylayer.getNodelist().get(desnodec.getName());
						int index = 0;
						index=ipcopylayer.getLinklist().size();
					    String name=srcnodec.getName()+"-"+desnodec.getName();
					    double length=0;
						double cost=0;
						length=length+link.getLength();
					    cost=cost+link.getCost();					   
						Link newlink = new Link(name,index,"",ipcopylayer,srcnodec1,desnodec1,length,cost);
						System.out.println("add new ipcopylayer��link�����ǣ�"+newlink.getName());
						ipcopylayer.addLink(newlink);								
					}
						
				Node srcnode=nodepair.getSrcNode();
				Node desnode=nodepair.getDesNode();
				Node srcnodecc=ipcopylayer.getNodelist().get(srcnode.getName());
				Node desnodecc=ipcopylayer.getNodelist().get(desnode.getName());
			//	System.out.println("Դ�ڵ��ǣ�"+srcnode.getName());
			//	System.out.println("Ŀ�ĵ��ǣ�"+desnode.getName());
				LinearRoute newroutew=new LinearRoute("",0,"");
				RouteSearching routesearching=new RouteSearching();
				routesearching.Dijkstras(srcnodecc, desnodecc, ipcopylayer, newroutew, null);
			
				
			
//��������ǽ�ipcopylayer���				
				HashMap<String,Link> map0 =ipcopylayer.getLinklist();
				Iterator<String> iter0=map0.keySet().iterator();
				while(iter0.hasNext()){
					Link link=(Link)(map0.get(iter0.next()));
					tempdel_LinkList_4.add(link);
				}	
				for(Link link:tempdel_LinkList_4){
					ipcopylayer.removeLink(link.getName());
				}
//				System.out.println("ipcopylayer link:"+ipcopylayer.getLinklist().size());
				tempdel_LinkList_4.clear();
				
				if(newroutew.getLinklist().size()!=0){//·�ɳɹ�
					System.out.println("����·��ip����·�ɳɹ���");
					System.out.println("��ip���ϵ�·���ǣ�");
					newroutew.OutputRoute_node(newroutew);
					System.out.println("---");
					ArrayList<Link> newrouteww=new ArrayList<Link>();//�洢����·����������link	
					//��ipcopylayer�ҵ���newroutew����Ӧ��link��������
					for(int i1=0;i1<newroutew.getLinklist().size();i1++){
						HashMap<String, Link> map1 = iplayer.getLinklist();
						Iterator<String> iter1 = map1.keySet().iterator();
						while(iter1.hasNext()){
							Link link=(Link)(map1.get(iter1.next()));
							if((link.getNodeA().getName().equals(newroutew.getLinklist().get(i1).getNodeA().getName()))&&(link.getNodeB().getName().equals(newroutew.getLinklist().get(i1).getNodeB().getName())))
							{	
							link.setFlow(link.getFlow()+nodepair.getTrafficdemand());	
							newrouteww.add(link);//iplayer·�ɹ���·����������iplink
							}
							
						}
					}
					for(int i4=0;i4<newrouteww.size();i4++){
						System.out.println("iplayer ����·��Ϊ��"+newrouteww.get(i4).getName());
						for(int i5=0;i5<newrouteww.get(i4).getPhysicallink().size();i5++){
							System.out.println("��������㾭����link�ǣ�"+newrouteww.get(i4).getPhysicallink().get(i5).getName());
						}
					}
					for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
						iplayer.addLink(link);
					}	
					tempdel_LinkList_5.clear();				
					for(Link link : tempdel_LinkList){//�ָ�
						iplayer.addLink(link);
					}
					tempdel_LinkList.clear();			
					HashMap<String, Link> map1 = iplayer.getLinklist();
					Iterator<String> iter1 = map1.keySet().iterator();
					while(iter1.hasNext()){
						Link link=(Link)(map1.get(iter1.next()));
						System.out.println("link ��ʣ��������"+link.getName()+":"+(link.getSumflow()-link.getFlow()));
					}
//			����·��·�ɳɹ���·�ɱ���·����
//���б���link���빤��·����link���غϵ�����link��Ҫɾ����
			    HashMap<String,Link> map60 =iplayer.getLinklist();
				Iterator<String> iter60=map60.keySet().iterator();
				while(iter60.hasNext()){
					Link link=(Link)(map60.get(iter60.next()));
					if(link.getIpremainflow()<nodepair.getTrafficdemand())tempdel_LinkList.add(link);//����������link	
				
				 for(int u=0;u<newrouteww.size();u++){
						for(int b=0;b<newrouteww.get(u).getPhysicallink().size();b++){							
							for(int c=0;c<link.getPhysicallink().size();c++){	
								if(link.getPhysicallink().get(c).getName().equals(newrouteww.get(u).getPhysicallink().get(b).getName()))
											tempdel_LinkList.add(link);																			
							}
						}						
						}
				}//�빤��·��newroutew���غ�����link��ip��link
					
				for(int o=0;o<tempdel_LinkList.size();o++){
					if(!tempdel_LinkList_2.contains(tempdel_LinkList.get(o)))
						tempdel_LinkList_2.add(tempdel_LinkList.get(o));
				}		
				
		for(Link link4 : tempdel_LinkList_2){
			 System.out.println("·�ɱ���·��ɾ������·�ǣ�"+link4.getName());//ɾ����������������Ϊ������link
			iplayer.removeLink(link4.getName());	
		}		
		  HashMap<String, Link> mapw = iplayer.getLinklist();
			Iterator<String> iterw = mapw.keySet().iterator();
			while(iterw.hasNext()){
				Link link=(Link)(mapw.get(iterw.next()));
				tempdel_LinkList_3.add(link);
			}
//�ҳ�w p����������������£������������Ϊ��ѡ	
			System.out.println("iplayer��link:"+tempdel_LinkList_3.size());	
			if(tempdel_LinkList_3.size()>1)
			{
				for(int h=0;h<tempdel_LinkList_3.size();h++){
				for(int h1=h+1;h1<tempdel_LinkList_3.size()-1;h1++){
					Link link=tempdel_LinkList_3.get(h);
					Link linka=tempdel_LinkList_3.get(h1);
					if(link.getNodeA().getName().equals(linka.getNodeA().getName())&&link.getNodeB().getName().equals(linka.getNodeB().getName())){
					if((linka.getSumflow()-linka.getFlow())<=(link.getSumflow()-link.getFlow()))
					{	
					tempdel_LinkList_5.add(linka);//�洢û�е�link
					}
					else
					{	
					tempdel_LinkList_5.add(link);
					}
					}
				}
			}
			}
			for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
				iplayer.removeLink(link.getName());
			}
			
			tempdel_LinkList_3.clear();	
			
			HashMap<String, Link> mapww = iplayer.getLinklist();
			Iterator<String> iterww = mapww.keySet().iterator();
			while(iterww.hasNext()){
				Link link=(Link)(mapww.get(iterww.next()));
				System.out.println("iplayer ʣ��link:"+link.getName());
				Node srcnodec=link.getNodeA();
				Node desnodec=link.getNodeB();
				
				Node srcnodec1=ipcopylayer.getNodelist().get(srcnodec.getName());
				Node desnodec1=ipcopylayer.getNodelist().get(desnodec.getName());
				int index = 0;
				index=ipcopylayer.getLinklist().size();
			    String name=srcnodec.getName()+"-"+desnodec.getName();
			    double length=0;
				double cost=0;
				length=length+link.getLength();
			    cost=cost+link.getCost();					   
				Link newlink = new Link(name,index,"",ipcopylayer,srcnodec1,desnodec1,length,cost);
				System.out.println("add new ipcopylayer��link�����ǣ�"+newlink.getName());
				ipcopylayer.addLink(newlink);								
			}
			Node srcnodec1=ipcopylayer.getNodelist().get(srcnode.getName());
			Node desnodec1=ipcopylayer.getNodelist().get(desnode.getName());
					LinearRoute newroutep=new LinearRoute("",0,"");
					RouteSearching routesearching1=new RouteSearching();
					routesearching1.Dijkstras(srcnodec1, desnodec1, ipcopylayer, newroutep, null);
					
					
											
					if(newroutep.getLinklist().size()!=0){
						System.out.println("����·��ip����·�ɳɹ���");
						System.out.println("��ip���ϵ�·���ǣ�");
						newroutew.OutputRoute_node(newroutew);
						System.out.println("---");
						for(int i1=0;i1<newroutep.getLinklist().size();i1++){
							HashMap<String, Link> map11 = iplayer.getLinklist();
							Iterator<String> iter11 = map11.keySet().iterator();
							while(iter11.hasNext()){
								Link link=(Link)(map11.get(iter11.next()));
								if((link.getNodeA().getName().equals(newroutep.getLinklist().get(i1).getNodeA().getName()))&&(link.getNodeB().getName().equals(newroutep.getLinklist().get(i1).getNodeB().getName())))
								{	
								link.setFlow(link.getFlow()+nodepair.getTrafficdemand());	
								
								}
								
							}
						}

						for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
							iplayer.addLink(link);
						}	
						tempdel_LinkList_5.clear();	

						for(Link link : tempdel_LinkList_2){//�ָ�link
							iplayer.addLink(link);
						}
						tempdel_LinkList.clear();
						tempdel_LinkList_2.clear();
						
						HashMap<String, Link> map111 = iplayer.getLinklist();
						Iterator<String> iter111 = map111.keySet().iterator();
						while(iter111.hasNext()){
							Link link=(Link)(map111.get(iter111.next()));
							System.out.println("link ��ʣ��������"+link.getFlow());
						}
					}
						else{
							//�ڹ���н�newroutew������������link��ɾ��
							for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
								iplayer.addLink(link);
							}	
							tempdel_LinkList_5.clear();	
							for(Link link : tempdel_LinkList_2){//�ָ�link
								iplayer.addLink(link);
							}
							tempdel_LinkList.clear();
							tempdel_LinkList_2.clear();	
							HashMap<String, Link> map8 = optlayer.getLinklist();
							Iterator<String> iter8 = map8.keySet().iterator();
							while(iter8.hasNext()){
								Link link=(Link)(map8.get(iter8.next()));
								for(int f=0;f<newrouteww.size();f++){
									for(int g=0;g<newrouteww.get(f).getPhysicallink().size();g++){
								if(link.getName().equals(newrouteww.get(f).getPhysicallink().get(g).getName()))
									optempdel_LinkList.add(link);	
							}
								}
							}
							for(Link link : optempdel_LinkList){
					//		    System.out.println("3����ɾ������·�ǣ�"+link.getName());//ɾ��worklink
								optlayer.removeLink(link.getName());
							}
							
							System.out.println("����·�ɲ��ɹ����½�һ����·��++++++++++");
							Node srcnodep=optlayer.getNodelist().get(srcnode.getName());
							Node desnodep=optlayer.getNodelist().get(desnode.getName());
							
							LinearRoute newroutep1=new LinearRoute("",0,"");
						    RouteSearching routesearchingp1=new RouteSearching();
							routesearchingp1.Dijkstras(srcnodep, desnodep, optlayer, newroutep1, null);

						     for(Link link : optempdel_LinkList){//�ָ�link
									optlayer.addLink(link);
								}
								optempdel_LinkList.clear();
						    if(newroutep1.getLinklist().size()==0){
					    		System.out.println("�ޱ���·��"); 
					     }
						    else{
						    	
							 System.out.println("����·�������ɹ����������·�ɾ����Ľڵ����£�------");			   
						     newroutep1.OutputRoute_node(newroutep1); 
						   						    						    	 
						     int slotnum=0;
							 int IPflow=nodepair.getTrafficdemand();
						     double X=1;//2000-4000 BPSK,1000-2000 QBSK,500-1000 8QAM,0-500 16QAM
						     double routelength=newroutep1.getlength();
						//     System.out.println("����·���ĳ����ǣ�"+routelength);
						     if(routelength>2000&&routelength<=4000){
						    	 X=12.5;
						     }
						    	 else if(routelength>1000&&routelength<=2000){
						    		 X=25.0;
						    	 }
						    	 else if(routelength>500&&routelength<=1000){
						    		 X=37.5;
						     }
						    	 else if(routelength>0&&routelength<=500){
						    		 X=50.0;
						    	 }
						    
						     slotnum=(int)Math.ceil(IPflow/X);	
						     //sumslotnum=sumslotnum+slotnum;
						     newroutep1.setSlotsnum(slotnum);
						//     System.out.println("�����slots���ǣ�"+slotnum);
						     
						     ArrayList<Integer> index_w=new ArrayList<Integer>(); 
						     
						     index_w=spectrumallocationOneRoute(newroutep1);
//						     System.out.println("size of index_w is:"+index_w.size());
//						     System.out.println("����Ƶ�״���");	
				//		     for(int k=0;k<index_w.size();k++)
						//     System.out.print(index_w.get(k));
						//     System.out.print("\n");
						   
							  if(index_w.size()==0)
								       {				         
							     //       total_block_bandwidth=total_block_bandwidth+IPflow;
							    	    //����������Դ
								       	System.out.println("����·��������������Ƶ����Դ");
								    }
					 
						        else{			        
						        	System.out.println("������������Ƶ����Դ");
						     	    //nodepair.getindexlist_w().add(index_w.get(0));  //����·�������״����з�ʽ
						     	    for(Link link:newroutep1.getLinklist())
						   	    {
						     	    Request request=null;
						     	   // request.setSlots(slotnum);
						     	    ResourceOnLink resourceonlink=new ResourceOnLink(request,link,index_w.get(0), slotnum); 
						   		    //SA.getRollist_w().add(resourceonlink);
						               //System.out.println("the name of link:"+link.getName()+"the start index of working path:"+index_w.get(0));
						     	 
						   	    }
						     	   for(int t=0;t<newroutep1.getLinklist().size();t++){
						     	    	newroutep1.getLinklist().get(t).setMaxslot(slotnum+newroutep1.getLinklist().get(t).getMaxslot());
						     	    	 System.out.println("���slot�ǣ�"+newroutep1.getLinklist().get(t).getName()+":"+newroutep1.getLinklist().get(t).getMaxslot());
						     	    }
						        }
//							  sumslotnum=sumslotnum+slotnum;
//					     	  System.out.println("��slot���ǣ�"+sumslotnum);
						     
							//�½�������·  
						    int index=iplayer.getLinklist().size();
						    String name=srcnodep.getName()+"-"+1+"-"+desnodep.getName();
						    double length=0;
						    double cost=0;
						    int nature = 1;//����·��
						    for(Link link2:newroutep1.getLinklist()){
						    	length=length+link2.getLength();
						    	cost=cost+link2.getCost();
						    }
						    Link newlink = new Link(name,index,"",iplayer,srcnode,desnode,length,cost);
						    System.out.println("�¹�·�������ǣ�"+newlink.getName());
						//    System.out.println("�¹�·�������ǣ�"+newlink.getNature());
						    //System.out.println("�¹�·������ǣ�"+newlink.getIndex());
						    //System.out.println("�¹�·�ĳ����ǣ�"+newlink.getLength());
						    //System.out.println("�¹�·��cost�ǣ�"+newlink.getCost());
							iplayer.addLink(newlink);
							newlink.setNature(1);
							newlink.setFlow(nodepair.getTrafficdemand());
							newlink.setSumflow(slotnum*X);
							System.out.println("��ͨ��ʣ�������ǣ�"+(newlink.getSumflow()-newlink.getFlow()));
							num_of_transponders=num_of_transponders+2;
							System.out.println("ʹ�õ�transponders���ǣ�"+num_of_transponders);
							//this.setNumTR(num_of_transponders); 
							newlink.setPhysicallink(newroutep1.getLinklist());//����·��������������link�洢
						     }
						}
				}

			    //second part add link to G		
				else{ 

					for(Link link:tempdel_LinkList_5){//ɾ����link�ӻ���
						iplayer.addLink(link);
					}	
					tempdel_LinkList_5.clear();				
					for(Link link : tempdel_LinkList){//�ָ�
						iplayer.addLink(link);
					}
					tempdel_LinkList.clear();
					System.out.println("����·�ɲ��ɹ����ڹ���½�һ����·��++++++++++");
					//���·��
					Node srcnode1=optlayer.getNodelist().get(srcnode.getName());
					Node desnode1=optlayer.getNodelist().get(desnode.getName());					
					LinearRoute newroute1=new LinearRoute("",0,"");
				    RouteSearching routesearching1=new RouteSearching();
//				     System.out.println("Դ�ڵ��ǣ�"+srcnode1.getName());
//					 System.out.println("Ŀ�ĵ��ǣ�"+desnode1.getName());
				     routesearching1.Dijkstras(srcnode1, desnode1, optlayer, newroute1, null);	
				     if(newroute1.getLinklist().size()==0){
				    	 System.out.println("����·����·��");
				    }
				     else{
				    	System.out.println("�������·�ɾ����Ľڵ����£�------");
				        newroute1.OutputRoute_node(newroute1); 
				     
				     int slotnum=0;
				     
					 int IPflow=nodepair.getTrafficdemand();
				     double X=1;//2000-4000 BPSK,1000-2000 QBSK,500-1000 8QAM,0-500 16QAM
				     double routelength=newroute1.getlength();
				     //System.out.println("����·���ĳ����ǣ�"+routelength);
				     if(routelength>2000&&routelength<=4000){
				    	 X=12.5;
				     }
				    	 else if(routelength>1000&&routelength<=2000){
				    		 X=25.0;
				    	 }
				    	 else if(routelength>500&&routelength<=1000){
				    		 X=37.5;
				     }
				    	 else if(routelength>0&&routelength<=500){
				    		 X=50.0;
				    	 }
				    
				     slotnum=(int)Math.ceil(IPflow/X);	
				     //sumslotnum=sumslotnum+slotnum;
				     newroute1.setSlotsnum(slotnum);
				//     System.out.println("�����slots���ǣ�"+slotnum);
				     
				     ArrayList<Integer> index_w=new ArrayList<Integer>(); 
				     
				     index_w=spectrumallocationOneRoute(newroute1);
//				     System.out.println("size of index_w is:"+index_w.size());
//				     System.out.println("����Ƶ�״���");	
				//     for(int k=0;k<index_w.size();k++)
				//     System.out.print(index_w.get(k));
				//     System.out.print("\n");

					  if(index_w.size()==0)
						       {				         
					     //       total_block_bandwidth=total_block_bandwidth+IPflow;
					    	    //����������Դ
						       	System.out.println("����·��������������Ƶ����Դ");
						    }
				    //System.out.println("-----");					     
				        else{			        
				        	System.out.println("������������Ƶ����Դ");
				     	    //nodepair.getindexlist_w().add(index_w.get(0));  //����·�������״����з�ʽ
				     	    for(Link link:newroute1.getLinklist())
				   	    {
				     	    Request request=null;
				     	   // request.setSlots(slotnum);
				     	    ResourceOnLink resourceonlink=new ResourceOnLink(request,link,index_w.get(0), slotnum); 
				               //System.out.println("the name of link:"+link.getName()+"the start index of working path:"+index_w.get(0));	     	 
				   	    }
				     	   for(int t=0;t<newroute1.getLinklist().size();t++){
				     	    	newroute1.getLinklist().get(t).setMaxslot(slotnum+newroute1.getLinklist().get(t).getMaxslot());
				     	    	 System.out.println("���slot�ǣ�"+newroute1.getLinklist().get(t).getName()+":"+newroute1.getLinklist().get(t).getMaxslot());
				     	    }
				        }
//					  sumslotnum=sumslotnum+slotnum;
//			     	    System.out.println("��slot���ǣ�"+sumslotnum);
				     
					//�½���·  
				    int index=iplayer.getLinklist().size();
				    String name=srcnode1.getName()+"-"+0+"-"+desnode1.getName();
				    double length=0;
				    double cost=0;
				    int nature = 0;//Ϊ����link
				    for(Link link2:newroute1.getLinklist()){
				    	length=length+link2.getLength();
				    	cost=cost+link2.getCost();
				    }
				    Link newlink = new Link(name,index,"",iplayer,srcnode,desnode,length,cost);
				    System.out.println("�¹�·�����ֺ��ǣ�"+newlink.getName());
				   // System.out.println("�¹�·�������ǣ�"+newlink.isNature());
				    //System.out.println("�¹�·������ǣ�"+newlink.getIndex());
				    //System.out.println("�¹�·�ĳ����ǣ�"+newlink.getLength());
				    //System.out.println("�¹�·��cost�ǣ�"+newlink.getCost());
					iplayer.addLink(newlink);
					newlink.setNature(0);
					newlink.setFlow(nodepair.getTrafficdemand());
					newlink.setSumflow(slotnum*X);
					System.out.println("��ͨ��ʣ�������ǣ�"+(newlink.getSumflow()-newlink.getFlow()));
					num_of_transponders=num_of_transponders+2;
					System.out.println("ʹ�õ�transponders���ǣ�"+num_of_transponders);
					newlink.setPhysicallink(newroute1.getLinklist());
					//this.setNumTR(num_of_transponders);
					
				    //second part��first part 
					//����·�������ã���ʼ·�ɱ���·��
					//���б���link���빤��·����link���غϵ�����link��Ҫɾ��!
	
					HashMap<String,Link> map11 =iplayer.getLinklist();
					Iterator<String> iter11=map11.keySet().iterator();
					while(iter11.hasNext()){
						Link link=(Link)(map11.get(iter11.next()));
			//			if(link.getNature()==0)	tempdel_LinkList.add(link);				
						if(link.getIpremainflow()<nodepair.getTrafficdemand()) tempdel_LinkList.add(link);
						for(int q=0;q<newroute1.getLinklist().size();q++){
							for(int p=0;p<link.getPhysicallink().size();p++){
								if(link.getPhysicallink().get(p).getName().equals(newroute1.getLinklist().get(q).getName()))																
									tempdel_LinkList.add(link);		//IP���빤��·��newroute1���غϵ�link							
							}	
							}
						}					
					for(int x=0;x<tempdel_LinkList.size();x++){
						if(!tempdel_LinkList_1.contains(tempdel_LinkList.get(x)))
							tempdel_LinkList_1.add(tempdel_LinkList.get(x));
					}		
					
			for(Link link4 : tempdel_LinkList_1){
			//	 System.out.println("·�ɱ���·��ɾ������·�ǣ�"+link4.getName());//ɾ����������������Ϊ������link
				iplayer.removeLink(link4.getName());	
			}
			 HashMap<String, Link> mapw = iplayer.getLinklist();
				Iterator<String> iterw = mapw.keySet().iterator();
				while(iterw.hasNext()){
					Link link=(Link)(mapw.get(iterw.next()));
					tempdel_LinkList_3.add(link);
				}
	//�ҳ�w p����������������£������������Ϊ��ѡ	
				System.out.println("iplayer��link:"+tempdel_LinkList_3.size());	
				if(tempdel_LinkList_3.size()>1)
				{
					for(int h=0;h<tempdel_LinkList_3.size();h++){
					for(int h1=h+1;h1<tempdel_LinkList_3.size()-1;h1++){
						Link link=tempdel_LinkList_3.get(h);
						Link linka=tempdel_LinkList_3.get(h1);
						if(link.getNodeA().getName().equals(linka.getNodeA().getName())&&link.getNodeB().getName().equals(linka.getNodeB().getName())){
						if((linka.getSumflow()-linka.getFlow())<=(link.getSumflow()-link.getFlow()))
						{	
						tempdel_LinkList_5.add(linka);//�洢û�е�link
						}
						else
						{	
						tempdel_LinkList_5.add(link);
						}
						}
					}
				}
				}
				for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
					iplayer.removeLink(link.getName());
				}
				
				tempdel_LinkList_3.clear();	
				
				HashMap<String, Link> mapww = iplayer.getLinklist();
				Iterator<String> iterww = mapww.keySet().iterator();
				while(iterww.hasNext()){
					Link link=(Link)(mapww.get(iterww.next()));
					System.out.println("iplayer ʣ��link:"+link.getName());
					Node srcnodec=link.getNodeA();
					Node desnodec=link.getNodeB();
					
					Node srcnodec1=ipcopylayer.getNodelist().get(srcnodec.getName());
					Node desnodec1=ipcopylayer.getNodelist().get(desnodec.getName());
					int index1 = 0;
					index1=ipcopylayer.getLinklist().size();
				    String name1=srcnodec.getName()+"-"+desnodec.getName();
				    double length1=0;
					double cost1=0;
					length1=length1+link.getLength();
				    cost1=cost1+link.getCost();					   
					Link newlink1 = new Link(name1,index1,"",ipcopylayer,srcnodec1,desnodec1,length1,cost1);
					System.out.println("add new ipcopylayer��link�����ǣ�"+newlink1.getName());
					ipcopylayer.addLink(newlink1);								
				}
						
			Node srcnode11=nodepair.getSrcNode();
			Node desnode11=nodepair.getDesNode();
			Node srcnodec1=ipcopylayer.getNodelist().get(srcnode11.getName());
			Node desnodec1=ipcopylayer.getNodelist().get(desnode11.getName());
//			System.out.println("Դ�ڵ��ǣ�"+srcnode11.getName());
//			System.out.println("Ŀ�ĵ��ǣ�"+desnode11.getName());
			LinearRoute newroutepp=new LinearRoute("",0,"");
			RouteSearching routesearchingpp=new RouteSearching();
		    routesearchingpp.Dijkstras(srcnodec1, desnodec1, ipcopylayer, newroutepp, null);
					
		   
					
					if(newroutepp.getLinklist().size()!=0){
						System.out.println("����·��ip����·�ɳɹ���");	
						System.out.println("��ip���ϵ�·���ǣ�");
						newroutepp.OutputRoute_node(newroutepp);
						System.out.println("---");
						
						for(int i1=0;i1<newroutepp.getLinklist().size();i1++){
							HashMap<String, Link> map22 = iplayer.getLinklist();
							Iterator<String> iter22 = map22.keySet().iterator();
							while(iter22.hasNext()){
								Link link=(Link)(map22.get(iter22.next()));
								if((link.getNodeA().getName().equals(newroutepp.getLinklist().get(i1).getNodeA().getName()))&&(link.getNodeB().getName().equals(newroutepp.getLinklist().get(i1).getNodeB().getName())))
								{	
								link.setFlow(link.getFlow()+nodepair.getTrafficdemand());	
								
								}
								
							}
						}

						for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
							iplayer.addLink(link);
						}	
						tempdel_LinkList_5.clear();	
						
						 for(Link link : tempdel_LinkList_1){//�ָ�
								iplayer.addLink(link);
							}
							tempdel_LinkList_1.clear();
							tempdel_LinkList.clear();
					}
						else{//second part ��second part
							for(Link link:tempdel_LinkList_5){//��ʱ��iplayerʣ��Ķ������ã�wp�Ƚ����������link
								iplayer.addLink(link);
							}	
							tempdel_LinkList_5.clear();	
							for(Link link : tempdel_LinkList_1){//�ָ�
									iplayer.addLink(link);
								}
								tempdel_LinkList_1.clear();
								tempdel_LinkList.clear();
							System.out.println("����·�ɲ��ɹ����½�һ����·��++++++++++");
							
			ArrayList<Link> temopLinkList=new ArrayList<Link>();
							HashMap<String, Link> mapop = optlayer.getLinklist();
							Iterator<String> iterop = mapop.keySet().iterator();
							while(iterop.hasNext()){
								Link linkop=(Link)(mapop.get(iterop.next()));
								for(int m=0;m<newroute1.getLinklist().size();m++){
								if(linkop.getName().equals(newroute1.getLinklist().get(m).getName()))
										temopLinkList.add(linkop);
										
								}
							}
								for(Link link5:temopLinkList){
							//		System.out.println("�ڹ��ɾ������·�ǣ�"+link5.getName());
									optlayer.removeLink(link5.getName());
								}								
							Node srcnodep=optlayer.getNodelist().get(srcnode.getName());
							Node desnodep=optlayer.getNodelist().get(desnode.getName());
						
							LinearRoute newroutepp1=new LinearRoute("",0,"");
						    RouteSearching routesearchingpp1=new RouteSearching();
						     routesearchingpp1.Dijkstras(srcnodep, desnodep, optlayer, newroutepp1, null);
						    
						     for(Link link : temopLinkList){//
									optlayer.addLink(link);
								}
						     temopLinkList.clear(); 
						     
						     if(newroutepp1.getLinklist().size()==0)
						    	 System.out.println("����·���ڹ�㽨��ʧ�ܣ�");	
						     else{
						    System.out.println("�������·�ɾ����Ľڵ����£�------");
						     newroutepp1.OutputRoute_node(newroutepp1); 
						   	
						     
						     int slotnum1=0;						     
							 int IPflow1=nodepair.getTrafficdemand();
						     double X1=1;//2000-4000 BPSK,1000-2000 QBSK,500-1000 8QAM,0-500 16QAM
						     double routelength1=newroutepp1.getlength();
						    // System.out.println("����·���ĳ����ǣ�"+routelength1);
						     if(routelength1>2000&&routelength1<=4000){
						    	 X1=12.5;
						     }
						    	 else if(routelength1>1000&&routelength1<=2000){
						    		 X1=25.0;
						    	 }
						    	 else if(routelength1>500&&routelength1<=1000){
						    		 X1=37.5;
						     }
						    	 else if(routelength1>0&&routelength1<=500){
						    		 X1=50.0;
						    	 }						    
						     slotnum1=(int)Math.ceil(IPflow1/X1);	
						     //sumslotnum=sumslotnum+slotnum;
						     newroutepp1.setSlotsnum(slotnum1);
						//     System.out.println("�����slots���ǣ�"+slotnum1);
						     
						     ArrayList<Integer> index_w1=new ArrayList<Integer>(); 
						     
						     index_w1=spectrumallocationOneRoute(newroutepp1);
//						     System.out.println("size of index_w is:"+index_w1.size());
//						     System.out.println("����Ƶ�״���");	
//						     for(int k=0;k<index_w1.size();k++)
//						     System.out.print(index_w1.get(k));
//						     System.out.print("\n");						   

							  if(index_w1.size()==0)
								       {				         
							     //       total_block_bandwidth=total_block_bandwidth+IPflow;
							    	    //����������Դ
								       	System.out.println("����·��������������Ƶ����Դ");
								    }
						    //System.out.println("-----");					   						    
						        else{			        
						        	System.out.println("������������Ƶ����Դ");
						     	    //nodepair.getindexlist_w().add(index_w.get(0));  //����·�������״����з�ʽ
						     	    for(Link link:newroutepp1.getLinklist())
						   	    {
						     	    Request request=null;
						     	   // request.setSlots(slotnum);
						     	    ResourceOnLink resourceonlink=new ResourceOnLink(request,link,index_w1.get(0), slotnum1); 
						   		    //SA.getRollist_w().add(resourceonlink);
						               //System.out.println("the name of link:"+link.getName()+"the start index of working path:"+index_w.get(0));
						     	 
						   	    }
						     	   for(int t=0;t<newroutepp1.getLinklist().size();t++){
						     	    	newroutepp1.getLinklist().get(t).setMaxslot(slotnum1+newroutepp1.getLinklist().get(t).getMaxslot());
						     	    	 System.out.println("���slot�ǣ�"+newroutepp1.getLinklist().get(t).getName()+":"+newroutepp1.getLinklist().get(t).getMaxslot());
						     	    }
						        }
//							  sumslotnum=sumslotnum+slotnum1;
//					     	    System.out.println("��slot���ǣ�"+sumslotnum);
						     
							//�½���·  
						    int index1=iplayer.getLinklist().size();
						    String name1=srcnodep.getName()+"-"+1+"-"+desnodep.getName();
						    double length1=0;
						    double cost1=0;
						    int nature1 = 1;//����·��
						    for(Link linkp:newroutepp1.getLinklist()){
						    	length1=length1+linkp.getLength();
						    	cost1=cost1+linkp.getCost();
						    }
						    Link newlink1 = new Link(name1,index1,"",iplayer,srcnode,desnode,length1,cost1);
						    System.out.println("�¹�·�������ǣ�"+newlink1.getName());
						  //  System.out.println("�¹�·�������ǣ�"+newlink1.getNature());
						    //System.out.println("�¹�·������ǣ�"+newlink.getIndex());
						    //System.out.println("�¹�·�ĳ����ǣ�"+newlink.getLength());
						    //System.out.println("�¹�·��cost�ǣ�"+newlink.getCost());
							iplayer.addLink(newlink1);
							newlink1.setNature(1);
							newlink1.setFlow(nodepair.getTrafficdemand());
							newlink1.setSumflow(slotnum1*X1);
							System.out.println("��ͨ��ʣ�������ǣ�"+(newlink1.getSumflow()-newlink1.getFlow()));
							num_of_transponders=num_of_transponders+2;
							System.out.println("ʹ�õ�transponders���ǣ�"+num_of_transponders);
							//this.setNumTR(num_of_transponders); 
							newlink1.setPhysicallink(newroutepp1.getLinklist());
						     }								
						}						
				}				
			}	
	}
			int maxslot=0;
			HashMap<String, Link> maps=optlayer.getLinklist();
				Iterator<String> iters=maps.keySet().iterator();
				while(iters.hasNext()){
					Link link=(Link)(maps.get(iters.next()));
					if(link.getMaxslot()>maxslot)
						maxslot=link.getMaxslot();
				}
			System.out.println("���������ʹ��slot��"+maxslot);
	}
	//����ֵ��nodepairlist,���������Ĵ�С�����������
	public static ArrayList<NodePair> Rankflow(Layer IPlayer)
	
	{
		ArrayList<NodePair> nodepairlist=new ArrayList<NodePair>(2000);
	    HashMap<String,NodePair>map3=IPlayer.getNodepairlist();
		Iterator<String>iter3=map3.keySet().iterator();
		while(iter3.hasNext()){		 
	     NodePair np=(NodePair)(map3.get(iter3.next()));	     
	     if(nodepairlist.size()==0)
	    	 nodepairlist.add(np);
	     else
	     {
	    	 boolean insert=false;
	    	 for(int i=0;i<nodepairlist.size();i++)
	    	 {
	    		int m_flow=np.getTrafficdemand();
	    		int n_flow=nodepairlist.get(i).getTrafficdemand();
	    		
	    		 if( m_flow>n_flow)
	    		 {  
	    			 nodepairlist.add(i, np);
	    			 insert=true;
	    			 break;
	    		 }
	    		 
	    	 }
	    	 
	    	 if(insert==false)
	    	 nodepairlist.add(np);			 
	     }
		}
		return  nodepairlist;	
	}


public static ArrayList<Integer> spectrumallocationOneRoute(LinearRoute route){
		ArrayList<Link> routelink = route.getLinklist();
			for(Link link : routelink){
				if(route.getSlotsnum() == 0){
					System.out.println("noslots");
					break;
				}
				link.getSlotsindex().clear();

//				
				for(int r = 0; r <= link.getSlotsarray().size() - route.getSlotsnum(); r++){
					//System.out.println(link.getName() + "\t" + link.getSlotsarray().size());
					//System.out.println(link.getName() + "\t" + link.getSlotsindex().size());
					
					//System.out.println(link.getSlotsarray().get(i));
						int s = 1;
						for(int k = r; k < route.getSlotsnum() + r; k ++){
							
							if(link.getSlotsarray().get(k).getoccupiedreqlist().size()!= 0){
								s = 0;
								break;
							}
							
						}
						if(s != 0){
							link.getSlotsindex().add(r);
							
						}
					}
				}	
			
			Link link = routelink.get(0);
			ArrayList<Integer> sameindex = new ArrayList<Integer>();
			sameindex.clear();
			for(int i = 0; i < link.getSlotsindex().size(); i ++){
				int index = link.getSlotsindex().get(i);
				int flag = 1;
				for(Link link2 : routelink){
					if(!link2.getSlotsindex().contains(index)){
						flag = 0;
						break;
					}
				}
				if(flag != 0){
					sameindex.add(link.getSlotsindex().get(i));
				}
			}
			return sameindex;		
		}       
}     
