/**
 * ����դΥΡ��ɤ�ɽ��
 * ���߰��֤ʤɤ��ݻ�
 */

package test.graph;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;

public class GraphNode {
	
	/**
	 * ���߰���
	 */
	protected Point pos = null;
	
	/**
	 * �Ρ��ɤΥ�٥�
	 */
	protected String label = null;
	
	/**
	 * ��ư����
	 */
	protected int dx = 0;
	protected int dy = 0;
	
	protected Dimension size = new Dimension(16,16);
	
	/**
	 * ���߰��֡���٥롢��ư�ϰϤ���ꤷ�ƽ�������ޤ�
	 * @param label ɽ����٥�
	 * @param pos �������
	 * @param area ��ư��ǽ�ϰ�
	 */
	public GraphNode(String label, Point pos) {
		this.label = label;
		this.pos = pos;
	}
	
	public void setMoveDelta(double dx, double dy) {
		this.dx += dx;
		this.dy += dy;
	}
	
	public boolean isFixed() {
		return false;
	}
	
	public Point getPosition() {
		return this.pos;
	}
	
	public void move(Rectangle area) {
		if (isFixed()) return;
		this.pos.x += Math.max(-5, Math.min(5, this.dx));
		this.pos.y += Math.max(-5, Math.min(5, this.dy));
		
		if (this.pos.x < area.getMinX()) {
			this.pos.x = (int)area.getMinX();
		} else if (this.pos.x > area.getMaxX()) {
			this.pos.x = (int)area.getMaxX();
		}
		
		if (this.pos.y < area.getMinY()) {
			this.pos.y = (int)area.getMinY();
		} else if (this.pos.y > area.getMaxY()) {
			this.pos.y = (int)area.getMaxY();
		}
		this.dx /= 2;
		this.dy /= 2;
	}
	
	public void paint(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(label);
		int h = fm.getHeight();
		g.setColor(new Color(64,128,255));
		g.fillOval(pos.x - size.width/2, pos.y - size.height/ 2, size.width, size.height);
		g.setColor(Color.black);
		g.drawOval(pos.x - size.width/2, pos.y - size.height/ 2, size.width, size.height);
		g.drawString(this.label, pos.x - (w-10)/2, (pos.y - (h-4)/2) + fm.getAscent()+size.height);
	}
}