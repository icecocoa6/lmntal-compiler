package graphic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import runtime.Functor;
import runtime.Membrane;

public abstract class GraphicObj{
	final static
	protected Functor POSITION_ATOM = new Functor("position",2); 

	final static
	protected Functor COLOR_ATOM = new Functor("color",3); 
	
	protected Color color = Color.BLACK;
	protected Point position = new Point(0,0);
	protected String memID = null;
	
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