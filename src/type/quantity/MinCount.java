package type.quantity;

public class MinCount extends Count {

	private final Count body;
	
	public MinCount(Count body){
		this.body = body;
	}
	
	public Count getBody(){
		return body;
	}
	
	public String toString(){
		return "(-" + body + ")";
	}
	public Count self(){
		if(body instanceof MinCount)
			return ((MinCount)body).body.self();
		else return this;
	}
	
	public Count add(Count c){
		if(c instanceof FixedCount)return ((FixedCount)c).add(this);
		else return new SumCount(c,this);
	}
	
	/**
	 * �롼���ѿ��ˤĤ������������֤�
	 */
	public Count reflesh(){
		return body.reflesh().inverse();
	}
	
	/** ����Ҥä����֤� */
	public Count inverse(){
		return body.self();
	}
	
	public FixedCount evaluate(){
		return body.evaluate().inverse();
	}
}
