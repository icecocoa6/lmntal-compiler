package runtime;

import util.Util;

/** ���ʸ�����ɽ�����뤿���1�����ե��󥯥���ɽ�����饹
 * todo inline�ΰ��������������Τǲ��Ȥ�����
 * @author n-kato
 * @see ObjectFunctor#getName() */
public class StringFunctor extends Functor {
	public StringFunctor(String data) { super(data, 1);}
	public String getQuotedAtomName() { return getStringLiteralText(getName()); }
	public String getQuotedFunctorName() { return getQuotedAtomName(); }
	public String stringValue() {return getName();}
	public String toString() {
		return Util.quoteString(getName(), '"') + "_" + getArity();
	}
	// 2006/06/28 by kudo
	public boolean equals(Object o){
		if(!(o instanceof StringFunctor))return false;
		//���󥹥ȥ饯����intern���Ƥ���Τǡ�==����ӤǤ��롣
		return (getName() == ((StringFunctor)o).getName());//2006.6.30 by inui
	}
}
