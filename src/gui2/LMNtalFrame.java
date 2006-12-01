package gui2;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

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
	
	private int step_ = 1;
	private boolean stopCalc_ = true;
	public boolean running = true;
	
	private GraphPanel panel = null;
	private SubFrame subFrame;
	private LogFrame logFrame;
	private Thread th;
	private Membrane rootMembrane;
	private List<Node> rootMemList = new ArrayList<Node>();
	

	/////////////////////////////////////////////////////////////////
	// ���󥹥ȥ饯��
	public LMNtalFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stopCalc_ = false;
				running = false;
			}
		});
		
//		addKeyListener(this);
		
		initComponents();
		setSize(WINDOW_WIDTH, WINDOW_HIEGHT);
		setVisible(true);
		
		// ����������ɥ�������
		subFrame = new SubFrame(this);
		logFrame = new LogFrame(this);
		
	}
	/////////////////////////////////////////////////////////////////
	
	/**
	 * ���٤Ƥ������ɽ��������
	 *
	 */
	public void hideAll(){
		panel.hideAll();
	}
	
	private void initComponents() {
		panel = new GraphPanel();

		setTitle(TITLE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_A){
			if(rootMemList.size() == 0){ return; }
			panel.setRootNode(rootMemList.remove(rootMemList.size() - 1));
			setRootMem(rootMembrane);
			System.out.println(rootMembrane);
		}
		else if(e.getKeyCode() == KeyEvent.VK_S){
			rootMemList.add(panel.getRootNode());
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
	/**
	 * ���������Τ��׻���³�Ԥ��Ƥ褤�����䤤��碌���ա�
	 *
	 */
	public void onTrace(){
		if(null != rootMembrane){
			logFrame.setLog(rootMembrane.toString());
		}
		while(running) {
			if(!stopCalc_){
				break;
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		stopCalc_ = (step_ == 0) ? true : false;
		step_--;
	}
	
	/**
	 * ��Ψ�򥻥åȤ���
	 * @param magni
	 */
	public void setMagnification(double magni){
		panel.setMagnification(magni);	
	}
	
	/**
	 * root��򥻥åȤ���
	 * @param mem
	 */
	public void setRootMem(Membrane mem){
		rootMembrane = mem;
		panel.setRootMem(mem);
	}
	
	public void setStep(int step){
		step_ = step;
	}
	
	public void setStopCalc(boolean flag){
		stopCalc_ = flag;
	}
	
	/**
	 * ���٤Ƥ����ɽ��������
	 *
	 */
	public void showAll(){
		panel.showAll();
	}
}