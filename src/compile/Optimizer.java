/*
 * ������: 2003/11/30
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.Functor;
import runtime.Env;

import runtime.Rule;

/**
 * ��Ŭ����Ԥ����饹�᥽�åɤ���ĥ��饹��
 * @author Mizuno
 */
public class Optimizer {
	/** �롼�륪�֥������Ȥ��Ŭ������ */
	public static void optimizeRule(Rule rule) {
		if (Env.optimize == 1) inlineExpandTailJump(rule.memMatch); else	// TODO ��Ŭ��������礹��
		optimize(rule.memMatch, rule.body);
	}
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
				reuseMem(head, body);
			}
			if (Env.optimize >= 2) {
				if (changeOrder(body)) {
					reuseAtom(head, body);
					removeUnnecessaryRelink(body);
				}
			}
			if (Env.optimize >= 7) {
				makeLoop(head, body);
			}
		}
	}
	///////////////////////////////////////////////////////
	// @author n-kato
	// TODO spec̿��ο�ʬ��ͤ���
	
	/** ̿�����������jump̿��򥤥�饤��Ÿ�����롣
	 * <pre>
	 *     [ spec[X,Y];  C;jump[L,A1..Am] ] where L:[spec[m,m+n];D]
	 * ==> [ spec[X,Y+n];C; D{ 1..m->A1..Am, m+1..m+n->Y+1..Y+n } ]
	 * </pre> */
	public static void inlineExpandTailJump(List insts) {
		if (insts.isEmpty()) return;
		Instruction spec = (Instruction)insts.get(0);
		if (spec.getKind() != Instruction.SPEC) return;
		int formals = spec.getIntArg1();
		int locals  = spec.getIntArg2();
		locals = inlineExpandTailJump(insts, locals);
		spec.updateSpec(formals, locals);
	}
	/** ̿�����������jump̿��򥤥�饤��Ÿ�����롣spec�Ϥޤ���������ʤ���*/
	public static int inlineExpandTailJump(List insts, int varcount) {
		if (insts.isEmpty()) return varcount;
		int size = insts.size();
		Instruction jump = (Instruction)insts.get(size - 1);
		if (jump.getKind() != Instruction.JUMP) return varcount;
		//
		InstructionList label = (InstructionList)jump.getArg1();
		List subinsts = InstructionList.cloneInstructions(label.insts);
		Instruction subspec = (Instruction)subinsts.get(0);

		HashMap map = new HashMap();
		// �������ϡ��°����ֹ���ִ����롣
		List memargs   = (List)jump.getArg2();
		List atomargs  = (List)jump.getArg3();
		List otherargs = (List)jump.getArg4();
		for (int i = 0; i < memargs.size(); i++)
			map.put( new Integer(i), memargs.get(i) );
		for (int i = 0; i < atomargs.size(); i++)
			map.put( new Integer(memargs.size() + i), atomargs.get(i) );
		for (int i = 0; i < otherargs.size(); i++)
			map.put( new Integer(memargs.size() + atomargs.size() + i), otherargs.get(i) );
		// �ɽ��ѿ��ϡ��������ѿ��ֹ���ִ����롣
		int subformals = subspec.getIntArg1();
		int sublocals  = subspec.getIntArg2();
		for (int i = subformals; i < sublocals; i++) {
			map.put( new Integer(i), new Integer(varcount++) );
		}
		//
		Instruction.applyVarRewriteMap(subinsts,map);
		subinsts.remove(0);		// spec�����
		insts.remove(size - 1);	// jump̿������
		insts.addAll(subinsts);
		return varcount;
	}
	
	// n-kato

	///////////////////////////////////////////////////////
	// ���Ŭ����Ϣ
		
	/**
	 * ��κ����Ѥ�Ԥ������ɤ��������롣<br>
	 * ̿������ˤϡ�1������removemem̿�᤬����Ƥ��ƤϤ����ʤ���
	 * ̿����κǸ��proceed̿��Ǥʤ���Фʤ�ʤ���
	 * @param list �ܥǥ�̿����
	 */
	private static void reuseMem(List head, List body) {
		Instruction spec = (Instruction)body.get(0);
		if (spec.getKind() != Instruction.SPEC) {
			return;
		}
		Instruction last = (Instruction)body.get(body.size() - 1);
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
		HashMap copyRulesMap = new HashMap();
		
		//�����Ѥ�������Ȥ߹�碌����ꤹ��
		Iterator it = body.iterator();
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
					break;
				case Instruction.COPYRULES:
					addToMap(copyRulesMap, inst.getArg2(), inst.getArg1());
					break;
			}
		}

		createReuseMap(reuseMap, reuseMems, parent, removedChildren, createdChildren,
					   pourMap, pourMems, new Integer(0));
		

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

		//�롼�������
		HashMap ruleMem = new HashMap(); //�롼������򤷤���
		HashMap varInBody = new HashMap(); // �إåɤǤ��ѿ�̾���ܥǥ��Ǥ��ѿ�̾
		
		Instruction react = (Instruction)head.get(head.size() - 1);
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP) {
			return;
		}
		int i = 0;
		List args = (List)react.getArg2();
		it = args.iterator();
		while (it.hasNext()) {
			varInBody.put((Integer)it.next(), new Integer(i++));
		}
		
		it = head.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			if (inst.getKind() == Instruction.NORULES) {
				//�롼��������оݤ��鳰��
				varInBody.remove(inst.getArg1());
			}
		}
		//���򤹤�̿�������
		ArrayList tmpInsts = new ArrayList();
		int nextArg = spec.getIntArg2();
		it = varInBody.keySet().iterator();
		while (it.hasNext()) {
			Integer memInHead = (Integer)it.next();
			Integer mem = (Integer)varInBody.get(memInHead);
			if (reuseMems.contains(mem)) {//�����Ѹ�����ξ��
				ArrayList copyRulesTo = (ArrayList)copyRulesMap.get(mem);
				boolean flg = false;
				if (copyRulesTo == null) {
					//���դ˥롼��ʸ̮���и����ʤ��Τ��������ס�����Τ�
					tmpInsts.add(new Instruction(Instruction.CLEARRULES, mem)); 
				} else {
					Iterator it2 = copyRulesTo.iterator();
					while (it2.hasNext()) {
						Integer dstmem = (Integer)it2.next();
						if (mem.equals(reuseMap.get(dstmem))) {
							flg = true; //��ʬ�˥��ԡ����Ƥ���Τ���������
							break;
						}
					}
					if (!flg) {
						//�������
						ruleMem.put(mem, new Integer(nextArg));
						tmpInsts.add(new Instruction(Instruction.ALLOCMEM, nextArg));
						tmpInsts.add(new Instruction(Instruction.COPYRULES, nextArg, mem));
						tmpInsts.add(new Instruction(Instruction.CLEARRULES, mem)); 
						nextArg++;
					}
				}
			}
		}
		body.addAll(1, tmpInsts);
		
		//TODO removeproxies/insertproxies̿���Ŭ�ڤ��ѹ�����
