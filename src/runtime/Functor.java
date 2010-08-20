package runtime;

import java.io.Serializable;

import util.Util;

import compile.parser.SrcName;

/**
 * String��̾���ȥ�󥯿����Ȥ���ʤ륢�ȥ��Functor��ɽ����ݥ��饹��
 * ���Υ��饹��̾���ȥ�󥯿��Υե�����ɤϻ��äƤ��ʤ��Τǡ�
 * ���֥��饹�Ϥ����ξ����������� getName, getArity ��������롥
 * ���֥������Ȥ������ϳƥ��֥��饹�� new ����¾�� build �᥽�åɤ�Ȥ����Ȥ�����롥
 */
public abstract class Functor implements Serializable {
	// **���**���ü��Functor���ɲä�����硢readObject�᥽�åɤ��ѹ��������
	
	/** �����¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� $in/2 */
	public static final Functor INSIDE_PROXY = new SpecialFunctor(SpecialFunctor.INSIDE_PROXY_NAME, 2);

	/** ��γ�¦�μ�ͳ��󥯴������ȥ��ɽ���ե��󥯥� $out/2 */
	public static final Functor OUTSIDE_PROXY = new SpecialFunctor(SpecialFunctor.OUTSIDE_PROXY_NAME, 2);

	/**
	 * $p�˥ޥå������ץ����μ�ͳ��󥯤Τ���˰��Ū�˻��Ѥ���륢�ȥ� ��ɽ���ե��󥯥� transient_inside_proxy
	 * ���̾�:star��
	 */
	public static final Functor STAR = new SpecialFunctor("$star", 2);
	
	/**
	 * cons ���ȥ��ɽ���ե��󥯥� ./3
	 */
	public static final Functor CONS = new SymbolFunctor(".", 3);

	/**
	 * nil ���ȥ��ɽ���ե��󥯥� []/1
	 */
	public static final Functor NIL = new SymbolFunctor("[]", 1);

	/**
	 * ñ�첽���̣����ե��󥯥���=/2
	 */
	public static final Functor UNIFY = new SymbolFunctor("=", 2);

	// //////////////////////////////////////////////////////////////

	/**
	 * �������ĥ��ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤��� �̾��̾���ʳ��ʿ��ͤ䵭��ˤξ�硢�������Ȥ����֤���
	 */
	public String getQuotedFunctorName() {
		return quoteFunctorName(getAbbrName());
	}

	/**
	 * ����ʸ������������ե��󥯥�̾���֤�
	 * @return ����ʸ������������ե��󥯥�̾
	 */
	public String getQuotedFullyFunctorName() {
		// \r��\n��parse�κݤ˼���ˤʤ뤿��
		return quoteFunctorName(getName()).replaceAll("\\\\r", "").replaceAll("\\\\n", "");
	}

	private String quoteFunctorName(String text) {
		if (Env.verbose > Env.VERBOSE_SIMPLELINK || ( Env.dump2 && (!Dumper2.isInfixNotation()||!Dumper2.isAbbrAtom() )) ) {
			if (!text.matches("^([a-z0-9][A-Za-z0-9_]*)$")) {
				text = quoteName(text);
			}
		} else {
			if (!text.matches("^([a-z0-9-\\+][A-Za-z0-9_]*)$")) {
				text = quoteName(text);
			}
		}
		if (getPath() != null)
			text = getPath() + "." + text;
		return text;
	}
	
	/**
	 * ������⤿�ʤ����ȥ��̾���Ȥ���ɽ��̾��������뤿���ʸ������֤���
	 * �̾��̾���ʳ��Τ�ΤΤ������ꥹ�ȹ������Ǥ���Ͱʳ��Τ�Τϥ������Ȥ����֤���
	 */
	public abstract String getQuotedAtomName();

	/**
	 * �������Ȥ��줿��ά���ʤ����ȥ�̾���֤�
	 * @return �������Ȥ��줿��ά���ʤ����ȥ�̾
	 */
	public String getQuotedFullyAtomName() {
		// \r��\n��parse�κݤ˼���ˤʤ뤿��
		return quoteAtomName(getName()).replaceAll("\\\\r", "").replaceAll("\\\\n", "");
	}

	protected String quoteAtomName(String text) {
		if (!text.matches("^([a-z0-9][A-Za-z0-9_]*|\\[\\])$")) {
			if (!text
					.matches("^(-?[0-9]+|[+-]?[0-9]*\\.?[0-9]+([Ee][+-]?[0-9]+)?)$")) {
				text = quoteName(text);
			}
		}
		if (getPath() != null)
			text = getPath() + "." + text;
		return text;
	}
	
	/**
	 * ���ꤵ�줿ʸ�����ɽ������ܥ��ƥ��Υƥ�����ɽ����������롣 �㤨�� a'b ���Ϥ��� 'a\'b' ���֤롣
	 */
	static final String quoteName(String text) {
		return Util.quoteString(text, '\'');
	}

	// //////////////////////////////////////////////////////////////

