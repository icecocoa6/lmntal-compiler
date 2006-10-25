package toolkit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import runtime.Membrane;
import runtime.Functor;
import runtime.SymbolFunctor;

public abstract class GraphicObj{
	final static
	protected Functor POSITION_ATOM = new SymbolFunctor("position", 2); 

	final static
	protected Functor COLOR_ATOM = new SymbolFunctor("color", 3); 
	final static
	protected Functor ANGLE_ATOM = new SymbolFunctor("angle", 1); 
	
	protected Color color = Color.BLACK;
	protected Point position = new Point(0,0);
	protected String memID = null;
	protected int angle = 0;
	
	///////////////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public GraphicObj(Membrane mem){
		memID = mem.getGlobalMemID();
		setMembrane(mem);
	}
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * ���ID���֤�
	 */
	public String getID(){ return memID; }
	
	/**
	 * ����˻Ȥ����򥻥åȤ���
	 * @param color
	 */
	public void setColor(Color color){ this.color = color; }
	
	/**
	 * ���褹�븶���򥻥åȤ���
	 * @param position
	 */
	public void setPosition(Point position){ this.position = position;}
	
	/**
	 * ���褹���ɸ����֤�
	 * @return��Point
	 */
	public Point getPosition(){ return position;}
	
	/**
	 * ���֥������Ȥ����褹�롥
	 * @param g
	 * @param delta - ������delta�Ȥ��������Ԥ�
	 */
	abstract public void drawAtom(Graphics g, Point delta);
	
	/**
	 * �줫��ɬ�פʾ����������롥
	 * @param mem
	 */
	abstract public void setMembrane(Membrane mem);
}