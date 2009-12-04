package runtime;

import java.io.IOException;

/**
 * �ü�ʥե��󥯥� (inside_proxy, outside_proxy, star) ��ɽ�����饹
 */
public class SpecialFunctor extends Functor {
	static final String OUTSIDE_PROXY_NAME = "$out".intern();
	static final String INSIDE_PROXY_NAME = "$in".intern();
	
	private String name;
	private int arity;
	private int kind;
	
	SpecialFunctor(String name, int arity) {
		this(name, arity, 0);
	}
	public SpecialFunctor(String name, int arity, int kind) {
		this.name = name.intern();
		this.arity = arity;
		this.kind = kind;
	}
	public boolean equals(Object o) {
		if(o instanceof SpecialFunctor) {
			SpecialFunctor f = (SpecialFunctor)o;
			return name == f.name && kind == f.kind;
		}
		return false;
	}
	
	/**
	 * outside_proxy ���ɤ�����Ƚ�ꤹ��
	 * @return outside_proxy �ʤ� true
	 */
	public boolean isOutsideProxy(){
		return name == OUTSIDE_PROXY_NAME;
	}
	
	/**
	 * inside_proxy ���ɤ�����Ƚ�ꤹ��
	 * @return outside_proxy �ʤ� true
	 */
	public boolean isInsideProxy(){
		return name == INSIDE_PROXY_NAME;
	}
	
	/**
	 * �ե��󥯥�̾���֤�
	 * @return �ե��󥯥�̾���֤�
	 */
	public String getName() {
		return name + (kind==0 ? "" : ""+kind); 
	}
	/**
	 * �����Ĥ��Υե��󥯥�̾���֤�
	 * @return �����Ĥ��Υե��󥯥�̾
	 */
	public String toString() {
		return name + (kind==0 ? "" : ""+kind) + "_" + getArity();
	}
	/**
	 * ��Υ����פ��֤�
	 * @return ��Υ�����
	 */
	public int getKind() {
		return kind;
	}

	/**
	 * ľ���������˸ƤФ�롣
	 * author mizuno
	 */
	protected void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		name = name.intern();
	}

	/** �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedFunctorName() {
		return getAbbrName();
	}

	@Override
	public String getQuotedAtomName() {
		return getAbbrName();
	}
	
	/**
	 * ����ܥ�ե��󥯥����ɤ�����Ĵ�٤롥
	 * @return false
	 */
	public boolean isSymbol() {
		return false;
	}
	
	/**
	 * ���Υե��󥯥��������ƥ��֤��ɤ�����������롣
	 * @return false
	 */
	public boolean isActive() {
		return false;
	}
	
	/**
	 * ���Υե��󥯥������ͤ��ɤ�����������롣
	 * @return false
	 */
	public boolean isNumber() {
		return false;
	}
	/**
	 * ���Υե��󥯥��� int �����ɤ�����������롣
	 * @return false
	 */
	public boolean isInteger() {
		return false;
	}
	
	/**
	 * �ե��󥯥����ͤ��֤�
	 * @return �ե��󥯥���̾��
	 */
	public Object getValue() {
		return name;
	}
	
	/**
	 * �ϥå��女���ɤ�׻�����
	 * @return �ϥå��女����
	 */
	public int hashCode() {
		return getName().hashCode() + getArity();
	}
	
	public int getArity() {
		return arity;
	}
}