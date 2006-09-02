package runtime;

/** ��ư�����������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author n-kato */

public class FloatingFunctor extends Functor {
	double value;
	public FloatingFunctor(double value) { this.value = value; }
//	public String getName() { return "" + value; }
	public String toString() { return getName() + "_1"; }
	public int hashCode() { return (int)(Double.doubleToLongBits(value) >> 32); }
	public double floatValue() { return value; }
	public Object getValue() { return new Float(value); }
	public boolean equals(Object o) {
		return (o instanceof FloatingFunctor) && ((FloatingFunctor)o).value == value;
	}
	
	/**
	 * ����ܥ�ե��󥯥����ɤ�����Ĵ�٤롥
	 * @return false
	 */
	public boolean isSymbol() {
		return false;
	}
	
	/**
	 * outside_proxy ���ɤ�����Ƚ�ꤹ�롥
	 * @return false
	 */
	public boolean isOutsideProxy() {
		return false;
	}
	
	/**
	 * ���Υե��󥯥��������ƥ��֤��ɤ�����������롣
	 * @return false
	 */
	public boolean isActive() {
		return false;
	}
	
	public String getName() {
		return Double.toString(value);
	}
	
	public int getArity() {
		return 1;
	}
}
