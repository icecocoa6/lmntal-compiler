package runtime;


/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��
 * TODO SymbolFunctor�Ȥ������֥��饹���ä��ۤ����������⤷��ʤ���
 */
public class Functor {
	/** �����¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� inside_proxy/2 */
	public static final Functor INSIDE_PROXY = new Functor("$in",2,null,"$in_2");
	/** ��γ�¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� outside_proxy/2 */
	public static final Functor OUTSIDE_PROXY = new Functor("$out",2,null,"$out_2");
	/** $p�˥ޥå������ץ����μ�ͳ��󥯤Τ���˰��Ū�˻��Ѥ���륢�ȥ�
	 * ��ɽ���ե��󥯥� transient_inside_proxy ���̾�:star��*/
	public static final Functor STAR = new Functor("$star",2,null,"$star_2");
	
	/** ����ܥ�̾�����Υ��饹�Υ��֥������Ȥξ��ϡ�̾����ɽ��̾����Ǽ����롣
	 * ��ʸ����ΤȤ��ϡ����֥��饹�Υ��֥������ȤǤ��뤳�Ȥ�ɽ����*/
	private String name;
	/** ����ƥ��ʰ����θĿ���*/
	private int arity;
	/** ����ܥ�ե��󥯥��Ȥ��Ƥ�ID���Ƽ�᥽�åɤǻȤ�������ݻ����Ƥ���������������� */
	private String strFunctor;
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
		if (strFunctor.startsWith("$")) return text;	// �׻�
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
		this.name  = name;
		this.arity = arity;
		this.path  = path;
		name = escapeName(name);
		if (path != null) name = escapeName(path) + "." + name;
		// == ����ӤǤ���褦�ˤ��뤿���intern���Ƥ�����
		strFunctor = (name + "_" + arity).intern();
	}
	private Functor(String name, int arity, String path, String strFunctor) {
		this.name  = name;
		this.arity = arity;
		this.path  = path;
		this.strFunctor = strFunctor;
	}

	////////////////////////////////////////////////////////////////

	/** Ŭ�ڤ˾�ά���줿ɽ��̾����� */
	public String getAbbrName() {
		String full = getName();
		return full.length() > 14 ? full.substring(0, 12) + ".." : full;
	}
	/** ����ܥ�̾��������롣
	 * @return name�ե�����ɤ��͡����֥��饹�Υ��֥������ȤΤȤ����ΤȤ��˸¤��ʸ�����֤롣*/
	public final String getSymbolName() {
		return name;
	}
	/** ����ܥ�ե��󥯥��Ȥ��Ƥ�ID��������롣
	 * @return strFunctor�ե�����ɤ��͡�*/
	public final String getSymbolFunctorID() {
		return strFunctor;
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
		if (getSymbolFunctorID().equals("n_1")) return false;
		return getSymbolFunctorID().matches("^[a-z].*$");
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
		return strFunctor.hashCode();
	}
	public boolean equals(Object o) {
		// ���󥹥ȥ饯����intern���Ƥ���Τǡ�==����ӤǤ��롣
		// ����o��Functor�Υ��֥��饹�ξ�硢false���֤���
		return ((Functor)o).strFunctor == this.strFunctor;
	}
	public String getPath() {
		return path;
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