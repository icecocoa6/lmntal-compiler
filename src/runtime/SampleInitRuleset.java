package runtime;

final class SampleInitRuleset extends Ruleset {
	public String toString() {
		return "Ruleset Sample";
	}
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean react(Membrane mem, Atom atom) {
		return false;
	}
	/**
	 * ̵���餹�٤Ƥ���롼��Ϥ���ʴ�����
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean react(Membrane mem) {
		Atom a, b, c;
		AbstractMembrane m1;
		
		a = mem.newAtom(new Functor("a", 1));
		b = mem.newAtom(new Functor("b", 2));
		m1 = mem.newMem();
		c = m1.newAtom(new Functor("c", 1));
		
		mem.newLink(a, 0, b, 0);
		mem.newLink(b, 0, a, 0);
		mem.newLink(b, 1, c, 0);
		mem.newLink(c, 0, b, 1);
		
		return true;
	}
	
}
