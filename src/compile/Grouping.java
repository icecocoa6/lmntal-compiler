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
	
	Grouping(){
		var2DefInst = new HashMap();
		Inst2GroupId = new HashMap();
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
		groupingInstsForAtomMatch(atom);
		//���Ƴ�ƥ�����
		groupingInsts(mem, false);
	}
	
	/** ̿����Υ��롼�ײ� 
	 * @param insts ���롼�ײ�����̿����
	 * @param isAtomMatch insts�����ȥ��Ƴ�ƥ��������ɤ����Υե饰
	 * */
	public void groupingInsts(List insts, boolean isAtomMatch){
		if(((Instruction)insts.get(0)).getKind() != Instruction.SPEC) return;
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
			ArrayList list = inst.getVarArgs();
			if(list.isEmpty()) continue;
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
				//todo �ɤ����̤��뤫�ͤ���
				for(int i2 = 1; i2 < insts.size(); i2++){
					Instruction inst2 = (Instruction)insts.get(i2);
					switch(inst2.getKind()){
					case Instruction.COMMIT:
					case Instruction.JUMP:
						break;
					case Instruction.ANYMEM:
					case Instruction.LOCKMEM:
						group = Inst2GroupId.get(inst);
						changegroup = Inst2GroupId.get(inst);
						changeMap(changegroup, group);
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
			subinsts.add(new Instruction(Instruction.SPEC, 0, 0));
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
			insts.add(i, new Instruction(Instruction.GROUP, subinsts));
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