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
	 * ��Ŭ�����롣
	 * TODO �����Ѥ�������Υ��ȥ�κ����ѡʸ���������Τߡ�
	 * TODO ̿��������ʰ�ư������ˤθ�ΨŪ�ʼ���
	 * @param list ��Ŭ��������̿����
	 */
	public static void optimize(List list) {
		
		/////////////////////////////////////////////////
		//
		// �����Ѥ��륢�ȥ���Ȥ߹�碌����ꤹ��
		//
		
		// removeatom/newatom/getlink/inherit̿��ξ����Ĵ�٤�
		HashMap removedAtoms = new HashMap(); // functor -> set of atomId
		HashMap createdAtoms = new HashMap(); // functor -> set of atomId
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		HashMap inheritlinkInsts = new HashMap(); // linkId -> inheritlink instruction
		
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
					getlinkInsts.put(inst.getArg1(), inst);
					break;
				case Instruction.INHERITLINK:
					inheritlinkInsts.put(inst.getArg3(), inst);
					break;
			}
		}
		
		//�����Ѥ��륢�ȥ���Ȥ߹�碌�����

		//���������Υ��ȥ�ID -> �����Ѹ�Υ��ȥ�ID
		HashMap reuseMap = new HashMap();
		//�����Ѥ���륢�ȥ��ID��reuseMap���ͤ����ꤵ��Ƥ���ID�ν����
		HashSet reuseAtoms = new HashSet(); 

		//������ˤ��롢Ʊ��̾���Υ��ȥ������Ѥ���	
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

		// ��Ĺ��getlink/inheritlink̿���õ����removelinkInsts/inheritlinkInsts��������
		it = getlinkInsts.keySet().iterator();
		while (it.hasNext()) {
			Integer linkId = (Integer)it.next();
			Instruction getlink = (Instruction)getlinkInsts.get(linkId);
			Instruction inheritlink = (Instruction)inheritlinkInsts.get(linkId);
			//inheritlink��1�����Υ��ȥ���б���������Ѹ�Υ��ȥ�ID
			Integer atomId = (Integer)reuseMap.get(inheritlink.getArg1());
			if (inheritlink != null && 
				atomId != null && atomId.equals(getlink.getArg2()) &&
				inheritlink.getArg2().equals(getlink.getArg3())) {

				it.remove();
				inheritlinkInsts.remove(linkId);
			}
		}
										
		// ���ȥ�����Ѥ򤹤��getlink̿�������inherit̿�᤬����������Τǡ�
		// getlink̿���̿�������Ƭ�˰�ư���롣
		it = getlinkInsts.values().iterator();
		while (it.hasNext()) {
			list.add(0, it.next());
		}

		//���פˤʤä�̿����������ȥ�ID��񤭴�����
		it = list.subList(getlinkInsts.size(), list.size()).iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM: {
					Integer atomId = (Integer)inst.getArg1();
					if (reuseAtoms.contains(atomId)) {
						it.remove();
					}
					break;
				}
				case Instruction.NEWATOM: {
					Integer atomId = (Integer)inst.getArg1();
					if (reuseMap.containsKey(atomId)) {
						it.remove();
					}
					break;
				}
				case Instruction.NEWLINK: {
					changeAtomArg1(inst, reuseMap);
					changeAtomArg3(inst, reuseMap);
					break;
				}
				case Instruction.GETLINK: {
					//��Ƭ�˰�ư�����Τǽ���
					it.remove();
					break;
				}
				case Instruction.INHERITLINK: {
					//inheritlinkInsts�ˤʤ����Ͼ�Ĺ��Ƚ�Ǥ��줿��ΤʤΤǺ��
					if (!inheritlinkInsts.containsKey(inst.getArg3())) {
						it.remove();
					} else {
						changeAtomArg1(inst, reuseMap);
					}
					break;
				}
			}
		}

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
	 * �裱������񤭴����롣
	 * @param inst �񤭴�����̿��
	 * @param reuseMap ���ȥ�����ѥޥå�
	 */
	private static void changeAtomArg1(Instruction inst, Map reuseMap) {
		Integer before = (Integer)inst.getArg1();
		Integer after = (Integer)reuseMap.get(before);
		if (after != null) {
			inst.setArg1(after);
		}
	}
	/**
	 * �裳������񤭴����롣
	 * @param inst �񤭴�����̿��
	 * @param reuseMap ���ȥ�����ѥޥå�
	 */
	private static void changeAtomArg3(Instruction inst, Map reuseMap) {
		Integer before = (Integer)inst.getArg3();
		Integer after = (Integer)reuseMap.get(before);
		if (after != null) {
			inst.setArg3(after);
		}
	}
}