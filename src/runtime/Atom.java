package runtime;

//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import java.awt.*;
import test.GUI.Node;
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
	
	static void gc() {
		lastId = 0;
	}
	
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
			pos = new Point((int)(Math.random()*Env.gui.getSize().width), (int)(Math.random()*Env.gui.getSize().height));
		}
	}

	///////////////////////////////
	// ���
	public void setFunctor(Functor newFunctor) {
		if (newFunctor.getArity() > args.length) {
			// TODO SystemError�Ѥ��㳰���饹���ꤲ��
			throw new RuntimeException("SYSTEM ERROR: insufficient link vector length");
		}
		functor = newFunctor;
	}
	/** �ե��󥯥�̾�����ꤹ�롣 */
	public void setName(String name) {
		set(name, getFunctor().getArity());
	}
	/** �ե��󥯥������ꤹ�롣
	 * ��°�줬��⡼�Ȥξ��⤢�ꡢ������AtomSet��ɬ���������ʤ���Фʤ�ʤ��Τǡ�
	 * ���alterAtomFunctor�᥽�åɤ�Ƥ֡�*/
	public void set(String name, int arity) {
		mem.alterAtomFunctor(this, new Functor(name, arity));
	}
	/** ���� TODO ��󥯤⤱���ʤ��ξ�硢�᥽�å�̾���Ѥ��Ʋ�������
	 * ����쥯�饹�˥᥽�åɤ��äƸƤ֤褦�ˤ��뤫���ޤ��ϡ�
	 * ���Υ᥽�åɤ�������쥯�饹�Υ᥽�åɤ�Ƥ֡��Ȥꤢ���������̤�θ�ԤǤ褤��*/
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
	
	Point pos;
	double vx, vy;
	public void initNode() {
		pos = new Point();
	}
	public Point getPosition() {
		return pos;
	}
	public void setPosition(Point p) {
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
	public void move(Rectangle area) {
		//if (n.isFixed()) return;
		pos.x += Math.max(-5, Math.min(5, vx));
		pos.y += Math.max(-5, Math.min(5, vy));
		
		if (pos.x < area.getMinX())		pos.x = (int)area.getMinX();
		else if (pos.x > area.getMaxX())	pos.x = (int)area.getMaxX();
		
		if (pos.y < area.getMinY())		pos.y = (int)area.getMinY();
		else if (pos.y > area.getMaxY())	pos.y = (int)area.getMaxY();
		
//		dx=dy=0;
		vx /= 2;
		vy /= 2;
	}
	public void paintEdge(Graphics g) {
		g.setColor(Color.BLACK);
		for(int i=0;i<getEdgeCount();i++) {
			Node n2 = getNthNode(i);
			if(this.hashCode() < n2.hashCode()) continue;
			g.drawLine(this.getPosition().x, this.getPosition().y, n2.getPosition().x, n2.getPosition().y);
		}
	}
	
	public void paintNode(Graphics g) {
		String label = getName();
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(label);
		int h = fm.getHeight();
		
		Dimension size = new Dimension(16, 16);
//		g.setColor(new Color(64,128,255));
		// Ŭ���˿�ʬ�����롪
		g.setColor(test.GUI.GraphLayout.colors[ Math.abs(label.hashCode()) % test.GUI.GraphLayout.colors.length ]);
		
		g.fillOval(pos.x - size.width/2, pos.y - size.height/ 2, size.width, size.height);
		
		g.setColor(Color.BLACK);
		g.drawOval(pos.x - size.width/2, pos.y - size.height/ 2, size.width, size.height);
		g.drawString(label, pos.x - (w-10)/2, (pos.y - (h-4)/2) + fm.getAscent()+size.height);
	}
}
