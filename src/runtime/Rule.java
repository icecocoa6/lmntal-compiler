/*
 * ������: 2003/10/24
 *
 */
package runtime;

/**
 * ��Ժ�����夦
 * @author hara
 */

public final class Rule {
	public HeadInstruction[] memMatch;
	public HeadInstruction[][] atomMatches; //?
	public BodyInstruction[] body;
	public String text;
	
	/**
	 * ���Ƴ�ƥ��Ȥ�Ԥ����ޥå������Ŭ�Ѥ���
	 * @return �롼���Ŭ�Ѥ�������true
	 */
	public boolean react(Membrane mem) {
		Env.c("Rule.react "+mem);
		return true;
	}
	public String toString() {
		return text;
	}
}
