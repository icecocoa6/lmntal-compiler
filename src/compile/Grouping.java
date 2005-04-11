/*
 * ������: 2004/11/10
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
/**
 * @author sakurai
 *
 * �إå�̿����򥰥롼�פ��Ȥ�ʬ����
 * group[ findatom
 *        deref
 *        func
 *        insint ]
 * group [������]
 * �Τ褦�ʷ��ˤʤ�
 */
public class Grouping {
	HashMap var2DefInst;         //�ѿ��ֹ梪�ѿ��ֹ���������̿��
	HashMap Inst2GroupId;        //̿�ᢪ���롼�׼����ֹ�
		
	public Grouping(List head){
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
		
		guardMoveOptimize(head);
		//mapView();
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
	//��������
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
				GuardOptimizer.guardMove((List)subinsts.insts);
			}
		}
	}
	
	//�������줿�ޥåפγ�ǧ �ǥХå���
	public void mapView(){
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
