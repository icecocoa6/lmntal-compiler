package runtime;

import java.io.*;

import compile.parser.SrcName;


/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��
 * todo SymbolFunctor�Ȥ������֥��饹���ä��ۤ����������⤷��ʤ���
 */
public class Functor implements Serializable {
	//**���**���ü��Functor���ɲä�����硢readObject�᥽�åɤ��ѹ��������
	/** �����¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� inside_proxy/2 */
	public static final Functor INSIDE_PROXY = new SpecialFunctor("$in",2);
	/** ��γ�¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� outside_proxy/2 */
	public static final Functor OUTSIDE_PROXY = new SpecialFunctor("$out",2);
	/** $p�˥ޥå������ץ����μ�ͳ��󥯤Τ���˰��Ū�˻��Ѥ���륢�ȥ�
	 * ��ɽ���ե��󥯥� transient_inside_proxy ���̾�:star��*/
	public static final Functor STAR = new SpecialFunctor("$star",2);
	
	/** ����ܥ�̾�����Υ��饹�Υ��֥������Ȥξ��ϡ�̾����ɽ��̾����Ǽ����롣
	 * ��� intern �����ͤ��Ǽ���롣
	 * ��ʸ����ΤȤ��ϡ����֥��饹�Υ��֥������ȤǤ��뤳�Ȥ�ɽ����*/
	private String name;
	/** ����ƥ��ʰ����θĿ���*/
	protected int arity;
	/** �ե��󥯥�����°����⥸�塼��̾������Ū�˻��ꤵ��Ƥ��ʤ�����null��*/
	private String path = null;
	
	////////////////////////////////////////////////////////////////
	
	/** ����ܥ��ɽ��̾���Ф�������̾��������롣
	 * ���ߤλ��ͤǤϡ�����̾�Ȥ� . �� $ �����������פ��줿̾����ɽ����*/
	public static String escapeName(String name) {
		name = name.replaceAll("\\.","..");
		name = name.replaceAll("\\$",".\\$");
		return name;
	}
	/** �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedFunctorName() {
		String text = getAbbrName();
		if (!text.matches("^([a-z0-9][A-Za-z0-9_]*)$")) {
			text = quoteName(text);
		}
		if (path != null) text = path + "." + text;
		return text;
	}
	/** ������⤿�ʤ����ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedAtomName() {
		String text = getAbbrName();
		if (!text.matches("^([a-z0-9][A-Za-z0-9_]*|\\[\\])$")) {
			if (!text.matches("^(-?[0-9]+|[+-]?[0-9]*\\.?[0-9]+([Ee][+-]?[0-9]+)?)$")) {
				text = quoteName(text);
			}
		}
		if (path != null) text = path + "." + text;
		return text;
	}
	/** ���ꤵ�줿ʸ�����ɽ������ܥ��ƥ��Υƥ�����ɽ����������롣
	 * �㤨�� a'b ���Ϥ��� 'a''b' ���֤롣*/
	static final String quoteName(String text) {
		if (text.equals("")) return "\"\"";
		if (text.indexOf('\n') == -1) {
			text = text.replaceAll("'","''");
			text = "'" + text + "'";
			return text;
		}
		return getStringLiteralText(text);
	}
	/** ���ꤵ�줿ʸ�����ɽ��ʸ�����ƥ��Υƥ�����ɽ����������롣
	 * �㤨�� a"b ���Ϥ��� "a\"b" ���֤롣
	 * <p>StringFunctor���饹�Υ��饹�᥽�åɤˤ���Τ���������*/
	static final String getStringLiteralText(String text) {
		text = text.replaceAll("\\\\","\\\\\\\\");
		text = text.replaceAll("\"","\\\\\"");
		text = text.replaceAll("\n","\\\\n");
		text = text.replaceAll("\t","\\\\t");
		text = text.replaceAll("\f","\\\\f");
		text = text.replaceAll("\r","\\\\r");
		text = "\"" + text + "\"";
		return text;
	}
	public Object getValue() {
		return name;
	}
	////////////////////////////////////////////////////////////////
	
	/** �⥸�塼��̾�ʤ��Υե��󥯥����������롣
	 * @param name ����ܥ�̾ */
	public Functor(String name, int arity) {
		this(name,arity,null);
	}	
	/** ���ꤵ�줿�⥸�塼��̾����ĥե��󥯥����������롣
	 * @param name ����ܥ�̾�ʥ⥸�塼��̾����ꤷ�ƤϤ����ʤ���
	 * @param arity �����θĿ�
	 * @param path �⥸�塼��̾�ʤޤ���null��
	 */
	public Functor(String name, int arity, String path) {
		this.name = name.intern();
		this.arity = arity;
		if (path != null)
			this.path = path.intern();
	}