	/** Ŭ�ڤ˾�ά���줿ɽ��̾����� */
	protected String getAbbrName() {
		String full = getName();
		return full.length() > Env.printLength ? full.substring(0,
				Env.printLength - 2)
				+ ".." : full;
	}
	
	/**
	 * �ե��󥯥�����°����⥸�塼��̾���֤�
	 * ��SymbolFunctor �ʳ��� ��� null ���֤���
	 * @return �ե��󥯥�����°����⥸�塼��̾
	 */
	public String getPath() {
		return null;
	}

	public String toString() {
		if (Env.compileonly)
			return Util.quoteString(getName(), '\'') + "_" + getArity();
		return getQuotedFunctorName() + "_" + getArity();
	}
	
	// //////////////////////////////////////////////////////////////
	//
	// serialize/deserialize/build
	//

	// todo path��StringFunctor���θ�������

	public String serialize() {
		return getName() + "_" + getArity(); // TODO ����ϡ�ľ�󲽤�Ȥ�
	}

	public static Functor deserialize(String text) {
		int loc = text.lastIndexOf('_');
		String name = "err";
		int arity = 0;
		try {
			name = text.substring(0, loc);
			arity = Integer.parseInt(text.substring(loc + 1));
		} catch (Exception e) {
		}
		if (arity == 2) {
			if (name.equals(SpecialFunctor.INSIDE_PROXY_NAME))
				return Functor.INSIDE_PROXY;
			if (name.equals(SpecialFunctor.OUTSIDE_PROXY_NAME))
				return Functor.OUTSIDE_PROXY;
		}
		return build(name, arity, SrcName.PLAIN);
	}

	/**
	 * ���ꤵ�줿�ե��󥯥����������롣�ʲ���
	 * <p>
	 * compile.parser.LMNParser.addSrcAtomToMem�����ư���Ƥ�����
	 * 
	 * @param name
	 *            ̾���ȡ������ɽ��ʸ����
	 * @param arity
	 *            �ե��󥯥��Υ���ƥ�
	 * @param nametype
	 *            ̾���ȡ�����μ����compile.parser.SrcName��������������Τ����줫��
	 */
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
					int radix = 10;
					if (name.matches("\\+[0-9]+")) {
						name = name.substring(1);
					} else if (name.matches("\\+0x[0-9a-fA-F]+")) {//+16�� 2006.6.26 by inui
						name = name.substring(3);
						radix = 16;
					} else if (name.matches("0x[0-9a-fA-F]+")) {//16�� 2006.6.26 by inui
						name = name.substring(2);
						radix = 16;
					}
					return new IntegerFunctor(Integer.parseInt(name, radix));
				} catch (NumberFormatException e) {
				}
				try {
					return new FloatingFunctor(Double.parseDouble(name));
				} catch (NumberFormatException e2) {
				}
			} else if (nametype == SrcName.STRING || nametype == SrcName.QUOTED) {
				return new StringFunctor(name); // new
				// runtime.ObjectFunctor(name);
			}
		}
		return new SymbolFunctor(name, arity, path);
	}
	
	////////////////////////////////////////////////
	// ��ݥ᥽�å�

	public abstract int hashCode();

	public abstract boolean equals(Object o);

	/**
	 * ����ܥ�ե��󥯥����ɤ�����Ƚ�ꤹ��
	 * @return ����ܥ��ɽ���ե��󥯥��ʤ� true 
	 */
	public abstract boolean isSymbol();

	/**
	 * inside_proxy ���ɤ������֤���SpecialFunctor �ʳ��Ͼ�� false
	 * @return inside_proxy �ʤ� true
	 */
	public abstract boolean isInsideProxy();
	
	/**
	 * outside_proxy ���ɤ������֤���SpecialFunctor �ʳ��Ͼ�� false
	 * @return outside_proxy �ʤ� true
	 */
	public abstract boolean isOutsideProxy();
	
	/**
	 * ���Υե��󥯥��������ƥ��֤��ɤ�����Ƚ�ꤹ�롣
	 * @return �����ƥ��֤ʤ� true
	 */
	public abstract boolean isActive();
	
	/**
	 * ���Υե��󥯥������ͥ��ȥफ�ɤ�����Ƚ�ꤹ�롣
	 * @return ���ͥ��ȥ�ʤ� true
	 */
	public abstract boolean isNumber();
	
	/**
	 * ���Υե��󥯥��� int ���Υ��ȥफ�ɤ�����Ƚ�ꤹ�롣
	 * @return int ���Υ��ȥ�ʤ� true
	 */
	public abstract boolean isInteger();
	
	/**
	 * ���Υե��󥯥����ͤ��֤�
	 * @return �ե��󥯥�����
	 */
	public abstract Object getValue();
	
	/** ̾����ɽ��̾��������롣���֥��饹�϶�ʸ���󤬽��Ϥ���ʤ��褦�˥����С��饤�ɤ��뤳�ȡ� */
	public abstract String getName();

	/** ����ƥ���������롣 */
	public abstract int getArity();


}