/*
 * ������: 2003/11/30
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.Functor;
import runtime.Env;

/**
 * ��Ŭ����Ԥ����饹�᥽�åɤ���ĥ��饹��
 * @author Mizuno
 */
public class Optimizer {
	/**	
	 * �Ϥ��줿̿����򡢸��ߤκ�Ŭ����٥�˱����ƺ�Ŭ�����롣<br>
	 * ̿������ˤϡ�1������removeatom/removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * ���ߤΰ������ͤϻ���Ū�ʤ�Τǡ������ѹ������ͽ�ꡣ
	 * @param head ���Ƴ�ޥå���̿����
	 * @param body �ܥǥ�̿����
	 */
	public static void optimize(List head, List body) {
		if (Env.optimize > 0) {
			if (Env.optimize >= 4) {
				reuseMem(body);
			}
			if (Env.optimize >= 2) {
				if (changeOrder(body)) {
					reuseAtom(body);
					removeUnnecessaryRelink(body);
				}
			}
			if (Env.optimize >= 7) {
				makeLoop(head, body); //�ޤ�������餱
			}
		}
	}

	///////////////////////////////////////////////////////
	// ���Ŭ����Ϣ
		
	/**
	 * ��κ����Ѥ�Ԥ������ɤ��������롣<br>
	 * ̿������ˤϡ�1������removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * ̿����κǸ��proceed̿��Ǥʤ���Фʤ�ʤ���
	 * @param list �ܥǥ�̿����
	 */
	private static void reuseMem(List list) {
		Instruction last = (Instruction)list.get(list.size() - 1);
		if (last.getKind() != Instruction.PROCEED) {
			return;
		}
		
		HashMap reuseMap = new HashMap();
		HashSet reuseMems = new HashSet(); // �����Ѥ�������ID�ν���
		HashMap parent = new HashMap();
		HashMap removedChildren = new HashMap(); // map -> list of children
		HashMap createdChildren = new HashMap(); // map -> list of children
		HashMap pourMap = new HashMap();
		HashSet pourMems = new HashSet(); // pour̿����裲�����˴ޤޤ����
		
		//�����Ѥ�������Ȥ߹�碌����ꤹ��
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEMEM:
					parent.put(inst.getArg1(), inst.getArg2());
					addToMap(removedChildren, inst.getArg2(), inst.getArg1());
					break;
				case Instruction.NEWMEM:
					parent.put(inst.getArg1(), inst.getArg2());
					addToMap(createdChildren, inst.getArg2(), inst.getArg1());
					break;
				case Instruction.MOVECELLS:
//					//���Ǥ˺����Ѥ��������������ޤäƤ�����Ǥʤ���С������Ѥ���������
//					if (!reuseMap.containsKey(inst.getArg1())) {
//						reuseMap.put(inst.getArg1(), inst.getArg2());
//						reuseMems.add(inst.getArg2());
//						break;
//					}
					addToMap(pourMap, inst.getArg1(), inst.getArg2());
					pourMems.add(inst.getArg2());
			}
		}

		createReuseMap(reuseMap, reuseMems, parent, removedChildren, createdChildren,
					   pourMap, pourMems, new Integer(0));
		
