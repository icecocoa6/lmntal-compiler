package runtime;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import java.awt.*;
import test.GUI.*;
import util.QueuedEntity;
//import util.Stack;

/**
 * ���ȥ९�饹�������롦��⡼�Ȥ˴ؤ�餺���Υ��饹�Υ��󥹥��󥹤���Ѥ��롣
 * @author Mizuno
 */
public final class Atom extends QueuedEntity implements test.GUI.Node {
	/** ��°�졣AbstractMembrane�Ȥ��Υ��֥��饹���ѹ����Ƥ褤��
	 * �������ͤ��ѹ�����Ȥ���index��Ʊ���˹������뤳�ȡ�(null,-1)�Ͻ�°��ʤ���ɽ����*/
	AbstractMembrane mem;
	/** ��°���AtomSet��ǤΥ���ǥå��� */
	int index = -1;
	
	/** �ե��󥯥���̾���ȥ�󥯿��� */
	private Functor functor;
	/** ��� */
	Link[] args;
	
	private static int lastId = 0;
	/** ���Υ��ȥ�Υ�����ID */
	private int id;
	
	/** ��⡼�ȥۥ��ȤȤ��̿��ǻ��Ѥ���뤳�Υ��ȥ��ID����⡼����˽�°����Ȥ��Τ߻��Ѥ���롣
	 * <p>��°��Υ���å�������塢��°���Ϣ³�����å�������Τ�ͭ����
	 * ����å���������˽�������졢����³����⡼�ȥۥ��Ȥؤ��׵���ۤ��뤿��˻��Ѥ���롣
	 * ��⡼�ȥۥ��Ȥؤ��׵�ǿ��������ȥब���������ȡ��������NEW_����������롣
	 * $inside_proxy���ȥ�ξ�硢̿��֥�å�������������ƥ�⡼��¦�Υ�����ID�Ǿ�񤭤���롣
	 * $inside_proxy�ʳ��Υ��ȥ�ξ�硢��å�����ޤ�NEW_�Τޤ����֤���롣
	 * @see Membrane.atomTable */
	protected String remoteid;

	///////////////////////////////
	// ���󥹥ȥ饯��

	/**
	 * ���ꤵ�줿̾���ȥ�󥯿�����ĥ��ȥ��������롣
	 * AbstractMembrane��newAtom�᥽�å���ǸƤФ�롣
	 * @param mem ��°��
	 */
	Atom(AbstractMembrane mem, Functor functor) {
		this.mem = mem;
		this.functor = functor;
		args = new Link[functor.getArity()];
		id = lastId++;
		
		if (Env.gui != null) {
			Rectangle r = Env.gui.lmnPanel.getGraphLayout().getAtomsBound();
//			Rectangle r = Env.gui.lmnPanel.getBounds();
			pos = new DoublePoint(Math.random()*r.width + r.x, Math.random()*r.height + r.y);
		}
	}

	///////////////////////////////
	// ���
	public void setFunctor(Functor newFunctor) {
		if (newFunctor.getArity() > args.length) {
			// todo SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
		}
		functor = newFunctor;
	}
	/** �ե��󥯥�̾�����ꤹ�롣 */
	public void setName(String name) {
		setFunctor(name, getFunctor().getArity());
	}
	/** �ե��󥯥������ꤹ�롣
	 * ��°�줬��⡼�Ȥξ��⤢�ꡢ������AtomSet��ɬ���������ʤ���Фʤ�ʤ��Τǡ�
	 * ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void setFunctor(String name, int arity) {
		mem.alterAtomFunctor(this, new Functor(name, arity));
	}
	/**@deprecated*/
	public void set(String name, int arity) {
		setFunctor(name,arity);
	}
	/** ���ȥ���°�줫��������ʥ����Υ��ȥ�Ͻ���ʤ���*/
	public void remove() {
		mem.removeAtom(this);
	}
	///////////////////////////////
	// ����μ���

	public String toString() {
		return functor.getName();
	}
	/**
	 * �ǥե���Ȥμ������Ƚ����Ϥ��������֤��Ѥ����Ѥ�äƤ��ޤ��Τǡ�
	 * ���󥹥��󥹤��Ȥ˥�ˡ�����id���Ѱդ��ƥϥå��女���ɤȤ������Ѥ��롣
	 */
	public int hashCode() {
		return id;
	}
	/** ���Υ��ȥ�Υ�����ID��������� */
	String getLocalID() {
		return Integer.toString(id);
	}

