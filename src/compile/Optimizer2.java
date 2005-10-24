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
	
	public static void instsRearrangement(List atom, List mem){
		//instsRearrangementForAtomMatch(atom);
		instsRearrangement(atom);
		instsRearrangement(mem);
	}
	
	/** 
	 * ������̿����ǽ�ʸ¤����˰�ư������.
	 * �ܥǥ�̿����¤��ؤ��ʤ�
	 */
	public static void instsRearrangement(List insts){
		for(int i=1; i<insts.size(); i++){
			boolean moveok = true; //��ư��ǽȽ��ե饰
			Instruction inst = (Instruction)insts.get(i);
			ArrayList list = inst.getVarArgs();
			
			//�ܥǥ�̿������¤��ؤ��ʤ� -> �ܥǥ�����Ƭ��commit
			if(inst.getKind() == Instruction.COMMIT) break;
			
			//�����������(���֤��Ѥ��ʤ�)
			else if(inst.getKind() == Instruction.NOT) continue;
			
			//����¾���֤��Ѥ������ʤ�̿��
			else if(inst.getKind() == Instruction.FINDATOM
				|| inst.getKind() == Instruction.ANYMEM
				|| inst.getKind() == Instruction.PROCEED) continue;
				
			else if(inst.getKind() == Instruction.GROUP
					|| inst.getKind() == Instruction.BRANCH){
				InstructionList subinsts = (InstructionList)inst.getArg1();
				instsRearrangement(subinsts.insts);
			}
			else
				for(int i2=i-1; i2>0; i2--){
					Instruction inst2 = (Instruction)insts.get(i2);
					ArrayList list2 = inst2.getVarArgs();
					for(int j=0; j<list.size(); j++){
						if((inst2.getOutputType() != -1
							&& list.get(j).equals(inst2.getArg1()))
							|| list2.contains(list.get(j))){
								moveok = false;
								break;
							}
					}
					if(moveok){
						insts.remove(i2+1);
						insts.add(i2, inst);
					}
					else break; 
				}
		}			
			/*
			//LOADMAP̿���SPEC̿���ľ��˰�ư
			for(int i=insts.size()-bodysize; i>1; i--){
				Instruction inst = (Instruction)insts.get(i);
				if(inst.getKind() == Instruction.LOADMAP){
					insts.remove(i);
					insts.add(1, inst);
					break;
				}
			}*/
	}
	    
	//���롼�ײ�
	public static void grouping(List atom, List mem){
		Group g = new Group();
		g.groupingForAtomMatch(atom);
		g.grouping(mem, false);
		g = null;
	}
		
	//�ޥå�����
	public static void mapping(List head){
		//MapMaker mapmaker = new MapMaker(head);
		//Mapmaker.viewMap();
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
	
	Group(){
		var2DefInst = new HashMap();
		Inst2GroupId = new HashMap();
	}
	
	private void initMap(){
		var2DefInst.clear();
		Inst2GroupId.clear();
	}
	
	public void grouping(List insts, boolean isAtomMatch){
		if(((Instruction)insts.get(0)).getKind() != Instruction.SPEC) return;
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			//�����郎������ϥ��롼�ײ���̵�� ����Ū����
			if(inst.getKind() == Instruction.NOT) return;
			//�ܥǥ�̿����ϥ��롼�ײ����ʤ�
			if(inst.getKind() == Instruction.COMMIT) break;
			//���롼���ֹ���꿶��	  
			Inst2GroupId.put(inst, new Integer(i));
			//�ѿ��ֹ梪̿��˥ޥåפ�ĥ��
			if(inst.getOutputType() != -1)
				var2DefInst.put(inst.getArg1(), inst);			
		}
		//viewMap();
		createGroup(insts, isAtomMatch);
		initMap();
	}
		
	public void groupingForAtomMatch(List insts){
		if(((Instruction)insts.get(0)).getKind() != Instruction.SPEC) return;
		for(int i=1; i<insts.size(); i++){
			Instruction branch = (Instruction)insts.get(i);
			if(branch.getKind() == Instruction.COMMIT) break;
			if(branch.getKind() == Instruction.BRANCH){
				InstructionList subinsts = (InstructionList)branch.getArg1();
				grouping(subinsts.insts, true);
			}
		}	
	}	

	//���롼��ʬ��
	//̿���ֹ梪���롼���ֹ�Ȥ�, Ʊ�����롼�פ�����̿���
	//Ʊ�����롼���ֹ�إޥåפ�ĥ����
	private void createGroup(List insts, boolean isAtomMatch){
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.COMMIT) break;
			Object group = null;
			Object changegroup = null;
			ArrayList list = inst.getVarArgs();
			if(list.isEmpty()) continue;
		
			//if(insth.getKind() == Instruction.LOADMAP) continue;
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).equals(new Integer(0))) continue;
				if (list.get(j).equals(new Integer(1)) && isAtomMatch) continue;
				group = Inst2GroupId.get(var2DefInst.get(list.get(j)));
				changegroup = Inst2GroupId.get(inst);
				changeMap(changegroup, group);
			}
			if(inst.getKind() == Instruction.ANYMEM
			  || inst.getKind() == Instruction.LOCKMEM){
				//����������̿�������Ʊ�����롼�פ�°�����Ȥˤ���
				//��������Ū����
				//{a}, {b, $p}, {c} �����������ϼºݤϰ㤦���롼��
				//{a(X)}, {a(Y)} ������������Ʊ�����롼��
				//�ɤ����̤���?
				for(int i2 = 1; i2 < insts.size(); i2++){
					Instruction inst2 = (Instruction)insts.get(i2);
					if(inst2.getKind() == Instruction.COMMIT) break;
					if(inst2.getKind() == Instruction.ANYMEM
						|| inst.getKind() == Instruction.LOCKMEM){
							group = Inst2GroupId.get(inst);
							changegroup = Inst2GroupId.get(inst);
							changeMap(changegroup, group);
						}
				}
			}
		  
		}
		//�ޥå�������λ
		
		//GROUP����
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.COMMIT) break;
			//if(inst.getKind() == Instruction.LOADMAP) continue;
			Object group = Inst2GroupId.get(inst);
			InstructionList subinsts = new InstructionList();
			subinsts.add(new Instruction(Instruction.SPEC, 0, 0));
			for(int i2=i; i2<insts.size(); i2++){
				Instruction inst2 = (Instruction)insts.get(i2);
				if(inst2.getKind() == Instruction.COMMIT) break;
				if(group.equals(Inst2GroupId.get(inst2))){
					subinsts.add(inst2);
					insts.remove(i2);
					i2 -= 1;
				}
			}
			subinsts.add(new Instruction(Instruction.PROCEED));
			insts.add(i, new Instruction(Instruction.GROUP, subinsts));
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