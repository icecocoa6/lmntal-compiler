package compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import runtime.Instruction;
import runtime.InstructionList;

/**
 * ̿����Υ��롼�ײ����Ԥ����饹��
 * @author sakurai
 */
public class Grouping {
	/** �ѿ��ֹ���ѿ��ֹ���������̿��Υޥå� */
	private HashMap var2DefInst;
	/** ̿��ͥ��롼�׼����ֹ�Υޥå� */
	private HashMap Inst2GroupId;
	/** ���롼��̿����η׻�������*/
	private HashMap group2Cost;
	
	Instruction spec;
	Grouping(){
		var2DefInst = new HashMap();
		Inst2GroupId = new HashMap();
		group2Cost = new HashMap();
		spec = null;
	}
	
	/*
	 * �ޥåפν����
	 */
	private void initMap(){
		var2DefInst.clear();
		Inst2GroupId.clear();
	}
	
	/** ̿����Υ��롼�ײ�
	 *  ���ȥ��Ƴ�ƥ����������Ƴ�ƥ�����������ʬ���롣
	 *  @atom ���ȥ��Ƴ�ƥ�������̿����
	 *  @param ���Ƴ�ƥ�������̿����
	 * */
	public void grouping(List atom, List mem){
		//���ȥ��Ƴ�ƥ�����
		if(!Optimizer.fMerging) groupingInstsForAtomMatch(atom);
		//���Ƴ�ƥ�����
		groupingInsts(mem, false);
	}
	
