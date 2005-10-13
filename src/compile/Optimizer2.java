/*
 * ������: 2004/11/04
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;


/**
 * @author sakurai
 *
 * ��Ŭ����Ԥ��᥽�åɤ���ĥ��饹����2�� 
 *
 */
public class Optimizer2 {
	
	/** 
	 * ������̿����ǽ�ʸ¤����˰�ư������.
	 * �Ȥꤢ���������إå�̿����ˤ��ä�̿��ΰ�ư�Ϲͤ��ʤ�.
	 */
	public static void guardMove(List head){
		for(int hid=0; hid<head.size()-1; hid++){
			Instruction insth = (Instruction)head.get(hid);
			ArrayList list = insth.getVarArgs();
			boolean moveOk = true;
			if(insth.getKind() == Instruction.NOT)
				continue;
			else if(insth.getKind() == Instruction.GROUP){
				InstructionList subinsts = (InstructionList)insth.getArg1();
				Optimizer2.guardMove((List)subinsts.insts);
			}
			else 
				for(int hid2=hid-1; hid2>0; hid2--){
					Instruction insth2 = (Instruction)head.get(hid2);
					ArrayList list2 = insth2.getVarArgs();
					for(int i=0; i<list.size(); i++){
						if((insth2.getOutputType() != -1
						   && list.get(i).equals(insth2.getArg1()))
						   || list2.contains(list.get(i))){
							moveOk = false;
							break;
						   }
					}
					if(moveOk){
						head.remove(hid2+1); //��ư�оݤ�̿��insth�ΰ��֤�hid2+1
						head.add(hid2, insth); //hid2�˰�ư
					}
					else break;
				}
		}
		/*
		//LOADMAP̿���SPEC̿���ľ��˰�ư
		for(int hid=head.size()-1; hid>0; hid--){
			Instruction insth = (Instruction)head.get(hid);
			if(insth.getKind() == Instruction.LOADMAP){
				head.remove(hid);
				head.add(1, insth);
				break;
			}
		}*/
	}
	    
		//���롼�ײ�
		public static void grouping(List head){
			Group group = new Group(head);	
		}
		
