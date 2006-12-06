package gui2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import runtime.Atom;
import runtime.Membrane;

public class Node implements Cloneable{

	///////////////////////////////////////////////////////////////////////////
	// final static
	
	final static
	private double ATOM_SIZE = 40.0;

	final static
	private Font FONT = new Font("SansSerif", Font.PLAIN, 20);
	
	final static
	private double MARGIN = 15.0;

	final static
	private double MAX_MOVE_DELTA = 300.0;
	
	/** �����Ͱʲ��ξ��ϰ�ư���ʤ� */
	final static
	private double MIN_MOVE_DELTA = 0.1;
	
	final static
	private double ROUND = 40;

	//////////////////////////////////////////////////////////////////////////
	// static

	static
	private int nextID_ = 0;
	
	static
	private GraphPanel panel_;
	
	///////////////////////////////////////////////////////////////////////////
	// private

	/** ��³��׻��ե饰 */
	private boolean clipped_ = false;
	
	/** ��ư���� */
	private double dx_;

	/** ��ư���� */
	private double dy_;
	
	/** ���ȥ�ޤ��ϥ��ȥ�Ū���Ĥ�����ˤǤ��뤫 */
	private boolean imAtom_;
	
	public ArrayList<Node> linkList_ = new ArrayList<Node>();
	
	/** ��Node�ο� */
	private Color myColor_ = Color.BLUE;
	
	/** ��Node��ID */
	private int myID_;
	
	/** ��Node�Υ��֥������ȡ�Atom �ޤ��� Membrane��*/
	private Object myObject_;

	/** Node̾ */
	private String name_ = "";
	
	/** ��Node */
	public Map<Object, Node> nodeMap_ = new HashMap<Object, Node>();
	
	/** ���� */
	private Node parent_;
	
	/** �������� */
	private boolean pickable_ = true;
	
	/** �ԥ�Υ��˥᡼��������ѿ� */
	public int pinAnime_ = 0;
	
	/** �ԥ�Υ��˥᡼������Ѻ�ɸ�ѿ� */
	private double pinPosY_;
	
	/** �����Ѥη� */
	private RoundRectangle2D.Double rect_ = new RoundRectangle2D.Double((Math.random() * 800) - 400,
			(Math.random() * 600) - 300,
			ATOM_SIZE,
			ATOM_SIZE,
			ROUND,
			ROUND);
	
	/** ��׻��ե饰 */
	private boolean uncalc_ = false;
	
	/** �Ļ�ե饰 */
	private boolean visible_;
	
	/** ���֥롼�Ȥ˶ᤤ�ԲĻ��Node */
	private Node invisibleRootNode_ = null;
	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	
	public Node(){}
	
	public Node(Node parent, Object object){
		parent_ = parent;
		myObject_ = object;
		myID_ = nextID_;
		nextID_++;

		visible_ = true;
		// �졢���ȥ�ν����
		if(object instanceof Atom){
			setAtom((Atom)object);
			initPosition();
		} else {
			setMembrane((Membrane)object);
		}
		
		// ������ʤ�вĻ뤽��ʳ����ԲĻ�
		if(null != parent_){
			setVisible(false, true);
			setInvisibleRootNode(null);
//			resetAllLink();
		}
	}
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * GraphPanle�򥻥åȤ���
	 * @return
	 */
	static 
	public void setPanel(GraphPanel panel) {
		panel_ = panel;
	}
	
	///////////////////////////////////////////////////////////////////////////

	/**
	 * ���ֺ�ɸ�ʤɤη׻�
	 */
	public void calc(){
		NodeFunction.calcRelaxAngle(this);
		if(clipped_ || uncalc_){ return; }
		NodeFunction.calcSpring(this);
		synchronized (nodeMap_) {
			NodeFunction.calcAttraction(this, nodeMap_);
			NodeFunction.calcRepulsive(this, nodeMap_);
			NodeFunction.calcDivergence(this, nodeMap_);
		}
//		moveCalc();
	}
	
