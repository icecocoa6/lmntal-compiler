package runtime;

import java.io.IOException;

import util.Util;

/**
 * �̾異�ȥ��ѤΥե��󥯥���ɽ�����饹
 * @author inui
 * @since 2006-08-30
 */
public class SymbolFunctor extends Functor {
	/**
	 * ����ܥ�̾�����Υ��饹�Υ��֥������Ȥξ��ϡ�̾����ɽ��̾����Ǽ����롣 ��� intern �����ͤ��Ǽ���롣
	 */
	private String name;

	/** ����ƥ��ʰ����θĿ��� */
	private int arity;
	
	/**
	 * �ե��󥯥�����°����⥸�塼��̾��
	 * ����Ū�˻��ꤵ��Ƥ��ʤ�����null�ʶ�ʸ����ǤϤ����ʤ��ˡ�
	 */
	private String path;
	
	/**
	 * �⥸�塼��̾�ʤ��Υե��󥯥����������롣
	 * 
	 * @param name ����ܥ�̾
	 */
	public SymbolFunctor(String name, int arity) {
		this(name, arity, null);
	}

	/**
	 * ���ꤵ�줿�⥸�塼��̾����ĥե��󥯥����������롣
	 * 
	 * @param name
	 *            ����ܥ�̾�ʥ⥸�塼��̾����ꤷ�ƤϤ����ʤ���
	 * @param arity
	 *            �����θĿ�
	 * @param path
	 *            �⥸�塼��̾�ʤޤ���null��
	 */
	public SymbolFunctor(String name, int arity, String path) {
		this.arity = arity;
		this.name = name.intern();
		if (path != null)
			this.path = path.intern();
	}
	
	public boolean equals(Object o) {
		// ���󥹥ȥ饯����intern���Ƥ���Τǡ�==����ӤǤ��롣
		if (!(o instanceof SymbolFunctor)) return false;
		SymbolFunctor f = (SymbolFunctor)o;
		return f.path == path && f.name == name && f.arity == arity;
	}
	
	/**
	 * ����ܥ�ե��󥯥����ɤ�����Ĵ�٤롥
	 * @return true
	 */
	public boolean isSymbol() {
		return true;
	}
	
	/**
	 * ���Υե��󥯥��������ƥ��֤��ɤ�����������롣
	 * @return �����ƥ��֤ʤ� true
	 */
	public boolean isActive() {
		if (name.equals(""))
			return false;
		if (equals(CONS))
			return false;
		if (equals(NIL))
			return false;
		if (name.equals("thread"))
			return false;
		return true;
	}
	
	/**
	 * �ե��󥯥����ͤ��֤�
	 * @return �ե��󥯥���̾��
	 */
	public Object getValue() {
		return name;
	}
	
	public String toString() {
		if (Env.compileonly)
			return (path == null ? "" : Util.quoteString(path, '\'') + ".") + Util.quoteString(name, '\'') + "_" + getArity();
		return getQuotedFunctorName() + "_" + getArity();
	}
	
	/**
	 * �ϥå��女���ɤ�׻�����
	 * @return �ϥå��女����
	 */
	public int hashCode() {
		return (path == null ? 0 : path.hashCode()*2) + name.hashCode() + arity;
	}
	
	/**
	 * �ե��󥯥�����°����⥸�塼��̾���֤�
	 * @return �ե��󥯥�����°����⥸�塼��̾���֤�
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * ľ���������˸ƤФ�롣
	 * @author mizuno
	 */
	protected void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		name = name.intern();
	}
	
	public String getName() {
		return name;
	}
	
	public int getArity() {
		return arity;
	}
}
