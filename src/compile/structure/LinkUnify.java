/**
 * ��󥯤�ñ�첽
 */

package compile.structure;

public final class LinkUnify extends Atom {
	
	/**
	 * ���ñ�첽�򤢤�魯���ȥ�̾
	 */
	public static final String LINK_UNIFY_NAME = "="; // "builtin::unify";
	
	/**
	 * ��󥯤�ñ�첽��ɽ��
	 * @param mem ����
	 * @param leftLink ��¦�Υ��
	 * @param rightLink ��¦�Υ��
	 */
	public LinkUnify(Membrane mem, LinkOccurrence leftLink, LinkOccurrence rightLink) {
		super(mem, LINK_UNIFY_NAME, 2);
		this.args[0] = leftLink;
		this.args[1] = rightLink;	
	}

	/**
	 * ��󥯤�ñ�첽��ɽ��
	 * @param mem ����
	 */
	public LinkUnify(Membrane mem) {
		super(mem, LINK_UNIFY_NAME, 2);
	}
}