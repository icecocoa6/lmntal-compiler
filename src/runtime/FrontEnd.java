/*
 * �쐬��: 2003/10/22
 */
package runtime;

import java.io.EOFException;

import org.gnu.readline.Readline;
import org.gnu.readline.ReadlineLibrary;

/**
 * LMNtal �̃��C��
 * 
 * <pre>
 * TODO for �e���Ȃ����� 
 * �R�}���h���C�������̏���
 * �i�t�@�C�������w�肳��Ă����炻������ߎ��s
 * �@�w�肳��ĂȂ������� runREPL() ���s�j
 * 
 * TODO ���O�� FrontEnd �ł����񂾂낤���B
 *       �āFFrontEnd
 *           �f���� Main
 * </pre>
 * 
 * �쐬��: 2003/10/22
 */
public class FrontEnd {
	/**
	 * LMNtal-REPL �����s����B
	 * 
	 * �C���^���N�e�B�u���[�h�B
	 * LMNtal ���ꂪ�󗝂��镶����� 1 �s���͂����
	 * ��������s�������ʂ̕����񂪕\�������B
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
	 * �S�Ă̎n�܂�
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FrontEnd fe = new FrontEnd();
		/**
		 * TODO �R�}���h���C����������������t�@�C���̒��g�����ߎ��s
		 */
		
		//�w�肪�Ȃ���΂�����Ă�
		fe.runREPL();
	}
}
