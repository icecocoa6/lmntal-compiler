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
					it.add(new Instruction(Instruction.INHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId, inst.getIntArg5()));
					nextId++;
					break;
				case Instruction.LOCALRELINK:
					if (nextId < 0) {
						throw new RuntimeException("SYSTEM ERROR: relink before spec instruction");
					}
					it.remove();
					it.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
					it.add(new Instruction(Instruction.LOCALINHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId, inst.getIntArg5()));
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
		HashMap parent = new HashMap();
		
		//�����Ѥ�������Ȥ߹�碌����ꤹ��
		//TODO �ץ���ʸ̮������ʤ���κ�����
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEMEM:
					parent.put(inst.getArg1(), inst.getArg2());
					break;
				case Instruction.NEWMEM:
					parent.put(inst.getArg1(), inst.getArg2());
					break;
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
		//���κݡ���Ĺ��removemem/addmem̿�������
		HashSet set = new HashSet(); //removemem/addmem̿������פ�������Ѥ˴ؤ����
		it = reuseMap.keySet().iterator();
		while (it.hasNext()) {
			Integer i1 = (Integer)it.next();
			Integer i2 = (Integer)reuseMap.get(i1);
			if (parent.get(i1).equals(parent.get(i2))) { //�Ƥ�Ʊ�����ä���
				set.add(i1);
				set.add(i2);
			}
		}
		
		//TODO removeproxies/insertproxies̿���Ŭ�ڤ��ѹ�����
		ListIterator lit = list.listIterator();
		while (lit.hasNext()) {
			Instruction inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEMEM:
					if (set.contains(inst.getArg1())) {
						lit.remove();
					}
					break;
				case Instruction.REMOVEPROXIES:
					if (set.contains(inst.getArg1())) {
						lit.remove();
					}
					break;
				case Instruction.NEWMEM:
					Integer arg1 = (Integer)inst.getArg1();
					if (reuseMap.containsKey(arg1)) {
						lit.remove();
						if (!set.contains(arg1)) {
							//addmem̿����ѹ�
							int m = ((Integer)reuseMap.get(arg1)).intValue();
							lit.add(new Instruction(Instruction.ADDMEM, inst.getIntArg2(), m)); 
						}
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
	 * �졦�ե��󥯥���˥��ȥ�ν����������뤿��Υ��饹��
	 * ���ȥ�����ѥ����ɤ���������ݤ˥��ȥ��������뤿��˻��Ѥ��롣
	 * @author Ken
	 */
	private static class AtomSet {
		HashMap map = new HashMap(); // mem -> (functor -> atoms)
		/**
		 * ���ȥ���ɲä���
		 * @param mem ���ȥब��°������
		 * @param functor ���ȥ�Υե��󥯥�
		 * @param atom �ɲä��륢�ȥ�
		 */
		void add(Integer mem, Functor functor, Integer atom) {
			HashMap map2 = (HashMap)map.get(mem);
			if (map2 == null) {
				map2 = new HashMap();
				map.put(mem, map2);
			}
			HashSet atoms = (HashSet)map2.get(functor);
			if (atoms == null) {
				atoms = new HashSet();
				map2.put(functor, atoms);
			}
			atoms.add(atom);
		}
		/**
		 * ���ꤵ�줿��˽�°���롢���ꤵ�줿�ե��󥯥�����ĥ��ȥ��ȿ���Ҥ��֤�
		 * @param mem ���ȥब��°���륢�ȥ�
		 * @param functor ���ȥ�Υե��󥯥���
		 * @return ȿ����
		 */
		Iterator iterator(Integer mem, Functor functor) {
			HashMap map2 = (HashMap)map.get(mem);
			if (map2 == null) {
				return util.Util.NULL_ITERATOR;
			}
			HashSet atoms = (HashSet)map2.get(functor);
			if (atoms == null) {
				return util.Util.NULL_ITERATOR;
			}
			return atoms.iterator();
		}
		/**
		 * ���ȿ���Ҥ��֤�
		 * @return ȿ����
		 */
		Iterator memIterator() {
			return map.keySet().iterator();
		}
		/**
		 * ���ꤵ�줿����ˤ��롢���Υ��󥹥��󥹤��������륢�ȥ�Υե��󥯥���ȿ���Ҥ��֤�
		 * @param mem ��
		 * @return ȿ����
		 */
		Iterator functorIterator(Integer mem) {
			HashMap map2 = (HashMap)map.get(mem);
			if (map2 == null) {
				return util.Util.NULL_ITERATOR;
			}
			return map2.keySet().iterator();
		}
	}
	/**	
	 * ���ȥ�����Ѥ�Ԥ������ɤ��������롣<br>
	 * �������Ϥ����̿����ϡ����ξ����������Ƥ���ɬ�פ����롣
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
		AtomSet removedAtoms = new AtomSet();
		AtomSet createdAtoms = new AtomSet();
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM:
					Integer atom = (Integer)inst.getArg1();
					Functor functor = (Functor)inst.getArg3();
					Integer mem = (Integer)inst.getArg2();
					removedAtoms.add(mem, functor, atom);
					break;
				case Instruction.NEWATOM:
					atom = (Integer)inst.getArg1();
					functor = (Functor)inst.getArg3();
					mem = (Integer)inst.getArg2();
					createdAtoms.add(mem, functor, atom);
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

		//Ʊ����ˤ��롢Ʊ��̾���Υ��ȥ������Ѥ���
		Iterator memIterator = removedAtoms.memIterator();
		while (memIterator.hasNext()) {
			Integer mem = (Integer)memIterator.next();
			Iterator functorIterator = removedAtoms.functorIterator(mem);
			while (functorIterator.hasNext()) {
				Functor functor = (Functor)functorIterator.next();
				Iterator removedAtomIterator = removedAtoms.iterator(mem, functor);
				Iterator createdAtomIterator = createdAtoms.iterator(mem, functor);
				while (removedAtomIterator.hasNext() && createdAtomIterator.hasNext()) {
					Integer removeAtom = (Integer)removedAtomIterator.next();
					reuseMap.put(createdAtomIterator.next(), removeAtom);
					reuseAtoms.add(removeAtom);
					//�����Ѥ��Ȥ߹�碌����ޤä���ΤϺ������
					//�ʤ��θ��³�������ȥ�̾���ۤʤ���ʤɤκ����Ѥ��Ȥ߹�碌����ꤹ������Τ��ᡣ��
					removedAtomIterator.remove();
					createdAtomIterator.remove();
				}
			}
		}
		
		//TODO �졦���ȥ�̾���ۤʤ��Τκ����Ѥ��Ȥ߹�碌����ꤹ�륳���ɤ򤳤��˽�

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