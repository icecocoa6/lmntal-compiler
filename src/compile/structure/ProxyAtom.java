package compile.structure;

/**
 * �ץ�����
 * �ץ�����������̾�Ȥ��ơ�PROXY_NAME �����
 * @author Takahiko Nagata
 * @date 2003/11/05
 */
final public class ProxyAtom extends Atom {
	
	public final static String PROXY_NAME  = "proxy";

	/**
	 * ����̲᤹�뼫ͳ��󥯤��������ץ��������������ޤ�
	 * @param mem ����
	 */
	public ProxyAtom(Membrane mem) {
		super(mem, PROXY_NAME, 2);
	}
}
