package test.GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;

import runtime.*;

/**
 * @author inui
 * �ǥХå����˻��Ѥ���ե졼��
 */
public class LMNtalDebugFrame extends JFrame {
	/** LMNtalFrame */
	private LMNtalFrame lmntalFrame;
	
	/** ��������ɽ������ */
	private JTextPane jt;
	
	/** ���󥽡��� */
	private JTextArea console;
	
	/** ���ֹ��ɽ������ƥ����ȥ��ꥢ */
	private JTextPane linenoArea;
	
	/**
	 * ���󥹥ȥ饯���Ǥ�
	 */
	public LMNtalDebugFrame(LMNtalFrame lmntalFrame) {
		this.lmntalFrame = lmntalFrame;
		
		lmntalFrame.setSize(600, 400);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initComponents();
		setSize(600, 600);
		setVisible(true);
	}
	
	/**
	 * �ǥХå���������ɽ���ѤΥƥ����ȥ��ꥢ����������
	 */
	private JTextPane createJTextArea() {
		final int SIZE = 18;
		
		final JTextPane jt=new JTextPane() {
			public void paint(Graphics g) {
				super.paint(g);
				
				// �֥졼���ݥ���Ȥ�ɽ��
				g.setColor(Color.red);
				Iterator iter = Debug.breakPointIterator();
				while (iter.hasNext()) {
					//g.fillRect(0, SIZE*(((Rule)iter.next()).lineno-1)+9, SIZE-8, SIZE-2);
					g.fillOval(0, SIZE*(((Rule)iter.next()).lineno-1)+9, SIZE-2, SIZE-2);
					
				}
				
				// ���������Υ롼���ɽ��
				g.setColor(Color.blue);
				//g.setFont(new Font("Monospace", Font.PLAIN, SIZE));
				int lineno = Debug.getCurrentRuleLineno();
				if (lineno > 0) {
					g.setColor(Color.blue);
					g.setXORMode(Color.black);
					g.fillRect(SIZE-4, SIZE*(lineno-1)+9, 600, SIZE-2);
					g.setPaintMode();
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
		jt.setFont(new Font("Monospaced", Font.PLAIN, SIZE));
		return jt;
	}
	
	/**
	 * ����ݡ��ͥ�Ȥ��������ޤ���
	 */
	protected void initComponents() {
		setTitle("It's LMNtal Debugger");
		getContentPane().setLayout(new BorderLayout());
		
		//�������ӥ塼
		final JPanel p = new JPanel(new BorderLayout());
		linenoArea = new JTextPane();
		p.add("West", linenoArea);
		jt = createJTextArea();
		p.add("Center", jt);
		linenoArea.setContentType("text/html");

		JScrollPane jsp = new JScrollPane(p);
		getContentPane().add(jsp, BorderLayout.CENTER);
		
		//���󥽡���
//		console = new JTextArea(5, 10);
//		jsp = new JScrollPane(console);
//		getContentPane().add("South", jsp);
		
		// �ġ���С�������
		JToolBar toolBar = new JToolBar();
		JButton nextButton = new JButton("Next");
		nextButton.setToolTipText("apply a rule only one time");
		nextButton.addActionListener(new NextButtonActionListener(lmntalFrame));
		toolBar.add(nextButton);
		JButton continueButton = new JButton("Continue");
		continueButton.setToolTipText("apply rules until a break point");
		continueButton.addActionListener(new ContinueButtonActionListener(lmntalFrame));
		toolBar.add(continueButton);
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
		
		JCheckBox demoCheckBox = new JCheckBox("Demo");
		demoCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Env.atomSize = 40;
					Env.fDEMO = true;
				} else {p.remove(linenoArea);
					Env.atomSize = 16;
					Env.fDEMO = false;
				}
			}
		});
		toolBar.add(demoCheckBox);
		
		getContentPane().add("North", toolBar);
	}
	
	/**
	 * ���������ꥢ�˥ƥ����Ȥ򥻥åȤ��ޤ�
	 * @param s �������ƥ�����
	 * @param lineno ���ֹ�
	 */
	public void setSourceText(String s, int lineno) {
		jt.setText(s);
		StringBuffer buf = new StringBuffer();
		buf.append("<style>pre {font-size:10px; font-family:monospace;}</style>\n");
		buf.append("<pre>\n");
		for (int i = 1; i <= lineno; i++)
			buf.append(i+"\n");
		buf.append("</pre>");
		linenoArea.setText(buf.toString());			
	}
	
	public JTextArea getConsole() {
		return console;
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
}