//		//��������ˡ��ɽ���ʥǥХå��ѡ�
//		System.out.println("result of reusing mem");
//		it = reuseMap.keySet().iterator();
//		while (it.hasNext()) {
//			Object key = it.next();
//			System.out.println(key + " " + reuseMap.get(key));
//		}

		//̿�����񤭴�����
		//���κݡ���Ĺ��removemem/addmem̿�������
		HashSet set = new HashSet(); //removemem/addmem̿������פ�������Ѥ˴ؤ����
		it = reuseMap.keySet().iterator();
		while (it.hasNext()) {
			Integer i1 = (Integer)it.next();
			Integer i2 = (Integer)reuseMap.get(i1);
			Integer p1 = (Integer)parent.get(i1);
			Integer p2 = (Integer)parent.get(i2);
			if (reuseMap.containsKey(p1)) {
				p1 = (Integer)reuseMap.get(p1);
			}
			if (p1.equals(p2)) { //�Ƥ�Ʊ�����ä���
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
					break;
			}
		}
		
		lit.previous(); //�Ǹ��proceed̿��μ������ɲ�
		addUnlockInst(lit, reuseMap, new Integer(0), createdChildren);

		Instruction.changeVar(list, reuseMap);
	}
	private static void addUnlockInst(ListIterator lit, HashMap reuseMap, Integer mem, HashMap children) {
		//�������˽���
		ArrayList c = (ArrayList)children.get(mem);
		if (c != null) {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				addUnlockInst(lit, reuseMap, (Integer)it.next(), children);
			}
		}
		
		if (reuseMap.containsKey(mem)) {
			lit.add(new Instruction(Instruction.UNLOCKMEM, mem));
		}
	}
	
	/**
	 * List���ͤȤ���褦�ʥޥåפ��ͤ��ɲä��롣
	 * ��Ŧ���줿���������Ǥ�¸�ߤ�������ͤΥꥹ�Ȥ�value���ɲä��롣
	 * ¸�ߤ��ʤ����Ͽ������ꥹ�Ȥ���Ͽ�����ޥåפ��ɲä��롣
	 * @param map �ޥå�
	 * @param key ����
	 * @param value ��
	 */
	private static void addToMap(HashMap map, Object key, Object value) {
		ArrayList list = (ArrayList)map.get(key);
		if (list == null) {
			list = new ArrayList();
			map.put(key, list);
		}
		list.add(value);
	}

	/**
	 * �����Ѥ��������ꤷ�ޤ���
	 * start�ǻ��ꤵ�줿����ˤ�����ˤĤ��ơ��Ƶ��ƤӽФ���Ԥ��ޤ���
	 * @param reuseMap ��������ˡ������뤿��Υޥå�
	 * @param reuseMems �����Ѥ������ν��������뤿��Υ��å�
	 * @param parent ����ؤΥޥå�
	 * @param children ���콸��ؤΥޥå�
	 * @param pourMap pour̿����б������줿�ޥå�
	 * @param pourMems pour̿����裲�����˴ޤޤ����Υ��å�
	 * @param start �����������κ����Ѥ���ꤹ�롣
	 */
	private static void createReuseMap(HashMap reuseMap, HashSet reuseMems, HashMap parent,
										 HashMap removedChildren, HashMap createdChildren,
										 HashMap pourMap, HashSet pourMems, Integer start) {

		ArrayList list = (ArrayList)createdChildren.get(start);
		if (list == null || list.size() == 0) {
			return;
		}

		Integer start2; //start�Ρ������Ѹ���ѿ��ֹ�
		if (reuseMap.containsKey(start)) {
			start2 = (Integer)reuseMap.get(start);
		} else {
			start2 = start;
		}
				
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Integer mem = (Integer)it.next();
			//mem�κ����Ѹ������
			 
			Integer candidate = null; //pour̿��ˤ������Ѹ���򣱤��ݻ����Ƥ���
			Integer result = null; //���ꤷ����������������
			ArrayList list2 = (ArrayList)pourMap.get(mem);
			if (list2 != null) {
				Iterator it2 = list2.iterator();
				while (it2.hasNext()) {
					Integer mem2 = (Integer)it2.next();
					//���Ǥ˺����Ѥ��뤳�Ȥ���ޤäƤ������̵��
					if (reuseMems.contains(mem2)) {
						continue;
					}
					
					//�������Ͽ
					candidate = mem2;
					
					if (parent.get(mem2).equals(start2)) {
						//���줬Ʊ���줫���pour̿�᤬������ϡ������ͥ��
						result = mem2;
						break;
					}
				}
			}
			if (result == null) {
				//�����ˡ�Ƿ�ޤ�ʤ��ä����
				if (candidate == null) {
					//���̤ο������ġ��ץ���ʸ̮�Τʤ�����椫��Ŭ���˷��ꡣ
					//���������줬�ʤ���к����Ѥ��ʤ�
					ArrayList list3 = (ArrayList)removedChildren.get(start2);
					if (list3 != null) {
						Iterator it3 = list3.iterator();
						while (it3.hasNext()) {
							Integer m = (Integer)it3.next();
							if (!pourMems.contains(m) && !reuseMems.contains(m)) {
								result = m;
								break;
							}
						}
					}
				} else {
					//pour̿�᤬�����椫��Ŭ���˷���
					result = candidate;
				}
			}
			if (result != null) {
				reuseMap.put(mem, result);
				reuseMems.add(result);
			}
			//�Ƶ��ƤӽФ�
			createReuseMap(reuseMap, reuseMems, parent, removedChildren, createdChildren,
						   pourMap, pourMems, mem);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// ���ȥ�����Ѵ�Ϣ

	/**
	 * relink̿���getlink/inheritlink̿����Ѵ�����
	 * getlink/unify̿���ܥǥ�̿�������Ƭ�˰�ư���롣
	 * ��Ƭ��̿���spec�Ǥʤ���Фʤ�ʤ���
	 * @param list �Ѵ�����̿����
	 * @return �Ѵ���������������true
	 */
	public static boolean changeOrder(List list) {
		Instruction spec = (Instruction)list.get(0);
		if (spec.getKind() != Instruction.SPEC) {
			return false;
		}
		int nextId = spec.getIntArg2();
		
		ArrayList moveInsts = new ArrayList();
		
		ListIterator it = list.listIterator(1);
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.UNIFY:
					moveInsts.add(inst);
					it.remove();
					break;
				case Instruction.RELINK:
					moveInsts.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
					it.set(new Instruction(Instruction.INHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId, inst.getIntArg5()));
					nextId++;
					break;
				case Instruction.LOCALRELINK:
					moveInsts.add(new Instruction(Instruction.GETLINK,  nextId, inst.getIntArg3(), inst.getIntArg4()));
					it.set(new Instruction(Instruction.LOCALINHERITLINK,  inst.getIntArg1(), inst.getIntArg2(), nextId, inst.getIntArg5()));
					nextId++;
					break;
			}
		}
		list.set(0, Instruction.spec(spec.getIntArg1(), nextId));
		list.addAll(1, moveInsts);
