package runtime;

public final class InterpretedRuleset extends Ruleset {
	/** �Ƃ肠�������[���̔z��Ƃ��Ď��� */
	private int id;
	private static int lastId;
	
	
	/** ���哱���s�p���ߗ�B�P�ڂ̓Y�����̓��[���ԍ� */
	private HeadInstruction[][] memMatch;
	/** �A�g���哱���s�p���ߗ�BMap�ɂ��ׂ��H */
	private HeadInstruction[][] atomMatches;
	/** �{�f�B���s�p���ߗ�B�P�ڂ̓Y�����̓��[���ԍ� */
	private BodyInstruction[][] body;
	
	public InterpretedRuleset(compile.Rule[] rules) {
	}
	
	public String toString() {
		return "@" + id;
	}
	/**
	 * �A�g���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	boolean react(Membrane mem, AbstractAtom atom) {
		return false;
	}
	/**
	 * ���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	boolean react(Membrane mem) {
		return false;
	}
	/**
	 * ���[����K�p����B<br>
	 * �����̖��ƁA�����̃A�g���̏������͂��łɃ��b�N����Ă�����̂Ƃ���B
	 * @param ruleid �K�p���郋�[��
	 * @param memArgs �������̂����A���ł������
	 * @param atomArgs �������̂����A�A�g���ł������
	 */
	private void body(int ruleid, AbstractMembrane[] memArgs, AbstractAtom[] atomArgs) {
	}
}
