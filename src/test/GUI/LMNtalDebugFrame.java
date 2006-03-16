package test.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import runtime.Debug;
import runtime.Env;
import runtime.FrontEnd;
import runtime.Rule;

/**
 * @author inui
 * ɸ����Ϥ�JTextArea�����ؤ��뤿��Υ��饹
 */
class ConsolePrintStream extends PrintStream {
	private JTextArea jt;
	private JScrollPane scroll;
	
	public ConsolePrintStream(OutputStream os, JTextArea jt, JScrollPane scroll) throws FileNotFoundException {
		super(os);
		this.jt = jt;
		this.scroll = scroll;
	}

	public void println(String s) {
		//super.println(s);
		jt.append(s+"\n");
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
	}
}

/**
 * @author inui
 * �ǥХå����˻��Ѥ���ե졼��
 */
public class LMNtalDebugFrame extends JFrame {	
	public boolean restart;
	
	/** LMNtalFrame */
	private LMNtalFrame lmntalFrame;
	
	/** ��������ɽ������ */
	private JTextPane jt;
	
	/** ���ֹ��ɽ������ƥ����ȥ��ꥢ */
	private JTextPane linenoArea;
	
	/** �ץ�ե���������ɽ�����뤫�ɤ��� */
	private boolean showProfile = false;
	
	/** ���󥽡��� */
	private JTextArea console;
	
	private JCheckBox guiCheckBox;
	private JButton nextButton;
	private JButton continueButton;
	private Thread th;
	
