package runtime;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
import java.net.*;
import java.io.*;

//TODO ����

/**
 * ʪ��Ū�ʷ׻����ζ����ˤ��äơ�LMNtalRuntime���󥹥��󥹤ȥ�⡼�ȥΡ��ɤ��б�ɽ���ݻ����롣
 * @author nakajima
 *
 */
public class LMNtalDaemon implements Runnable {
	/*
	 * ��(ά��):    a :- { b }@"banon" 
	 * ��:          a :- connectruntime("banon", B) | {b}@B.
	 *              a :- connectruntimefailure("banon") | fail("banon").
	 * 
	 * 
	 * �Ȥꤢ�����ͤ��Ƥߤ��ץ�ȥ���
	 * 
	 * ���㡧 rgid  �� runtime group id
	 * 
	 * ��Ͽ�ط�
	 * HELO �ġʥΎߧ��ߡˤ��Ϥ褦
	 * READY ��ack
	 * REGISTERLOCAL rgid �ĥ�����ˤ���ޥ�����󥿥������Ͽ
	 * OK, msgid �� ����
	 * FAIL, msgid �� ����
	 * REGISTERREMOTE, rgid �ļ긵����Ͽ���Ƥ���ޥ�����󥿥������������
	 * REGISTERFINISHED, rgid �Ĺ�������Ǥ������Ȥ�ޥ�����󥿥��ब����׻�����daemon������
	 * CONNECTRUNTIME, msgid, "banon"
	 * TERMINATE, rgid 
	 * 
	 * �¹Դط�
	 * COPYRULESET �ĥ롼�륻�åȤ���������
	 * COPYRULE �ĥ롼���������
	 * COPYPROCESSCONTEXT ��$p������
	 * COPYFREELINK
	 * COPYATOM
	 * 
	 *
	 * 
	 */
	 

	/*��n-kato����Υ����ȡ�
	 * - REGIST �� REGISTER ����������(��: 2004-05-18 nakajima)
	 * - REGISTREMOTE �ϡ�runtimegroupid�ʡ�ޥ�����󥿥����(runtime)id�ˤ�����ɬ�פ����롣
	 * - REGISTFINISHED �ϡ��ޥ�����󥿥��ब����׻����ǤϤʤ���REGISTREMOTE��ȯ�Ԥ����׻����������֤���
	 *   �������äơ�runtimegroupid ������˻���ɬ�פ����롣
	 * - TERMINATE runtimegroupid ��ɬ�ס����������鼫ʬ���ΤäƤ������ƤΥ�󥿥����Ʊ����å����������롣
	 */
	 
	static int LMNTAL_DAEMON_PORT = 60000;
	ServerSocket servSocket = null;

	public LMNtalDaemon() {
		try {
			servSocket = new ServerSocket(LMNTAL_DAEMON_PORT);
		} catch (Exception e) {
			System.out.println("ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
		}
	}

	public void run() {
		try {
			while (true) {
				Thread t2 =
					new Thread(new LMNtalDaemonThread(servSocket.accept()));
				t2.start();
			}
		} catch (Exception e) {
			System.out.println("ERROR in LMNtalDaemon.run() " + e.toString());
		}
	}
}

class LMNtalDaemonThread implements Runnable {
	Socket socket;

	public LMNtalDaemonThread(Socket tmpSocket) {
		socket = tmpSocket;
		System.out.println(
			"new LMNtalDaemonThread created using " + socket.toString());
	}

	public void run() {
		try {
			//����stream			
			BufferedReader inStream =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			//����stream
			BufferedWriter outStream =
				new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));

			//1����
			outStream.write("LMNtalDaemon @ " + InetAddress.getLocalHost().toString());
			outStream.write("enter 'quit' to terminate the programme.\n");
			outStream.flush();

			//2���ܰʹߡ�"quit"�ǽ�λ��
			while (true) {
				String input = inStream.readLine();
				if (input.equals("quit")) {
					break;
				}
				System.out.println("input: " + input);

				outStream.write(input);
				outStream.flush();
			}

			//���ǽ���
			inStream.close();
			outStream.close();
			socket.close();
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemonThread.run()!!! " + e.toString());
		}
	}
}
