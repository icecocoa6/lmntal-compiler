package runtime;

/** ���֥������Ȥؤλ��Ȥ��ݻ�����ե��󥯥���
 * ���Υ��饹��SymbolFunctor�δ��쥯�饹�ˤ��������褵����
 * ʬ���Ķ���ž��������ǽ���������硢���֥������Ȥ�Serializable���󥿡��ե��������������ɬ�פ����롣
 * @author n-kato */
public class ObjectFunctor extends Functor {
	Object data;
	public ObjectFunctor(Object data) { super("",1);  this.data = data; }
	public int hashCode() { return data.hashCode(); }
	public Object getObject() { return data; }
	public Object getValue() { return data; }
	public boolean equals(Object o) {
//		return o.getClass() == getClass() && data.equals(((ObjectFunctor)o).data);
		
		//2006.6.26 by inui
		try {
			return (o.getClass() == getClass()) && isSubclass(Class.forName(data.toString()), Class.forName(((ObjectFunctor)o).data.toString()));
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	public String getName() { return data.toString(); }
	
	//c1��c2�Υ��֥��饹���ɤ���Ƚ�ꤷ�ޤ� 2006.6.26 by inui
	private static boolean isSubclass(Class c1, Class c2) {
		if (c1.equals(c2)) return true;
		if (c1.equals(Object.class)) return false;
		return isSubclass(c1.getSuperclass(), c2);
	}
}