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
	
	/**
	 * �ե��󥯥�ɽ����ν�°��̾�������������ɤ�����Ū�˻��ꤵ�줿�餽�졣
	 * ���ꤵ��ʤ��ä��顢�ǥե���ȤȤ��Ƥ��Υե��󥯥����ºݤ˽�°�����졣
	 */
	public String   path;
	
	/**
	 * ��°�줬����Ū�˻��ꤵ��ʤ��ä����˿���
	 */
	public boolean pathFree;
	
	public Functor(String name, int arity) {
		this(name, arity, null);
	}
	public Functor(String name, int arity, compile.structure.Membrane m) {
		// ̾������
		int pos = name.indexOf('.');
		if(pos!=-1) {
			this.path = name.substring(0, pos);
			this.name = name.substring(pos+1);
		} else {
			this.pathFree = true;
			if(m!=null) this.path = m.name;
			this.name = name;
		}
		//Env.p("new Fun "+path+"  "+name+" "+m);
		this.arity = arity;
		// == ����ӤǤ���褦�ˤ��뤿���intern���Ƥ�����
		strFunctor = (name + "_" + arity).intern();
	}
	/** Ŭ�ڤ˾�ά���줿̾������� */
	public String getAbbrName() {
		String full = path==null ? name : path+"."+name;
		return full.length() > 10 ? full.substring(0, 10) : full;
	}
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

class IntegerFunctor extends Functor {
	int value;
	public IntegerFunctor(int value) { super("",1);  this.value = value; }
	public String toString() { return "" + value; }
	public int hashCode() { return value; }
	public int intValue() { return value; }
	public boolean equals(Object o) {
		return (o instanceof IntegerFunctor) && ((IntegerFunctor)o).value == value;
	}
	// builtin�ƤӽФ��ѡʷײ����
	// ��ա��ºݤˤ������黻���Ȥ߹���̿��˥���ѥ��뤵��뤿�ᡢ�����ϻȤ��ʤ���
	// �ޤ��������ɤǤ�builtin�ϻȤ��ʤ��Ȼפ��뤿�ᡢlt�ʤɤ�̵��̣���⤷��ʤ�����
	// ����ͤ�void�Ǥ褤���⤷��ʤ���
	public static boolean builtin__2B(Membrane mem, Link[] links) { // "+"
		int x = ((IntegerFunctor)links[0].getAtom().getFunctor()).value;
		int y = ((IntegerFunctor)links[1].getAtom().getFunctor()).value;
		Atom atom = mem.newAtom(new IntegerFunctor(x+y));
		mem.inheritLink(atom,0,links[2]);
		return true;
	}
	public static boolean builtin__2F(Membrane mem, Link[] links) { // "/"
		int x = ((IntegerFunctor)links[0].getAtom().getFunctor()).value;
		int y = ((IntegerFunctor)links[1].getAtom().getFunctor()).value;
		if (y == 0) return false;
		Atom atom = mem.newAtom(new IntegerFunctor(x/y));
		mem.inheritLink(atom,0,links[2]);
		return true;
	}
	public static boolean builtin__3C(Membrane mem, Link[] links) { // "<"
		int x = ((IntegerFunctor)links[0].getAtom().getFunctor()).value;
		int y = ((IntegerFunctor)links[1].getAtom().getFunctor()).value;
		return x < y;
	}
	public static boolean builtin_abs(Membrane mem, Link[] links) { // "abs"
		int x = ((IntegerFunctor)links[0].getAtom().getFunctor()).value;
		int y = (x >= 0 ? x : -x);
		Atom atom = mem.newAtom(new IntegerFunctor(y));
		mem.inheritLink(atom,0,links[1]);
		return true;
	}
}

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