package runtime;

final class SystemRuleset extends Ruleset {
	public String toString() {
		return "System Ruleset Object";
	}
	/**
	 * �A�g���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	boolean react(Membrane mem, Atom atom) {
		return false;
	}
	/**
	 * ���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	boolean react(Membrane mem) {
		return false;
	}
	
}
