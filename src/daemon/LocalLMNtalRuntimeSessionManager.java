package daemon;

import java.net.Socket;

import runtime.LocalLMNtalRuntime;
import runtime.LMNtalRuntimeManager;
/*
 * LocalLMNtalRuntime��LMNtalDaemon�δ֤ˤ��ơ�������ۥ�����TCP�̿������ݤ���³���������åȤ��Ĥ���ޤǸ�³���롣
 * 
 * @author nakajima, n-kato
 */
class LocalLMNtalRuntimeSessionManager {
	//todo ̾���ѹ���
	//���� 2004-08-16 nakajima
	
	static boolean DEBUG = true;
	
	public static void main(String[] args){
		try {
			String callerMsgid = args[0];
			String rgid = args[1];

			Socket socket = new Socket("localhost", LMNtalDaemon.DEFAULT_PORT);
			LMNtalRuntimeMessageProcessor node = new LMNtalRuntimeMessageProcessor(socket,rgid);
			LMNtalRuntimeManager.daemon = node;
			
			Thread nodeThread = new Thread(node);
			nodeThread.start();
			if (node.sendWaitRegisterLocal("remote")) {
				//LocalLMNtalRuntime��ư				
				LocalLMNtalRuntime runtime = new LocalLMNtalRuntime();
				node.respondAsOK(callerMsgid);	// node.runtimeid���֤���硢����res�ΰ����ˤ���
				//socket�����Ǥ���ޤ��Ԥ�
				nodeThread.join();
				//LMNtalRuntimeManager.terminateAll();
			}
		} catch (Exception e) {
			System.out.println("ERROR in DummyRemoteRuntime.run()" + e.toString());
			e.printStackTrace();
		}
	}
	/**@deprecated*/
/*	static void processMessage(BufferedReader in, BufferedWriter out){
		String input = "";
		String inputParsed[] = new String[3];
		
		while(true){ 
			 //todo ��³�������ͥ��������ڤ�ޤǤޤ��ĤŤ���(���Ƚ��򤹤�)
			
			try {
				input = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				continue; //todo ����Ǥ����Τ���
			}
			
			inputParsed = input.split(" ",3);
			
			if(inputParsed[0].equalsIgnoreCase("connect")){
				//CONNECT msgid
				//CONNECT���褿��ok���֤�
				try {
					out.write("res " + inputParsed[1] + " ok\n"); //todo inputParsed[1]�������ä������к�
				} catch (IOException e1) {
					e1.printStackTrace();
					System.out.println("Error in CONNECT");
				}
				
			//} else if(inputParsed[0].equalsIgnoreCase("")){
				
			} else {
				//cannot parse
				System.out.println("Error in LocalLMNtalRuntimeSessionManager.processMessage(): cannot parse input");
				continue;
			}
		}
	}*/
}
