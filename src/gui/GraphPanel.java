package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import runtime.Membrane;

final
public class GraphPanel extends JPanel {

	///////////////////////////////////////////////////////////////////////////
	// static

	final static
	private int AUTO_FOCUS_POS_DELTA = 5;
	
	final static
	private int AUTO_FOCUS_SIZE_DELTA = 10;
	
	final static
	private double POINT_DELTA_AREA = 10.0;
	
	static
	private double magnification_ = 0.5;
	
	static
	private Image pin_;
	
	///////////////////////////////////////////////////////////////////////////
	
	private AffineTransform af_ = new AffineTransform();
	private AutoFocusThread autoFocusTh_ = null;
	private CalcThread calcTh_ = null;
	private CommonListener commonListener_ = new CommonListener(this);
	private double deltaX;
	private double deltaY;
	private boolean history_ = false;
	private List<String> logList_ = new ArrayList<String>();
	private Node moveTargetNode_ = null;
	private Node selectedNode_ = null;
	private Node orgRootNode_;
	private RepaintThread repaintTh_ = null;
	private Membrane rootMembrane_;
	private Node rootNode_;
	private List<Node> rootNodeList_ = new ArrayList<Node>();
	private GraphPanel myPanel_ = this;
	
	///////////////////////////////////////////////////////////////////////////
	
