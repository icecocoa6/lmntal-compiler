package runtime;

import java.util.*;

public class Dumper {
	/** todo ���Υ��饹�ˤ���ΤϤ��������Τ�Ŭ�ڤʾ��˰�ư���� */
	static boolean isInfixOperator(String name) {
		return name.equals("=");
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
			    && !a.getFunctor().getInternalName().equals("")	// �̾�Υե��󥯥������ˤ�����
			//	&& !a.getName().matches("-?[0-9]+|^[A-Z]")) {	// IntegerFunctor̤���ѻ��θŤ�������
				&& !a.getName().matches("^[A-Z]")) {			// �ݴɤ��줿��ͳ��󥯤ϰ������֤�����
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
		StringBuffer buf = new StringBuffer();
		buf.append(a.getAbbrName());
		atoms.remove(a);
		int arity = a.getArity();
		if (arity > 0) {
			buf.append("(");
			buf.append(dumpLink(a.args[0], atoms));
			for (int i = 1; i < arity; i++) {
				buf.append(",");
				buf.append(dumpLink(a.args[i], atoms));
			}
			buf.append(")");
		}
		return buf.toString();
	}
	private static String dumpAtomGroupWithoutLastArg(Atom a, Set atoms) {
		StringBuffer buf = new StringBuffer();
		buf.append(a.getAbbrName());
		atoms.remove(a);
		int arity = a.getArity();
		if (arity > 1) {
			buf.append("(");
			buf.append(dumpLink(a.args[0], atoms));
			//�ǽ������ʳ������
			for (int i = 1; i < arity - 1; i++) {
				buf.append(",");
				buf.append(dumpLink(a.args[i], atoms));
			}
			buf.append(")");
		}
		return buf.toString();
	}
	private static String dumpLink(Link l, Set atoms) {
		if (l.isFuncRef() && atoms.contains(l.getAtom())) {
			return dumpAtomGroupWithoutLastArg(l.getAtom(), atoms);
		} else {
			return l.toString();
		}
	}
}
