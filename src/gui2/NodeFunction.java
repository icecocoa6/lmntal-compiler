package gui2;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import runtime.Atom;
import runtime.Membrane;

/**
 * ���ȥ�Ρ��ɡ���Ρ��ɤ˴ؤ������
 * <p>
 * ���٤ƤΥ᥽�åɤ�static����������
 * @author Nakano
 *
 */
public class NodeFunction {

	///////////////////////////////////////////////////////////////////////////
	// final static
	
	/** �Ф���� */
	final static
	private double CONSTANT_SPRING = 0.02;

	/** ������� */
	final static
	private double CONSTANT_ATTRACTION = 0.00001;
	
	/** ������� */
	final static
	private double CONSTANT_REPULSIVE = 0.1;
	
	/** ȯ����� */
	final static
	private double CONSTANT_DIVERGENCE = 0.01;
	
	/** ȯ������ */
	final static
	private int DIVERGENCE_TIMER = 200;
	
	///////////////////////////////////////////////////////////////////////////
	// static
	
	static
	private boolean attractionFlag_ = true;
	
	static
	private boolean repulsiveFlag_ = true;
	
	static
	private boolean springFlag_ = true;
	
	static
	private boolean angleFlag_ = true;
	
	static
	private int divergenceTimer_ = 0;
	
	///////////////////////////////////////////////////////////////////////////

	
	/**
	 * ���Ϥη׻�
	 * <p>
	 * ����濴��Node���夤�ХͤǤĤʤ���Ƥ���ȹͤ��롥
	 * <BR>
	 * <font color="red">���Υ᥽�åɤ�ƤӽФ��Ȥ���synchronized (nodeMap_) ��Ԥ����ȡ�</font>
	 * @param node
	 * @param nodeMap
	 */
	static
	public void calcAttraction(Node node, Map nodeMap){
		if(divergenceTimer_ != 0 ||
				!attractionFlag_ ||
				!(node.getObject() instanceof Membrane) ||
				null != node.getInvisibleRootNode())
		{
			return;
		}
		
		Point2D myPoint = node.getCenterPoint();

		Iterator<Node> nodes = nodeMap.values().iterator();
		while(nodes.hasNext()){
			// ɽ������Ƥ���Node���������
			Node targetNode = nodes.next();
			Point2D nthPoint = targetNode.getCenterPoint();

			double distance =
				Point2D.distance(myPoint.getX(), myPoint.getY(), nthPoint.getX(), nthPoint.getY());

			double f = -CONSTANT_ATTRACTION * distance;

			double dx = myPoint.getX() - nthPoint.getX();
			double dy = myPoint.getY() - nthPoint.getY();

			double ddx = f * dx;
			double ddy = f * dy;
			targetNode.moveDelta(-ddx, -ddy);
		}
	}
	
	
	/**
	 * ȯ���η׻�
	 * <P>
	 * �������濴��Node������ΥХͤǤĤʤ���Ƥ���ȹͤ���
	 * <BR>
	 * <font color="red">���Υ᥽�åɤ�ƤӽФ��Ȥ���synchronized (nodeMap_) ��Ԥ����ȡ�</font>
	 * @param node
	 * @param nodeMap
	 */
	static
	public void calcDivergence(Node node, Map nodeMap){
		if(divergenceTimer_ == 0 ||
				!(node.getObject() instanceof Membrane) ||
				null != node.getInvisibleRootNode())
		{
			return;
		}
		divergenceTimer_--;
		
		Point2D myPoint = node.getCenterPoint();

		Iterator<Node> nodes = nodeMap.values().iterator();
		while(nodes.hasNext()){
			// ɽ������Ƥ���Node���������
			Node targetNode = nodes.next();
			Point2D nthPoint = targetNode.getCenterPoint();

//			double distance =
//				Point2D.distance(myPoint.getX(), myPoint.getY(), nthPoint.getX(), nthPoint.getY());

			double f = -CONSTANT_DIVERGENCE;

			double dx = myPoint.getX() - nthPoint.getX();
			double dy = myPoint.getY() - nthPoint.getY();

			double ddx = f * dx;
			double ddy = f * dy;
			targetNode.moveDelta(ddx, ddy);
		}
	}
	