	/** ̿����Υ��롼�ײ� 
	 * @param insts ���롼�ײ�����̿����
	 * @param isAtomMatch insts�����ȥ��Ƴ�ƥ��������ɤ����Υե饰
	 * */
	public void groupingInsts(List insts, boolean isAtomMatch){
		if(((Instruction)insts.get(0)).getKind() != Instruction.SPEC) return;
		spec = (Instruction)insts.get(0);
		int last = -1;
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			//�����郎������ϥ��롼�ײ���̵�� ����Ū����
			if(inst.getKind() == Instruction.NOT) return;
			//if (inst.getKind() == Instruction.COMMIT) break;
			if(inst.getKind() == Instruction.COMMIT) continue;
			if (inst.getKind() == Instruction.JUMP) continue;
			//���롼���ֹ���꿶��	  
			Inst2GroupId.put(inst, new Integer(i));
			//�ѿ��ֹ梪̿��˥ޥåפ�ĥ��
			//System.out.println("instruction = "+inst);
			if(inst.getOutputType() != -1)
				var2DefInst.put(inst.getArg1(), inst);
			last = i+1;
		}
		if(isAtomMatch){
			for(int i=1; i<insts.size(); i++){
				Instruction inst = (Instruction)insts.get(i);
				if(inst.getKind() == Instruction.FUNC){
					Inst2GroupId.put(inst, new Integer(last));
					var2DefInst.put(inst.getArg1(), inst);
					break;
				}
			}
		}
		//viewMap();
		createGroup(insts, isAtomMatch);
		initMap();
	}
	
	/** ̿����Υ��롼�ײ� ���ȥ��Ƴ�ƥ����� 
	 * @param insts ���롼�ײ����륢�ȥ��Ƴ�ƥ�������̿����
	 * */
	public void groupingInstsForAtomMatch(List insts){
		if(((Instruction)insts.get(0)).getKind() != Instruction.SPEC) return;
		for(int i=1; i<insts.size(); i++){
			Instruction branch = (Instruction)insts.get(i);
			//if(branch.getKind() == Instruction.COMMIT) break;
			if(branch.getKind() == Instruction.BRANCH){
				InstructionList subinsts = (InstructionList)branch.getArg1();
				groupingInsts(subinsts.insts, true);
			}
		}	
	}	

	/**
	 * �ޥåפ˴�Ť��ƥ��롼�פ��������롣
	 * var2DefInst�򻲾Ȥ������롼�׼����ֹ椬Ʊ��̿���Ʊ�����롼�פȤ��롣 
	 * @param insts ̿����
	 * @param isAtomMatch insts�����ȥ��Ƴ�ƥ��������ɤ����Υե饰
	 * */
	private void createGroup(List insts, boolean isAtomMatch){
		//�ޥåפν񤭴���
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.COMMIT
				|| inst.getKind() == Instruction.JUMP) break;
			Object group = null;
			Object changegroup = null;
			ArrayList list = inst.getVarArgs(new HashMap());
			if(list.isEmpty()) continue;
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).equals(new Integer(0))) continue;
				//if (list.get(j).equals(new Integer(1)) && isAtomMatch) continue;
				group = Inst2GroupId.get(var2DefInst.get(list.get(j)));
				changegroup = Inst2GroupId.get(inst);
				changeMap(changegroup, group);
			}
		}
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.COMMIT
				|| inst.getKind() == Instruction.JUMP) break;
			Object group = null;
			Object changegroup = null;
			boolean norules = false;
			boolean meminsttype = false; //anymem -> true  lockmem -> false
			int natoms = -1;
			int nmems = -1;
			if(inst.getKind() == Instruction.ANYMEM
					  || inst.getKind() == Instruction.LOCKMEM){
						//��򥰥롼��ʬ��
						//�롼���̵ͭ�ˤ����� {a, $p, @p} �� {a, $p}�ϰ㤦���롼��
						//���ȥ�, ����ο��ˤ����� {a, b} �� {a, a, b, b} �ϰ㤦���롼��
						//���ΤȤ����ξ��϶��̤Ǥ��ʤ��Τ�Ʊ�����롼�פȤ���
						//{a(X), b(X)}, {a(Y)}, b(Y) �����������ϰ㤦���롼��
						//$in, $out �ο��Ƕ��̲�ǽ?
						//todo �ɤ����̤��뤫�ͤ���
				if(inst.getKind() == Instruction.ANYMEM) meminsttype = true;
				else meminsttype = false;
						for(int i2 = i+1; i2 < insts.size(); i2++){
							Instruction inst2 = (Instruction)insts.get(i2);
							switch(inst2.getKind()){
							case Instruction.COMMIT:
							case Instruction.JUMP:
								break;
							case Instruction.NORULES:
								if((inst2.getArg1()).equals(inst.getArg1())) norules = true;
								break;
							case Instruction.NATOMS:
								if((inst2.getArg1()).equals(inst.getArg1())) natoms = inst2.getIntArg2();
								break;
							case Instruction.NMEMS:
								if((inst2.getArg1()).equals(inst.getArg1())) nmems = inst2.getIntArg2();
								break;
							default : break;
							}
						}
						for(int j = i+1; j < insts.size(); j++){
							Instruction inst2 = (Instruction)insts.get(j);
							switch(inst2.getKind()){
							case Instruction.COMMIT:
							case Instruction.JUMP:
								break;
							case Instruction.ANYMEM:
							case Instruction.LOCKMEM:
								boolean eqgroup = true;
								boolean norules2 = false;
								boolean meminsttype2;
								if(inst2.getKind() == Instruction.ANYMEM) meminsttype2 = true;
								else meminsttype2 = false;
								int natoms2 = -1;
								int nmems2 = -1;
								for(int j2 = j+1; j2 < insts.size(); j2++){
									Instruction inst3 = (Instruction)insts.get(j2);
									switch(inst3.getKind()){
									case Instruction.COMMIT:
									case Instruction.JUMP:
										break;
									case Instruction.NORULES:
										if((inst3.getArg1()).equals(inst2.getArg1())) norules2 = true;
										break;
									case Instruction.NATOMS:
										if((inst3.getArg1()).equals(inst2.getArg1())) natoms2 = inst3.getIntArg2();
										break;
									case Instruction.NMEMS:
										if((inst3.getArg1()).equals(inst2.getArg1())) nmems2 = inst3.getIntArg2();
										break;
									default : break;
									}
								}
								if(meminsttype != meminsttype2) eqgroup = false;
								if(natoms != natoms2) eqgroup = false;
								if(nmems != nmems2) eqgroup = false;
								if(natoms == -1 || nmems == -1 ||
									natoms2 == -1 || nmems2 == -1) eqgroup = true;
								if(norules != norules2) eqgroup = false;
								if(eqgroup){
									group = Inst2GroupId.get(inst);
									changegroup = Inst2GroupId.get(inst2);
									changeMap(changegroup, group);
								}
								break;
							default : break;
							}
						}
					}
		}
		
		//�ޥå׽񤭴�����λ
		//GROUP����
		for(int i=1; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.COMMIT
				|| inst.getKind() == Instruction.JUMP) break;
			Object group = Inst2GroupId.get(inst);
			InstructionList subinsts = new InstructionList();
			//SPEC������?
			subinsts.add(spec);
			for(int i2=i; i2<insts.size(); i2++){
				Instruction inst2 = (Instruction)insts.get(i2);
				if(inst2.getKind() == Instruction.COMMIT
					|| inst.getKind() == Instruction.JUMP) break;
				if(group.equals(Inst2GroupId.get(inst2))){
					subinsts.add(inst2);
					insts.remove(i2);
					i2 -= 1;
				}
			}
			subinsts.add(new Instruction(Instruction.PROCEED));
			Instruction groupinst = new Instruction(Instruction.GROUP, subinsts);
			insts.add(i, groupinst);
			Cost cost = new Cost();
			cost.evaluateCost(subinsts.insts);
