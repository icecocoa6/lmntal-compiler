package gui.model.forces;

import gui.model.Node;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

import runtime.Membrane;

public class AttractionForce {

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
	public void calcAttraction(Node node, Map<Object, Node> nodeMap){
		if(NodeFunction.heatingTime_ != 0 ||
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

	static 
	public void setAttractionFlag(boolean attractionFlag) {
		attractionFlag_ = attractionFlag;
	}

	/** ������� */
	final static double CONSTANT_ATTRACTION = 0.000001;
	static boolean attractionFlag_ = false;

}
