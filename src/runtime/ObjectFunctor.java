package runtime;

/** ���֥������Ȥؤλ��Ȥ��ݻ�����ե��󥯥�
 * @author n-kato */
public class ObjectFunctor extends Functor {
	Object data;
	public ObjectFunctor(Object data) { super("",1);  this.data = data; }
	public int hashCode() { return data.hashCode(); }
	public Object getObject() { return data; }
	public boolean equals(Object o) {
		return o.getClass() == getClass() && data.equals(((ObjectFunctor)o).data);
	}
	public String getName() { return data.toString(); }
	// �ʲ�2�Ĥϡ������StringFunctor��ͭ�ν�����ˡ�ΤϤ�����Ϥ�FunctorFactory������
	public String getQuotedFuncName() { return getStringLiteralText(data.toString()); }
	public String getQuotedAtomName() { return getStringLiteralText(data.toString()); }
}