//          �ǥХå��ѥ�����ɽ��
//			System.out.print(groupinst + "\n cost = ");
//			for(int s = 0; s<cost.costvalueN.size(); s++){
//				if(s>0){
//					if(((Integer)cost.costvalueN.get(s)).intValue() > 1)
//						System.out.print(((Integer)cost.costvalueN.get(s)).intValue());
//				} else System.out.print(((Integer)cost.costvalueN.get(s)).intValue());
//				if(s<cost.costvalueM.size()){
//					if(((Integer)cost.costvalueM.get(s)).intValue() == 1)
//						System.out.print("m");
//					else if(((Integer)cost.costvalueM.get(s)).intValue() > 1)
//						System.out.print("m^"+((Integer)cost.costvalueM.get(s)).intValue());
//				}
//				if(s==1)System.out.print("n");
//				else if(s>1)System.out.print("n^"+s);
//				
//				if(s != cost.costvalueN.size()-1) System.out.print(" + ");
//			}
//			System.out.println("");
			
			group2Cost.put(groupinst, cost);
		}
//		Group̿����¤��ؤ�
		//���ȥ��Ƴ�ƥ��ȤǤϺǽ��func̿���ޤ॰�롼�פΰ��֤���Ƭ�ΤޤޤȤ���
		int groupstart = 0;
		if(isAtomMatch){
			for(int i=0; i<insts.size(); i++){
				Instruction inst = (Instruction)insts.get(i);
				if(inst.getKind() == Instruction.GROUP){
					groupstart = i+1;
					break;
				}
			}
		}
		for(int i=groupstart; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			if(inst.getKind() == Instruction.GROUP){
				Cost cost1 = null;
				if(group2Cost.containsKey(inst)) cost1 = (Cost)group2Cost.get(inst);
				else break;
				for(int j=i-1; j>0; j--){
					Instruction inst2 = (Instruction)insts.get(j);
					if (inst2.getKind() == Instruction.GROUP){
						Cost cost2 = null;
						if(group2Cost.containsKey(inst2)) cost2 = (Cost)group2Cost.get(inst2);
						else break;
						if(cost2.igtCost(cost1)){
							insts.add(j--, inst);
							insts.remove(i+1);
							continue;
						}
					}
					else break;
				}
			}
		}
		//viewMap();
	}

	/**
	 * �ޥå�Inst2GroupId�ν񤭴���
	 * �ޥåפ��⡢�� group1 ��������Ƥ����Ǥ��� group2 �˽񤭴�����
	 * @param group1 �񤭴���������
	 * @param group2 �񤭴��������
	 * */
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