	/**
	 * �̺�ɸ�ʤɤη׻���ʬ��ޤ᤿���٤Ƥλ�Node�ˤƹԤ�
	 */
	public void calcAll(){
		calc();
		if(uncalc_){ return; }
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.calcAll();
			}
		}
	}
	
	public void calcMembraneSize(){
		synchronized (nodeMap_) {
			double maxX = Integer.MIN_VALUE;
			double maxY = Integer.MIN_VALUE;
			double minX = Integer.MAX_VALUE;
			double minY = Integer.MAX_VALUE;
			
			// �����ꥻ�å�
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				
				Rectangle2D rectangle = node.getBounds2D();
				minX = (minX < rectangle.getMinX()) ? minX : rectangle.getMinX(); 
				maxX = (maxX > rectangle.getMaxX()) ? maxX : rectangle.getMaxX(); 
				minY = (minY < rectangle.getMinY()) ? minY : rectangle.getMinY(); 
				maxY = (maxY > rectangle.getMaxY()) ? maxY : rectangle.getMaxY(); 
			}

			// �������ѹ�
			if(visible_ && 0 < nodeMap_.size()){
				rect_.setFrameFromDiagonal(minX - MARGIN, minY - MARGIN, maxX + MARGIN, maxY + MARGIN);
			}
			else if(rect_.width != ATOM_SIZE || rect_.height != ATOM_SIZE){
				rect_.setFrameFromCenter(rect_.getCenterX(),
						rect_.getCenterY(),
						rect_.getCenterX() - ATOM_SIZE,
						rect_.getCenterY() - ATOM_SIZE);
			}
		}
		
	}
	
	public Node cloneNode(Map<Node, Node> cloneMap){
		synchronized (nodeMap_) {
			Node cloneNode = new Node();
			cloneMap.put(this, cloneNode);

			Iterator keys = nodeMap_.keySet().iterator();
			while(keys.hasNext()){
				Object key = keys.next();
				Node oldNode = nodeMap_.get(key);
				oldNode.cloneNode(cloneMap);

			}
			return cloneNode;
		}
	}
	
	public Node cloneNodeParm(Map<Node, Node> cloneMap, Node orginNode){
		synchronized (orginNode.nodeMap_) {
			parent_ = cloneMap.get(orginNode.parent_);
			invisibleRootNode_ = cloneMap.get(orginNode.invisibleRootNode_);
			myObject_ = orginNode.myObject_;
			clipped_ = orginNode.clipped_;
			dx_ = orginNode.dx_;
			dy_ = orginNode.dy_;
			imAtom_ = orginNode.imAtom_;
			myColor_ = orginNode.myColor_;
			myID_ = orginNode.myID_;
			name_ = orginNode.name_;
			pickable_ = orginNode.pickable_;
			pinAnime_ = orginNode.pinAnime_;
			pinPosY_ = orginNode.pinPosY_;
			rect_ = orginNode.rect_;
			uncalc_ = orginNode.uncalc_;
			visible_ = orginNode.visible_;

			Map<Object, Node> cloneNodeMap = new HashMap<Object, Node>();
			ArrayList<Node> cloneLinkList = new ArrayList<Node>();

			Iterator keys = orginNode.nodeMap_.keySet().iterator();
			while(keys.hasNext()){
				Object key = keys.next();
				Node oldNode = orginNode.nodeMap_.get(key);
				Node newNode = cloneMap.get(oldNode);
				if(newNode == null){ continue; }
				newNode.cloneNodeParm(cloneMap, oldNode);
				cloneNodeMap.put(key, newNode);

			}
			nodeMap_ = cloneNodeMap;

			Iterator<Node> linkNodes = orginNode.linkList_.iterator();
			while(linkNodes.hasNext()){
				Node oldNode  = linkNodes.next();
				Node newNode = cloneMap.get(oldNode);
//				cloneMap.put(oldNode, newNode);
				cloneLinkList.add(newNode);
			}

			linkList_ = cloneLinkList;
			return this;
		}
	}

	/**
	 * ���֡���ɸ���������
	 * @return
	 */
	public Rectangle2D getBounds2D(){
		return rect_.getBounds2D();
	}
	
	/**
	 * Node���濴�����������
	 * @return Node���濴��
	 */
	public Point2D.Double getCenterPoint(){
		return (new Point2D.Double(rect_.getCenterX(), rect_.getCenterY()));
	}
	
	/**
	 * ��Node��Iterator���������
	 * @return
	 */
	public Map<Object, Node> getChildMap(){
		return nodeMap_;
	}

	public int getEdgeCount(){
		return linkList_.size();
	}
	
	/**
	 * ��ɽ��Node���������
	 * <p>
	 * ��ɽ��Node������Node�Τ�äȤ⺬�˶ᤤ��ɽ����Node��
	 * @return ��ɽ����Node
	 */
	public Node getInvisibleRootNode(){
		return invisibleRootNode_;
	}
	
	/**
	 * �٤�Node���������
	 * @param i
	 * @return
	 */
	public Node getNthNode(int i){
		return linkList_.get(i);
	}
	
	/**
	 * �����֥������ȡ�Atom,Membrane�ˤ��֤�
	 * @return
	 */
	public Object getObject(){
		return myObject_;
	}
	
	/**
	 * Node��ͭ��ID���������
	 * @return
	 */
	public int getID(){
		return myID_;
	}
	
	/**
	 * ��Node���֤�
	 * @return
	 */
	public Node getParent(){
		return parent_;
	}
	
	/**
	 * ���ꤵ�줿��ɸ�ˤ���Node���������
	 */
	public Node getPointNode(int x, int y, boolean force){
		synchronized (nodeMap_) {
			if(rect_.contains(x, y)){
				if(isPickable()){
					return this;
				} else{
					Iterator<Node> nodes = nodeMap_.values().iterator();
					while(nodes.hasNext()){
						Node node = nodes.next();
						node = node.getPointNode(x, y, force);
						if(null != node){ return node; }
					}
				}
				return (force && null != parent_) ? this : null;
			}
		}
		return null;
	}
	
	public double getSize(){
		if(myObject_ instanceof Atom){
			return (ATOM_SIZE / 2);
		}
		else {
			
		}
		return 0;
	}
	
	/**
	 * ������֤��󥯤���Ƥ��륢�ȥ�˶��϶᤯����
	 *
	 */
	public void initPosition(){
		if(!(myObject_ instanceof Atom)){ return; }
		int nthNum = ((Atom)myObject_).getEdgeCount();
		if(0 == nthNum){ return; }
		if(1 == nthNum){
			Node nthNode = LinkSet.getNodeByAtom(((Atom)myObject_).getNthAtom(0));
			if(null == nthNode){ return; }
			Point2D nthPoint = nthNode.getCenterPoint();
			rect_.x = nthPoint.getX() + 10;
			rect_.y = nthPoint.getY() + 10;
		} else {
			double x = 0;
			double y = 0;
			double findNthNum = 0;
			for(int i = 0; i < nthNum; i++){
				Node nthNode = LinkSet.getNodeByAtom(((Atom)myObject_).getNthAtom(i));
				if(null == nthNode){ continue; }
				Point2D nthPoint = nthNode.getCenterPoint();
				x += nthPoint.getX();
				y += nthPoint.getY();
				findNthNum++;
			}
			if(1 == findNthNum){
				rect_.x = x;
				rect_.y = x;
			}
			else if(1 < findNthNum){
				rect_.x = x / findNthNum;
				rect_.y = x / findNthNum;
			}
			
		}
	}
	
	/**
	 * ���ȥ�ʤޤ����Ĥ�����ˤǤ�����True���֤���
	 * @return
	 */
	public boolean isAtom(){
		return (imAtom_ || !visible_);
	}
	
	/**
	 * �ޥ����ǽ����夲�뤳�Ȥ�����뤫
	 * @return
	 */
	public boolean isPickable(){
		return (pickable_ && isAtom() && null != parent_);
	}
	
	/**
	 * �Ļ���֤Ǥ��뤫
	 */
	public boolean isVisible(){
		if(Atom.class.isInstance(myObject_)){
			return parent_.isVisible();
		}
		return visible_;
	}
	
	/**
	 * �Ļ�ˤʤꥢ�ȥ�Ū�ˤʤ�
	 *
	 */
	private void iWillBeAMembrane(){
		if(!Membrane.class.isInstance(myObject_)){
			return;
		}
		visible_ = true;
		if(null == parent_){ return; }
		parent_.iWillBeAMembrane();
	}
	
	/**
	 * �ԲĻ�ˤʤꥢ�ȥ�Ū�ˤʤ�
	 *
	 */
	private void iWillBeAnAtom(){
		if(!Membrane.class.isInstance(myObject_)){
			return;
		}
		rect_.setFrameFromCenter(rect_.getCenterX(), rect_.getCenterY(), rect_.getCenterX() - 40, rect_.getCenterY() - 40);
		LinkSet.addLink(this);
		
	}
	
	/**
	 * ��ư��ʬ��ޤ᤿���٤Ƥλ�Node�ˤƹԤ�
	 */
	public void moveAll(){
		if(uncalc_){ return; }
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.moveAll();
			}
		}
		moveCalc();
		if(myObject_ instanceof Membrane){
			calcMembraneSize();
		}
	}
	
	/**
	 * moveDelta�ǲû����줿��ư��Υʬ��ºݤ˰�ư�����롥
	 * ��ư��ϰ�ưͽ���Υ���������롥
	 */
	public void moveCalc(){
		// �����ư��Υ������
		if(MAX_MOVE_DELTA < dx_){ dx_ = MAX_MOVE_DELTA; }
		else if(dx_ < -MAX_MOVE_DELTA){ dx_ = -MAX_MOVE_DELTA; }
		if(MAX_MOVE_DELTA < dy_){ dy_ = MAX_MOVE_DELTA; }
		else if(dy_ < -MAX_MOVE_DELTA){ dy_ = -MAX_MOVE_DELTA; }

		// ��ư��˸³���ۤ��ʤ�
		if(Integer.MAX_VALUE - dx_ < rect_.x){ return; }
		if(rect_.x < Integer.MIN_VALUE - dx_){ return; }
		if(Integer.MAX_VALUE - dy_ < rect_.y){ return; }
		if(rect_.y < Integer.MIN_VALUE - dy_){ return; }
		
		if(MIN_MOVE_DELTA < Math.abs(dx_)){
			rect_.x += dx_;
		}
		if(MIN_MOVE_DELTA < Math.abs(dy_)){
			rect_.y += dy_;
		}
		dx_ = 0;
		dy_ = 0;
	}
	
	/**
	 * ��ưͽ���Υ��û����롥
	 * moveCalc���ƤФ��ޤǤϰ�ư�Ϥ���ʤ���
	 * @param dx
	 * @param dy
	 */
	public void moveDelta(double dx, double dy){
		if(uncalc_ || clipped_){ return; }
		dx_ += dx;
		dy_ += dy;
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.moveDelta(dx, dy);
			}
		}
	}
	
	/**
	 * ���Ȥ����褹��
	 * @param g
	 */
	public void paint(Graphics g){
		if(null == parent_){
			LinkSet.paint(g);
		}
		synchronized (nodeMap_) {
			// ���ȥ�ޤ����Ĥ����������
			if(isAtom()){
				g.setColor(myColor_);
				((Graphics2D)g).fill(rect_);
				g.setColor(Color.BLACK);
				((Graphics2D)g).draw(rect_);
				g.setFont(FONT);
				g.drawString(name_, (int)rect_.x, (int)rect_.y);
			}
			// �������
			else {
				Iterator<Node> nodes = nodeMap_.values().iterator();
				while(nodes.hasNext()){
					Node node = nodes.next();
					node.paint(g);
				}
				if(null != parent_){
					g.setColor(myColor_);
					if(visible_){
						((Graphics2D)g).draw(rect_);
						g.setColor(Color.BLACK);
						g.setFont(FONT);
						g.drawString(name_, (int)rect_.x, (int)rect_.y);
					} else {
						((Graphics2D)g).fill(rect_);
						g.setColor(Color.BLACK);
						g.setFont(FONT);
						g.drawString(name_, (int)rect_.x, (int)rect_.y);
					}
				}

			}
			if(clipped_){
				paintPin(g, 0, 0);
			}
		}
	}
	
	/**
	 * �ԥ�����褪��ӥ��˥᡼������Ѥη׻���Ԥ�
	 * @param g
	 * @param deltaX
	 * @param deltaY
	 */
	public void paintPin(Graphics g, int deltaX, int deltaY){
		if((pinAnime_ != 0) && (pinPosY_ < rect_.getCenterY())){
			pinPosY_ += Math.abs(panel_.getHeight() / pinAnime_); 
		}
		if(pinPosY_ > rect_.getCenterY()){ pinAnime_ = 0; }
		if(pinAnime_ == 0){ pinPosY_ = rect_.getY() - (panel_.getPin().getHeight(panel_) / 2); }

		if((myObject_ instanceof Membrane) && visible_){
			return;
		}
		g.drawImage(panel_.getPin(),
				(int)(rect_.getCenterX() + deltaX),
				(int)(pinPosY_ + deltaY),
				panel_
				);
		
	}
	
	/**
	 * node����ӡ����λ�Node��LinkSet����������
	 * @param node
	 */
	public void removeAll(Node node){
		synchronized (node.getChildMap()) {
			Iterator<Node> nodes = node.getChildMap().values().iterator();
			while(nodes.hasNext()){
				Node targetNode = nodes.next();
				targetNode.removeAll(targetNode);
			}
		}
		LinkSet.removeLink(node);
	}
	
	/**
	 * Node��ꥻ�åȤ���
	 * @param object
	 */
	public boolean reset(Object object){
		boolean success = true;
		myObject_ = object;

		if(Atom.class.isInstance(object)){
			setAtom((Atom)object);
		} else {
			success = setMembrane((Membrane)object);
		}
		return success;
	}
	
	public void resetAllLink(){
//		resetLink();
//		synchronized (nodeMap_) {
//			Iterator<Node> nodes = nodeMap_.values().iterator();
//			while(nodes.hasNext()){
//				Node node = nodes.next();
//				node.resetAllLink();
//			}
//		}
	}
	
	/**
	 * ������Ƽ���
	 */
	public boolean resetLink(){
		if(!(myObject_ instanceof Atom)){ return true; }
		int nthNum = ((Atom)myObject_).getEdgeCount();
		linkList_.clear();
		for(int i = 0; i < nthNum; i++){
			Node node = LinkSet.getNodeByAtom(((Atom)myObject_).getNthAtom(i));
			if(null == node) return false;
			linkList_.add(node);
		}
		return true;
	}
	
	/**
	 * ���ȥ��Ѥν��������
	 * @param atom
	 */
	private void setAtom(Atom atom){
		imAtom_ = true;
		name_ = (null != atom.getName()) ? atom.getName() : "";
		
		// [0, 7] -> [128, 255] for eash R G B
		int ir = 0x7F - ((name_.hashCode() & 0xF00) >> 8) * 0x08 + 0x7F;
		int ig = 0x7F - ((name_.hashCode() & 0x0F0) >> 4) * 0x08 + 0x7F;
		int ib = 0x7F - ((name_.hashCode() & 0x00F) >> 0) * 0x08 + 0x7F;
		myColor_ = new Color(ir, ig, ib);
		if(0 < atom.getEdgeCount()){
			LinkSet.addLink(this);
		}
	}
	
	/**
	 * ��³��׻��ե饰��Ω�Ƥ�
	 * @param clipped
	 */
	public void setClipped(boolean clipped){
		clipped_ = clipped;
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.setClipped(clipped_);
			}
		}
		if(clipped_){
			pinAnime_ = 5;
			pinPosY_ =
				-(
						panel_.getHeight() +
						(panel_.getPin().getHeight(panel_) * Math.random() * 15)
				) / GraphPanel.getMagnification();
		}
	}
	
	/**
	 * ���֥롼�Ȥ˶ᤤ�ԲĻ��Node�򥻥åȤ���
	 * Node��NULL�ǡ���ʬ���ԲĻ�Ǥ�����ϡ�
	 * ��Node�ˤϰ��֥롼�Ȥ˶ᤤ�ԲĻ��Node�ϼ�ʬ�Ǥ��������
	 * @param invisbleNode
	 */
	public void setInvisibleRootNode(Node invisibleNode){
		invisibleRootNode_ = ((null == invisibleNode) && (!visible_)) ? this : invisibleNode;
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.setInvisibleRootNode(invisibleRootNode_);
			}
		}
	}
	
	/**
	 * ���Ѥν��������
	 * @param mem ���åȤ�����
	 * @param setChildren ��Node�򹹿����뤫�ʥ������ι��������ʤ��false��
	 */
	public boolean setMembrane(Membrane mem){
		boolean success = true;
		synchronized (nodeMap_) {
			LinkSet.addLink(this);
			imAtom_ = false;
			name_ = (null != mem.getName()) ? mem.getName() : "";

			Map<Membrane, Node> memNodeMap = new HashMap<Membrane, Node>();
			Map<Atom, Node> atomNodeMap = new HashMap<Atom, Node>();

			// �����ꥻ�å�
			Iterator mems = mem.memIterator();
			while(mems.hasNext()){
				Membrane childMem = (Membrane)mems.next();
				Node node;
				if(nodeMap_.containsKey(childMem)){
					node = nodeMap_.get(childMem);
					if(success){
						success = node.reset(childMem);
					} else {
						node.reset(childMem);
					}
				} else {
					node = new Node(this, childMem);
					nodeMap_.put(childMem, node);
					if(success){
						success = node.reset(childMem);
					} else {
						node.reset(childMem);
					}
				}
				
				memNodeMap.put(childMem, node);
			}

			// ���ȥ��ꥻ�å�
			Iterator atoms = mem.atomIterator();
			while(atoms.hasNext()){
				Atom childAtom = (Atom)atoms.next();
				// ProxyAtom��̵��
				if(childAtom.getFunctor().isInsideProxy() ||
						childAtom.getFunctor().isOutsideProxy())
				{
					continue;
				}

				Node node;
				if(nodeMap_.containsKey(childAtom)){
					node = nodeMap_.get(childAtom);
					if(success){
						success = node.reset(childAtom);
					} else {
						node.reset(childAtom);
					}
				} else {
					node = new Node(this, childAtom);
					nodeMap_.put(childAtom, node);
					node.reset(childAtom);
				}
				atomNodeMap.put(childAtom, node);
			}

			// ���פˤʤä���󥯤���
			Iterator<Object> removeNodes = nodeMap_.keySet().iterator();
			while(removeNodes.hasNext()){
				Object key = removeNodes.next();
				if(!memNodeMap.containsKey(key) && !atomNodeMap.containsKey(key)){
//					LinkSet.removeLink(key)
					removeAll(nodeMap_.get(key));
				}
			}

			// Node�Υ�󥯾���������
			Iterator<Node> newAtoms = atomNodeMap.values().iterator();
			while(newAtoms.hasNext()){
				Node newAtom = newAtoms.next();
				if(success){
					success = newAtom.resetLink();
				} else {
					newAtom.resetLink();
				}
			}
			
			// ���פˤʤä�Node���
			nodeMap_.clear();
			nodeMap_.putAll(memNodeMap);
			nodeMap_.putAll(atomNodeMap);
		}
		return success;
	}
	
	/**
	 * ����򥻥åȤ���
	 * @param memNode
	 */
	public void setParentNode(Node node) {
		parent_ = node;
	}
	
	/**
	 * �ޥ����ǽ����夲�ε��Ĥ�����
	 * @param pick
	 */
	public void setPickable(boolean pick){
		pickable_ = pick;
	}
	
	/**
	 * ��ɸ�ζ����ѹ�
	 * moveCalc���Ԥ�����¨�¤˰�ư���롥
	 * ����������ưͽ���Υ�Ȥϰ㤤�������κ�ɸ�˶���Ū�˰�ư����
	 * @param p
	 */
	public void setPos(double x, double y){
		synchronized (nodeMap_) {
			x = x - (rect_.getWidth() / 2);
			y = y - (rect_.getHeight() / 2);
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.setPosDelta(x, y);
			}
			rect_.x = x;
			rect_.y = y;
		}
	}
	
	/**
	 * ��ɸ�ζ����ѹ�
	 * moveCalc���Ԥ�����¨�¤˰�ư���롥
	 * ��������setPos�Ȥϰ㤤����������ʬ�˶���Ū�˰�ư����
	 * @param p
	 */
	public void setPosDelta(double dx, double dy){
		if(null == parent_){
			dx = dx + rect_.x;
			dy = dy + rect_.y;
		} else {
			Rectangle2D parentRect = parent_.getBounds2D();
			dx = dx + (rect_.x - parentRect.getX());
			dy = dy + (rect_.y - parentRect.getY());
		}
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.setPosDelta(dx, dy);
			}
			rect_.x = dx;
			rect_.y = dy;
		}
	}
	
	/**
	 * ��׻��Ρ��ɤΥե饰�򥻥åȤ���
	 * @param uncalc
	 */
	public void setUncalc(boolean uncalc) {
		uncalc_ = uncalc;
		synchronized (nodeMap_) {
			Iterator<Node> nodes = nodeMap_.values().iterator();
			while(nodes.hasNext()){
				Node node = nodes.next();
				node.setUncalc(uncalc);
			}
		}
	}
	
	/**
	 * �Ļ���֤򥻥åȤ���
	 * @param flag �Ļ����
	 * @param foruce ����Ū�˻����Ļ���֤�flag�˥��åȤ���
	 */
	public void setVisible(boolean flag, boolean foruce){
		visible_ = (parent_ != null) ? flag : true;

		
		if(foruce){
			synchronized (nodeMap_) {
				Iterator<Node> nodes = nodeMap_.values().iterator();
				while(nodes.hasNext()){
					Node node = nodes.next();
					node.setVisible(flag, true);
				}
			}
		}
		
		// �Ļ�ˤʤä�
		if(visible_ && Membrane.class.isInstance(myObject_)){
			iWillBeAMembrane();
			calcMembraneSize();
			if(this == invisibleRootNode_){
				setInvisibleRootNode(null);
			}
		}
		// �ԲĻ�ˤʤä�
		else if(!visible_ && Membrane.class.isInstance(myObject_)){
			iWillBeAnAtom();
			setInvisibleRootNode(this);
		}
	}
	
	/**
	 * ��³��׻��ե饰��ȿž����
	 *
	 */
	public void swapClipped(){
		setClipped(!clipped_);
	}
	
	/**
	 * �Ļ��ԲĻ��ȿž����
	 *
	 */
	public void swapVisible(){
		// ���ȥब���򤵤줿���Ͽ����ȿž
		if(Atom.class.isInstance(myObject_)){
			parent_.swapVisible();
			return;
		}
		setVisible(!visible_, false);
	}
}
