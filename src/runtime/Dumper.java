package runtime;

import java.util.*;

public class Dumper {
	/** todo ���Υ��饹�ˤ���ΤϤ��������Τ�Ŭ�ڤʾ��˰�ư���� */
	static HashMap binops = new HashMap();
	static final int xfy = 0;
	static final int yfx = 1;
	static final int xfx = 2;
	static {
		binops.put("*",   new int[]{yfx,400});
		binops.put("/",   new int[]{yfx,400});
		binops.put("*.",  new int[]{yfx,400});
		binops.put("/.",  new int[]{yfx,400});
		binops.put("+",   new int[]{yfx,500});
		binops.put("-",   new int[]{yfx,500});
		binops.put("+.",  new int[]{yfx,500});
		binops.put("-.",  new int[]{yfx,500});
		binops.put("=",   new int[]{xfx,700});
		binops.put("==",  new int[]{xfx,700});
		binops.put("=:=", new int[]{xfx,700});
		binops.put("=\\=",new int[]{xfx,700});
		binops.put(">",   new int[]{xfx,700});
		binops.put(">=",  new int[]{xfx,700});
		binops.put("<",   new int[]{xfx,700});
		binops.put("=<",  new int[]{xfx,700});
		binops.put("!=",  new int[]{xfx,700});
		binops.put("=:=.",new int[]{xfx,700});
		binops.put(">.",  new int[]{xfx,700});
		binops.put(">=.", new int[]{xfx,700});
		binops.put("<.",  new int[]{xfx,700});
		binops.put("=<.", new int[]{xfx,700});
		binops.put("!=.", new int[]{xfx,700});
	}
	static boolean isInfixOperator(String name) {
		return binops.containsKey(name);
	}
	static int getBinopType(String name) {
		return ((int[])binops.get(name))[0];
	}
	static int getBinopPrio(String name) {
		return ((int[])binops.get(name))[1];
	}
	/** �����Ȥ���Ϥ��롣���Ϸ����λ���Ϥޤ��Ǥ��ʤ��� */
	public static String dump(AbstractMembrane mem) {
		StringBuffer buf = new StringBuffer();
		List predAtoms[] = {new ArrayList(),new ArrayList(),new ArrayList(),new ArrayList()};
		//Set atoms = new HashSet(mem.getAtomCount());
		Set atoms = new HashSet(mem.atoms.size()); // ����proxy��ɽ�����Ƥ��뤿�ᡣ���������᤹
		boolean commaFlag = false;
		
		// #1 - ���ȥ�ν���
		
		Iterator it = mem.atomIterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			atoms.add(a);
		}
		
		// �����ˤ��륢�ȥ�Ȥ���ͥ����:
		//  0. �����ʤ��Υ��ȥࡢ����Ӻǽ�������������ʳ��ؤΥ�󥯤Ǥ��륢�ȥ�
		//  1. 2�����黻�ҤΥ��ȥ�
		//  2. �����ʳ���1�����ǥ���褬�ǽ������Υ��ȥ�
		//  3. �����ʳ��ǥ���褬�ǽ������Υ��ȥ�
		
		// �����ˤ��ʤ����ȥ��EXPANDATOMS̵������Τߡ�
		//  - ���ߤ�̵��
		
