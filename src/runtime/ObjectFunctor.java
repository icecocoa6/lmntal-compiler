package runtime;

/** ���֥������Ȥؤλ��Ȥ��ݻ�����ե��󥯥���
 * ���Υ��饹��SymbolFunctor�δ��쥯�饹�ˤ��������褵����
 * @author n-kato */
public class ObjectFunctor extends Functor {
	Object data;
	public ObjectFunctor(Object data) { super("",1);  this.data = data; }
	public int hashCode() { return data.hashCode(); }
	public Object getObject() { return data; }
	public Object getValue() { return data; }
	public boolean equals(Object o) {
		return o.getClass() == getClass() && data.equals(((ObjectFunctor)o).data);
	}
	public String getName() { return data.toString(); }
}