	/**
	 * ��������Node�֤����Ϥη׻�
	 * <P>
	 * <font color="red">���Υ᥽�åɤ�ƤӽФ��Ȥ���synchronized (nodeMap_) ��Ԥ����ȡ�</font>
	 * @param node
	 * @param nodeMap
	 */
	static
	public void calcRepulsive(Node node, Map nodeMap){
		if(!repulsiveFlag_ || !Membrane.class.isInstance(node.getObject())){
			return;
		}

		Iterator<Node> nodes = nodeMap.values().iterator();
		while(nodes.hasNext()){
			Node sourceNode = nodes.next();
			Point2D sourcePoint = sourceNode.getCenterPoint();
			Rectangle2D sourceRect = sourceNode.getBounds2D();
			
			Iterator<Node> targetNodes = nodeMap.values().iterator();
			while(targetNodes.hasNext()){
				// ɽ������Ƥ���Node���������
				Node targetNode = targetNodes.next();
				// ���Ϥ�Ư�����ʤ�
//				if(sourceNode == targetNode ||
//						!sourceRect.intersects(targetNode.getBounds2D()))
				if(sourceNode.getID() == targetNode.getID())
				{
					continue; 
				}

				Point2D targetPoint = targetNode.getCenterPoint();
				// TODO: Node�����礭��
				double distance =
					Point2D.distance(sourcePoint.getX(), sourcePoint.getY(), targetPoint.getX(), targetPoint.getY()) / 80;
				
				if(distance > 1){
					continue; 
				}

				double f = CONSTANT_REPULSIVE * (
						(1.25 * distance * distance * distance) -
						(2.375 * distance * distance) +
						1.125);
//				double f = CONSTANT_REPULSIVE * distance;
				
				double dx = sourcePoint.getX() - targetPoint.getX();
				double dy = sourcePoint.getY() - targetPoint.getY();
				
				double ddx = f * dx;
				double ddy = f * dy;
				sourceNode.moveDelta(ddx, ddy);
				targetNode.moveDelta(-ddx, -ddy);
			}
		}
	}
	
