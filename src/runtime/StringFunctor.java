package runtime;

/** ���ʸ�����ɽ�����뤿���1�����ե��󥯥���ɽ�����饹
 * todo inline�ΰ��������������Τǲ��Ȥ�����
 * @author n-kato
 * @see ObjectFunctor#getName() */
public class StringFunctor extends ObjectFunctor {
	public StringFunctor(String data) { super(data); }
	public String getQuotedAtomName() { return getStringLiteralText(getName()); }
	public String getQuotedFuncName() { return getQuotedAtomName(); }
}