	public GraphPanel() {
		super();
		Node.setPanel(this);
		pin_ = Toolkit.getDefaultToolkit().getImage(getClass().getResource("gabyou.gif"));
		// PIN�Υ����Ԥ�����������
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(pin_, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//��PIN�Υ����Ԥ��������ޤ�
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		addMouseListener(new MouseAdapter() {
			
			/** 
			 * �ޥ����������줿�Ȥ��ν���
			 */
			public void mousePressed(MouseEvent e) {
				if(null == rootNode_){ return; }
				//��Node��������Ƚ���Ԥ�����ˡ�����å�����Point���ϰϡ�Rectangle�ˤ��Ѵ�
				int pointX = (int)((e.getX() - (getWidth() / 2)) / getMagnification());
				int pointY = (int)((e.getY() - (getHeight() / 2)) / getMagnification());
				Rectangle2D rect = new Rectangle2D.Double(pointX - ((POINT_DELTA_AREA / 2) / getMagnification()),
						pointY - ((POINT_DELTA_AREA / 2) / getMagnification()),
						POINT_DELTA_AREA / getMagnification(),
						POINT_DELTA_AREA / getMagnification());
				
				// �Ļ��ԲĻ��ȿž
				if(e.isControlDown()){
					moveTargetNode_ = rootNode_.getPointNode(rect, true);
					if(null == moveTargetNode_){ return; }
					moveTargetNode_.swapVisible();
					moveTargetNode_.setUncalc(false);
					moveTargetNode_ = null;
					return;
				}
				
				if(e.getClickCount() == 2){
					moveTargetNode_ = rootNode_.getPointNode(rect, true);
					if(null != moveTargetNode_){
						moveTargetNode_.swapClipped();
					}
					moveTargetNode_ = null;
					return;
				}
				
				setCursor(new Cursor(Cursor.MOVE_CURSOR));
				moveTargetNode_ = rootNode_.getPointNode(rect, true);
				
				/*
				 * ������å�����
				 * Node�Υ�󥯤�٥���������
				 */
				if(e.getButton() == MouseEvent.BUTTON3){
					if(null == moveTargetNode_){ return; }
					
					NodeFunction.showNodeMenu(moveTargetNode_, myPanel_);
					
					moveTargetNode_ = null;
					return;
				}
				
				if(null != moveTargetNode_){
					moveTargetNode_.setUncalc(true);
					if(selectedNode_ != moveTargetNode_ && 
							!moveTargetNode_.isBezNode()){
						moveTargetNode_.setSelected(true);
						if(null != selectedNode_){
							selectedNode_.setSelected(false);
						}
						commonListener_.setSelectedNode(moveTargetNode_);
						selectedNode_ = moveTargetNode_;
					} else if(selectedNode_ == moveTargetNode_){
						moveTargetNode_.setSelected(false);
						selectedNode_ = null;
						commonListener_.setSelectedNode(null);
					}
				} else if(null == moveTargetNode_){
					moveTargetNode_ = rootNode_;
				}
				deltaX = e.getX() - (moveTargetNode_.getCenterPoint().x * getMagnification());
				deltaY = e.getY() - (moveTargetNode_.getCenterPoint().y * getMagnification());
			}
			
			/**
			 * �ޥ�����Υ���줿�Ȥ��ν���
			 * <p>�ǽ�˲����줿�Ȥ�����ư���Ƥ������ư������Υ�����</p>
			 */
			public void mouseReleased(MouseEvent e) {
				if(null != moveTargetNode_){
					moveTargetNode_.setUncalc(false);
					moveTargetNode_ = null;
				}
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			
			/**
			 * �ޥ������ɥ�å����줿�Ȥ��ν���
			 * <p>��ư������Υ�����</p>
			 */
			public void mouseDragged(MouseEvent e) {
				if(moveTargetNode_ == null){ return; }
				int pointX = (int)((e.getX() - deltaX) / getMagnification());
				int pointY = (int)((e.getY() - deltaY) / getMagnification());
				moveTargetNode_.setPos(pointX, pointY);
			}

		}
		);
		
		addMouseWheelListener(commonListener_);

		calcTh_ = new CalcThread();
		calcTh_.start();
		repaintTh_ = new RepaintThread();
		repaintTh_.start();
	}
	///////////////////////////////////////////////////////////////////////////
	
	public void autoFocus(){
		autoFocusTh_ = new AutoFocusThread();
		autoFocusTh_.start();
	}
	
	/**
	 * �ϳإ�ǥ�η׻�����ư�η׻���Ԥ�
	 *
	 */
	public void calc(){
		if(null == rootNode_){ return; }
		if(rootNode_ == orgRootNode_){
			rootNode_.setMembrane(rootMembrane_);
		}
		rootNode_.calcAll();
		rootNode_.moveAll();
	}

	/**
	 * ����̾�����Ψ���������
	 * @return
	 */
	static
	public double getMagnification(){
		return magnification_;
	}

	/**
	 * Pin�Υ��᡼�����������
	 * @return
	 */
	public Image getPin(){
		return pin_;
	}
	
	/**
	 * ���٤Ƥ������ɽ��������
	 *
	 */
	public void hideAll(){
		rootNode_.setVisible(false, true);
		rootNode_.setInvisibleRootNode(null);
	}

	public void loadState(int value){
		if(rootNodeList_.size() == 0 || value >= rootNodeList_.size() || value < 0){
			return;
		}
		if(value == rootNodeList_.size() - 1){
			rootNode_ = orgRootNode_;
			LinkSet.resetNodes(orgRootNode_);
			commonListener_.setLog(logList_.get(value));
		} else {
			commonListener_.setLog(logList_.get(value));
			Node newNode = rootNodeList_.get(value);
			rootNode_ = newNode;
			LinkSet.resetNodes(newNode);
		}
	}

	/**
	 * ����դ����褹��
	 */
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0,(int)getWidth(), (int)getHeight());
	
		g.setColor(Color.WHITE);
		af_.setTransform(getMagnification(), 0, 0, getMagnification(), getWidth() / 2, getHeight() / 2);
		((Graphics2D)g).setTransform(af_);
	
		g.setColor(Color.BLACK);
		if(null != rootNode_){
			rootNode_.paint(g);
		}
	
		// ������֤��᤹
		int divergenceTimer = NodeFunction.getDivergence();
		if(0 < divergenceTimer){
			((Graphics2D)g).setTransform(new AffineTransform());
			g.setColor(Color.RED);
			g.drawString("Divergence Timer:" + divergenceTimer, 10, 30);
		}
	}
	
	public void revokeState(){
		commonListener_.revokeTime();
		rootNodeList_.clear();
		logList_.clear();
	}
	
	public void resetLink(){
		if(rootNode_ != orgRootNode_){
			rootNode_ = orgRootNode_;
			LinkSet.resetNodes(orgRootNode_);
		}
		boolean success = false;
		while(!success){
			success = rootNode_.setMembrane(rootMembrane_);
		}
	}
	
	
	public void saveState(){
		if(!history_){ return; }
		// Ʊ�����֤Ǥ���е�Ͽ���ʤ�
		if(logList_.size() != 0 &&
				commonListener_.getLog().equals(logList_.get(logList_.size() - 1)))
		{
			return;
		}
		if(0 < rootNodeList_.size()){
			commonListener_.addTime();
		}
		resetLink();
		Map<Node, Node> cloneMap = new HashMap<Node, Node>();
		Node newNode = rootNode_.cloneNode(cloneMap);
		newNode.cloneNodeParm(cloneMap, rootNode_);
		rootNodeList_.add(newNode);
		if(0 == logList_.size()){
			logList_.add(commonListener_.getLog());
		} else {
			logList_.add(rootMembrane_.toString());
		}
	}
	
