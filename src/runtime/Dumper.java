package runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class Dumper {
	/** ���̒��g���o�͂���B�o�͌`���̎w��͂܂��ł��Ȃ��B */
	static String dump(AbstractMembrane mem) {
		StringBuffer buf = new StringBuffer();
		List predAtoms = new ArrayList();
		Set atoms = new HashSet(mem.getAtomCount());

		// �A�g���̏o��
		Iterator it = mem.atomIterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			atoms.add(a);
			//�����̃A�g�����N�_�ɂ���B
			//�ŏI�������m���Ȃ����Ă���ꍇ�Aarity��1�̕���D�悵���������������B
			if (a.getArity() == 0 ||						//�����N�̂Ȃ��ꍇ
				a.getLastArg().getAtom().getMem() != mem ||	//�ŏI���������̎��R�����N�̏ꍇ
				a.getLastArg().isFuncRef() ) {				//�ŏI�������m���Ȃ����Ă���ꍇ
				predAtoms.add(a);
			}
		}

		//predAtoms���̃A�g�����N�_�ɏo��
		it = predAtoms.iterator();
		while (it.hasNext()) {
			Atom a = (Atom)it.next();
			//���łɏo�͂���Ă��܂��Ă���ꍇ������
			if (atoms.contains(a)) {
				buf.append(dumpAtomGroup(a, atoms));
				buf.append(", ");
			}
		}
		//�H������ꍇ�ɂ͂܂��c���Ă���̂ŁA�K���ȏ�����o�́B
		//�H�̕�����T���������������A�Ƃ肠�������̂܂܁B
		while (true) {
			it = atoms.iterator();
			if (!it.hasNext()) {
				break;
			}
			buf.append(dumpAtomGroup((Atom)it.next(), atoms));
			buf.append(", ");
		}

		//�q���̏o��		
		it = mem.memIterator();
		while (it.hasNext()) {
			buf.append("{");
			buf.append(dump((Membrane)it.next()));
			buf.append("}, ");
		}
		
		//���[���̏o��
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
			//�ŏI�����ȊO���o��
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
