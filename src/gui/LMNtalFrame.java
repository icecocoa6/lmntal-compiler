package gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import runtime.Env;
//import compile.parser.ParseException;

class MyThread extends Thread {
	LMNtalFrame f;
	MyThread(LMNtalFrame ff) {
		f = ff;
	}
	
	/**
	 * ������sleep������
	 */
	public void run() {
		while(true) {
			try {
				f.busy = false;
				sleep(4000);
			} catch (Exception e) {
			}
		}
	}
}

public class LMNtalFrame extends JFrame implements KeyListener {
	Thread th;
	
	public LMNGraphPanel lmnPanel = null;
	JTextArea jt;
	public boolean busy = true;
	public boolean running = true;
	
	/** �ե졼����������� */
	public LMNtalFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				running = busy = false;
			}
		});
		initComponents();
		setSize(800,600);
		if(Env.getExtendedOption("screen").equals("max")) {
			setExtendedState(Frame.MAXIMIZED_BOTH | getExtendedState());
		}
		setVisible(true);
		if(!Env.getExtendedOption("auto").equals("")) {
			th = new MyThread(this);
			th.start();
		}
	}
	
	/** �����������줿�Ȥ��ν���(���᥽�å�) */
	public void keyPressed(KeyEvent e) {
	}
	
	/** ������Υ���줿�Ȥ��ν���(���᥽�å�) */
	public void keyReleased(KeyEvent e) {
	}
	
	/** ����ʸ���򥿥��ԥ󥰤��줿�Ȥ��������줿ʸ������Ϥ��� */
	public void keyTyped(KeyEvent e) {
		System.out.println(e.getKeyChar());
	}

	/** ��������ե졼������� */
	protected void initComponents() {
		lmnPanel = new LMNGraphPanel(this);
		lmnPanel.getGraphLayout().initGraphDialog(this);
		JButton bt;
//		try {
//			;
////			p.setSource("a(\n b(c, \n  d(e,\nf(g\n,h))))\n, b");
////			p.setSource("append(c(aa,c(bb,c(cc,n))),\nc(dd,c(ee,n)),\nresult)");
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		setTitle("It's LMNtal");
		getContentPane().setLayout(new BorderLayout());
//		getContentPane().add(jt=new JTextArea(5, 80), BorderLayout.NORTH);
		getContentPane().add(new JScrollPane(lmnPanel), BorderLayout.CENTER);
		getContentPane().add(bt=new JButton("Go ahead"), BorderLayout.SOUTH);
		bt.addActionListener(new ActionAdapter(this));
//		getContentPane().addKeyListener(new MyKeyAdapter(this));
	}
	
//	/**
//	 * ������ LMNtal �����������ɤˤ�ꥰ��դ򹹿����롣
//	 * @param s
//	 */
//	void setSource(String s) {
//		try {
//			lmnPanel.setSource(s);
//		} catch (ParseException e) {
////			e.printStackTrace();
//		}
//	}
	
	/** busy��true�ΤȤ����Ԥ� */
	public void waitBusy() {
//		lmnPanel.getGraphLayout().calc();
		lmnPanel.getGraphLayout().setAllowRelax(true);
		busy = true;
		while(busy) {
			try {
				this.wait(100);
			} catch (Exception e) {
			}
		}
		lmnPanel.getGraphLayout().setAllowRelax(false);
	}
	
	/** 
	 * gui��ͭ�����ä���lmnPanel��ư����λ����ޤǸ�ĥ�äƤ��롣
	 * @return �롼�륹��åɤμ¹Ԥ��³���Ƥ褤���ɤ��� 
	 */
	public boolean onTrace() {
		if(Env.gui.running) {
			lmnPanel.start();
//			Env.gui.lmnPanel.setMembrane((runtime.Membrane)Env.theRuntime.getGlobalRoot());
			Env.gui.waitBusy();
			lmnPanel.stop();
		}
		return Env.gui.running;
	}
}

class ActionAdapter implements ActionListener {
	LMNtalFrame frame;
	ActionAdapter(LMNtalFrame f) {
		frame = f;
	}
	public void actionPerformed(ActionEvent e) {
		frame.busy = false;
	}
}

class MyKeyAdapter extends KeyAdapter {
	LMNtalFrame frame;
	MyKeyAdapter(LMNtalFrame f) {
		frame = f;
	}
	public void keyPressed(KeyEvent e) {
		frame.busy = false;
	}
}
