package runtime;

/** �������ȥ��Ѥ�1�����ե��󥯥���ɽ�����饹
 * @author n-kato */

public class IntegerFunctor extends Functor {
	int value;
	public IntegerFunctor(int value) {
		this.value = value;
	}
	
	public int hashCode() { return value; }
	public int intValue() { return value; }
	public Object getValue() { return new Integer(value); }
	public boolean equals(Object o) {
		return (o instanceof IntegerFunctor) && ((IntegerFunctor)o).value == value;
	}
	
	/**
	 * ����ܥ�ե��󥯥����ɤ�����Ĵ�٤롥
	 * @return false
	 */
	public boolean isSymbol() {
		return false;
	}

	public String toString() {
		return getAbbrName() + "_" + getArity();
	}
	
	/**
	 * ���Υե��󥯥��������ƥ��֤��ɤ�����������롣
	 * @return false
	 */
	public boolean isActive() {
		return false;
	}
	
	public String getName() {
		return Integer.toString(value);
	}
	
	public int getArity() {
		return 1;
	}
}
