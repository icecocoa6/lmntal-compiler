package runtime;

/** ���ʸ�����ɽ�����뤿���1�����ե��󥯥���ɽ�����饹
 * @author n-kato
 * @see ObjectFunctor#getName() */
public class StringFunctor extends ObjectFunctor {
	public StringFunctor(String data) { super(data); }
	public String getQuotedAtomName() {
		if (getName().startsWith("/*inline*/")) return "/*inline*/";
		return getStringLiteralText(getName());
	}
	public String getQuotedFuncName() { return getQuotedAtomName(); }
}