/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.*;
import java.lang.SecurityException;
import java.util.*;

import util.StreamDumper;

import compile.*;
import compile.parser.*;

/**
 * LMNtal �Υᥤ��
 * 
 * 
 * ������: 2003/10/22
 */
public class FrontEnd {
	/**
	 * ���ƤλϤޤꡣ
	 * @param args
	 */
	public static void main(String[] args) {
		//�С����������å� by ����
		try {
			String ver = System.getProperty("java.version");
			StringTokenizer tokenizer = new StringTokenizer(ver, ".");
			int major = Integer.parseInt(tokenizer.nextToken());
			int minor = Integer.parseInt(tokenizer.nextToken());
			if (major < 1 || (major == 1 && minor < 4)) {
				System.err.println("use jre 1.4 or higher!!");
				System.exit(-1);
			}
		// ���ޤ������ʤ��ä�����̵�뤹��
		} catch (SecurityException e) {
		} catch (NoSuchElementException e) {
		} catch (NumberFormatException e) {
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				Inline.terminate();
			}
		});
		
		/**
		 * ���ޥ�ɥ饤����������ä���ե��������Ȥ���¹�
		 */
		for(int i = 0; i < args.length;i++){
			// ɬ��length>0, '-'�ʤ饪�ץ����
			// -> ������ "" �ˤ����Ĺ�� 0 �ˤʤ�Τǥ����å����롣
			if(args[i].length()>0 && args[i].charAt(0) == '-'){
				if(args[i].length() < 2){ // '-'�Τߤλ�
					System.out.println("�����ʥ��ץ����:" + args[i]);
					System.exit(-1);
				} else { // ���ץ��������
					switch(args[i].charAt(1)){
					case 'x':
						/// -x <name> <value>
						/// User defined option.
						/// <name>    <value>    description
						/// ===========================================================
						/// screen    max        full screen mode
						/// auto      on         reaction auto proceed mode when GUI on
						if (i + 2 < args.length) {
							String name  = i+1<args.length ? args[i+1] : "";
							String value = i+2<args.length ? args[i+2] : "";
							Env.extendedOption.put(name, value);
						}
						i+=2;
						break;
					case 'L':
						/// -L <path>
						/// Additional path for classpath (inline code compile time)
						Inline.classPath.add(0, new File(args[++i]));
						break;
					case 'I':
						/// -I <path>
						/// Additional path for LMNtal library
						/// The default path is ./lmntal_lib and ../lmntal_lib
						compile.Module.libPath.add(new File(args[++i]));
						break;
					case 'c':
						/// -cgi
						/// CGI mode.  Output the header 'Content-type:text/html'
						if (args[i].equals("-cgi")) {
							Env.fCGI = true;
						}
						break;
					case 'd':
						/// -d[<0-9>]
						/// Debug output level.
						if (args[i].matches("-d[0-9]")) {
							Env.debug = args[i].charAt(2) - '0';
						} else {
							Env.debug = Env.DEBUG_DEFAULT;
						}
//						System.out.println("debug level " + Env.debug);
						break;
					case 'v':
						/// -v[<0-9>]
						/// Verbose output level.
						if (args[i].matches("-v[0-9]")) {
							Env.verbose = args[i].charAt(2) - '0';
						} else {
							Env.verbose = Env.VERBOSE_DEFAULT;
						}
//						System.out.println("verbose level " + Env.verbose);
						break;
					case 'g':
						/// -g
						/// GUI mode. Atoms, membranes and links are drawn graphically.
						/// Click button to proceed reaction. Close the window to quit.
						Env.fGUI = true;
						break;
					case 't':
						/// -t
						/// Trace mode.
						Env.fTrace = true;
						break;
					case 's':
						/// -s[<0-9>]  (-s=-s3)
						/// Shuffle level.  Select a strategy of rule application.
						///   0: use an atom stack for each membrane (LIFO)
						///   1: default (atoms are selected in some arbitrary manner)
						///   2: select atoms and membranes randomly from a membrane
						///   3: select atoms, mems and rules randomly from a membrane
						if (args[i].matches("-s[0-9]")) {
							Env.shuffle = args[i].charAt(2) - '0';
						} else {
							Env.shuffle = Env.SHUFFLE_DEFAULT;
						}
						System.out.println("shuffle level " + Env.shuffle);
						break;
					case 'e':
						/// -e <LMNtal program>
						/// One liner code execution like Perl.
						/// Example: -e 'a,(a:-b)'
						if (++i < args.length)  Env.oneLiner = args[i];
						break;
					case 'O':
						/// -O<0-9>
						/// Optimize level.
						/// Intermediate instruction sequences are optimized.
						if (args[i].length() == 2) {
							Env.optimize = 5;
						} else if (args[i].matches("-O[0-9]")) {
							Env.optimize = args[i].charAt(2) - '0';
						} else {
							System.out.println("Invalid option: " + args[i]);
							System.exit(-1);
						}
						break;
					case '-': // ʸ���󥪥ץ����
						if(args[i].equals("--help")){
							/// --help
							/// Show usage (this).
							System.out.println("usage: java -jar lmntal.jar [options...] [filenames...]");
							// usage: FrontEnd [options...] [filenames...]
							
							// commandline: perl src/help_gen.pl < src/runtime/FrontEnd.java > src/runtime/Help.java
							Help.show();
					        System.exit(-1);
						} else if(args[i].equals("--demo")){
							/// --demo
							/// Demo mode.  Draw atoms and text larger.
							Env.fDEMO = true;
						} else if(args[i].equals("--remain")){
							/// --remain
							/// Processes are remain.
							Env.fREMAIN = true;
						} else if(args[i].equals("--start-daemon")){
							/// --start-daemon
							/// Start LMNtalDaemon
							Env.startDaemon = true;			
						} else if(args[i].matches("--port")){
							/// --port portnumber
							/// Specifies the port number that LMNtalDaemon listens on. The default is 60000. 
							/// Only dynamic and private ports defined by IANA is usable: port 49152 through 65535. 
							
							if (args[i+1].matches("\\d*")) {
								try{ 
									/*
									 * http://www.iana.org/assignments/port-numbers
									 * 
									 * DYNAMIC AND/OR PRIVATE PORTS
									 *  The Dynamic and/or Private Ports are those from 49152 through 65535
									 */
									int portnum = Integer.parseInt(args[i+1]);
									if(portnum < 49152 || portnum > 65535){
										System.out.println("Invalid option: " + args[i] + " " + args[i+1]);
										System.out.println("only port 49152 through 65535 is available");
										System.exit(-1);
									}
									Env.daemonListenPort = portnum;
								} catch (NumberFormatException e){
									//e.printStackTrace();
									System.out.println("Invalid option: " + args[i] + " " + args[i+1]);
									System.out.println("Cannot parse as integer: " + args[i+1]);
									System.exit(-1);
								}
							} else {
								System.out.println("Invalid option: " + args[i] + " " + args[i+1]);
								System.exit(-1);
							}
							
							i++;
						} else if(args[i].equals("--debug-daemon")){
							/// --debug-daemon
							/// dump debug message of LMNtalDaemon
							Env.debugDaemon = Env.DEBUG_DEFAULT;
						} else {
							System.out.println("Invalid option: " + args[i]);
							System.exit(-1);
						}
						break;
					default:
						System.out.println("Invalid option: " + args[i]);
						System.out.println("Use option --help to see a long list of options.");
						System.exit(-1);						
					}
				}
			}else{ // '-'�ʳ��ǻϤޤ��Τ� (�¹ԥե�����̾, argv[0], arg[1], ...) �Ȥߤʤ�
				Env.argv.add(args[i]);
			}
		}
		
		if(Env.fCGI) {
			System.setErr(System.out);
			System.out.println("Content-type: text/html\n");
		}
		
		// �¹�

		//start LMNtalDaemon
		if(Env.startDaemon){
			String classpath = System.getProperty("java.class.path");
			String newCmdLine =
				new String(
						"java -classpath"
						+ " "
						+ classpath
						+ " "
						+"daemon.LMNtalDaemon"
						+ " "
						+ Env.debugDaemon
						+ " "
						+ Env.daemonListenPort
						); 
			
			//System.out.println(newCmdLine);
			try {
				Process daemon = Runtime.getRuntime().exec(newCmdLine);

				//daemon����ư����ޤ��Ԥ�
				//TODO ����LMNtalDaemon����ư���Ƥ����鵯ư���ʤ�
				InputStreamReader daemonStdout =new InputStreamReader(daemon.getInputStream()); 
				for(int i = 0;  i < 10; i++){ //�ͥåȥ�����󥿥ե������������äƤʤ����ϱʵפ�ready()��false�ʤΤ�
					if(daemonStdout.ready()) break;
					try {
						Thread.sleep(100);
						//System.out.println("LMNtalDaemon not yet started...");
						//System.out.println(daemonStdout.ready());
					} catch (InterruptedException e2) {
						//e2.printStackTrace();
					}
				}
				//daemonStdout.close();

				if(Env.debugDaemon > 0){
					Thread dumpErr = new Thread(new StreamDumper("LMNtalDaemon.stderr", daemon.getErrorStream()),"StreamDumper");
					Thread dumpOut = new Thread(new StreamDumper("LMNtalDaemon.stdout", daemon.getInputStream()),"StreamDumper");
					dumpErr.start();
					dumpOut.start();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
		}
		
		if(Env.oneLiner!=null) {
			// ��Լ¹Ԥξ��Ϥ����ͥ��
			REPL.processLine(Env.oneLiner);
			return;
		}
		
		// ����������ʤ饽��������¹ԡ��ʤ��ʤ� REPL��
		if(Env.argv.isEmpty()) {
			REPL.run();
		} else {
			run(Env.argv);
			if(Env.fREMAIN) REPL.run();
		}
	}
	
	/**
	 * Ϳ����줿̾���Υե����뤿���򤯤äĤ����������ˤĤ��ơ���Ϣ�μ¹Ԥ�Ԥ���
	 * 
	 * @param files �������ե�����
	 */
	public static void run(List files) {
		InputStream is = null;
		try{
			for(Iterator i=files.iterator();i.hasNext();) {
				String filename = (String)i.next();
				FileInputStream fis = new FileInputStream(filename);
				if(is==null) is = fis;
				else         is = new SequenceInputStream(is, fis);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch(SecurityException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		// ʣ���Υե�����ΤȤ��ϥե�����̾�����Ĥ˷����ʤ���
		String unitName = files.size()==1 ? (String)files.get(0) : InlineUnit.DEFAULT_UNITNAME;
		run( new BufferedReader(new InputStreamReader(is)), unitName );
	}
	/**
	 * Ϳ����줿�������ˤĤ��ơ���Ϣ�μ¹Ԥ�Ԥ���
	 * @param src Reader ����ɽ���줿������
	 */
	public static void run(Reader src) {
		run(src, InlineUnit.DEFAULT_UNITNAME);
	}
	/**
	 * Ϳ����줿�������ˤĤ��ơ���Ϣ�μ¹Ԥ�Ԥ���
	 * @param src Reader ����ɽ���줿������
	 * @param unitName String �ե�����̾������饤�󥳡��ɤΥ���å���Ϥ���̾���١����Ǵ�������롣
	 */
	public static void run(Reader src, String unitName) {
		try {
			LMNParser lp = new LMNParser(src);
			boolean ready = true;
			
			compile.structure.Membrane m = lp.parse();
//			if (Env.debug >= Env.DEBUG_SYSDEBUG) {
//				Env.d("");
//				Env.d( "Parse Result: " + m.toStringWithoutBrace() );
//			}
			
			Ruleset rs = RulesetCompiler.compileMembrane(m, unitName);
			Inline.makeCode();
			((InterpretedRuleset)rs).showDetail();
			m.showAllRules();
			
			// if (Env.nerrors > 0)  ready = false;
			
			// �¹�
			MasterLMNtalRuntime rt = new MasterLMNtalRuntime();
			LMNtalRuntimeManager.init();

			Membrane root = rt.getGlobalRoot();
			
			if(Env.fREMAIN) {
				if(Env.remainedRuntime!=null) {
					root.moveCellsFrom(Env.remainedRuntime.getGlobalRoot());
					root.rulesets.addAll(Env.remainedRuntime.getGlobalRoot().rulesets);
				}
			}
			
			Env.initGUI();
			root.rect = new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
			
			if(Env.fGUI) Env.gui.lmnPanel.getGraphLayout().setRootMem(root);
			//root.blockingLock();
			rs.react(root); // TODO �ڸ��ڡ۽�����֤ǻҥ��������ä����ˤɤ��ʤ뤫�ͤ��뢪LMNParser�������뤳�Ȥˤ���
			if (Env.gui != null) {
				Env.gui.lmnPanel.getGraphLayout().calc();
				if (!Env.gui.onTrace())  ready = false;
			}
			if (ready) {
				//root.blockingUnlock();
				((Task)root.getTask()).execAsMasterTask();

				if (!Env.fTrace && Env.verbose > 0) {
					Env.d( "Execution Result:" );
					Env.p( Dumper.dump(rt.getGlobalRoot()) );
				}
				if (Env.gui != null) {
					while(Env.gui.running) Env.gui.onTrace();
				}
			}
			if(Env.fREMAIN) {
				Env.remainedRuntime = rt;
			}
			
			if (Env.gui != null)  Env.gui = null;
//			LMNtalRuntimeManager.terminateAll();
			LMNtalRuntimeManager.terminateAllThreaded();
			//if(true) System.out.println("FrontEnd: terminateAll() finished!");
			LMNtalRuntimeManager.disconnectFromDaemon();
			
		} catch (Exception e) {
			e.printStackTrace();
//			Env.e("!! catch !! "+e+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
	}
}
// TODO ������֤ǻҥ���������
