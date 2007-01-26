package type.quantity;

/**
 * ɾ���Ѥߤ��ͤ�ɽ�����饹������ͤ⤷���ϼ¿��͡��ʤ���̵���ͤ�����
 * @author kudo
 *
 */
public abstract class ValueCount{
	public abstract ValueCount add(ValueCount v);
	public abstract ValueCount mul(int m);
	
	public abstract int compare(ValueCount vc);

	public boolean equals(ValueCount v){
		return compare(v)==0;
	}
}