//		tmpInsts = new ArrayList();
		ListIterator lit = body.listIterator();
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
						//addmem��enqueuemem̿����ѹ�
						int m = ((Integer)reuseMap.get(arg1)).intValue();
						if (!set.contains(arg1)) {
							lit.add(new Instruction(Instruction.ADDMEM, inst.getIntArg2(), m)); 
						}
						lit.add(new Instruction(Instruction.ENQUEUEMEM, m)); 
					}
					break;
				case Instruction.MOVECELLS:
					if (reuseMems.contains(inst.getArg2())) {
						//addmem̿��ǰ�ư����λ���Ƥ��뤿�����
						//�������줬��2�İʾ����κ����Ѥκ���Ȥʤ뤳�ȤϤʤ�
						lit.remove();
					}
					break;
//				case Instruction.LOADRULESET:
//					//�롼������򤷤ʤ����������ƺǸ�˰�ư
//					tmpInsts.add(inst);
//					lit.remove();
//					break;
				case Instruction.COPYRULES:
					Integer srcmem = (Integer)inst.getArg2();
					Integer dstmem = (Integer)inst.getArg1();
					if (ruleMem.containsKey(srcmem)) {
						if (!ruleMem.get(srcmem).equals(dstmem)) { //����Τ����̿��Ǥʤ����
							//���򤷤��줫��Υ��ԡ����ѹ�
							lit.remove();
							lit.add(new Instruction(Instruction.COPYRULES, dstmem.intValue(), ((Integer)ruleMem.get(srcmem)).intValue())); 
						}
					} else if (srcmem.equals(reuseMap.get(dstmem))) {
						//��ʬ�ؤΥ��ԡ��ʤΤǺ��
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
//		//loadruleset̿��ΰ�ư
//		it = tmpInsts.iterator();
//		while (it.hasNext()) {
//			lit.add(it.next());
//		}
		//�����Ѥ������unlockmem̿����ɲ�
		addUnlockInst(lit, reuseMap, new Integer(0), createdChildren);
		//����˻��Ѥ�����β���
		it = ruleMem.values().iterator();
		while (it.hasNext()) {
			lit.add(new Instruction(Instruction.FREEMEM, it.next()));
		}

		//spec���ѹ�
//		body.set(0, Instruction.spec(spec.getIntArg1(), nextArg));
		spec.updateSpec(spec.getIntArg1(), nextArg);
		
		Instruction.changeMemVar(body, reuseMap);
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
//		list.set(0, Instruction.spec(spec.getIntArg1(), nextId));
		spec.updateSpec(spec.getIntArg1(), nextId);
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
	private static void reuseAtom(List head, List body) {
		/////////////////////////////////////////////////
		//
		// �����Ѥ��륢�ȥ���Ȥ߹�碌����ꤹ��
		//
		
		// removeatom/newatom/getlink̿��ξ����Ĵ�٤�
		AtomSet removedAtoms = new AtomSet();
		AtomSet createdAtoms = new AtomSet();
		HashMap getlinkInsts = new HashMap(); // linkId -> getlink instruction
		
		Iterator it = body.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
				case Instruction.REMOVEATOM:
					try {
						Integer atom = (Integer)inst.getArg1();
						Functor functor = (Functor)inst.getArg3();
						Integer mem = (Integer)inst.getArg2();
						removedAtoms.add(mem, functor, atom);
					} catch (IndexOutOfBoundsException e) {}
					break;
				case Instruction.NEWATOM:
					Integer atom = (Integer)inst.getArg1();
					Functor functor = (Functor)inst.getArg3();
					Integer mem = (Integer)inst.getArg2();
					createdAtoms.add(mem, functor, atom);
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
				//removeproxies��insertproxies������Τǡ������ѤǤ��ʤ�
				if (functor.equals(Functor.INSIDE_PROXY) ||
					functor.equals(Functor.OUTSIDE_PROXY) ||
					functor.equals(Functor.STAR)) {
					continue;
				}
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

		
		//////////////////////////////////////////////////
		//
		// ���ȥ������Ѥ���褦��̿�������������
		//

		//�������
		HashMap links = new HashMap();
		it = head.iterator();
		while (it.hasNext()) {
			Instruction inst = (Instruction)it.next();
			if (inst.getKind() == Instruction.DEREF) {
				Link l1 = new Link(inst.getIntArg2(), inst.getIntArg3());
				Link l2 = new Link(inst.getIntArg1(), inst.getIntArg4());
				links.put(l1, l2);
				links.put(l2, l1);
			}
		}
										
		//���פˤʤä�removeatom/freeatom/newatom̿������
		ListIterator lit = body.listIterator(/*getlinkInsts.size() + */1);
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
				case Instruction.NEWLINK:
					Integer a1 = (Integer)reuseMap.get(inst.getArg1());
					Integer a2 = (Integer)reuseMap.get(inst.getArg3());
					if (a1 != null && a2 != null) {
						Link l1 = new Link(a1.intValue(), inst.getIntArg2());
						Link l2 = new Link(a2.intValue(), inst.getIntArg4());
						if (l2.equals(links.get(l1))) {
							lit.remove();
						}
					}
					break;
			}
		}
		//TO DO enqueueatom̿������� �� ����̿����ˤ����Τ��Ȥ���Τ�����

		Instruction.changeAtomVar(body, reuseMap);

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
			if (o == null) {
				return false;
			}
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
	 *  <li>derefatom,dereffunc�����Ѥ��Ƥ��ʤ�
	 * </ul>
	 * ��郎��������ʤ����ϲ��⤷�ʤ���
	 * @param head ���Ƴ�ޥå���̿����
	 * @param body �ܥǥ�̿����
	 */	
	private static void makeLoop(List head, List body) {
		Instruction inst = (Instruction)head.get(0);
		if (inst.getKind() != Instruction.SPEC) {
			return;
		}
//		if (!inst.getArg2().equals(new Integer(0))) {
//			//�ޥå���̿����˥������ѿ���������
//			return;
//		}
		inst = (Instruction)head.get(1);
		if (inst.getKind() != Instruction.FINDATOM || inst.getIntArg2() != 0) {
			return;
		}
		Integer firstAtom = (Integer)inst.getArg1();

		//���˹��פ��뤫�����ܾ������
		HashMap links = new HashMap(); //newlink̿�������������󥯤ξ���
		HashMap linkGetFrom = new HashMap(); //��� -> getlink����(atom,pos)
		HashMap functor = new HashMap(); // atom -> functor
		HashMap inherit = new HashMap(); // (atom,pos) -> inherit������
		//���ऴ�Ȥ��ѿ����������
		ArrayList memvars = new ArrayList();
		ArrayList atomvars = new ArrayList();
		ArrayList othervars = new ArrayList();
		Instruction react = (Instruction)head.get(head.size() - 1);
//		Iterator it = ((List)react.getArg2()).iterator();
//		while (it.hasNext()) {
//			memvars.add(it.next());
//		}
//		it = ((List)react.getArg3()).iterator();
//		while (it.hasNext()) {
//			atomvars.add(it.next());
//		}
		HashMap varInBody = new HashMap();

		List memlist = (List)react.getArg2();
		for (int i = 0; i < memlist.size(); i++) {
			memvars.add(new Integer(i));
			varInBody.put(memlist.get(i), new Integer(i));
		}
		List atomlist = (List)react.getArg3();
		Integer newFirstAtom = firstAtom;
		for (int i = 0; i < atomlist.size(); i++) {
			atomvars.add(new Integer(memlist.size() + i));
			if (firstAtom.equals(atomlist.get(i))) {
				newFirstAtom = new Integer(memlist.size() + i);
			}
			varInBody.put(atomlist.get(i), new Integer(memlist.size() + i));
		}
		firstAtom = newFirstAtom;
// added by <<n-kato
		List otherlist = (List)react.getArg4();
		for (int i = 0; i < otherlist.size(); i++) {
			othervars.add(new Integer(i));
			varInBody.put(otherlist.get(i), new Integer(memlist.size() + atomlist.size() + i));
		}
// n-kato

		ListIterator lit = head.listIterator();
		while (lit.hasNext()) {
			inst = (Instruction)lit.next();
			switch (inst.getKind()) {
				
				case Instruction.FINDATOM:
					functor.put(varInBody.get(inst.getArg1()), inst.getArg3());
					break;
				case Instruction.FUNC:
					functor.put(varInBody.get(inst.getArg1()), inst.getArg2());
					break;
			}
		}

		lit = body.listIterator();
		while (lit.hasNext()) {
			inst = (Instruction)lit.next();
			switch (inst.getOutputType()) {
				case Instruction.ARG_ATOM:
					atomvars.add(inst.getArg1());
					break;
				case Instruction.ARG_MEM:
					memvars.add(inst.getArg1());
					break;
				case Instruction.ARG_VAR:
					othervars.add(inst.getArg1());
					break;
				case -1:
					break;
				default:
					throw new RuntimeException("invalid output type : " + inst);
			}
			switch (inst.getKind()) {
				case Instruction.DEREFATOM:
				case Instruction.DEREFFUNC:
//					System.out.println(inst);
					return;
				case Instruction.REMOVEATOM:
//				case Instruction.FREEATOM:
					if (inst.getArg1().equals(firstAtom)) {
//						System.out.println(inst);
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
				case Instruction.INHERITLINK:
					Link l = new Link(inst.getIntArg1(), inst.getIntArg2());
					inherit.put(l, inst.getArg3());
					break;
				case Instruction.NEWATOM:
				case Instruction.LOCALNEWATOM:
					functor.put(inst.getArg1(), inst.getArg3());
//					atomvars.add(inst.getArg1());
					break;
				case Instruction.GETLINK:
					linkGetFrom.put(inst.getArg1(), new Link(inst.getIntArg2(), inst.getIntArg3()));
//					othervars.add(inst.getArg1());
					break;
			}
		}

		
		//�롼����̿���������
		
		//�ޤ��ϥ��ԡ������ѿ��ֹ��դ��ؤ�
		Instruction spec = (Instruction)body.get(0);

		ArrayList loop = new ArrayList(); //�롼�����̿����
		//�ޥå���̿����
		lit = head.subList(2, head.size() - 1).listIterator(); //spec,findatom,react/jump�����
		while (lit.hasNext()) {
			loop.add(((Instruction)lit.next()).clone());
		}
		if (react.getKind() != Instruction.REACT && react.getKind() != Instruction.JUMP) {
//			System.out.println(react);
			return;
		}
		//�ܥǥ�̿������ѿ����碌��
		Instruction.applyVarRewriteMap(loop, varInBody);
		//�ܥǥ�̿����
		lit = body.listIterator(1); //spec�����
		while (lit.hasNext()) {
			loop.add(((Instruction)lit.next()).clone());
		}

		//�롼�����ѿ����դ��ؤ�

		//��Ȥ�Ȥ��ѿ����롼����ǡ��������������ѿ�
		HashMap memVarMap = new HashMap();
		HashMap atomVarMap = new HashMap();
		HashMap otherVarMap = new HashMap();
		//�롼����Ǻ�������Ƥ����ѿ�����Ȥ�Ȥ��ѿ�
		HashMap reverseAtomVarMap = new HashMap();
		int base = atomvars.size() + memvars.size() + othervars.size(); //�롼����̿����ǻ��Ѥ����ѿ��γ����� 
		int nextArg = base;
		//��
		Iterator it = memvars.subList(1, memvars.size()).iterator(); //�Ϥ��������
		while (it.hasNext()) {
			memVarMap.put(it.next(), new Integer(nextArg++));
		}
		memVarMap.put(new Integer(0), new Integer(0));
		//���ȥ�
		it = atomvars.iterator();
		while (it.hasNext()) {
			Integer a = (Integer)it.next();
			if (!a.equals(firstAtom)) {
				Integer t = new Integer(nextArg++);
				atomVarMap.put(a, t);
				reverseAtomVarMap.put(t, a); 
			}
		}
		Integer firstAtomInLoop = new Integer(memvars.size() + atomvars.indexOf(firstAtom)); 
		atomVarMap.put(firstAtom, firstAtomInLoop);
		reverseAtomVarMap.put(firstAtomInLoop, firstAtom);
		
		//����¾
		it = othervars.iterator();
		while (it.hasNext()) {
			otherVarMap.put(it.next(), new Integer(nextArg++));
		}
		Instruction.changeMemVar(loop, memVarMap);
		Instruction.changeAtomVar(loop, atomVarMap);
		Instruction.changeOtherVar(loop, otherVarMap);

		Instruction resetVars = Instruction.resetvars(memvars, atomvars, othervars);

		//�롼����Ρ��ѿ�->������ǡ��������ä��ѿ�
		HashMap beforeVar = new HashMap();
		//�롼�׳����ѿ�->�롼����Ǥ�������ǡ��������ä��ѿ�
		HashMap outToBeforeVar = new HashMap();
		for (int i = 0; i < memvars.size(); i++) {
			beforeVar.put(memVarMap.get(memvars.get(i)), new Integer(i));
			outToBeforeVar.put(memvars.get(i), new Integer(i));
		}
		for (int i = 0; i < atomvars.size(); i++) {
			beforeVar.put(atomVarMap.get(atomvars.get(i)), new Integer(memvars.size() + i));
			outToBeforeVar.put(atomvars.get(i), new Integer(memvars.size() + i));
		}
		for (int i = 0; i < othervars.size(); i++) {
			beforeVar.put(otherVarMap.get(othervars.get(i)), new Integer(memvars.size() + atomvars.size() + i));
			outToBeforeVar.put(othervars.get(i), new Integer(memvars.size() + atomvars.size() + i));
		}
		
		//̿������ѹ���Ԥ�

		//�롼�������ѿ��֤������ޥå�
		//�������ά���ơ�����롼�פ��ѿ�����Ѥ�����Ρ�
		// �ֺ�������������ѿ�->����롼�׻����ͤ����äƤ����ѿ���
		HashMap atomVarMap2 = new HashMap();
		//��ε�
//		HashMap reverseAtomVarMap2 = new HashMap();
//		HashMap memVarMap2 = new HashMap();
		HashMap otherVarMap2 = new HashMap();
//		memVarMap2.put(new Integer(0), new Integer(0)); //����
//		atomVarMap.put(new Integer(firstAtom.intValue() + base), firstAtom);
		atomVarMap2.put(firstAtomInLoop, firstAtomInLoop);
//		reverseAtomVarMap2.put(firstAtom, firstAtom);
		
		//dereflink̿�ᤫ�顢���Ǥ˥�󥯤��Ƥ�������狼�äƤ���(atom,pos) -> (atom,pos) (������)
		HashMap alreadyLinked = new HashMap();
		
		ArrayList moveInsts = new ArrayList();
		ListIterator baseIterator = head.subList(2, head.size() - 1).listIterator(); //��������̿����
		ListIterator loopIterator = loop.listIterator(); //�롼����̿����
		while (baseIterator.hasNext()) {
			baseIterator.next();
			inst = (Instruction)loopIterator.next();
			switch (inst.getKind()) {
				case Instruction.DEREF:
//					Link l = (Link)links.get(new Link(inst.getIntArg2(), inst.getIntArg3()));
//					Link l = (Link)links.get(new Link(((Integer)reverseAtomVarMap.get(inst.getArg2())).intValue(), inst.getIntArg3()));
					if (atomVarMap2.containsKey(inst.getArg2())) {
						Integer a = (Integer)atomVarMap2.get(inst.getArg2());
						if (a.intValue() < memvars.size() + atomvars.size()) {
							Link l = (Link)links.get(new Link(((Integer)atomvars.get(a.intValue() - memvars.size())).intValue(), inst.getIntArg3()));
							if (l != null) {
								if (l.pos == inst.getIntArg4()) {
			//						atomVarMap2.put(inst.getArg1(), new Integer(l.atom));
									atomVarMap2.put(inst.getArg1(), outToBeforeVar.get(new Integer(l.atom)));
			//						reverseAtomVarMap2.put(new Integer(l.atom), inst.getArg1());
									loopIterator.remove();
									break; //��������ΤǸ�ν����Ϥ��ʤ�
								} else {
									//���м��Ԥ���Τǥ롼�ײ����ʤ�
//									System.out.println(inst);
									return;
								}
							}
						}
					}
					
//begin
//					Integer beforeAtom = (Integer)atomVarMap.get(atomvars.get(((Integer)beforeVar.get(inst.getArg2())).intValue() - memvars.size()));
					Integer atom = (Integer)atomVarMap2.get(inst.getArg2());
					if (atom != null && atom.intValue() < memvars.size() + atomvars.size()) {
						Integer out = (Integer)atomvars.get(atom.intValue() - memvars.size());
						Link t = new Link(out.intValue(), inst.getIntArg3());
						if (inherit.containsKey(t)) { 
							Integer beforeAtom = (Integer)atomVarMap.get(out);
//end
							if (beforeVar.get(beforeAtom).equals(atomVarMap2.get(beforeAtom))) {
								Integer t1 = (Integer)inherit.get(t);
								Integer t2 = (Integer)outToBeforeVar.get(t1);
								loopIterator.set(new Instruction(Instruction.DEREFLINK, inst.getIntArg1(), t2.intValue(), inst.getIntArg4()));
								
								//��Ĺ��newlink����Τ���Υǡ���
								Link l1 = new Link(inst.getIntArg1(), inst.getIntArg4());
								Link l2out = (Link)linkGetFrom.get(t1);
								Link l2 = new Link(((Integer)outToBeforeVar.get(new Integer(l2out.atom))).intValue(), l2out.pos);
								alreadyLinked.put(l1, l2);
							}
						}
					}
					break;
				case Instruction.FUNC:
					if (atomVarMap2.containsKey(inst.getArg1())) {
//begin
//						Integer atom = (Integer)atomVarMap2.get(inst.getArg1());
						int afterchange = ((Integer)atomVarMap2.get(inst.getArg1())).intValue();
						if (afterchange < memvars.size() + atomvars.size()) {
							atom = (Integer)atomvars.get(afterchange - memvars.size());
//end
							if (functor.containsKey(atom)) {
								if (!functor.get(atom).equals(inst.getArg2())) {
									//���м��Ԥ���Τ�ʣ����Ʊ��Ŭ�ѤϹԤ�ʤ�
//									System.out.println(inst);
									return;
								}
								//������������Τǽ���	
								loopIterator.remove();
							}
						}
					}
					break;					
			}
		}

		HashMap changeToNewlink = new HashMap(); //newlink���ѹ������� -> �����
		HashMap changeLink = new HashMap(); //inheritlink�ΰ�ư��ȼ��������줿getlink̿�����2����������롼�׻��ѿ�����1����
		HashSet movableEnqueue = new HashSet(); //enqueue̿���롼�׸�˰�ư�Ǥ��륢�ȥ�
		baseIterator = body.listIterator(1); //��������̿����
		//loopIterator�Ϥ��ä���³��
		while (baseIterator.hasNext()) {
			Instruction baseInst = (Instruction)baseIterator.next();
			inst = (Instruction)loopIterator.next();
			switch (inst.getKind()) {
				case Instruction.GETLINK:
					//����褬�狼�äƤ���getlink�ν���
					Integer atom = (Integer)inst.getArg2();
//					Integer baseAtom;
					if (atomVarMap2.containsKey(atom)) {
						atom = (Integer)atomVarMap2.get(inst.getArg2()); //�ѿ��֤�������
//						baseAtom = (Integer)reverseAtomVarMap2.get(atom); //����롼��
						if (atom.intValue() < memvars.size() + atomvars.size()) {
							Integer baseAtom = (Integer)atomvars.get(atom.intValue() - memvars.size()); //�롼�׳��ѿ�
	//					} else {
	//						baseAtom = null;//(Integer)atomVarMap.get(atom);
	//					}
	//					if (baseAtom != null) {
							Link l = (Link)links.get(new Link(baseAtom.intValue(), inst.getIntArg3()));
							if (l != null) { //����Υ롼�פ�newlink�ˤ�äƥ���褬����Ǥ�����
	//							Integer baseAtom2 = new Integer(l.atom);
								Integer atom2 = (Integer)atomVarMap.get(new Integer(l.atom));
	//							if (baseAtom.equals(atomVarMap2.get(atom)) ||
	//								baseAtom2.equals(atomVarMap2.get(atomVarMap.get(baseAtom2)))) { //����Υ롼�פ�newlink̿�᤬���������Τξ��
								Integer baseAtomInLoop = (Integer)atomVarMap.get(baseAtom);
								if (beforeVar.get(baseAtomInLoop).equals(atomVarMap2.get(baseAtomInLoop)) ||
									beforeVar.get(atom2).equals(atomVarMap2.get(atom2))) { //����Υ롼�פ�newlink̿�᤬���������Τξ��
									//getlink��������inheritlink��newlink���ѹ�
									loopIterator.remove();
									changeToNewlink.put(inst.getArg1(), l);
									//��������Τ�¾�ν����ϹԤ�ʤ�
									break;
								}
							}
	
							//inheritlink��Ǹ�˰�ư�������ȼ��getlink�ν���
	//						if (baseAtom.equals(atomVarMap2.get(reverseAtomVarMap.get(baseAtom)))) {
							if (atom.intValue() < memvars.size() + atomvars.size()) {
								Integer beforeOutVar = (Integer)atomvars.get(atom.intValue() - memvars.size());
								Integer beforeAtom = (Integer)atomVarMap.get(beforeOutVar);
								if (beforeVar.get(beforeAtom).equals(atomVarMap2.get(beforeAtom))) {
									loopIterator.remove();
		//							changeLink.put(beforeAtom, inst.getArg1());
									otherVarMap2.put(inst.getArg1(), outToBeforeVar.get(inherit.get(new Link(beforeOutVar.intValue(), inst.getIntArg3()))));
								}
							}
						}
					}
					break;
				case Instruction.INHERITLINK:
					Integer atomVar = (Integer)inst.getArg1();
//					Integer baseAtom = (Integer)reverseAtomVarMap.get(atomVar);
					Integer beforeAtom = (Integer)beforeVar.get(atomVar);
					if (beforeAtom.equals(atomVarMap2.get(atomVar))) { //��Ȥ�Ȥ��ѿ��ֹ��Ʊ���ˤʤäƤ�����
						//�Ǹ�˰�ư
						moveInsts.add(baseInst);
						baseIterator.remove();
						loopIterator.remove();
//						if (changeLink.containsKey(atomVar)) {
//							otherVarMap2.put(changeLink.get(atomVar), beforeVar.get(inst.getArg1()));
//						}
					} else {
						Integer linkVar = (Integer)inst.getArg3();
						if (changeToNewlink.containsKey(linkVar)) {
							Link l = (Link)changeToNewlink.get(linkVar);
							//newlink��롼�׸�˰�ư�����Τ�ȼ��inheritlink��newlink���ѹ�
							Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
							Link l2 = new Link(((Integer)outToBeforeVar.get(new Integer(l.atom))).intValue(), l.pos);
							//��Ĺ�ʾ��Ͻ���
							if (l1.equals(alreadyLinked.get(l2)) || l2.equals(alreadyLinked.get(l1))) {
								loopIterator.remove();
							} else {
								try {
									loopIterator.set(Instruction.newlink(l1.atom, l1.pos, l2.atom, l2.pos, inst.getIntArg5()));
								} catch (IndexOutOfBoundsException e) {
									loopIterator.set(new Instruction(Instruction.NEWLINK, l1.atom, l1.pos, l2.atom, l2.pos));
								}
							}
						}
					}
					break;
				case Instruction.NEWLINK:
					atomVar = (Integer)inst.getArg1();
					Integer atomVar2 = (Integer)inst.getArg3();
					beforeAtom = (Integer)beforeVar.get(inst.getArg1());
					Integer beforeAtom2 = (Integer)beforeVar.get(inst.getArg3());
					if (beforeAtom.equals(atomVarMap2.get(atomVar)) ||
						beforeAtom2.equals(atomVarMap2.get(atomVar2))) { //��Ȥ�Ȥ��ѿ��ֹ��Ʊ���ˤʤäƤ�����
						//�Ǹ�˰�ư
						moveInsts.add(baseInst);
						baseIterator.remove();
						loopIterator.remove();
						break;
					}

					//��Ĺ�ʥ�������ν���					
					Link l1 = new Link(inst.getIntArg1(), inst.getIntArg2());
					Link l2 = new Link(inst.getIntArg3(), inst.getIntArg4());
					if (l1.equals(alreadyLinked.get(l2)) || l2.equals(alreadyLinked.get(l1))) {
						loopIterator.remove();
					}
					break;
				case Instruction.DEQUEUEATOM:
					if (atomVarMap2.containsKey(inst.getArg1())) {
						atom = (Integer)atomVarMap2.get(inst.getArg1());
						if (atom.intValue() < memvars.size() + atomvars.size()) {
							loopIterator.remove();
							movableEnqueue.add(atomVarMap.get(atomvars.get(atom.intValue() - memvars.size())));
						}
					}
					break;
				case Instruction.ENQUEUEATOM:
					if (movableEnqueue.contains(inst.getArg1())) {
						loopIterator.remove();
						baseIterator.remove();
						moveInsts.add(baseInst);
					}
					break;
			}
		}
		//�롼�פκǸ��resetvars̿�������
		int memmax = ((List)react.getArg2()).size();
		int atommax = memmax + ((List)react.getArg3()).size();

		ArrayList memvars2 = new ArrayList();
		ArrayList atomvars2 = new ArrayList();
		ArrayList othervars2 = new ArrayList();
		lit = memvars.listIterator();
		while (lit.hasNext()) {
			memvars2.add((Integer)memVarMap.get(lit.next()));
		}
		lit = atomvars.listIterator();
		while (lit.hasNext()) {
			Integer var = (Integer)atomVarMap.get(lit.next());
			if (atomVarMap2.containsKey(var)) {
				var = (Integer)atomVarMap2.get(var);
			}
			atomvars2.add(var);
		}
		lit = othervars.listIterator();
		while (lit.hasNext()) {
			othervars2.add((Integer)otherVarMap.get(lit.next()));
		}
				
		loop.add(loop.size() - 1, Instruction.resetvars(memvars2, atomvars2, othervars2));
		
		Instruction.changeAtomVar(loop, atomVarMap2);
		
		//proceed̿�����������
		body.add(body.size() - 1, resetVars);
		ArrayList looparg = new ArrayList();
		looparg.add(loop);
		body.add(body.size() - 1, new Instruction(Instruction.LOOP, looparg));
		//�Ǹ��1��¹Ԥ���̿�������
		Instruction.applyVarRewriteMap(moveInsts, outToBeforeVar);
		body.addAll(body.size() - 1, moveInsts);
		
		//spec̿����ѹ�
		if (nextArg > spec.getIntArg1()) {
//			body.set(0, Instruction.spec(spec.getIntArg1(), nextArg));
			spec.updateSpec(spec.getIntArg1(), nextArg);
		}
	}

}