class Cost {
	List costvalueN;
	List costvalueM;
	HashMap memend;
	int n;
	
	Cost(){
		costvalueN = new ArrayList();
		costvalueM = new ArrayList();
		memend = new HashMap();
		n = 0;
	}
	
	public void evaluateCost(List insts){
		int vn = 0;
		int vm = 0;
		for(int i=0; i<insts.size(); i++){
			Instruction inst = (Instruction)insts.get(i);
			switch(inst.getKind()){
			case Instruction.FINDATOM:
				costvalueN.add(n++, new Integer(vn));
				costvalueM.add(new Integer(vm));
				vn = 1;
				break;
			case Instruction.ANYMEM:
				if(costvalueN.size() <= n) costvalueN.add(new Integer(++vn));
				else costvalueN.set(n, new Integer(++vn));
				for(int j=insts.size()-1; j>i; j--) {
					Instruction inst2 = (Instruction)insts.get(j);
					switch(inst2.getKind()){
					case Instruction.PROCEED:
						memend.put(inst.getArg1(), inst2);
						break;
					case Instruction.NATOMS:
					case Instruction.NMEMS:
					case Instruction.NORULES:
						if(!Optimizer.fGuardMove && inst2.getIntArg1() == inst.getIntArg1()){
							if(memend.containsKey(inst2.getArg1())){
								memend.put(inst2.getArg1(), inst2);
								j = -1;
								break;
							}
						}
						break;
					}
				}
				if(costvalueM.size() <= n) costvalueM.add(new Integer(++vm));
				else costvalueM.set(n, new Integer(++vm));
				break;
			case Instruction.NATOMS:
			case Instruction.NMEMS:
			case Instruction.NORULES:
				if(Optimizer.fGuardMove) {vn++; break;}
				if(costvalueN.size() <= n) costvalueN.add(new Integer(++vn));
				else costvalueN.set(n, new Integer(++vn));
				if(memend.containsValue(inst)){
//					while(costvalueM.size() <=n) costvalueM.add(new Integer(vm));
//						costvalueM.set(n, new Integer(vm--));
					vm--;
				}
				break;
			case Instruction.PROCEED:
				if(memend.containsValue(inst)){
//					while(costvalueM.size() <=n) costvalueM.add(new Integer(vm));
//						costvalueM.set(n, new Integer(vm--));
					vm--;
				}
				break;
			default:
				if(costvalueN.size() <= n) costvalueN.add(new Integer(++vn));
				else costvalueN.set(n, new Integer(++vn));
				
				break;
			}
		}
		if(costvalueN.size() == 0) costvalueN.add(vn);
	}
	
	public boolean igtCost(Cost c){
		List costsn = (ArrayList)c.costvalueN;
		List costsm = (ArrayList)c.costvalueM;
		if(costvalueN.size() > costsn.size()) return true;
		else if(costvalueN.size() < costsn.size()) return false;
		else {
			for(int i=costvalueN.size()-1, j=costvalueM.size()-1; i>=0 && j>=0; i--, j--){
				if(costvalueM.size() > j && costsm.size() > j){
					if(((Integer)costvalueM.get(j)).intValue() > ((Integer)costsm.get(j)).intValue()) return true;
					else if(((Integer)costvalueM.get(j)).intValue() < ((Integer)costsm.get(j)).intValue())return false;
				}
				if(((Integer)costvalueN.get(i)).intValue() > ((Integer)costsn.get(i)).intValue()) return true;
				else if(((Integer)costvalueN.get(i)).intValue() < ((Integer)costsn.get(i)).intValue()) return false;
				else continue;
			}
			return false;
		}
	}
}