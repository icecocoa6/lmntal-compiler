package compile.structure;

/**
 * �ץ�����
 * �ץ�����������̾�Ȥ��ơ�PROXY_NAME �����
 * �ץ����Υ�󥯤Τ�����
 * ¾����ؤΥ�󥯤�1���ܤΥ��(���� 0����)
 * Ʊ����ؤΥ�󥯤�2���ܤΥ��(���� 1����)
 * �Ȥ���
 * @author Takahiko Nagata
 * @date 2003/11/05
 * TODO type���ѻߤ���ʸ���󤫤�ľ���������������褤���ޤ���runtime.Functor��������Ȥ������Υե�������ѻߤ���
 */
final public class ProxyAtom extends Atom {
	
	public final static String PROXY_NAME  = "proxy";

	/**
	 * ��ͳ��󥯽��ϴ������ȥॿ����
	 */
	public final static int INSIDE_PROXY = 0x01;

	/**
	 * ��ͳ��󥯽��ϴ������ȥ�̾
	 */
	public final static String INSIDE_PROXY_NAME = "inside_proxy";
	
	/**
	 * ��ͳ��󥯽��ϴ������ȥॿ����
	 */
	public final static int OUTSIDE_PROXY = 0x02;

	/**
	 * ��ͳ��󥯽��ϴ������ȥ�̾
	 */
	public final static String OUTSIDE_PROXY_NAME = "outside_proxy";

	/**
	 * ����̲᤹�뼫ͳ��󥯤��������ץ��������������ޤ�
	 * @param mem ����
	 */
	public ProxyAtom(Membrane mem) {
		this(-1, mem);
	}
	
	/**
	 * ����̲᤹�뼫ͳ��󥯤��������ץ��������������ޤ�
	 * @param type �ץ����μ��� 
	 * @param mem ����
	 */
	public ProxyAtom(int type, Membrane mem) {
		super(mem, getProxyName(type), 2);
	}
	
	/**
	 * �ץ����μ��फ��ץ���̾�����ޤ�
	 * @param type �ץ��������� INSIDE_PROXY or OUTSIDE_PROXY
	 * @return �����פ˽��ä��ץ���̾
	 */
	public static String getProxyName(int type) {
		switch (type) {
			case INSIDE_PROXY: return INSIDE_PROXY_NAME;
			case OUTSIDE_PROXY: return OUTSIDE_PROXY_NAME;
			default: return PROXY_NAME;
		}
	}
}
