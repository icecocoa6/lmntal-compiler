/*
 * ������: 2006/04/04
 */
package debug;

import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class HelpFrame extends JFrame {
	public HelpFrame() {
		super("LMNtal Debugger Help");
		
		JTextPane jt = new JTextPane();
		jt.setEditable(false);
		jt.setContentType("text/html");
//		try {
//			FileReader fr = new FileReader("help.html");
//			BufferedReader br = new BufferedReader(fr);
//			StringBuffer buf = new StringBuffer();
//			String s = null;
//			while ((s = br.readLine()) != null) {
//				buf.append(s);
//			}
//			jt.setText(buf.toString());
			//TODO: �ե����뤫���ɤ߹���褦�ˤ���
			String s =
				"<h1>LMNtal�ǥХå� �إ��</h1>"+
				"<h2>�֥졼���ݥ����</h2>"+
				"<ul>"+
				"<li>�֥졼���ݥ���Ȥ����ꤷ�����롼��򥯥�å�����"+
				"<li>�⤦���٥���å�����Ȳ�������"+
				"</ul>"+
				""+
				"<h2>�¹�</h2>"+
				"<li>Next�ܥ���ǥ롼���1��Ŭ�Ѥ�����ߤ����-g���ץ�����Go ahead��Ʊ��ư���"+
				"<li>Continue�ܥ���ǡ��֥졼���ݥ���Ȥ����ꤵ�줿�롼�뤬Ŭ�Ѥ����ޤǥ롼���Ŭ�Ѥ���"+
				"<li>Restart�ܥ����Ʊ���ץ�����ǽ餫��¹Ԥ���"+
				"</ul>"+
				""+
				"<h2>����¾</h2>"+
				"<ul>"+
				"<li>LineNumber<br>"+
				"���ֹ��ɽ�������ؤ���"+
				"<li>Demo<br>"+
				"ʸ���䥢�ȥ���礭������"+
				"<li>ShowProfile<br>"+
				"--profile���ץ������������������<br>"+
				"������� / Ŭ�Ѳ�� (�¹Ի��� ms)"+
				"<li>GUI<br>"+
				"����ե��å���ɽ�����ڤ��ؤ���"+
				"</ul>";
			jt.setText(s);
//		} catch (IOException e) {
//			System.err.println(e);
//		}

		JScrollPane scroll = new JScrollPane(jt);
		getContentPane().add("Center", scroll);
		pack();
		setVisible(true);
	}
}
