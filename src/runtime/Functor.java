package runtime;

/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��
 */
public class Functor {
	//TODO ��ͳ��󥯴������ȥ��̾�����̾異�ȥ��Ʊ���ˤʤ�ʤ��褦�ˤ���
	/** �����¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� inside_proxy/2 */
	public static final Functor INSIDE_PROXY = new Functor("$inside_proxy", 2);
	/** ��γ�¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� outside_proxy/2 */
	public static final Functor OUTSIDE_PROXY = new Functor("$outside_proxy", 2);
	/** $p�˥ޥå������ץ����μ�ͳ��󥯤Τ���˰��Ū�˻��Ѥ���륢�ȥ�
	 * ��ɽ���ե��󥯥� temporary_inside_proxy ���̾�:star��*/
	public static final Functor STAR = new Functor("$transient_inside_proxy", 2);
	
	private String name;	// "" ��ͽ��
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
		// ����o��Functor�Υ��֥��饹�ξ�硢false���֤���
		return ((Functor)o).strFunctor == this.strFunctor;
	}
}

//////////////////////////////

class IntegerFunctor extends Functor {
	int value;
	public IntegerFunctor(int value) { super("",1);  this.value = value; }
	public String toString() { return "" + value; }
	public int hashCode() { return value; }
	public boolean equals(Object o) {
		return (o instanceof IntegerFunctor) && ((IntegerFunctor)o).value == value;
	}
}
class ObjectFunctor extends Functor {
	Object data;
	public ObjectFunctor(Object data) { super("",1);  this.data = data; }
	public String toString() { return data.toString(); }
	public int hashCode() { return data.hashCode(); }
	public boolean equals(Object o) {
		return o.getClass() == getClass() && data.equals(((ObjectFunctor)o).data);
	}
}
/*
class VectorFunctor extends ObjectFunctor {
	public VectorFunctor() { super(new java.util.ArrayList()); }
	public String toString() {
		return "{ ... }";
	}
}
*/