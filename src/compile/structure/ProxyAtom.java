package compile.structure;

/**
 * ����̲᤹�뼫ͳ��󥯤��������ץ������ȥ�ι�¤��ɽ�����饹��
 * �ץ����Υ�󥯤Τ�����
 * ¾����ؤΥ�󥯤�1���ܤΥ��(���� 0����)
 * Ʊ����ؤΥ�󥯤�2���ܤΥ��(���� 1����)
 * �Ȥ��롣
 * @author Takahiko Nagata, n-kato
 * @date 2003/11/05
 * <p>todo ���Υ��饹�Ϥ������ѻߤ���
 */
final public class ProxyAtom extends Atom {

	/** ��ͳ��󥯽��ϴ������ȥ�̾ */
	public final static String INSIDE_PROXY_NAME = runtime.Functor.INSIDE_PROXY.getName();
	
	/** ��ͳ������ϴ������ȥ�̾ */
	public final static String OUTSIDE_PROXY_NAME = runtime.Functor.OUTSIDE_PROXY.getName();
	
	/**
	 * ���󥹥ȥ饯��
	 * @param mem ��°��
	 * @param name �ץ�����̾��
	 */
	public ProxyAtom(Membrane mem, String name) {
		super(mem,name,2);
	}
	
}