	public void setShowRules(boolean flag){
		rootNode_.setShowRules(flag);
	}
	
	public void setHistory(boolean flag){
		history_ = flag;
		if(history_){
			saveState();
		}
	}

	public void setMagnification(double magni){
		magnification_ = magni * 2;
		if(0.01 > magnification_){
			magnification_ = 0.01;
		}
	}

	/**
	 * �롼����򥻥åȤ���
	 * @param mem
	 */
	public void setRootMem(Membrane mem){
		rootMembrane_ = mem;
		rootNode_ = new Node(null, mem);
		orgRootNode_ = rootNode_;
	}
	
	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		rootNode_.setVisible(true, true);
		rootNode_.setInvisibleRootNode(null);
	}
	
	///////////////////////////////////////////////////////////////////////////
	/**
	 * �黻�ѥ���å�
	 * @author nakano
	 *
	 */
	class CalcThread extends Thread {
		private boolean runnable_ = true;
		
		public CalcThread() {}
		
		public void run() {
			while(true) {
				try {
					sleep(40);
					if(runnable_){
						calc();
					}
				} catch (Exception e) {
				}
			}
		}
		
		public void setRunnable(boolean runnable){
			runnable_ = runnable;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * �����ѥ���å�
	 * @author nakano
	 *
	 */
	class RepaintThread extends Thread {
		private boolean runnable_ = true;
		
		public RepaintThread() {}
		
		public void run() {
			while(true) {
				try {
					sleep(50);
					if(runnable_){
						repaint();
					}
				} catch (Exception e) {
				}
			}
		}
		
		public void setRunnable(boolean runnable){
			runnable_ = runnable;
		}
		
	}

	///////////////////////////////////////////////////////////////////////////
	/**
	 * AutoFocus�ѥ���å�
	 * @author nakano
	 *
	 */
	class AutoFocusThread extends Thread {
		boolean change_ = true;
		boolean reduction_ = false;
		
		public AutoFocusThread() {}
		
		public void run() {
			while(change_ && autoFocusTh_ == Thread.currentThread()) {
				try {
					sleep(20);
					autoFocus();
					repaint();
				} catch (Exception e) {
				}
			}
		}
		
		
		/**
		 * rect������Ŭ��ɽ���Ǥ���褦����Ψ����Ӱ��֤�Ĵ�᤹�롣
		 * @param rect
		 */
		public void autoFocus() {
			Rectangle rect = rootNode_.getArea();
			change_ = false;
			// �̾�����
			if(0.01 < magnification_ &&
					(getHeight() < (rect.height * magnification_) + AUTO_FOCUS_SIZE_DELTA ||
					getWidth() < (rect.width * magnification_) + AUTO_FOCUS_SIZE_DELTA))
			{
				change_ = true;
				reduction_ = true;
				magnification_ -= 0.01;
				double value = (magnification_ / 2) * SubFrame.SLIDER_MAX;
				commonListener_.setMagnificationSliderValue((int)value);
			}
			// �������
			else if(magnification_ < 2 && 
					!reduction_ &&
					(getHeight() > (rect.height * magnification_) + AUTO_FOCUS_SIZE_DELTA ||
							getWidth() > (rect.width * magnification_) + AUTO_FOCUS_SIZE_DELTA))
			{
				change_ = true;
				reduction_ = false;
				magnification_ += 0.01;
				double value = (magnification_ / 2) * SubFrame.SLIDER_MAX;
				commonListener_.setMagnificationSliderValue((int)value);
			}
			// ��ư����
			if(AUTO_FOCUS_POS_DELTA < Math.abs(rect.getCenterX())){
				change_ = true;
				rootNode_.setPosDelta(-(rect.getCenterX() / 10),
						0);
			}
			if(AUTO_FOCUS_POS_DELTA < Math.abs(rect.getCenterY())){
				change_ = true;
				rootNode_.setPosDelta(0,
						-(rect.getCenterY() / 10));
			}
		}
		
	}
}