	static
	public void calcRelaxAngle(Node node){
		if(!angleFlag_ || !Atom.class.isInstance(node.getObject())){
			return;
		}
		Atom targetAtom = (Atom)node.getObject();
		int edgeNum = targetAtom.getEdgeCount(); 
		
		if(edgeNum < 2){ return; }

		Node sourceNode = LinkSet.getNode(targetAtom);
		if(null == sourceNode){ return; }
		Point2D myPoint = sourceNode.getCenterPoint();
		Map<Double, Node> treeMap = new TreeMap<Double, Node>();
		
		// �Ĥʤ��äƤ��륢�ȥ������
		for(int i = 0; i < edgeNum; i++){
			Atom nthAtom = targetAtom.getNthAtom(i);
			Node nthNode = LinkSet.getNode(nthAtom);
			if(null == nthNode){ continue; }
			Point2D nthPoint = nthNode.getCenterPoint();

			if(null == nthNode ||
					null == nthPoint ||
					sourceNode == nthNode)
			{ 
				continue; 
			}
			
			if(null != nthAtom){
				double dx = nthPoint.getX() - myPoint.getX();
				double dy = nthPoint.getY() - myPoint.getY();
				
				if(dx == 0.0){ dx=0.000000001; }
				double angle = Math.atan(dy / dx);
				if(dx < 0.0) angle += Math.PI;
				treeMap.put(angle, nthNode);
			}
		}
		
		Object[] nthAngles = treeMap.keySet().toArray();
		for(int i = 0; i < nthAngles.length; i++ ){
			Double nthAngle = (Double)nthAngles[i];
			Node nthNode = treeMap.get(nthAngle);
			Point2D nthPoint = nthNode.getCenterPoint();

			
			if(nthNode == null){ continue; }
			
			if(null != nthNode){
				double anglePre = (i != 0) ? ((Double)nthAngles[i]).doubleValue() - ((Double)nthAngles[i - 1]).doubleValue() 
						: (Math.PI * 2) - ((Double)nthAngles[nthAngles.length - 1]).doubleValue() + ((Double)nthAngles[0]).doubleValue();
				double angleCur = (i != nthAngles.length - 1) ? ((Double)nthAngles[i + 1]).doubleValue() - ((Double)nthAngles[i]).doubleValue() 
						: (Math.PI * 2) - ((Double)nthAngles[nthAngles.length - 1]).doubleValue() + ((Double)nthAngles[0]).doubleValue();
				double angleR = angleCur - anglePre;
				double dx = nthPoint.getX() - myPoint.getX();
				double dy = nthPoint.getY() - myPoint.getY();
				double edgeLength = Math.sqrt(dx * dx + dy * dy);
				if(edgeLength == 0.0){ edgeLength = 0.00001; }
				//��ʬ�˿�ľ��Ĺ�����Υ٥��ȥ�
				double tx = -dy / edgeLength;
				double ty =  dx / edgeLength;
				
				dx = 1.5 * tx * angleR;
				dy = 1.5 * ty * angleR;
				
				dx = dx * 2;
				dy = dy * 2;
				
				sourceNode.moveDelta(-dx, -dy);
				nthNode.moveDelta(dx, dy);
				
			}
		}
	}
	
	/**
	 * �Фͥ�ǥ�η׻�
	 * @param node
	 * @param nodeMap
	 *
	 */
	static
	public void calcSpring(Node node){
		if(!springFlag_ || !Atom.class.isInstance(node.getObject())){
			return;
		}
		
		Atom atom = (Atom)node.getObject();
		// ɽ������Ƥ���Node���������
		Node sourceNode = LinkSet.getNode(atom);
		if(null == sourceNode){ return; }
		Point2D myPoint = sourceNode.getCenterPoint();
		for(int i = 0; i < atom.getEdgeCount() ; i++){
			Atom nthAtom = atom.getNthAtom(i);
			// ɽ������Ƥ���Node���������
			Node nthNode = LinkSet.getNode(nthAtom);
			if(null == nthNode){ continue; }
			Point2D nthPoint = nthNode.getCenterPoint();
			if(null == nthNode ||
					null == nthPoint ||
					sourceNode == nthNode)
			{ 
				continue; 
			}

			double distance =
				Point2D.distance(myPoint.getX(), myPoint.getY(), nthPoint.getX(), nthPoint.getY());

			double f = -CONSTANT_SPRING * ((distance / ( 80 * 2)) - 1.0);

			double dx = myPoint.getX() - nthPoint.getX();
			double dy = myPoint.getY() - nthPoint.getY();

			double ddx = f * dx;
			double ddy = f * dy;
			node.moveDelta(ddx, ddy);
			nthNode.moveDelta(-ddx, -ddy);

		}
	}
	
	static
	public int getDivergence(){
		return divergenceTimer_;
	}
	
	static
	public void setDivergence(){
		divergenceTimer_ += DIVERGENCE_TIMER;
	}
	
	static 
	public void setAttractionFlag(boolean attractionFlag) {
		attractionFlag_ = attractionFlag;
	}
	
	static 
	public void setRepulsiveFlag(boolean repulsiveFlag) {
		repulsiveFlag_ = repulsiveFlag;
	}
	
	static 
	public void setSpringFlag(boolean springFlag) {
		springFlag_ = springFlag;
	}
	
	static 
	public void setAngleFlag(boolean angleFlag) {
		angleFlag_ = angleFlag;
	}
}
