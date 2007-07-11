package gui.model.forces;

import gui.model.Node;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.awt.geom.Rectangle2D;
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
	public void groupNode(Node node,Map<Object, Node> nodeMap,
		Map<Set<Node>, Rectangle2D.Double> nodeGroupMap)
	{
		Iterator<Node> nodes = nodeMap.values().iterator();
		
		nodesWhile:
		while(nodes.hasNext()){
			Node targetNode = nodes.next();
			Iterator<Set<Node>> nodeSets = nodeGroupMap.keySet().iterator();
			while(nodeSets.hasNext()){
				Set<Node> nodeSet = nodeSets.next();
				if(nodeSet.contains(targetNode)){
					continue nodesWhile;
				}
			}
			
			Set<Node> nodeSet = new HashSet<Node>();
			addNthNode(nodeSet, targetNode);
			Rectangle2D.Double rect = createRect(nodeSet);
			nodeGroupMap.put(nodeSet, rect);
		}

	}
	
	
	static
	public Rectangle2D.Double createRect(Set<Node> nodeSet){
		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;

		if(NodeFunction.heatingTime_ != 0)
		{
			return(null);
		}
		
		Iterator<Node> nodes = nodeSet.iterator();
		while(nodes.hasNext()){
			Node node = nodes.next();
			// TODO: node���礭�����θ����褦�ˡ�
			Point2D p = node.getPoint();
			double x = p.getX();
			double y = p.getY();
			if(maxX < x) maxX = x + node.getSize();
			if(x < minX) minX = x - node.getSize();
			if(maxY < y) maxY = y + node.getSize();
			if(y < minY) minY = y - node.getSize();
		}
		
		return (new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY));
		
	}
	
	static
	public void updateRect(Map<Set<Node>, Rectangle2D.Double> nodeGroupMap){
		Iterator<Set<Node>> nodeSets = nodeGroupMap.keySet().iterator();
		while(nodeSets.hasNext()){
			Set<Node> nodeSet = nodeSets.next();
			Rectangle2D.Double rect = nodeGroupMap.get(nodeSet);
			double maxX = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;

			Iterator<Node> nodes = nodeSet.iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				Point2D p = node.getPoint();
				double x = p.getX();
				double y = p.getY();
				if(maxX < x) maxX = x;
				if(x < minX) minX = x;
				if(maxY < y) maxY = y;
				if(y < minY) minY = y;
			}
			rect.setRect(minX, minY, maxX - minX, maxY - minY);
		}
		
		
	}

	private static void addNthNode(Set<Node> nodeSet, Node node){
		if(nodeSet.contains(node)){
			return;
		}
		nodeSet.add(node);
		for(int i = 0; i < node.getEdgeCount(); i++){
			addNthNode(nodeSet, node.getNthNode(i));
		}
	}
	
	static
	public void calcAttraction(Node node, Map<Object, Node> nodeMap,
			Map<Set<Node>, Rectangle2D.Double> nodeGroupMap){
		if(NodeFunction.heatingTime_ != 0 ||
				!attractionFlag_ ||
				!(node.getObject() instanceof Membrane) ||
				null != node.getInvisibleRootNode())
		{
			return;
		}

		Iterator<Set<Node>> nodeSets = nodeGroupMap.keySet().iterator();
		nodeSets:
		while(nodeSets.hasNext()){
			Set<Node> nodeSet = nodeSets.next();
			Rectangle2D.Double rect = nodeGroupMap.get(nodeSet);
			if(rect == null){
				continue;
			}
			double centerX = rect.getCenterX();
			double centerY = rect.getCenterY();
			Point2D myPoint = node.getCenterPoint();
			if(node.isRoot()){
				myPoint = new Point2D.Double(0,0);
			}
			
			double distance =
				Point2D.distance(myPoint.getX(), myPoint.getY(), centerX, centerY);
	
			double f = -CONSTANT_ATTRACTION * distance;
	
			double dx = myPoint.getX() - centerX;
			double dy = myPoint.getY() - centerY;
	
			double ddx = f * dx;
			double ddy = f * dy;
			Iterator<Node> nodes = nodeSet.iterator();
			while(nodes.hasNext()){
				Node targetNode = nodes.next();
				if(targetNode.isUncalc() || targetNode.isClipped()){
					continue nodeSets;
				}

			}
			
			nodes = nodeSet.iterator();
			while(nodes.hasNext()){
				Node targetNode = nodes.next();
				targetNode.moveDelta(-ddx, -ddy);
			}
			// TODO: nodeSet�ο����node�ˤ��濴�������
			// TODO: nodeSet���濴���������node�ˤ��濴�˶��դ��褦���Ϥ򻻽�
			// TODO: nodeSet�Τ��٤Ƥ�Node���Ϥ�Ŭ��
			
			
		}
		

	}

	static 
	public void setAttractionFlag(boolean attractionFlag) {
		attractionFlag_ = attractionFlag;
	}

	/** ������� */
	final static double CONSTANT_ATTRACTION = 0.0001;
	static boolean attractionFlag_ = false;

}
