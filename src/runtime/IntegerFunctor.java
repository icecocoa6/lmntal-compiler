package runtime;

/** �������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author n-kato */

public class IntegerFunctor extends Functor {
	int value;
	public IntegerFunctor(int value) { super("",1);  this.value = value; }
	public String getName() { return "" + value; }
	public int hashCode() { return value; }
	public int intValue() { return value; }
	public Object getValue() { return new Integer(value); }
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