		//�ޥå�����
		public static void mapping(List head){
			//apMaker mapmaker = new MapMaker(head);
			//mapmaker.viewMap();
		}
 }
 
	/**
	 * �إå�̿����򥰥롼�פ��Ȥ�ʬ����
	 * group[ findatom
	 *        deref
	 *        func
	 *        insint ]
	 * group [������]
	 * �Τ褦�ʷ��ˤʤ�
	 */
	class Group {
		HashMap var2DefInst;         //�ѿ��ֹ梪�ѿ��ֹ���������̿��
		HashMap Inst2GroupId;        //̿�ᢪ���롼�׼����ֹ�
		
		Group(List head){
			var2DefInst = new HashMap();
			Inst2GroupId = new HashMap();
			
			if(((Instruction)head.get(0)).getKind() != Instruction.SPEC) return;
			if(((Instruction)head.get(head.size()-1)).getKind() != Instruction.JUMP) return;

			//���롼���ֹ���꿶��
			//spec, jump�ʳ������Ƥ�̿��˹��ֹ�򥰥롼���ֹ�Ȥ��Ƴ�꿶��
			for(int hid=1; hid<head.size()-1; hid++){
				Inst2GroupId.put(head.get(hid), new Integer(hid));
			}
		
			//�ѿ��ֹ梪̿���ֹ�˥ޥåפ�ĥ��
			for(int hid=1; hid<head.size()-1; hid++){
				Instruction insth = (Instruction)head.get(hid);
				if (insth.getOutputType() != -1) {
					var2DefInst.put(insth.getArg1(), insth);
				}
			}
			createGroup(head);
		}
	
		//���롼��ʬ��
		//̿���ֹ梪���롼���ֹ�Ȥ�, Ʊ�����롼�פ�����̿���
		//Ʊ�����롼���ֹ�إޥåפ�ĥ����
		private void createGroup(List head){
			for(int hid=1; hid<head.size()-1; hid++){
				Instruction insth = (Instruction)head.get(hid);
				Object group = null;
				Object changegroup = null;
				ArrayList list = insth.getVarArgs();
			
				//�����郎���ä����, ���Ƥ�̿���Ʊ�����롼�פˤ���.
				//����Ū����
				if(insth.getKind() == Instruction.NOT){
					allInstsToSameGroup();
					break;
				}
				//else if(insth.getKind() == Instruction.LOADMAP) continue;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).equals(new Integer(0))) continue;
					group = Inst2GroupId.get(var2DefInst.get(list.get(i)));
					changegroup = Inst2GroupId.get(insth);
					changeMap(changegroup, group);
				}
				if(insth.getKind() == Instruction.ANYMEM
				  || insth.getKind() == Instruction.LOCKMEM){
					//����������̿�������Ʊ�����롼�פ�°�����Ȥˤ���
					//��������Ū����
					//{a}, {b, $p}, {c} �����������ϼºݤϰ㤦���롼��
					//{a(X)}, {a(Y)} ������������Ʊ�����롼��
					//�ɤ����̤���?
					for(int i = 1; i < head.size()-1; i++){
						Instruction inst = (Instruction)head.get(i);
						if(inst.getKind() == Instruction.ANYMEM
							|| inst.getKind() == Instruction.LOCKMEM){
								group = Inst2GroupId.get(insth);
								changegroup = Inst2GroupId.get(inst);
								changeMap(changegroup, group);
							}
					}
				}
			  
			}
			//�ޥå�������λ
		
			//GROUP����
			for(int hid=1; hid<head.size()-1; hid++){
				Instruction insth = (Instruction)head.get(hid);
				//if(insth.getKind() == Instruction.LOADMAP) continue;
				Object group = Inst2GroupId.get(insth);
				InstructionList subinsts = new InstructionList();
				subinsts.add(new Instruction(Instruction.SPEC,0,0));
				for(int hid2=hid; hid2<head.size()-1; hid2++){
					Instruction insth2 = (Instruction)head.get(hid2);
					if(group.equals(Inst2GroupId.get(insth2))){
						subinsts.add(insth2);
						head.remove(hid2);
						hid2 -= 1;
					}
				}
				subinsts.add(new Instruction(Instruction.PROCEED));
				head.add(hid, new Instruction(Instruction.GROUP, subinsts));
			}
			
			//viewMap();
		}
	
		//Inst2GroupId����, ��group1�������ƤΥ������Ф�, ��group2�إޥåפ�ĥ���ؤ���.
		public void changeMap(Object group1, Object group2){
			Iterator it = Inst2GroupId.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (group1.equals(Inst2GroupId.get(key))) {
					Inst2GroupId.put(key, group2);
				}
			}
		}
	
		//���Ƥ�̿���Ʊ�����롼�פˤ���.
		//���ߤ���������
		private void allInstsToSameGroup(){
			Iterator it = Inst2GroupId.keySet().iterator();
			while(it.hasNext()){
				Object key = it.next();
				Inst2GroupId.put(key, new Integer(1));
			}
		}
	
		//GROUP����¤��ؤ��ˤ���Ŭ��
		private void guardMoveOptimize(List list){
			//������̿��ΰ�ư
			for(int i=0; i<list.size(); i++){
				Instruction inst = (Instruction)list.get(i);
				if(inst.getKind() == Instruction.GROUP){
					InstructionList subinsts = (InstructionList)inst.getArg1();
					Optimizer2.guardMove((List)subinsts.insts);
				}
			}
		}
		
		
		//�������줿�ޥåפγ�ǧ �ǥХå���
		public void viewMap(){
			Set set1 = var2DefInst.entrySet();
			Set set2 = Inst2GroupId.entrySet();
	
			Iterator it1 = set1.iterator();
			Iterator it2 = set2.iterator();
		
			System.out.println("var2DefInst :- ");
			while(it1.hasNext()){
				Map.Entry mapentry = (Map.Entry)it1.next();
				System.out.println(mapentry.getKey() + "/" + mapentry.getValue());
			}

			System.out.println("Inst2GroupId :- ");
			while(it2.hasNext()){
				Map.Entry mapentry = (Map.Entry)it2.next();
				System.out.println(mapentry.getKey() + "/" + mapentry.getValue());
			}	
		}
	}
	
	/*
		 //�ѿ��ֹ�������(?)�ؤΥޥåפ��������륯�饹
		 //findatom	[1, 0, a_0]
		 //deref		[2, 1, 0, 0]
		 //���ξ��1->0, 2->1�˥ޥåפ�ĥ��
		 //�Ĥޤ����Ƥ��ѿ��ֹ��, �����ֹ��������뤭�ä����Ȥʤä�
		 //findatom��anymem��������줿�ֹ�˥ޥåפ�ĥ��
	class MapMaker{
		HashMap map;
		List list;
		public MapMaker(List insts){
			map = new HashMap();
			list = insts;
			makeMap();
			//LOADMAP̿���SPEC̿��μ�������
			insts.add(1, new Instruction(Instruction.LOADMAP, map));
		}
		 	
		public void makeMap(){
		  for(int i=0; i<list.size(); i++){
			  Instruction inst = (Instruction)list.get(i);
			  List defvars = new ArrayList(); //��������ѿ��ֹ�Υꥹ��
			  List usevars = new ArrayList(); //��̿��ǻ��Ѥ����ѿ��ֹ�Υꥹ��
			  if(inst.getOutputType() != -1){
				if(inst.getKind() == Instruction.FINDATOM 
					|| inst.getKind() == Instruction.ANYMEM){
					defvars.add(inst.getArg2());
					map.put(inst.getArg1(), defvars);
				}
				else {
					usevars = inst.getVarArgs();
					if(usevars.isEmpty()) {
						//allocatom�ʤɽ��ϰ����Τߤ�̿��ˤϤȤꤢ����[0]�إޥåԥ�
						defvars.add(new Integer(0));
						map.put(inst.getArg1(), defvars);
					} 
					else map.put(inst.getArg1(), getDefVars(usevars, i));
				} 
			  }
			  else continue;
		  }
		}
	
		//usevars����ѿ��ֹ��������븵�Ȥʤä�findatom, anymem
		//����������ѿ��ֹ�Υꥹ�Ȥ��֤�
		//õ��findatom, anymem̿���̿�����pc���ܰ����ˤ���Ϥ�
		public List getDefVars(List usevars, int pc){
			List defvars = new ArrayList();
			List vars = new ArrayList();
			for(int i=0; i<usevars.size(); i++){
				Object usevar = usevars.get(i);
				for(int j=pc-1; j>0; j--){
					Instruction inst = (Instruction)list.get(j);
					if(inst.getOutputType() != -1){
						if(inst.getKind() == Instruction.FINDATOM || inst.getKind() == Instruction.ANYMEM) {
							if(inst.getArg1().equals(usevar)) {
								defvars.add(usevar);
								break;
							}
							else continue;
						}
						else if(inst.getArg1().equals(usevar)){
							vars = inst.getVarArgs();
							if(vars.isEmpty()) defvars.add(new Integer(0));
							else defvars.addAll(getDefVars(vars, j));
							break;
						}
					}
					else continue;
				}
			}
			return defvars;
		}
	 	
		public void viewMap(){
			Set set = map.entrySet();
			Iterator it = set.iterator();

			System.out.println("Map :- ");
			while(it.hasNext()){
				Map.Entry mapentry = (Map.Entry)it.next();
				System.out.println(mapentry.getKey() + "/" + mapentry.getValue());
			}	
		}
		
	 }
	 */