package daemon;

import java.net.Socket;

import runtime.Env;
import runtime.LocalLMNtalRuntime;
import runtime.LMNtalRuntimeManager;

class SlaveLMNtalRuntimeLauncher {
	//todo ̾���ѹ���������LocalLMNtalRuntime��LMNtalDaemon�δ֤ˤ��ơ�������ۥ�����TCP�̿������ݤ���³���������åȤ��Ĥ���ޤǸ�³���롣
	
	public static void main(String[] args){
		try {
			//java -classpath classpath daemon.SlaveLMNtalRuntimeLauncher msgid rgid Env.debugDaemon 
			if(args.length < 2){
				System.out.println("Invalid option");
				System.exit(-1);
			}

			String callerMsgid = args[0];
			String rgid = args[1];
			try{
				int debug = Integer.parseInt(args[2]);
				Env.debugDaemon = debug;
			} catch (Exception e){
				System.out.println("Cannot parse as integer");
				e.printStackTrace();
				System.exit(-1);
			}

			Socket socket = new Socket("localhost", Env.daemonListenPort);
			LMNtalRuntimeMessageProcessor node = new LMNtalRuntimeMessageProcessor(socket,rgid);
			LMNtalRuntimeManager.daemon = node;
			
			Thread nodeThread = new Thread(node, "LMNtalRuntimeMessageProcessor");
			nodeThread.start();
			if (node.sendWaitRegisterLocal("SLAVE")) {
				//LocalLMNtalRuntime��ư				
				LocalLMNtalRuntime runtime = new LocalLMNtalRuntime();
				node.respondAsOK(callerMsgid);	// node.runtimeid���֤���硢����res�ΰ����ˤ���
				nodeThread.join(); //socket�����Ǥ���ޤ��Ԥ�
				//LMNtalRuntimeManager.terminateAll(); //TODO (nakajima)�׻�����λ����������Ƚ�λ����褦�ˤ���
				//LMNtalRuntimeManager.disconnectFromDaemon();
			}
		} catch (Exception e) {
			System.out.println("ERROR in SlaveLMNtalRuntimeLauncher.run()" + e.toString());
			e.printStackTrace();
		}
	}
}