	/**
	 * ���󥹥ȥ饯���Ǥ�
	 */
	public LMNtalDebugFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initComponents();
		setSize(600, 600);
		setVisible(true);
	}
	
	/**
	 * �ǥХå���������ɽ���ѤΥƥ����ȥ��ꥢ����������
	 */
	private JTextPane createJTextArea() {		
		final JTextPane jt=new JTextPane() {
			public void paint(Graphics g) {
				super.paint(g);
				
				final int SIZE = (Env.fDEMO  ? 25 : 18);
				
				// �֥졼���ݥ���Ȥ�ɽ��
				g.setColor(Color.red);
				Iterator iter = Debug.breakPointIterator();
				while (iter.hasNext()) {
					//g.fillRect(0, SIZE*(((Rule)iter.next()).lineno-1)+9, SIZE-8, SIZE-2);
					final int x = 0;
					final int y = SIZE*(((Integer)iter.next()).intValue()-1)+9;
					final int w = SIZE-(Env.fDEMO ? 5 : 2);
					final int h = SIZE-(Env.fDEMO ? 5 : 2);
					g.fillOval(x, y, w, h);
				}
				
				// ���������Υ롼���ɽ��
				g.setColor(Color.blue);
				int lineno = Debug.getCurrentRuleLineno();
				if (lineno > 0) {
					g.setColor(Color.blue);
					g.setXORMode(Color.black);
					g.fillRect(SIZE-4, SIZE*(lineno-1)+9, getWidth(), SIZE-2);
					g.setPaintMode();
				}
				
				if (Env.profile && showProfile) {
					g.setColor(Color.blue);
					//g.setFont(new Font("Monospace", Font.PLAIN, SIZE));
					Font font = new Font(getFont().getName(), Font.PLAIN, Env.fDEMO ? 16 : 12);
					g.setFont(font);
					Iterator rules = Debug.ruleIterator();
					if (rules != null) {
						while (rules.hasNext()) {
							Rule r = (Rule)rules.next();
							double time = (Env.majorVersion == 1 && Env.minorVersion > 4) ? r.time / 1000000 : r.time;
							String s = r.succeed + "/" + r.apply+ "(" + time + "ms)";
							g.drawString(s, getWidth()-(Env.fDEMO ? 110 : 70), SIZE*r.lineno+(Env.fDEMO ? 2 : 4));
						}
					}
				}
			}
		};
		jt.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int offset = jt.getCaretPosition();
				try {
					String text = jt.getText(0, offset);
					int lineno = 0;
					for (int i = 0; i < text.length(); i++) {
						if (text.charAt(i) == '\n')
							lineno++;
					}
					Debug.toggleBreakPointAt(lineno);
					//System.out.println("lineno"+lineno);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				jt.repaint();
			}
		});
		jt.setContentType("text/html");
		jt.setEditable(false);
		return jt;
	}
	
	/**
	 * ����ݡ��ͥ�Ȥ��������ޤ���
	 */
	protected void initComponents() {
		setTitle("It's LMNtal Debugger - "+Debug.getUnitName());
		getContentPane().setLayout(new BorderLayout());
		
		final JPanel p = new JPanel(new BorderLayout());
		
		//���ֹ�ɽ���ΰ�
		linenoArea = new JTextPane();
		linenoArea.setContentType("text/html");
		p.add("West", linenoArea);

		//�������ӥ塼
		jt = createJTextArea();
		p.add("Center", jt);
		JScrollPane scroll1 = new JScrollPane(p);
		//getContentPane().add(jsp, BorderLayout.CENTER);
		
		//���󥽡���
		console = new JTextArea();
		console.setFont(new Font("Monospaced", Font.PLAIN, Env.fDEMO ? 16 : 12));
		console.setEditable(false);
		JScrollPane scroll2 = new JScrollPane(console);
		scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//ɸ����Ϥ��ڤ��ؤ���
		try {
			System.setOut(new ConsolePrintStream(System.out, console, scroll2));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll1, scroll2);
		getContentPane().add(split, BorderLayout.CENTER);
		split.setDividerLocation(400);
		
		//�ġ���С�������
		JToolBar toolBar = new JToolBar();

		nextButton = new JButton("Next");
		nextButton.setToolTipText("apply a rule only one time");
		toolBar.add(nextButton);
		continueButton = new JButton("Continue");
		continueButton.setToolTipText("apply rules until a break point");
		toolBar.add(continueButton);
		
//		JButton openButton = new JButton("Open");
//		openButton.addActionListener(new OpenButtonActionListener());
//		toolBar.add(openButton);
		
		JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(new RestartButtonActionListener());
		toolBar.add(restartButton);
		
		//���ֹ�����å��ܥå���
		{
			JCheckBox linenoCheckBox = new JCheckBox("LineNumber");

			linenoCheckBox.setSelected(true);
			linenoCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) p.add("West", linenoArea);
					else p.remove(linenoArea);
					getContentPane().validate();
				}
			});
			toolBar.add(linenoCheckBox);
		}
		
		//�ǥ⥪�ץ�����ڤ��ؤ������å��ܥå���
		{
			JCheckBox checkBox = new JCheckBox("Demo");
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Env.atomSize = 40;//TODO �ɤ�����������Ѱդ���
						Env.fDEMO = true;
					} else {
						Env.atomSize = 16;
						Env.fDEMO = false;
					}
					changeFontSize(jt);
					changeFontSize(linenoArea);
					console.setFont(new Font("Monospaced", Font.PLAIN, Env.fDEMO ? 16 : 12));
				}
			});
			toolBar.add(checkBox);
		}
		
		//�ץ�ե������ɽ�����ץ��������å��ܥå���
		{
			JCheckBox checkBox = new JCheckBox("ShowProfile");
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					showProfile = (e.getStateChange() == ItemEvent.SELECTED);
					jt.repaint();
				}
			});
			toolBar.add(checkBox);
		}
		
		//g���ץ�������ؤ������å��ܥå���
		{
			guiCheckBox = new JCheckBox("GUI");
			guiCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						lmntalFrame.setVisible(true);
					} else {
						lmntalFrame.setVisible(false);
					}
					
				}
			});
			toolBar.add(guiCheckBox);
		}
		
		getContentPane().add("North", toolBar);
	}
	
	private void changeFontSize(JTextPane jt) {
		String s = jt.getText();
		if (Env.fDEMO) s = s.replaceFirst("10px", "14px");
		else s = s.replaceFirst("14px", "10px");
		jt.setText(s);
	}
	
	/**
	 * ���������ꥢ�˥ƥ����Ȥ򥻥åȤ��ޤ�
	 * @param s �������ƥ�����
	 * @param lineno ���ֹ�
	 */
	public void setSourceText(String s, int lineno) {
		jt.setText(s);
		StringBuffer buf = new StringBuffer();
		//buf.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; CHARSET=EUC-JP\">\n");
		buf.append("<style>pre {font-size:10px; font-family:monospace;}</style>\n");
		buf.append("<pre>\n");
		for (int i = 1; i <= lineno; i++)
			buf.append(i+"\n");
		buf.append("</pre>\n");
		linenoArea.setText(buf.toString());			
	}
	
	public void setLMNtalFrame(LMNtalFrame lmntalFrame) {
		this.lmntalFrame = lmntalFrame;
		nextButton.addActionListener(new NextButtonActionListener(lmntalFrame));
		continueButton.addActionListener(new ContinueButtonActionListener(lmntalFrame));
		lmntalFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				guiCheckBox.setSelected(false);
			}
		});
	}

	/** Next�ܥ���򲡤����Ȥ���Action */
	class NextButtonActionListener implements ActionListener {
		private LMNtalFrame frame;
		
		public NextButtonActionListener(LMNtalFrame f) {
			frame = f;
		}
		
		public void actionPerformed(ActionEvent e) {
			frame.busy = false;
			Debug.endBreakPoint(Debug.NEXT);
		}
	}
	
	/** Continue�ܥ���򲡤����Ȥ���Action */
	class ContinueButtonActionListener implements ActionListener {
		private LMNtalFrame frame;
		
		public ContinueButtonActionListener(LMNtalFrame f) {
			frame = f;
		}
		
		public void actionPerformed(ActionEvent e) {
			frame.busy = false;
			Debug.endBreakPoint(Debug.CONTINUE);
		}
	}
	
	/** Open�ܥ���򲡤����Ȥ���Action */
	class OpenButtonActionListener implements ActionListener, Runnable {
		Reader r;
		String name;
		
		public void actionPerformed(ActionEvent e) {
			if (lmntalFrame != null) lmntalFrame.dispose();
			console.setText("");
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(null);
			File f = fc.getSelectedFile();
			Debug.setUnitName(f.getName());
			try {
				r = new FileReader(f);
				name = f.getName();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			th = new Thread(this);
			th.start();
			guiCheckBox.setSelected(false);
		}
		
		public void run() {
			restart = false;
			FrontEnd.run(r, name);
		}
	}
	
	/** Restart�ܥ���򲡤����Ȥ���Action */
	class RestartButtonActionListener implements ActionListener, Runnable {
		public void actionPerformed(ActionEvent e) {
			if (lmntalFrame != null) lmntalFrame.dispose();
			console.setText("");
			guiCheckBox.setSelected(false);
			th = new Thread(this);
			th.start();
		}
		
		public void run() {
			restart = true;
			try {
				FileReader r = new FileReader(Debug.getUnitName());
				FrontEnd.run(r, Debug.getUnitName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}