//		spec.data.set(1, new Integer(nextId)); //�������ѿ��ο����ѹ�
		return true;
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
//				case Instruction.GETLINK:
//					//��Ǿ����ư����
//					getlinkInsts.put(inst.getArg1(), inst);
//					break;
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

//		//��������ˡ��ɽ���ʥǥХå��ѡ�
//		
//		it = reuseMap.keySet().iterator();
//		while (it.hasNext()) {
//			Object key = it.next();
//			System.out.println(key + " " + reuseMap.get(key));
//		}
		
		//////////////////////////////////////////////////
		//
		// ���ȥ������Ѥ���褦��̿�������������
		//

										
//		// ���ȥ�����Ѥ򤹤��getlink̿�������inherit̿�᤬����������Τǡ�
//		// getlink̿���spec̿��θ�˰�ư���롣
//		// TO DO Ŭ�ڤʰ�ư���򸫤Ĥ���
//		it = getlinkInsts.values().iterator();
//		while (it.hasNext()) {
//			list.add(1, it.next());
//		}

		//���פˤʤä�removeatom/freeatom/newatom̿������
		ListIterator lit = list.listIterator(/*getlinkInsts.size() + */1);
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
//				case Instruction.GETLINK:
//					//��Ƭ�˰�ư�����Τǽ���
//					lit.remove();
//					break;
			}
		}
		//TO DO enqueueatom̿������� �� ����̿����ˤ����Τ��Ȥ���Τ�����

		Instruction.changeVar(list, reuseMap);

