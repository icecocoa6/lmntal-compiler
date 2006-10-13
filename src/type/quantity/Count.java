package type.quantity;

public abstract class Count {
	
	public final static InfinityCount INFINITY = new InfinityCount(false);
	public final static InfinityCount M_INFINITY = new InfinityCount(true);	
	
	public abstract String toString();
	public Count self(){
		return this;
	}
	/**
	 * �̤βû�
	 * @param c
	 * @return
	 */
	public abstract Count add(Count c);
//		return new SumCount(this,c);
//	}
	/**
	 * �̤�or���
	 * @param c
	 * @return
	 */
//	public Count merge(Count c){
//		return new OrCount(this,c);
//	}

	/**
	 * �롼���ѿ��ˤĤ�������������Τ��֤�
	 * @return
	 */
	public abstract Count reflesh();
	
	/**
	 * ����Ҥä����֤�����Τ��֤�
	 * 
	 */
	public abstract Count inverse();
	
	/**
	 * ɾ���ͤ��֤�
	 * @return
	 */
	public abstract FixedCount evaluate();
}
