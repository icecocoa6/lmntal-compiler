/*
 * ������: 2003/11/30
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.Functor;

/**
 * @author Mizuno
 *
 */
public class Optimizer {
	/**	
	 * �Ϥ��줿̿������Ŭ�����롣
	 * @param list ��Ŭ��������̿���󡣺��ΤȤ���ܥǥ�̿�����Ϥ���뤳�Ȥ��ꤷ�Ƥ��롣
	 */
	public static void optimize(List list) {
		//TODO relink̿���getlink/inheritlink̿����Ѵ����롩
		reuseAtom(list);
		removeUnnecessaryRelink(list);
	}
	
	/**	
	 * ���ȥ�����Ѥ�Ԥ���
	 * @param list ��Ŭ��������̿���󡣺��ΤȤ���ܥǥ�̿�����Ϥ���뤳�Ȥ��ꤷ�Ƥ��롣
	 */
	private static void reuseAtom(List list) {
		/////////////////////////////////////////////////
		//
		// �����Ѥ��륢�ȥ���Ȥ߹�碌����ꤹ��
		//
		
		// removeatom/newatom/getlink̿��ξ����Ĵ�٤�
		HashMap removedAtoms = new HashMap(); // functor -> set of atomId
		HashMap createdAtoms = new HashMap(); // functor -> set of atomId
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM:
					//�Ȥꤢ�����������
					if (inst.getIntArg2() == 0) {
						Integer atomId = (Integer)inst.getArg1();
						Functor functor = (Functor)inst.getArg3();
						addToMap(removedAtoms, functor, atomId);
					}
					break;
				case Instruction.NEWATOM:
					//�Ȥꤢ�����������
					if (inst.getIntArg2() == 0) {
						Integer atomId = (Integer)inst.getArg1();
						Functor functor = (Functor)inst.getArg3();
						addToMap(createdAtoms, functor, atomId);
					}
					break;
				case Instruction.GETLINK:
					//��Ǿ����ư����
					getlinkInsts.put(inst.getArg1(), inst);
					break;
			}
		}
		
		//�����Ѥ��륢�ȥ���Ȥ߹�碌�����

		//���������Υ��ȥ�ID -> �����Ѹ�Υ��ȥ�ID
		HashMap reuseMap = new HashMap();
		//�����Ѥ���륢�ȥ��ID��reuseMap���ͤ����ꤵ��Ƥ���ID�ν����
		HashSet reuseAtoms = new HashSet(); 

		//������ˤ��롢Ʊ��̾���Υ��ȥ������Ѥ���
		//�Ȥꤢ�����ǤƤ�������б�������	
		Iterator functorIterator = removedAtoms.keySet().iterator();
		while (functorIterator.hasNext()) {
			Functor functor = (Functor)functorIterator.next();
			if (createdAtoms.containsKey(functor)) {
				Iterator removedAtomIterator = ((HashSet)removedAtoms.get(functor)).iterator();
				Iterator createdAtomIterator = ((HashSet)createdAtoms.get(functor)).iterator();
				while (removedAtomIterator.hasNext() && createdAtomIterator.hasNext()) {
					Integer removeAtomId = (Integer)removedAtomIterator.next();
					reuseMap.put(createdAtomIterator.next(), removeAtomId);
					reuseAtoms.add(removeAtomId);
					//�����Ѥ��Ȥ߹�碌����ޤä���ΤϺ������
					//�ʤ��θ��³�������ȥ�̾���ۤʤ���ʤɤκ����Ѥ��Ȥ߹�碌����ꤹ������Τ��ᡣ��
					removedAtomIterator.remove();
					createdAtomIterator.remove();
				}
			}
		}
		
		//TODO ���ȥ�̾���ۤʤ��Τκ����Ѥ��Ȥ߹�碌����ꤹ�륳���ɤ򤳤��˽�

		//��������ˡ��ɽ���ʥǥХå��ѡ�
		it = reuseMap.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			System.out.println(key + " " + reuseMap.get(key));
		}
		
		//////////////////////////////////////////////////
		//
		// ���ȥ������Ѥ���褦��̿�������������
		//

										
		// ���ȥ�����Ѥ򤹤��getlink̿�������inherit̿�᤬����������Τǡ�
		// getlink̿���̿�������Ƭ�˰�ư���롣
		// TODO Ŭ�ڤʰ�ư���򸫤Ĥ���
		it = getlinkInsts.values().iterator();
		while (it.hasNext()) {
			list.add(0, it.next());
		}

		//���פˤʤä�removeatom/newatom̿������
		ListIterator lit = list.listIterator(getlinkInsts.size());
		while (lit.hasNext()) {
			Instruction inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM: {
					Integer atomId = (Integer)inst.getArg1();
					if (reuseAtoms.contains(atomId)) {
						lit.remove();
					}
					break;
				}
				case Instruction.NEWATOM: {
					Integer atomId = (Integer)inst.getArg1();
					if (reuseMap.containsKey(atomId)) {
						lit.remove();
					}
					break;
				}
				case Instruction.GETLINK: {
					//��Ƭ�˰�ư�����Τǽ���
					lit.remove();
					break;
				}
			}
		}

		//���ȥ�ID���դ��ؤ�
		Instruction.changeAtomId(list, reuseMap);
	}

	/**
	 * HashMap���ͤ���ʣ�����ͤν����ɽ��HashSet�Ǥ����Τ��Ф����ͤ��ɲä���
	 * @param map �ͤ��ɲä���HashMap
	 * @param key ����
	 * @param value �ɲä�������
	 */
	private static void addToMap(HashMap map, Object key, Object value) {
		HashSet set = (HashSet)map.get(key);
		if (set == null) {
			set = new HashSet();
			set.add(value);
			map.put(key, set);
		} else {
			set.add(value);
		}
	}			

	/**
	 * ��Ĺ��relink/inheritlink̿������ޤ���
	 * @param list
	 */
	private static void removeUnnecessaryRelink(List list) {
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		ArrayList remove = new ArrayList();
		
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.GETLINK:
					getlinkInsts.put(inst.getArg1(), inst);
					break;
				case Instruction.INHERITLINK:
					Instruction getlink = (Instruction)getlinkInsts.get(inst.getArg3());
					if (getlink.getArg2().equals(inst.getArg1()) &&  // <- atomID
						 getlink.getArg3().equals(inst.getArg2())) { // <- pos
						//��Ĺ�ʤΤǽ���
						remove.add(getlink);
						remove.add(inst);
					}
					break;
			}
		}
		list.removeAll(remove);
	}
}