package runtime;

import java.math.BigDecimal;

/**
 * ��ư�����������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author inui
 * @since 2006.07.03
 */

public class BigFloatingFunctor extends ObjectFunctor {
	public BigFloatingFunctor(BigDecimal value) {
		super(value);
	}
	
	public BigDecimal floatValue() {
		return (BigDecimal)data;
	}
	
	//¿��Ĺ�ʤΤǾ�ά���ʤ�
	protected String getAbbrName() {
		return data.toString();
	}
}
