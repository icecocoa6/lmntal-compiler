package runtime;

/**
 * ���[���̏W���B
 * ���݂̓��[���̔z��Ƃ��ĕ\�����Ă��邪�A�����I�ɂ͕����̃��[���̃}�b�`���O��
 * �P�̃}�b�`���O�e�X�g�ōs���悤�ɂ���B
 */
abstract public class Ruleset {
	abstract public String toString();
	/**
	 * �A�g���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	abstract boolean react(Membrane mem, Atom atom);
	/**
	 * ���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	abstract boolean react(Membrane mem);
}