package test.distribute;

//import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

//TODO ��λ����褦�ˤ���

public class DaemonThreadTest2 {
	public static void main(String args[]) {
		Thread t1 = new Thread(new DaemonHontai2());
		t1.start();
	}
}

class GlobalConstants {
	public static int LMNTAL_DAEMON_PORT = 60000;
}

/**
 * ʪ��Ū�ʷ׻����ζ����ˤ��äơ�LMNtalRuntime���󥹥��󥹤ȥ�⡼�ȥΡ��ɤ��б�ɽ���ݻ����롣
 * @author nakajima
 *
 */
class DaemonHontai2 implements Runnable {

	ServerSocket servSocket = null;
	HashMap nodeTable = new HashMap();

	public DaemonHontai2() {
		try {
			servSocket = new ServerSocket(GlobalConstants.LMNTAL_DAEMON_PORT);
		} catch (Exception e) {
			System.out.println(
				"ERROR in LMNtalDaemon.LMNtalDaemon() " + e.toString());
		}
	}

	public void run() {
		Socket tmpSocket;
		BufferedReader tmpInStream;
		BufferedWriter tmpOutStream;

		while (true) {
			try {
				tmpSocket = servSocket.accept();

				//����stream			
				tmpInStream =
					new BufferedReader(
						new InputStreamReader(tmpSocket.getInputStream()));

				//����stream
				tmpOutStream =
					new BufferedWriter(
						new OutputStreamWriter(tmpSocket.getOutputStream()));

				if (regist(tmpSocket,
					new LMNtalNode(tmpInStream, tmpOutStream))) {
					//��Ͽ������
					Thread t2 =
						new Thread(
							new LMNtalDaemonThread2(tmpInStream, tmpOutStream));
					t2.start();
				} else {
					//��Ͽ���ԡ�����
					tmpInStream.close();
					tmpOutStream.close();
					tmpSocket.close();
				}

			} catch (IOException e) {
				System.out.println(
					"ERROR in DaemonHontai2.run() " + e.toString());
				break;
			}
		}
	}

	boolean regist(Socket socket, LMNtalNode node) {
		if (nodeTable.containsKey(socket)) {
			return false;
		}

		nodeTable.put(socket, node);

		return true;
	}

	boolean disconnect(Socket socket) {
		LMNtalNode node = (LMNtalNode) nodeTable.get(socket);

		try {
			node.tmpInStream.close();
			node.tmpOutStream.close();
			socket.close();

			return true;
		} catch (Exception e) {

		}

		return false;
	}
}

class LMNtalDaemonThread2 implements Runnable {
	BufferedReader in;
	BufferedWriter out;

	public LMNtalDaemonThread2(BufferedReader inTmp, BufferedWriter outTmp) {
		in = inTmp;
		out = outTmp;
	}

	public void run() {
		System.out.println("LMNtalDaemonThread2.run()");

		while (true) {
			try {
				String input = in.readLine();
				//�ƥ����ѡ�
				//String input = new String("msgid \"localhost\" runtimegroupid connect");
				System.out.println("input: " + input);
				if (input == null) {
					break;
				}

//				StringTokenizer tokens = new StringTokenizer(input, " ");
//
//				String currentToken;
//				int msgid;
//				int rgid;
//				String remoteNodeFQDN;
//				StringBuffer command = new StringBuffer("");
//
//				currentToken = tokens.nextToken();
//
//				//��륳�ޥ�ɤ���
//				if (currentToken.equals("RES")) {
//					//RES�ν���
//				} else {
//					//msgid�Ȥߤʤ�
//					msgid = Integer.parseInt(currentToken);
//					remoteNodeFQDN = tokens.nextToken();
//
//					while (tokens.hasMoreTokens()) {
//						command.append(tokens.nextToken());
//						command.append(" ");
//					}
//
//					Communicator.connect(remoteNodeFQDN, command.toString());
//				}

				int msgid;
				int rgid;
				String command;
				String[] tmpString = new String[3];
				
				tmpString = input.split(" ", 3);
				
				if (tmpString[1].equals("RES")){
					//RES�ν���
				} else {
					//msgid�Ȥߤʤ�
					msgid = Integer.parseInt(tmpString[0]);
					
					Communicator.connect(tmpString[1], tmpString[2]);
				}
				

			} catch (IOException e) {
				System.out.println("ERROR:���Υ���åɤˤϽ񤱤ޤ���! " + e.toString());
				break;
			}
		}
	}

}

class LMNtalNode {
	BufferedReader tmpInStream;
	BufferedWriter tmpOutStream;

	LMNtalNode(BufferedReader in, BufferedWriter out) {
		tmpInStream = in;
		tmpOutStream = out;
	}
}

class Communicator {

	static void connect(String remoteNodeFQDN, String command) {
		/* 
		 * ̿����
		 * "banon" runtimegroupid connect
		 */
		System.out.println(
			"now in Communicator: " + remoteNodeFQDN + " " + command);

		String fqdnNoQuote;

		StringTokenizer tokens = new StringTokenizer(remoteNodeFQDN, "\"");
		fqdnNoQuote = tokens.nextToken();
		System.out.println("fqdnNoQuote: " + fqdnNoQuote);

		try {
			Socket socket =
				new Socket(fqdnNoQuote, GlobalConstants.LMNTAL_DAEMON_PORT);

			//����stream
			BufferedWriter out =
				new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));

			out.write(command);
			out.flush();
			out.close();

		} catch (Exception e) {

		}
	}
}