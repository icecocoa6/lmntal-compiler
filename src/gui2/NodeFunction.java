package gui2;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;

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
	/** �Ф���� */
	final static
	private double CONSTANT_SPRING = 0.01;
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * �Фͥ�ǥ�η׻�
	 *
	 */
	static
	public void calcSpring(Node node){
		if(!Atom.class.isInstance(node.getObject())){
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
	
	/**
	 * ���Ϥη׻�
	 * <p>
	 * ���Υ᥽�åɤ�ƤӽФ��Ȥ���synchronized (nodeMap_) ��Ԥ����ȡ�
	 * @param node
	 */
	static
	public void calcAttraction(Node node, Map nodeMap){
		if(!(node.getObject() instanceof Membrane) ||
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

			double f = -CONSTANT_SPRING * ((distance / 5) - 1.0);

			double dx = myPoint.getX() - nthPoint.getX();
			double dy = myPoint.getY() - nthPoint.getY();

			double ddx = f * dx;
			double ddy = f * dy;
			System.out.println("move:"+ddx);
			targetNode.moveDelta(-ddx, -ddy);
		}
	}
}
