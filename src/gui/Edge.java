package gui;

/**
 * ͭ������դ��ա�
 * 
 * @author 
 */
public class Edge implements Comparable {
	public Node from, to;
	
	/** ���Ѥ���ؿ������� */
	public Edge(Node f, Node t) {
		from = f;
		to = t;
	}
	
	/**
	 * ���гѤ�׻������֤���(rad)
	 * �줬 0 �����ײ���������
	 * 
	 * @return a
	 */
	public double getAngle() {
		double x = getVx();
		double y = getVy();
		
		if(x==0.0) x=0.000000001;
		double a = Math.atan(y/x);
		if(x<0.0) a += Math.PI;
		
		a = GraphLayout.regulate(a);
		return a;
	}
	
	/** �����Ȥ���ΤˤĤ��� */
	public int compareTo(Object o) {
		Edge ie = (Edge)o;
		return ie.getAngle() < this.getAngle() ? 1 : -1;
	}
	
	/** to��from��x��ɸ��������� */
	public double getVx() {
		return to.getPosition().x - from.getPosition().x;
	}

	/** to��from��y��ɸ��������� */
	public double getVy() {
		return to.getPosition().y - from.getPosition().y;
	}
	
	/** ���ȥॵ�����ο������ѹ� */
	public double getStdLen() {
		return runtime.Env.fDEMO ? 100.0 : 50.0;
	}
	
	/** �����Ƚ����ε�Υ��������� */
	public double getLen() {
		double x = getVx();
		double y = getVy();
		x = Math.sqrt(x*x+y*y);
		return x==0.0 ? 0.00001 : x;
	}
	
	/** ���Ϥ���ʸ������� */
	public String toString() {
		return "from "+from+" to "+to+" angle= "+getAngle()*180/Math.PI;
	}
}
