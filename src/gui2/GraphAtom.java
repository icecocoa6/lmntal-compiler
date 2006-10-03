package gui2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import runtime.Atom;

public class GraphAtom {

	/////////////////////////////////////////////////////////////////
	// ���
	
	final static
	public double ATOM_DEF_SIZE = 50;
	
	final static
	private BasicStroke ATOM_STROKE = new BasicStroke(1.0f);
	
	final static
	private Color ATOM_BORDER_COLOR = new Color(100, 100, 100);
	
	final static
	private Color ATOM_PIN_COLOR = new Color(0, 0, 0);
	
	final static
	private Color ATOM_NAME_COLOR = new Color(0, 0, 0);

	/////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////
	
	private int posX;
	private int posY;
	private double dx;
	private double dy;
	private String name;
	private boolean isHold = false;
	private boolean clipped = false;
	
	final public Atom me;
	
	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public GraphAtom(Atom atom){
		posX = (int)(Math.random() * 500.0);
		posY = (int)(Math.random() * 500.0);
		if(atom != null){
			me = atom;
			name = atom.getName();
		} else {
			me = null;
			name = ".";
		}
	}
	/////////////////////////////////////////////////////////////////
	
	static
	public int getAtomSize(){ return (int)(ATOM_DEF_SIZE * GraphPanel.getMagnification()); }

	public boolean isClipped(){
		return clipped;
	}
	
	public boolean isDummy(){
		return (me == null) ? true : false;
	}
	
	public void paint(Graphics g){
		// [0, 7] -> [128, 255] for eash R G B
		int ir = 0x7F - ((name.hashCode() & 0xF00) >> 8) * 0x08 + 0x7F;
		int ig = 0x7F - ((name.hashCode() & 0x0F0) >> 4) * 0x08 + 0x7F;
		int ib = 0x7F - ((name.hashCode() & 0x00F) >> 0) * 0x08 + 0x7F;
		
		g.setColor(ATOM_NAME_COLOR);
		g.drawString(name, posX, posY);

		g.setColor(new Color(ir, ig, ib));
		g.fillOval(posX,
				posY,
				getAtomSize(),
				getAtomSize());
		
		g.setColor(ATOM_BORDER_COLOR);
        ((Graphics2D)g).setStroke(ATOM_STROKE);
		g.drawOval(posX,
				posY,
				getAtomSize(),
				getAtomSize());
		
		if(clipped){
			g.setColor(ATOM_PIN_COLOR);
			g.fillOval(posX + getAtomSize() / 3,
					posY + getAtomSize() / 3,
					getAtomSize() / 3,
					getAtomSize() / 3);
		}
	}
	
	/** �ºݤ˥��ȥ���ư������ */
	public void moveCalc(){
		if(!clipped && !isHold){
			posX += (int)dx;
			posY += (int)dy;
		}
		dx = dy = 0.0;
	}
	
	/** ���ȥ��X��ɸ��������� */
	public int getPosX(){ return posX; }
	
	/** ���ȥ��Y��ɸ��������� */
	public int getPosY(){ return posY; }
	
	/** ��ư��Υ�����ꤹ��
	 * <p>
	 * ���Υ᥽�åɤ����ꤵ�줿��ư��Υ�����Ѥ�moveCalc()�Ǥ�
	 * ��ư��Υ�Ȥʤ롣 
	 */
	public void moveDelta(double x, double y){
		dx += x;
		dy += y;
	}
	
	/** �ºݤ˥��ȥ���ư������ */
	public void setPosition(int x, int y){
		posX = x;
		posY = y;
	}
	
	/** ���ȥ����ꤹ��ʥɥ�å��ѡ� */
	public void setHold(boolean hold){ isHold = hold; }
	
	/** ���ȥ����ꤹ��ʥ��֥륯��å��ѡ� */
	public void flipClip(){ clipped = !clipped; }
	
}
