/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.EOFException;

import org.gnu.readline.Readline;
import org.gnu.readline.ReadlineLibrary;

/**
 * LMNtal �Υᥤ��
 * 
 * <pre>
 * TODO for �ư��ʤ����礵�� 
 * ���ޥ�ɥ饤������ν���
 * �ʥե�����̾�����ꤵ��Ƥ����餽�����¹�
 * �����ꤵ��Ƥʤ��ä��� runREPL() �¹ԡ�
 * 
 * TODO ̾���� FrontEnd �Ǥ������������
 *       �ơ�FrontEnd
 *           ��ľ�� Main
 * </pre>
 * 
 * ������: 2003/10/22
 */
public class FrontEnd {
	/**
	 * LMNtal-REPL ��¹Ԥ��롣
	 * 
	 * ���󥿥饯�ƥ��֥⡼�ɡ�
	 * LMNtal ���줬��������ʸ����� 1 �����Ϥ����
	 * �����¹Ԥ�����̤�ʸ����ɽ������롣
	 *
	 */
	public void runREPL() {
		String line;
		try {
			//Readline.load(ReadlineLibrary.Getline);
			//Readline.load(ReadlineLibrary.GnuReadline);
			Readline.load(ReadlineLibrary.PureJava);
		}
		catch (UnsatisfiedLinkError ignore_me) {
			System.err.println("couldn't load readline lib. Using simple stdin.");
		}

		Readline.initReadline("LMNtal");

		Runtime.getRuntime()                       // if your version supports
		  .addShutdownHook(new Thread() {          // addShutdownHook (since 1.3)
			 public void run() {
			   Readline.cleanup();
			 }
			});

		System.out.println("        LMNtal version 0.01");
		System.out.println("");
		System.out.println("[TIPS] Type q to quit.");
		System.out.println("");
		while (true) {
			try {
				line = Readline.readline("LMNtal> ");
				if (line == null) {
					//System.out.println("no input");
				} else if(line.equals("q")) {
					break;
				} else {
					processLine(line);
				}
			} 
			catch (EOFException e) {
				break;
			} 
			catch (Exception e) {
				//doSomething();
			}
		}
		Readline.cleanup();  // see note above about addShutdownHook
	}
	
	/**
	 * Process a line
	 * 
	 * @param line     LMNtal statement (one liner)
	 */
	private void processLine(String line) {
		System.out.println(line+"  =>  {a, b, {c}}, ({b, $p}:-{c, $p})");
	}
	
	/**
	 * ���ƤλϤޤ�
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FrontEnd fe = new FrontEnd();
		/**
		 * TODO ���ޥ�ɥ饤����������ä���ե��������Ȥ���¹�
		 */
		
		//���꤬�ʤ���Ф����Ƥ�
		fe.runREPL();
	}
}
