package runtime;

/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��
 */
public class Functor {
	/** ��ͳ��󥯴������ȥ� */
	public static final Functor INSIDE_PROXY = new Functor("inside_proxy", 2);
	/** ��ͳ��󥯴������ȥ� */
	public static final Functor OUTSIDE_PROXY = new Functor("outside_proxy", 2);
	
	private String name;
	private int arity;
	/** �Ƽ�᥽�åɤǻȤ�������ݻ����Ƥ���������������� */
	private String strFunctor;
	public Functor(String name, int arity) {
		this.name = name;
		this.arity = arity;
		// == ����ӤǤ���褦�ˤ��뤿���intern���Ƥ�����
		strFunctor = (name + "_" + arity).intern();
	}
	public String getName() {
		return name;
	}
	public int getArity() {
		return arity;
	}
	public String toString() {
		return strFunctor;
	}
	public int hashCode() {
		return strFunctor.hashCode();
	}
	public boolean equals(Object o) {
		// ���󥹥ȥ饯����intern���Ƥ���Τǡ�==����ӤǤ��롣
		return ((Functor)o).strFunctor == this.strFunctor;
	}
}
