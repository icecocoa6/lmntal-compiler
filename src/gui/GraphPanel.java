package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * �ѥͥ�����褹��
 */
public class GraphPanel extends JPanel implements Runnable {

	private GraphLayout gLayout = null;
	private Thread th = null;
	private Image OSI = null;
	private Graphics OSG = null;
	
	/**
	 * ���᡼����������Ƥ������褹��
	 */
	public GraphPanel() {
		super();
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				OSI = createImage((int) getSize().getWidth(), (int) getSize().getHeight());
				OSG = OSI.getGraphics();			
			}

		});
		gLayout = new GraphLayout(this);
	}
	
	public GraphLayout getGraphLayout() {
		return gLayout;
	}
	
	/** ����åɤ��Ϥޤ�Ȥ��ν���
	 * <p>��ʬ�򥹥�åɤ������gLayout��ư����</p>
	 */
	public void start() {
		if (th == null) {
			th = new Thread(this);
			th.start();
			gLayout.start();
		}
	}
	
	/** ����åɤ������Ȥ��ν��� 
	 * <p>gLayout��Ʊ���˽�λ</p>
	 */
	public void stop() {
		th = null;
		gLayout.stop();
	}
	
	/** �⤷��ʬ������åɤ����äƤ��������äƤ��� */
	public void run() {
		Thread me = Thread.currentThread();
		while (me == th) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			repaint();
		}
	}
	
	/** 
	 * OSI�˥��᡼�������äƤ��ʤ��ä��饤�᡼����������Ƥ��롣
	 * ���äƤ�������طʤλͳѤ�����Τ����᡼��(OSI)��Ž�롣 
	 */
	public void paint(Graphics g) {
		if (OSI == null) {
			OSI = createImage((int) getSize().getWidth(), (int) getSize().getHeight());
			OSG = OSI.getGraphics();			
		}
		if (OSI != null){
			OSG.setColor(Color.WHITE);
			OSG.fillRect(0,0,(int) getSize().getWidth(), (int) getSize().getHeight());
			gLayout.paint(OSG);
			g.drawImage(OSI,0,0,this);
		}
	}
	
//	public Rectangle getPreferredArea() {
//		return gLayout.getPreferredArea();
//	}
	
//	public Dimension getPreferredSize() {
//		return getPreferredArea().getSize(); 	
//	}
}