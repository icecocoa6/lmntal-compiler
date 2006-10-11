package type.quantity;

public abstract class Count {
	public static final Count INFINITY = new SpecialCount("inf");
	
	public abstract String toString();
	public Count self(){
		return this;
	}
	/**
	 * �̤βû�
	 * @param c
	 * @return
	 */
	public Count add(Count c){
		return new SumCount(this,c);
	}
	/**
	 * �̤�or���
	 * @param c
	 * @return
	 */
	public Count merge(Count c){
		return new OrCount(this,c);
	}
}