	/** ��°��μ��� */
	public AbstractMembrane getMem() {
		return mem;
	}
	/** �ե��󥯥������ */
	public Functor getFunctor(){
		return functor;
	}
	/** ̾������� */
	public String getName() {
		return functor.getName();
	}
	/** Ŭ�ڤ˾�ά���줿̾������� */
	public String getAbbrName() {
		return functor.getAbbrName();
	}
	/** ��󥯿������ */
	int getArity() {
		return functor.getArity();
	}
	/** �ǽ���������� */
	Link getLastArg() {
		return args[getArity() - 1];
	}
	/** ��pos�����˳�Ǽ���줿��󥯥��֥������Ȥ���� */
	public Link getArg(int pos) {
		return args[pos];
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��̾����������� */
	public String nth(int n) {
		return nthAtom(n).getFunctor().getName();
	}
	/** �� n �����ˤĤʤ��äƤ륢�ȥ��������� */
	public Atom nthAtom(int n) {
		return args[n].getAtom();
	}
//	/** ��°������ꤹ�롣AbstractMembrane�Ȥ��Υ��֥��饹�Τ߸ƤӽФ��Ƥ褤��*/
//	void setMem(AbstractMembrane mem) {
//		this.mem = mem;
//	}
//	/**@deprecated*/	
//	void remove() {
//		mem.removeAtom(this);
//		mem = null;
//	}
//	/** �����å������äƤ���н���� */
//	public void dequeue() {
//		super.dequeue();
//	}
	
	///////////////////////////////////////////////////////////////
	
	DoublePoint pos;
	double vx, vy;
	public void initNode() {
		pos = new DoublePoint();
	}
	public DoublePoint getPosition() {
		return pos;
	}
	public void setPosition(DoublePoint p) {
		pos = p;
	}
	public Node getNthNode(int index) {
		return args[index].getAtom();
	}
	public int getEdgeCount() {
		return functor.getArity();
	}
	public void setMoveDelta(double dx, double dy) {
		vx += dx;
		vy += dy;
	}
	public void initMoveDelta() {
		vx = vy = 0;
	}
	public void move(Rectangle area) {
		//if (n.isFixed()) return;
		final int M = 100;
		pos.x += Math.max(-M, Math.min(M, vx));
		pos.y += Math.max(-M, Math.min(M, vy));
		
		if (pos.x < area.getMinX())		pos.x = (int)area.getMinX();
		else if (pos.x > area.getMaxX())	pos.x = (int)area.getMaxX();
		
		if (pos.y < area.getMinY())		pos.y = (int)area.getMinY();
		else if (pos.y > area.getMaxY())	pos.y = (int)area.getMaxY();
		
//		vx=vy=0;
		vx /= 2;
		vy /= 2;
	}
	public void paintEdge(Graphics g) {
		g.setColor(Color.BLACK);
		for(int i=0;i<getEdgeCount();i++) {
			Node n2 = getNthNode(i);
			if(this.hashCode() < n2.hashCode()) continue;
			g.drawLine((int)getPosition().x, (int)getPosition().y, (int)n2.getPosition().x, (int)n2.getPosition().y);
		}
	}
	
	public void paintNode(Graphics g) {
		String label = getName();
		FontMetrics fm = g.getFontMetrics();
		if(Env.fDEMO) {
			g.setFont(new Font(null, Font.PLAIN, 30));
		}
		int w = fm.stringWidth(label);
		int h = fm.getHeight();
		
		int wh = Env.fDEMO ? 40 : 16;
		Dimension size = new Dimension(wh, wh);
//		g.setColor(new Color(64,128,255));
		// Ŭ���˿�ʬ�����롪
		
		// [0, 7] -> [128, 255] for eash R G B
		int ir = 0x7F - ((label.hashCode() & 0xF00) >> 8) * 0x08 + 0x7F;
		int ig = 0x7F - ((label.hashCode() & 0x0F0) >> 4) * 0x08 + 0x7F;
		int ib = 0x7F - ((label.hashCode() & 0x00F) >> 0) * 0x08 + 0x7F;
		
//		System.out.println(label + "  " + ir + "  " + ig + "  " + ib);
		g.setColor(new Color(ir, ig, ib));
		
		g.fillOval((int)(pos.x - size.width/2), (int)(pos.y - size.height/ 2), size.width, size.height);
		
		g.setColor(Color.BLACK);
		g.drawOval((int)(pos.x - size.width/2), (int)(pos.y - size.height/ 2), size.width, size.height);
		g.drawString(label, (int)(pos.x - (w-10)/2), (int)(pos.y - (h-4)/2) + fm.getAscent()+size.height);
	}
}
