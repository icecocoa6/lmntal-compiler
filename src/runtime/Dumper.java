package runtime;

import java.util.*;

public class Dumper {
	/** �����Ȥ���Ϥ��롣���Ϸ����λ���Ϥޤ��Ǥ��ʤ��� */
	public static String dump(AbstractMembrane mem) {
		StringBuffer buf = new StringBuffer();
		List predAtoms = new ArrayList();
		Set atoms = new HashSet(mem.getAtomCount());

		// ���ȥ�ν���
		Iterator it = mem.atomIterator();
		while (it.hasNext()) {


			Atom a = (Atom)it.next();
			atoms.add(a);
			//�����Υ��ȥ�����ˤ��롣
			//�ǽ�����Ʊ�Τ��Ĥʤ��äƤ����硢arity��1��ʪ��ͥ�褷�������������⡣
			if (a.getArity() == 0 ||						//��󥯤Τʤ����
				a.getLastArg().getAtom().getMem() != mem ||	//�ǽ���������μ�ͳ��󥯤ξ��
				a.getLastArg().isFuncRef() ) {				//�ǽ�����Ʊ�Τ��Ĥʤ��äƤ�����
				predAtoms.add(a);
			}
		}

		//predAtoms��Υ��ȥ�����˽���
		it = predAtoms.iterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			//���Ǥ˽��Ϥ���Ƥ��ޤäƤ�����⤢��
			if (atoms.contains(a)) {
				buf.append(dumpAtomGroup(a, atoms));
				buf.append(", ");
			}
		}
		//��ϩ��������ˤϤޤ��ĤäƤ���Τǡ�Ŭ���ʽ꤫����ϡ�
		//��ϩ����ʬ��õ�����������������Ȥꤢ�������Τޤޡ�
		while (true) {
			it = atoms.iterator();
			if (!it.hasNext()) {
				break;
			}
			buf.append(dumpAtomGroup((Atom)it.next(), atoms));
			buf.append(", ");
		}

		//����ν���		
		it = mem.memIterator();
		while (it.hasNext()) {
			buf.append("{");
			buf.append(dump((Membrane)it.next()));
			buf.append("}, ");
		}
		
		//�롼��ν���
		it = mem.rulesetIterator();
		while (it.hasNext()) {
			buf.append((Ruleset)it.next());
			buf.append(", ");
		}

		return buf.toString();
	}
	private static String dumpAtomGroup(Atom a, Set atoms) {
		StringBuffer buf = new StringBuffer();
		buf.append(a.getName());
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
		buf.append(a.getName());
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
