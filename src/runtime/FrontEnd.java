/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import util.StreamDumper;

import compile.Optimizer;
import compile.RulesetCompiler;
import compile.Translator;
import compile.parser.LMNParser;
import compile.parser.ParseException;

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

//    	//TODO REPL �� LMNtal �ץ�����¹���ξ��ϡ��¹Ԥ���ߤ��ƥץ��ץȤ����褦�ˤ��롣
//		//Ctrl-C �Υϥ�ɥ�
//        Signal.handle(new Signal("INT"), new SignalHandler () {
//            public void handle(Signal sig) {
//            }
//        });

		processOptions(args);
		
		// �¹�

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
	 * ���ޥ�ɥ饤������ν���
	 * @param args ����
	 */
	public static void processOptions(String[] args) {
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
						/// dump      1          indent mem (etc...)
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
						/// -c
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
					case '3':
						/// -3
						/// 3d mode.
						//System.out.println("f3D true");
						Env.fGUI = false;
						Env.f3D = true;
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
						int level = -1;
						if (args[i].length() == 2)
							level = 5;
						else if (args[i].length() == 3)
							level = args[i].charAt(2) - '0';
						
						if (level >= 0 && level <= 9) {
							Optimizer.setLevel(level);
							break;
						} else {
							System.out.println("Invalid option: " + args[i]);
							System.exit(-1);
						}
						break;
					//����Ū��Ŭ�����ץ���� �����ɡ����롼�״ط� sakurai
					case 'Z':
					    if(args[i].length() == 2){
					    	Env.zoptimize = 0;	
					    } else if(args[i].matches("-Z[0-9]")){
					    	Env.zoptimize = args[i].charAt(2) - '0';
					    } else {
					    	System.out.println("�����ʥ��ץ����:" + args[i]);
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
						} else if(args[i].equals("--3d")){
							/// --3d
							/// 3d mode.
							Env.fGUI = false;
							Env.f3D = true;
						} else if(args[i].equals("--remain")){
							/// --remain
							/// Processes remain.
							Env.fREMAIN = true;
						} else if(args[i].equals("--immediate")){
							/// --immediate
							/// ʸ�������ǲ��Ԥ���ȼ¹Ԥ���
							/// �ǥե���ȤǤϲ��ԣ��ĤǼ¹�
							Env.replTerm = "immediate";
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
						} else if (args[i].equals("--keep-temporary-files")) {
							/// --keep-temporary-files
							/// Do not delete the translated Java source.
							Translator.fKeepSource = true;
						} else if (args[i].startsWith("--temporary-dir=")) {
							/// --temporary-dir=<dir>
							/// use <dir> as temporary directory
							Translator.baseDirName = args[i].substring(16);
						} else if (args[i].equals("--interpret")) {
							/// --interpret
							/// Interpret intermediate instruction sequences without translating into Java.
							/// In REPL mode and one-liner, alwas interpret.
							Env.fInterpret = true;
						} else if (args[i].equals("--use-source-library")) {
							/// --use-source-library
							/// Use source library in lmntal_lib/src.
							Env.fUseSourceLibrary = true;
						} else if (args[i].equals("--library")) {
							/// --library
							/// Generate library.
							Env.fLibrary = true;
						} else if (args[i].equals("--pp0")) {
							// ���ꥪ�ץ����
							Env.preProcess0 = true;
						} else if(args[i].equals("--showproxy")){
							// ���ꥪ�ץ���� PROXY��ɽ������
							Env.hideProxy = false;
						} else if(args[i].equals("--optimize-inlining")) {
							/// --optimize-inlining
							/// Inlining tail jump.
							Optimizer.fInlining = true;
						} else if(args[i].equals("--optimize-reuse-atom")) {
							/// --optimize-reuse-atom
							/// Reuse atoms.
							Optimizer.fReuseAtom = true;
						} else if(args[i].equals("--optimize-reuse-mem")) {
							/// --optimize-reuse-mem
							/// Reuse mems.
							Optimizer.fReuseMem = true;
						} else if(args[i].equals("--optimize-loop")) {
							/// --optimize-loop
							/// Use loop instruction. (EXPERIMENT)
							Optimizer.fLoop = true;
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
		//REPL �� one-liner �ǤϾ�˲��¹�
		if (Env.oneLiner != null || Env.argv.isEmpty()) {
			Env.fInterpret = true;
		}
		
		if(Env.fCGI) {
			System.setErr(System.out);
			System.out.println("Content-type: text/html\n");
		}
		
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
		if(Env.preProcess0) src = preProcess0(src);
		try {
			compile.structure.Membrane m;
			Env.clearErrors();
			try {
				LMNParser lp = new LMNParser(src);
				m = lp.parse();
			}	
			catch (ParseException e) {
				Env.p("Compilation Failed");
				return;	
			}
			
			Ruleset rs;
			if (Env.fInterpret) {
				rs = RulesetCompiler.compileMembrane(m, unitName);
				Inline.makeCode();
				if (Env.nErrors > 0) {
					Env.p("Compilation Failed");
					return;
				}
			} else {
				try {
					if (!Translator.init(unitName)) {
						//���顼��å������Ͻ��ϺѤ�
						return;
					}
					rs = RulesetCompiler.compileMembrane(m, unitName);
					if (Env.nErrors > 0) {
						Env.p("Compilation Failed");
						return;
					}
					Translator.genInlineCode();
					Translator.genModules(m);
					if (!Env.fLibrary) {
						Translator.genMain((InterpretedRuleset)rs, m);
					}
					Translator.genJAR();
				} catch (IOException e) {
					Env.e("Failed to write Translated File. " + e.getLocalizedMessage());
					return;
				} finally {
					if (!Translator.fKeepSource)
						Translator.deleteTemporaryFiles();
				}
			}

			((InterpretedRuleset)rs).showDetail();
			m.showAllRules();

			if (Env.fInterpret) {
				run(rs);
			}
//			else {
//				new Translator((InterpretedRuleset)rs).translate(true);
//			}
		} catch (Exception e) {
			e.printStackTrace();
//			Env.e("!! catch !! "+e+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
	}
	/**
	 * Ϳ����줿����ǡ��������롼�륻�åȤ򸵤ˡ���Ϣ�μ¹Ԥ�Ԥ���
	 * @param rs (:-m) �Ȥ����롼�룱�Ĥ�������ʤ�롼�륻�å�
	 */
	public static void run(Ruleset rs) {
		try {
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
			Env.init3D();
			root.rect = new java.awt.geom.Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);

			if(Env.fGUI) Env.gui.lmnPanel.getGraphLayout().setRootMem(root);
			if(Env.f3D) Env.threed.lmnPanel.getGraph3DLayout().setRootMem(root);
//			root.asyncLock();
			rs.react(root);
//			root.asyncUnlock();
//			rt.asyncFlag = false;

			boolean ready = true;
			if (Env.gui != null) {
				Env.gui.lmnPanel.getGraphLayout().calc();
				if (!Env.gui.onTrace())  ready = false;
			}
			/*TODO:3d calc*/
			if (Env.threed != null) {
				Env.threed.lmnPanel.getGraph3DLayout().init();
				if (!Env.threed.onTrace())  ready = false;
			}
			if (ready) {
				((Task)root.getTask()).execAsMasterTask(); //rt.exec();

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
	/**
	 * �ץ�ץ��å�0
	 * 
	 * ���<u> ... :- ...  ==> ��� ... :- unary(���) | ...
	 * 
	 * Ʊ�ͤˡ�
	 *   u -> unary
	 *   g -> ground
	 *   s -> string
	 *   i -> int
	 * 
	 * �㡧
	 *   a(Hah<u>), b(A<g>):-rhs.
	 *  ==>
	 *   a(Hah), b(A):-ground(A), unary(Hah), |rhs.
	 * 
	 * @param r
	 * @return
	 */
	static Reader preProcess0(Reader r) {
		try {
			BufferedReader br = new BufferedReader(r);
			String s;
			StringBuffer sb=new StringBuffer();
			while((s=br.readLine())!=null) {
				sb.append(s);
			}
			s = sb.toString();
			s = s.replaceAll(":\\-([^|.]*)\\.", ":-|$1.");
//			System.out.println(s);
			
			String b=s, a;
			{
				for(a=b;;b=a) {
					a = a.replaceAll("([A-Z][0-9a-zA-Z]*)<u>(.*?)\\:\\-(.*?)\\|", "$1$2:-unary($1), $3|");
					a = a.replaceAll("([A-Z][0-9a-zA-Z]*)<s>(.*?)\\:\\-(.*?)\\|", "$1$2:-string($1), $3|");
					a = a.replaceAll("([A-Z][0-9a-zA-Z]*)<i>(.*?)\\:\\-(.*?)\\|", "$1$2:-int($1), $3|");
					a = a.replaceAll("([A-Z][0-9a-zA-Z]*)<g>(.*?)\\:\\-(.*?)\\|", "$1$2:-ground($1), $3|");
					if(b.equals(a)) break; // "stable"
//					System.out.println(a);
				}
			}
			System.out.println(a);
			return new StringReader(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
// TODO ������֤ǻҥ���������
