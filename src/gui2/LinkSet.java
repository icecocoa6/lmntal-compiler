package gui2;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import runtime.Atom;

/**
 * UNYO-UNYO�Υ�󥯤�������륯�饹
 * <P>
 * ���٤ƤΥ᥽�åɤ�static����������
 * @author nakano
 *
 */
public class LinkSet {
	
	///////////////////////////////////////////////////////////////////////////

	static
	private Map<Object, Node> linkMap_ = new HashMap<Object, Node>();
	
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * ��󥯤���äƤ��륢�ȥ���ɲä���
	 */
	static
	public void addLink(Object key, Node node){
		synchronized (linkMap_) {
			linkMap_.put(key, node);
		}
	}
	
	/**
	 * ���ȥ������
	 */
	static
	public void removeLink(Object key){
		synchronized (linkMap_) {
			linkMap_.remove(key);
		}
	}
	
	/**
	 * ��󥯤����褹��
	 * @param g
	 */
	static
	public void paint(Graphics g){
		synchronized (linkMap_) {
			Iterator keys = linkMap_.keySet().iterator();
			while(keys.hasNext()){
				Object key = keys.next();
				
				// key��Atom�ξ��
				if(Atom.class.isInstance(key)){
					Atom atom = (Atom)key;
					Node nodeSource = getVisibleNode(linkMap_.get(atom));
					Rectangle2D rectSource = nodeSource.getBounds2D();

					for(int n = 0; n < atom.getEdgeCount(); n++){
						Atom nthAtom = atom.getNthAtom(n);
						if(null == nthAtom){ continue; }
						if(atom.getid() < nthAtom.getid()){
							Node nodeTarget = getVisibleNode(linkMap_.get(nthAtom));
//							System.out.println((null != nodeSource.getInvisibleRootNode()));
							if((null == nodeTarget) ||
									(
											(null != nodeSource.getInvisibleRootNode()) &&
											(nodeSource.getInvisibleRootNode().equals(nodeTarget.getInvisibleRootNode()))
									)
							)
							{
								continue;
							}
							Rectangle2D rectTarget = nodeTarget.getBounds2D();
							g.drawLine((int)rectSource.getCenterX(),
									(int)rectSource.getCenterY(),
									(int)rectTarget.getCenterX(),
									(int)rectTarget.getCenterY());
						}
					}
				}
			}
		}
	}
	
	static
	public Point2D.Double getNodePoint(Object key){
		Node node = getVisibleNode(linkMap_.get(key));
		return (null != node) ? node.getCenterPoint() : null;
	}
	
	static
	public Node getNode(Object key){
		return getVisibleNode((Node)linkMap_.get(key));
	}
	
	/**
	 * �Ļ�Node���������
	 * @param node
	 * @return
	 */
	static
	private Node getVisibleNode(Node node){
		if(null == node ||
				null == node.getParent() ||
				null == node.getParent().getInvisibleRootNode())
		{
			return node;
		}
		else {
			return getVisibleNode(node.getParent());
		}
	}
	
}
