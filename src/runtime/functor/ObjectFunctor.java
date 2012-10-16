package runtime.functor;


/** ���֥������Ȥؤλ��Ȥ��ݻ�����1�����ե��󥯥���
 * <strike>ʬ���Ķ���ž��������ǽ���������硢���֥������Ȥ�Serializable���󥿡��ե��������������ɬ�פ����롣</strike>
 * @author n-kato */
public class ObjectFunctor extends DataFunctor
{
	protected Object data;

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
	
	@Override
	public String getQuotedAtomName() {
		return "'"+data.getClass().getSimpleName()+"'";
	}

	/**
	 * ���Υե��󥯥���̾�����֤�
	 * @return �ݻ����Ƥ��륪�֥������Ȥ�̾��
	 */
	public String getName() {
		return data.toString();
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public boolean isString() {
		return false;
	}
}
