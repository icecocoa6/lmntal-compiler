package daemon;

import java.net.Socket;

import runtime.LocalLMNtalRuntime;
import runtime.LMNtalRuntimeManager;

class SlaveLMNtalRuntimeLauncher {
	//todo ̾���ѹ���������LocalLMNtalRuntime��LMNtalDaemon�δ֤ˤ��ơ�������ۥ�����TCP�̿������ݤ���³���������åȤ��Ĥ���ޤǸ�³���롣
	
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
			if (node.sendWaitRegisterLocal("REMOTE")) {
				//LocalLMNtalRuntime��ư				
				LocalLMNtalRuntime runtime = new LocalLMNtalRuntime();
				node.respondAsOK(callerMsgid);	// node.runtimeid���֤���硢����res�ΰ����ˤ���
				nodeThread.join(); //socket�����Ǥ���ޤ��Ԥ�
				LMNtalRuntimeManager.terminateAllNeighbors();
				LMNtalRuntimeManager.disconnectFromDaemon();
			}
		} catch (Exception e) {
			System.out.println("ERROR in DummyRemoteRuntime.run()" + e.toString());
			e.printStackTrace();
		}
	}
}