	/**
	 * ľ���������˸ƤФ�롣
	 * author mizuno
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		name = name.intern();
		if (path != null)
			path = path.intern();
	}
	////////////////////////////////////////////////////////////////

	/** Ŭ�ڤ˾�ά���줿ɽ��̾����� */
	public String getAbbrName() {
		String full = getName();
		return full.length() > Env.printLength ? full.substring(0, Env.printLength-2) + ".." : full;
	}
	/** ����ܥ�̾��������롣
	 * @return name�ե�����ɤ��͡����֥��饹�Υ��֥������ȤΤȤ����ΤȤ��˸¤��ʸ�����֤롣*/
	public final String getSymbolName() {
		return name;
	}
	/** ̾����ɽ��̾��������롣���֥��饹�϶�ʸ���󤬽��Ϥ���ʤ��褦�˥����С��饤�ɤ��뤳�ȡ�*/
	public String getName() {
		return name;
	}
	/** ����ƥ���������롣*/
	public int getArity() {
		return arity;
	}
	/** ���Υե��󥯥��������ƥ��֤��ɤ�����������롣*/
	public boolean isActive() {
		// �ʲ���
		if (arity == 0) return true;
		if (name.equals("")) return false;
		char c = name.charAt(0);
		return c >= 'a' && c <= 'z';
	}
	/** ���Υ��饹�Υ��֥������Ȥ��ɤ�����Ĵ�٤롣*/
	public boolean isSymbol() {
		return !name.equals("");
	}
	/** �����С��饤�ɤ��ʤ��¤� getAbbrName()+"_"+getArity() ���֤� */
	public String toString() {
		return getAbbrName() + "_" + getArity();
	}
	public int hashCode() {
		return (path == null ? 0 : path.hashCode()) + name.hashCode() + arity;
	}
	public boolean equals(Object o) {
		// ���󥹥ȥ饯����intern���Ƥ���Τǡ�==����ӤǤ��롣
		// ����o��Functor�Υ��֥��饹�ξ�硢false���֤���
		Functor f = (Functor)o;
		return f.path == path && f.name == name && f.arity == arity;
	}
	public String getPath() {
		return path;
	}
	
	////////////////////////////////////////////////////////////////
	//
	// serialize/deserialize/build
	//
	
	// todo path��StringFunctor���θ�������
	
	public String serialize() {
		return getName() + "_" + getArity(); // todo ����ϡ�ľ�󲽤�Ȥ�
	}
	public static Functor deserialize(String text) {
		int loc = text.lastIndexOf('_');
		String name = "err";
		int arity = 0;
		try {
			name = text.substring(0,loc);
			arity = Integer.parseInt(text.substring(loc + 1));
		} catch (Exception e) {}
		if (arity == 2) {
			if (name.equals("$in"))  return Functor.INSIDE_PROXY;
			if (name.equals("$out")) return Functor.OUTSIDE_PROXY;
		}
		return build(name,arity,SrcName.PLAIN);
	}
	
	/** ���ꤵ�줿�ե��󥯥����������롣�ʲ���
	 * <p>compile.parser.LMNParser.addSrcAtomToMem�����ư���Ƥ�����
	 * @param name ̾���ȡ������ɽ��ʸ����
	 * @param arity �ե��󥯥��Υ���ƥ�
	 * @param nametype ̾���ȡ�����μ����compile.parser.SrcName��������������Τ����줫��*/
	public static Functor build(String name, int arity, int nametype) {
		String path = null;
		if (nametype == SrcName.PATHED) {
			int pos = name.indexOf('.');
			path = name.substring(0, pos);
			name = name.substring(pos + 1);
		}
		if (arity == 1 && path == null) {
			if (nametype == SrcName.PLAIN || nametype == SrcName.SYMBOL) {
				try {
					return new IntegerFunctor(Integer.parseInt(name));
				}
				catch (NumberFormatException e) {}
				try {
					return new FloatingFunctor(Double.parseDouble(name));
				}
				catch (NumberFormatException e2) {}
			}
			else if (nametype == SrcName.STRING || nametype == SrcName.QUOTED) {
				return new StringFunctor(name); // new runtime.ObjectFunctor(name);
			}
		}
		return new Functor(name, arity, path);
	}
}

class SpecialFunctor extends Functor {
	private String name;
	SpecialFunctor(String name, int arity) {
		super("", arity);
		this.name = name;
	}
	public int hashCode() {
		return name.hashCode() + arity;
	}
	public boolean equals(Object o) {
		return this == o;
	}
	public String getName() {
		return name; 
	}

	/**
	 * ľ���������˸ƤФ�롣
	 * author mizuno
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		name = name.intern();
	}

	/** �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤� */
	public String getQuotedFunctorName() {
		return getAbbrName();
	}
}

//////////////////////////////

/*
class VectorFunctor extends ObjectFunctor {
	public VectorFunctor() { super(new java.util.ArrayList()); }
	public String toString() {
		return "{ ... }";
	}
}
*/