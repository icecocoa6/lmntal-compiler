package runtime;

/** ���֥������Ȥؤλ��Ȥ��ݻ�����1�����ե��󥯥���
 * <strike>ʬ���Ķ���ž��������ǽ���������硢���֥������Ȥ�Serializable���󥿡��ե��������������ɬ�פ����롣</strike>
 * @author n-kato */
public class ObjectFunctor extends DataFunctor {
	Object data;
	public ObjectFunctor(Object data) {
		this.data = data;
	}
	
	public int hashCode() { return data.hashCode(); }
	/**
	 * ���Υե��󥯥����ݻ����Ƥ��륪�֥������Ȥ�������ޤ�
	 * @return ���Υե��󥯥����ݻ����Ƥ��륪�֥�������
	 */
	public Object getObject() { return data; }
	/**
	 * ���Υե��󥯥����ݻ����Ƥ��륪�֥������Ȥ�������ޤ�
	 * @return ���Υե��󥯥����ݻ����Ƥ��륪�֥�������
	 */
	public Object getValue() { return data; }
	
	public boolean equals(Object o) {
		return o.getClass() == getClass() && data.equals(((ObjectFunctor)o).data);
	}
	
	/**
	 * ���Υե��󥯥���̾�����֤�
	 * @return �ݻ����Ƥ��륪�֥������Ȥ�̾��
	 */
	public String getName() {
		return data.toString();
	}
	
	/**
	 * c1��c2�Υ��֥��饹���ɤ���Ƚ�ꤷ�ޤ�
	 * @since 2006.6.26
	 * @author inui
	 */
	public static boolean isSubclass(Class c1, Class c2) {
//		System.err.println(c1+","+c2);
		if (c1.equals(c2)) return true;
		if (c1.equals(Object.class)) return false;
		return isSubclass(c1.getSuperclass(), c2);
	}
}