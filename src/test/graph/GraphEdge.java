/**
 * ����դΥΡ��ɴ֤���������󥯤�ɽ���ޤ�
 */

package test.graph;

import java.awt.Graphics;

public class GraphEdge {
	
	public GraphNode from = null;
	public GraphNode to = null;
	public double len = 0.0;
	
	/**
	 * ��󥯤���Ρ��ɤ���ꤷ�ƽ�������ޤ�
	 * @param from ��󥯤λϤޤ�ΥΡ���
	 * @param to ��󥯤ν����ΥΡ���
	 */
	public GraphEdge(GraphNode from, GraphNode to) {
		this.from = from;
		this.to = to;
		this.len = 70.0;
	}
	
	public void paint(Graphics g) {
		if (from != null && to != null) {
			g.drawLine((int)from.getPosition().getX(), (int)from.getPosition().getY(),
				(int)to.getPosition().getX(), (int)to.getPosition().getY());
		}
	}
}
