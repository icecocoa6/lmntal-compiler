/*
 * �쐬��: 2003/10/24
 *
 */
package runtime;

/**
 * ���s���낿�イ
 * @author hara
 */

public final class Rule {
	public HeadInstruction[] memMatch;
	public HeadInstruction[][] atomMatches; //?
	public BodyInstruction[] body;
	public String text;
	
	/**
	 * ���哱�e�X�g���s���A�}�b�`����ΓK�p����
	 * @return ���[����K�p�����ꍇ��true
	 */
	public boolean react(Membrane mem) {
		Env.c("Rule.react "+mem);
		return true;
	}
	public String toString() {
		return text;
	}
}
