package runtime;


/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��
 */
public class Functor {
	//TODO ��ͳ��󥯴������ȥ��̾�����̾異�ȥ��Ʊ���ˤʤ�ʤ��褦�ˤ���
	/** �����¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� inside_proxy/2 */
	public static final Functor INSIDE_PROXY = new Functor("$inside", 2);
	/** ��γ�¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� outside_proxy/2 */
	public static final Functor OUTSIDE_PROXY = new Functor("$outside", 2);
	/** $p�˥ޥå������ץ����μ�ͳ��󥯤Τ���˰��Ū�˻��Ѥ���륢�ȥ�
	 * ��ɽ���ե��󥯥� temporary_inside_proxy ���̾�:star��*/
	public static final Functor STAR = new Functor("$transient_inside_proxy", 2);
	
	private String name;	// "" ��ͽ��
	private int arity;
	/** �Ƽ�᥽�åɤǻȤ�������ݻ����Ƥ���������������� */
	private String strFunctor;
//	/** �ե��󥯥�ɽ����ν�°��̾������Ū�˻��ꤵ��Ƥ��ʤ�����null��*/
//	public String path = null;

//	/**
//	 * �ե��󥯥�ɽ����ν�°��̾�������������ɤ�����Ū�˻��ꤵ�줿�餽�졣
//	 * ���ꤵ��ʤ��ä��顢�ǥե���ȤȤ��Ƥ��Υե��󥯥����ºݤ˽�°�����졣
//	 * @deprecated
//	 */
//	public String path;
//
//	/**
//	 * ��°�줬����Ū�˻��ꤵ��ʤ��ä����˿���
//	 */
//	public boolean pathFree;
	
	public Functor(String name, int arity) {
//		this(name, arity, null);
//	}
//	public Functor(String name, int arity, compile.structure.Membrane m) {

//		int pos = name.indexOf('.');
//		if(pos!=-1) {
//			this.path = name.substring(0, pos);
//			if(path.indexOf('\n')!=-1 || path.indexOf('/')!=-1 || path.indexOf('*')!=-1) this.path=null;
//		}
		this.name = name;
		this.arity = arity;
		// == ����ӤǤ���褦�ˤ��뤿���intern���Ƥ�����
		strFunctor = (name + "_" + arity).intern();
	}
	/** Ŭ�ڤ˾�ά���줿ɽ��̾����� */
	public String getAbbrName() {
		String full = getName();
		return full.length() > 10 ? full.substring(0, 10) : full;
	}
	/** ̾��������̾��������롣
	 * @return name�ե�����ɤ��͡����֥��饹�ʤ�ж�ʸ�����֤롣*/
	public final String getInternalName() {
		return name;
	}
	/** ̾����ɽ��̾��������롣���֥��饹�϶�ʸ���󤬽��Ϥ���ʤ��褦�˥����С��饤�ɤ��뤳�ȡ�*/
	public String getName() {
		return name;
	}
	public int getArity() {
		return arity;
	}
	/**
	 * ����̾���������ƥ��֤ʤ�true
	 */
	boolean isActive() {
		//�Ȥꤢ�������������ƥ���
		return true;
	}
	public String toString() {
		return strFunctor.length() > 10 ? strFunctor.substring(0, 10) : strFunctor;
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

class ObjectFunctor extends Functor {
	Object data;
	public ObjectFunctor(Object data) { super("",1);  this.data = data; }
	public String toString() { return data.toString(); }
	public int hashCode() { return data.hashCode(); }
	public Object getObject() { return data; }
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