package runtime;

final class SystemRuleset extends Ruleset {
	public String toString() {
		return "System Ruleset Object";
	}
	/**
	 * ���ȥ��Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean public react(Membrane mem, Atom atom) {
		return false;
	}
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	boolean public react(Membrane mem) {
		return false;
	}
	
}
