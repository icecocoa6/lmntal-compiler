package daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import runtime.LocalLMNtalRuntime;
//import runtime.LMNtalRuntimeManager;

class SlaveLMNtalRuntimeLauncher {
	//TODO ̾���ѹ���������LocalLMNtalRuntime��LMNtalDaemon�δ֤ˤ��ơ�������ۥ�����TCP�̿������ݤ���³���������åȤ��Ĥ���ޤǸ�³���롣
	
	static boolean DEBUG = true;
	
	static LocalLMNtalRuntime runtime;
	static String rgid;
	
	public static void main(String[] args){
		try {
			rgid = args[0];
			String callerMsgid = args[1];

			Socket socket = new Socket("localhost", 60000);

			BufferedWriter out =
				new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader in =
				new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			//REGSITERLOCAL rgid
			String command = new String("registerlocal " + rgid + "\n");

			if(DEBUG)System.out.println("DummyRemoteRuntime.run(): now omitting: " + command);

			out.write(command);
			out.flush();

			String input;

			//�ǽ��1��
			input = in.readLine();
				
			if(input.equalsIgnoreCase("ok")){
				//connect��ȯ�Ԥ��������Ф���res msgid ok���֤�
				command = "res " + callerMsgid + " ok\n";
				out.write(command);
				out.flush();
				
				//LocalLMNtalRuntime��ư
				runtime = new LocalLMNtalRuntime();
		
				//��å������Ԥ������롣
				//processMessage(in, out);
				//LMNtalRuntimeManager.terminateAll();
			} else {
				//connect��ȯ�Ԥ��������Ф���res msgid fail���֤�
				command = "res " + callerMsgid + " fail\n";
				out.write(command);
				out.flush();
			}

		} catch (Exception e) {
			System.out.println("ERROR in DummyRemoteRuntime.run()" + e.toString());
			e.printStackTrace();
		}
	}
	
	static void processMessage(BufferedReader in, BufferedWriter out){
		String input = "";
		String inputParsed[] = new String[3];
		
		while(true){ 
			 //TODO ��³�������ͥ��������ڤ�ޤǤޤ��ĤŤ���(���Ƚ��򤹤�)
			
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
				System.out.println("Error in SlaveLMNtalRuntimeLauncher.processMessage(): cannot parse input");
				continue;
			}
		}
	}
}
