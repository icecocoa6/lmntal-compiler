/*
 * ������: 2003/11/30
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.Functor;

/**
 * ��Ŭ����Ԥ����饹�᥽�åɤ���ĥ��饹��
 * @author Mizuno
 * TODO ̿����λ��ͤ��ǧ
 */
public class Optimizer {
	/**	
	 * �Ϥ��줿̿������Ŭ�����롣<br>
	 * ̿������ˤϡ�1������removeatom/removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * @param list ��Ŭ��������̿���󡣺��ΤȤ���ܥǥ�̿�����Ϥ���뤳�Ȥ��ꤷ�Ƥ��롣
	 */
	public static void optimize(List list) {
		normalize(list);
		reuseMem(list);
		reuseAtom(list);
		removeUnnecessaryRelink(list);
	}
	
	/**
	 * relink̿���getlink/inheritlink̿����Ѵ����롣
	 * @param list �Ѵ�����̿����
	 */
	private static void normalize(List list) {
		int nextId = -1;
		
		ListIterator it = list.listIterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.SPEC:
					if (nextId >= 0) {
						throw new RuntimeException("SYSTEM ERROR: more than one spec instruction");
					}
					nextId = inst.getIntArg2();
					break;
				case Instruction.RELINK:
					if (nextId < 0) {
						throw new RuntimeException("SYSTEM ERROR: relink before spec instruction");
					}
					it.remove();
					it.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
					it.add(new Instruction(Instruction.INHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId));
					nextId++;
					break;
				case Instruction.LOCALRELINK:
					if (nextId < 0) {
						throw new RuntimeException("SYSTEM ERROR: relink before spec instruction");
					}
					it.remove();
					it.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
					it.add(new Instruction(Instruction.LOCALINHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId));
					nextId++;
					break;
			}
		}
	}

	/**
	 * ��κ����Ѥ�Ԥ������ɤ��������롣<br>
	 * ̿������ˤϡ�1������removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * 
	 * @param list
	 */
	private static void reuseMem(List list) {
		HashMap reuseMap = new HashMap();
		HashSet reuseMems = new HashSet(); // �����Ѥ�������ID�ν���
		
		//�����Ѥ�������Ȥ߹�碌����ꤹ��
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
//TODO �ץ���ʸ̮������ʤ���κ�����
//				case Instruction.REMOVEMEM:
//					break;
//				case Instruction.NEWMEM:
//					break;
				case Instruction.MOVECELLS:
					//���Ǥ˺����Ѥ��������������ޤäƤ�����Ǥʤ���С������Ѥ���������
					if (!reuseMap.containsKey(inst.getArg1())) {
						reuseMap.put(inst.getArg1(), inst.getArg2());
						reuseMems.add(inst.getArg2());
						break;
					}
			}
		}
		
		//̿�����񤭴�����
		//TODO insertproxies̿��ν��֡������Ŭ�ڤ��ѹ�����
		ListIterator lit = list.listIterator();
		while (lit.hasNext()) {
			Instruction inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				case Instruction.NEWMEM:
					if (reuseMap.containsKey(inst.getArg1())) {
						//addmem̿����ѹ�
						lit.remove();
						int m = ((Integer)reuseMap.get(inst.getArg1())).intValue();
						lit.add(new Instruction(Instruction.ADDMEM, inst.getIntArg2(), m)); 
					}
					break;
				case Instruction.MOVECELLS:
					if (reuseMems.contains(inst.getArg2())) {
						//addmem̿��ǰ�ư����λ���Ƥ��뤿�����
						//�������줬��2�İʾ����κ����Ѥκ���Ȥʤ뤳�ȤϤʤ�
						lit.remove();
					}
					break;
				case Instruction.FREEMEM:
					if (reuseMems.contains(inst.getArg1())) {
						lit.remove();
					}
			}
		}
		Instruction.changeMemId(list, reuseMap);
	}
		
	/**	
	 * ���ȥ�����Ѥ�Ԥ������ɤ��������롣<br>
	 * ʬ���Ķ��λ��Ϲͤ��Ƥ��ʤ���
	 * �ޤ����������Ϥ����̿����ϡ����ξ����������Ƥ���ɬ�פ����롣
	 * <ul>
	 *  <li>1������removeatom̿�����Ѥ��Ƥ��ʤ�
	 *  <li>getlink̿�����Ѥ��Ƥ��ʤ�
	 * </ul>
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
		// getlink̿���spec̿��θ�˰�ư���롣
		// TODO Ŭ�ڤʰ�ư���򸫤Ĥ���
		it = getlinkInsts.values().iterator();
		while (it.hasNext()) {
			list.add(1, it.next());
		}

		//���פˤʤä�removeatom/freeatom/newatom̿������
		ListIterator lit = list.listIterator(getlinkInsts.size() + 1);
		while (lit.hasNext()) {
			Instruction inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM:
				case Instruction.FREEATOM:
					Integer atomId = (Integer)inst.getArg1();
					if (reuseAtoms.contains(atomId)) {
						lit.remove();
					}
					break;
				case Instruction.NEWATOM:
					atomId = (Integer)inst.getArg1();
					if (reuseMap.containsKey(atomId)) {
						lit.remove();
					}
					break;
				case Instruction.GETLINK:
					//��Ƭ�˰�ư�����Τǽ���
					lit.remove();
					break;
			}
		}
		//TODO enqueueatom̿�������

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