package runtime;

import util.Util;

/** ���ʸ�����ɽ�����뤿���1�����ե��󥯥���ɽ�����饹
 * todo inline�ΰ��������������Τǲ��Ȥ�����
 * @author n-kato
 * @see ObjectFunctor#getName() */
public class StringFunctor extends ObjectFunctor {
	public StringFunctor(String data) { super(data); }
	public String getQuotedAtomName() { return getStringLiteralText(getName()); }
	public String getQuotedFunctorName() { return getQuotedAtomName(); }
	public String stringValue() {return getName();}
	// 2006/06/28 by kudo
	public boolean equals(Object o){
		if(!(o instanceof StringFunctor))return false;
		return ((StringFunctor)o).data.equals(this.data);
	}
}
