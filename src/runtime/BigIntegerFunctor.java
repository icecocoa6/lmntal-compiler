package runtime;

import java.math.BigInteger;

/**
 * �������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author inui
 * @since 2006.07.03
 */

public class BigIntegerFunctor extends Functor {
	BigInteger value;
	public BigIntegerFunctor(BigInteger value) { super(value.toString(),1);  this.value = value; }
//	public BigIntegerFunctor(String value) { super(value,1);  this.value = new BigInteger(value); }
	public int hashCode() { return value.hashCode(); }
	public BigInteger intValue() { return value; } //Object ���֤������� cast ������
	public Object getValue() { return value; }
	public boolean equals(Object o) {
		return (o instanceof BigIntegerFunctor) && ((BigIntegerFunctor)o).value.equals(value);
	}
	protected String getAbbrName() {
		return value.toString();
	}
}
