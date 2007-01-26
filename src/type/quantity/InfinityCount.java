package type.quantity;

/**
 * ̵���͡�
 * @author kudo
 *
 */
public class InfinityCount extends ValueCount {
	
	public final boolean minus;
	
	public InfinityCount(boolean minus){
		this.minus = minus;
	}
	public String toString(){
		return (minus?"-":"") + "#inf";
	}

	public ValueCount mul(int m){
		if(m >=0)
			return this;
		else if(minus)return Count.INFINITY;
		else return Count.M_INFINITY;
	}
	/**
	 * ̵�¤˲���­���Ƥ�̵��(-̵�¤λ��Ϥ��ꤦ�뤫?)
	 */
	public ValueCount add(ValueCount v){
		return this;
	}
	
	public int compare(ValueCount vc){
		if(vc instanceof ValueCount)
			return (minus?-1:1);
		else{
			InfinityCount ic = (InfinityCount)vc;
			if(minus == ic.minus)return 0;
			else if(minus && !ic.minus)return -1;
			else return 1;
		}
	}
}
