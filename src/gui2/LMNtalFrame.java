package gui2;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import runtime.Membrane;

public class LMNtalFrame extends JFrame implements KeyListener {

	/////////////////////////////////////////////////////////////////
	// ���
	
	final static
	public int WINDOW_WIDTH = 800;
	
	final static
	public int WINDOW_HIEGHT = 600;
	
	final static
	public String TITLE = "It's LMNtal";
	
	final static
	private long SLEEP_TIME = 500;
	
	/////////////////////////////////////////////////////////////////
	
	public boolean stopCalc = true;
	public boolean running = true;
	
	private GraphPanel panel = null;
	private SubFrame subFrame;
	private LogFrame logFrame;
	private Thread th;
	private Membrane rootMembrane;
	

	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LMNtalFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stopCalc = false;
				running = false;
			}
		});
		
		initComponents();
		setSize(WINDOW_WIDTH, WINDOW_HIEGHT);
		setVisible(true);
		
		// ����������ɥ�������
		subFrame = new SubFrame(this);
		logFrame = new LogFrame(this);
		
	}
	/////////////////////////////////////////////////////////////////
	
	private void initComponents() {
		panel = new GraphPanel();

		setTitle(TITLE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
	}
	
	public void setMagnification(double magni){
		panel.setMagnification(magni);	
	}
	
	public void onTrace(){
		if(null != rootMembrane){
			logFrame.setLog(rootMembrane.toString());
		}
		while(running) {
			if(!stopCalc){
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				// TODO ��ư�������줿 catch �֥�å�
				e.printStackTrace();
			}
		}
		stopCalc = true;
	}
	
	public void setRootMem(Membrane mem){
		rootMembrane = mem;
		panel.setRootMem(mem);
	}
	
	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		panel.showAll();
	}
	
	/**
	 * ���٤Ƥ������ɽ��������
	 *
	 */
	public void hideAll(){
		panel.hideAll();
	}
	
	public void keyPressed(KeyEvent e) {
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {
		System.out.println(e.getKeyChar());
	}
}

class MyThread extends Thread {
	LMNtalFrame f;
	MyThread(LMNtalFrame ff) {
		f = ff;
	}
	
	public void run() {
		while(true) {
			try {
				sleep(4000);
			} catch (Exception e) {
			}
		}
	}
}