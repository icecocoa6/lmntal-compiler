/*
 * ������: 2006/03/17
 *
 * TODO �����������줿�ե�����Υƥ�ץ졼�Ȥ��ѹ�����ˤϼ��إ�����:
 * ������ɥ� - ���� - Java - �����ɡ��������� - �����ɡ��ƥ�ץ졼��
 */
package debug;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
