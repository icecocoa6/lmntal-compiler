package compile.structure;

/**
 * �ץ�����
 * �ץ�����������̾�Ȥ��ơ�PROXY_NAME �����
 * @author Takahiko Nagata
 * @date 2003/11/05
 */
final public class ProxyAtom extends Atom {
	
	public final static String PROXY_NAME  = "builtin::proxy";

	/**
	 * ����̲᤹�뼫ͳ��󥯤��������ץ��������������ޤ�
	 * @param mem ����
	 * @param insideLink ��¦�ؤΥ��
	 * @param outsideLink ����ͤ�ȴ���볰¦�ؤΥ��
	 */
	public ProxyAtom(Membrane mem, LinkOccurrence insideLink, LinkOccurrence outsideLink) {
		super(mem, PROXY_NAME, 2);
		this.args[0] = insideLink;
		this.args[1] = outsideLink;
	}
}