//		it = list.iterator();
//		while (it.hasNext()) {
//			System.out.println(it.next());
//		}
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

	//////////////////////////////////////////////////////////////
	// �롼�ײ���Ϣ

	/**
	 * ���ȥ�Ȱ����ֹ���Ȥ��ݻ����륯�饹��
	 * @author Ken
	 */	
	private static class Link {
		int atom;
		int pos;
		Link(int atom, int pos) {
			this.atom = atom;
			this.pos = pos;
		}
		public boolean equals(Object o) {
			Link l = (Link)o;
			return this.atom == l.atom && this.pos == l.pos;
		}
		public int hashCode() {
			return atom + pos;
		}
		public String toString() {
			return "(" + atom + ", " + pos + ")";
		}
	}
	/**
	 * Ʊ��롼���ʣ����Ʊ��Ŭ��<br>
	 * �Ȥꤢ���������ξ����������Ƥ�����ˤΤ߽�����Ԥ���
	 * <ul>
	 *  <li>�ܥǥ��¹Բ������ȼ°�����Ʊ���Ǥ��롣
	 *  <li>spec̿��ʳ��κǽ��̿�᤬findatom�ǡ��������������0�Ǥ��롣
	 *  <li>�Ϥ����findatom̿��ˤ�äƼ������줿���ȥब�����Ѥ���Ƥ��롣
	 * </ul>
	 * ����ܤξ�郎��������ʤ�����ư����������ʤ�����TODO ����ϲ��Ȥ����롣��
	 * ����ʳ��ξ�郎��������ʤ����ϲ��⤷�ʤ���
	 * @param head ���Ƴ�ޥå���̿����
	 * @param body �ܥǥ�̿����
	 */	
	private static void makeLoop(List head, List body) {
		Instruction inst = (Instruction)head.get(0);
		if (inst.getKind() != Instruction.SPEC) {
			return;
		}
		inst = (Instruction)head.get(1);
		if (inst.getKind() != Instruction.FINDATOM || inst.getIntArg2() != 0) {
			return;
		}
		Integer firstAtom = (Integer)inst.getArg1();

		//���˹��פ��뤫�����ܾ������
		HashMap links = new HashMap(); //newlink̿�������������󥯤ξ���
		HashMap functor = new HashMap(); // atom -> functor
		ListIterator lit = body.listIterator();
		while (lit.hasNext()) {
			inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM:
//				case Instruction.FREEATOM:
					if (inst.getArg1().equals(firstAtom)) {
						return;
					}
					break;
				case Instruction.NEWLINK:
				case Instruction.LOCALNEWLINK:
					Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
					Link l2 = new Link(inst.getIntArg3(), inst.getIntArg4());
					links.put(l1, l2);
					links.put(l2, l1);
					break;
				case Instruction.NEWATOM:
					functor.put(inst.getArg1(), inst.getArg3());
					break;
				case Instruction.FINDATOM:
					functor.put(inst.getArg1(), inst.getArg3());
					break;
				case Instruction.FUNC:
					functor.put(inst.getArg1(), inst.getArg2());
					break;
			}
		}
		HashMap changeMap = new HashMap();

		ArrayList loop = new ArrayList(); //�롼�����̿����
		
		//�롼����̿���������
		
		//�ޤ��ϥ��ԡ������ѿ��ֹ��դ��ؤ�
		Instruction spec = (Instruction)body.get(0);
		//�롼����̿����ǻ��Ѥ����ѿ��γ�����
		int base = spec.getIntArg2(); 
		//��Ȥ�Ȥ��ѿ����롼����Ǥ��ѿ�
		HashMap varMap = new HashMap();
		varMap.put(new Integer(0), new Integer(0)); //����
		for (int i = 1; i < base; i++) {
			varMap.put(new Integer(i), new Integer(base + i));
			System.out.println(i + " -> " + (base + i));
		}
		lit = head.subList(2, head.size() - 1).listIterator(); //spec,findatom,react�����
		while (lit.hasNext()) {
			loop.add(((Instruction)lit.next()).clone());
		}
		lit = body.listIterator(1);
		while (lit.hasNext()) {
			loop.add(((Instruction)lit.next()).clone());
		}
		Instruction.changeVar(loop, varMap); //spec�����

		//̿������ѹ���Ԥ�
		varMap = new HashMap();
		varMap.put(firstAtom, firstAtom);
		
		ArrayList moveInsts = new ArrayList();
		ListIterator baseIterator = head.subList(2, head.size() - 1).listIterator(); //��������̿����
		ListIterator loopIterator = loop.listIterator(); //�롼����̿����
		while (lit.hasNext()) {
			baseIterator.next();
			inst = (Instruction)loopIterator.next();
			switch (inst.getKind()) {
				case Instruction.DEREF:
					Link l = (Link)links.get(new Link(inst.getIntArg2(), inst.getIntArg3()));
					if (l != null && l.pos == inst.getIntArg4()) {
						varMap.put(inst.getArg1(), new Integer(l.atom));
						loopIterator.remove();
					}
					break;
				case Instruction.FUNC:
					if (varMap.containsKey(inst.getArg1())) {
						Integer atom = (Integer)varMap.get(inst.getArg1());
						if (functor.containsKey(atom)) {
							if (!functor.get(atom).equals(inst.getArg2())) {
								//���м��Ԥ���Τ�ʣ����Ʊ��Ŭ�ѤϹԤ�ʤ�
								return;
							}
							//������������Τǽ���	
							loopIterator.remove();
						}
					}
					break;					
			}
		}

		HashMap changeToNewlink = new HashMap(); //newlink���ѹ������� -> �����
		baseIterator = body.listIterator(1); //��������̿����
		loopIterator = loop.listIterator(); // �롼����̿����
		while (lit.hasNext()) {
			Instruction baseInst = (Instruction)baseIterator.next();
			inst = (Instruction)loopIterator.next();
			switch (inst.getKind()) {
				case Instruction.GETLINK:
					int atom = inst.getIntArg2();
					Link l = (Link)links.get(new Link(atom, inst.getIntArg3()));
					if (l != null) { //����Υ롼�פ�newlink�ˤ�äƥ���褬����Ǥ�����
						int atom2 = l.atom;
						if (varMap.get(new Integer(atom)).equals(new Integer(atom- base)) ||
							varMap.get(new Integer(atom2)).equals(new Integer(atom2 - base))) { //����Υ롼�פ�newlink̿�᤬���������Τξ��
							//getlink��������inheritlink��newlink���ѹ�
							changeToNewlink.put(inst.getArg1(), l);
							loopIterator.remove();
						}
					}
					break;
				case Instruction.INHERITLINK:
					Integer linkVar = (Integer)inst.getArg3();
					if (changeToNewlink.containsKey(linkVar)) {
						l = (Link)changeToNewlink.get(linkVar);
						//newlink���ѹ�
						loopIterator.set(Instruction.newlink(inst.getIntArg1(),
															 inst.getIntArg2(),
															 l.atom,
															 l.pos));
															 //inst.getIntArg5()));
						
					}
					break;
				case Instruction.NEWLINK:
					int atomVar = inst.getIntArg1();
					int atomVar2 = inst.getIntArg3();
					if (varMap.get(new Integer(atomVar)).equals(new Integer(atomVar - base)) ||
						varMap.get(new Integer(atomVar2)).equals(new Integer(atomVar2 - base))) { //��Ȥ�Ȥ��ѿ��ֹ��Ʊ���ˤʤäƤ�����
						//�Ǹ�˰�ư
						moveInsts.add(baseInst);
						baseIterator.remove();
						loopIterator.remove();
					}
					break;
			}
		}
		
		Instruction.changeVar(loop, varMap);
		
		//proceed̿�����������
		ArrayList looparg = new ArrayList();
		looparg.add(loop);
		body.add(body.size() - 1, new Instruction(Instruction.LOOP, looparg));
		//�Ǹ��1��¹Ԥ���̿�������
		body.addAll(body.size() - 1, moveInsts);
	}

}