		it = mem.atomIterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			if (a.getArity() == 0 || a.getLastArg().getAtom().getMem() != mem) {
				predAtoms[0].add(a);
			}
			else if (a.getArity() == 2 && isInfixOperator(a.getName())) {
				predAtoms[1].add(a);
			}
			else if (a.getLastArg().isFuncRef()
				// todo �����ɤ������������ΤǤʤ�Ȥ�����
			    && a.getFunctor().isSymbol()					// �̾�Υե��󥯥������ˤ�����
			//	&& !a.getName().matches("-?[0-9]+|^[A-Z]")) {	// IntegerFunctor̤���ѻ��θŤ�������
				&& !a.getName().matches("^[A-Z].*")				// �䴰���줿��ͳ��󥯤ϰ������֤�����
				&& !a.getFunctor().equals(Functor.INSIDE_PROXY)
				&& !a.getFunctor().equals(Functor.OUTSIDE_PROXY) ) {
				predAtoms[a.getArity() == 1 ? 2 : 3].add(a);
			}
		}
		//predAtoms��Υ��ȥ�����˽���
		for (int phase = 0; phase < predAtoms.length; phase++) {
			it = predAtoms[phase].iterator();
			while (it.hasNext()) {
				Atom a = (Atom)it.next();
				if (atoms.contains(a)) { // �ޤ����Ϥ���Ƥ��ʤ����
					if(commaFlag) buf.append(", "); else commaFlag = true;
					buf.append(dumpAtomGroup(a, atoms));
				}
			}
		}
		//��ϩ��������ˤϤޤ��ĤäƤ���Τǡ�Ŭ���ʽ꤫����ϡ�
		//��ϩ����ʬ��õ�����������������Ȥꤢ�������Τޤޡ�
		while (true) {
			it = atoms.iterator();
			if (!it.hasNext()) {
				break;
			}
			if(commaFlag) buf.append(", "); else commaFlag = true;
			buf.append(dumpAtomGroup((Atom)it.next(), atoms));
		}

		// #2 - ����ν���		
		it = mem.memIterator();
		while (it.hasNext()) {
			Membrane m = (Membrane)it.next();
			if(commaFlag) buf.append(", "); else commaFlag = true;
			if(m.name!=null) buf.append(m.name+":");
			buf.append("{");
			buf.append(dump(m));
			buf.append("}");
		}
		
		// #3 - �롼��ν���
		it = mem.rulesetIterator();
		while (it.hasNext()) {
			if(commaFlag) buf.append(", "); else commaFlag = true;
			buf.append((Ruleset)it.next());
		}

		return buf.toString();
	}
	private static String dumpAtomGroup(Atom a, Set atoms) {
		return dumpAtomGroup(a,atoms,0,999);
	}
	private static String dumpAtomGroupWithoutLastArg(Atom a, Set atoms, int outerprio) {
		return dumpAtomGroup(a,atoms,1,outerprio);
	}
	/** ���ȥ�ΰ�����Ÿ�����ʤ���ʸ������Ѵ����롣
	 * �����������ȥ�a�κǸ��reducedArgCount�Ĥΰ����Ͻ��Ϥ��ʤ���
	 * <p>
	 * ���Ϥ������ȥ��atoms��������롣
	 * ���Ϥ��륢�ȥ��atoms�����ǤǤʤ���Фʤ�ʤ���
	 * @param a ���Ϥ��륢�ȥ�
	 * @param atoms �ޤ����Ϥ��Ƥ��ʤ����ȥ�ν��� [in,out]
	 * @param reducedArgCount a�Τ������Ϥ��ʤ��Ǹ�ΰ�����Ĺ��
	 */
	private static String dumpAtomGroup(Atom a, Set atoms, int reducedArgCount, int outerprio) {
		atoms.remove(a);
		Functor func = a.getFunctor();
		int arity = func.getArity() - reducedArgCount;
		if (arity == 0) {
			return func.getQuotedAtomName(); // func.getAbbrName();
		}
		StringBuffer buf = new StringBuffer();
		if (arity == 2 && isInfixOperator(func.getName())) {
			int type = getBinopType(func.getName());
			int prio = getBinopPrio(func.getName());
			int innerleftprio  = prio + ( type == yfx ? 1 : 0 );
			int innerrightprio = prio + ( type == xfy ? 1 : 0 );
			boolean needpar = (outerprio < innerleftprio || outerprio < innerrightprio);
			if (needpar) buf.append("(");
			buf.append(dumpLink(a.args[0], atoms, innerleftprio));
			buf.append(" ");
			buf.append(func.getName());
			buf.append(" ");
			buf.append(dumpLink(a.args[1], atoms, innerrightprio));
			if (needpar) buf.append(")");
			return buf.toString();
		}
		if (arity == 2 && func.equals(new Functor(".",3))) {
			buf.append("[");
			buf.append(dumpLink(a.args[0], atoms, outerprio));
			buf.append(dumpListCdr(a.args[1], atoms));
			buf.append("]");
			return buf.toString();
		}
		buf.append(func.getQuotedFunctorName());
		buf.append("(");
		buf.append(dumpLink(a.args[0], atoms));
		for (int i = 1; i < arity; i++) {
			buf.append(",");
			buf.append(dumpLink(a.args[i], atoms));
		}
		buf.append(")");
		return buf.toString();
	}
	private static String dumpListCdr(Link l, Set atoms) {
		StringBuffer buf = new StringBuffer();
		while (true) {		
			if (!( l.isFuncRef() && atoms.contains(l.getAtom()) )) break;
			Atom a = l.getAtom();
			if (!a.getFunctor().equals(new Functor(".",3))) break;
			atoms.remove(a);
			buf.append(",");
			buf.append(dumpLink(a.args[0], atoms));
			l = a.args[1];
		}
		if (l.getAtom().getFunctor().equals(new Functor("[]",1))) {
			atoms.remove(l.getAtom());
		} else {
			buf.append("|");
			buf.append(dumpLink(l, atoms));
		}
		return buf.toString();
	}
	private static String dumpLink(Link l, Set atoms) {
		return dumpLink(l,atoms,999);
	}
	private static String dumpLink(Link l, Set atoms, int outerprio) {
		if (Env.verbose < Env.VERBOSE_EXPANDATOMS && l.isFuncRef() && atoms.contains(l.getAtom())) {
			return dumpAtomGroupWithoutLastArg(l.getAtom(), atoms, outerprio);
		} else {
			return l.toString();
		}
	}
}
