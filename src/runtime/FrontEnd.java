/*
 * ������: 2003/10/22
 */
package runtime;

import java.io.*;
import java.lang.SecurityException;
import java.util.*;

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
	 * 
	 * <pre>
	 * ���ޥ�ɥ饤�����
	 *   �ʤ�                       �� REPL ��ư
	 *   �ե�����̾                 �� �ե��������Ȥ�¹Ԥ��ƽ����
	 *   -e [LMNtal oneliner]       �� [LMNtal oneliner] �ʣ���ʸ�ˤ�¹Ԥ��ƽ����
	 *   -d                         �� �ǥХå��⡼��
	 *   --help                     �� �إ�פ�ɽ��
	 *   -g                         �� GUI ������в��ɽ������
	 * </pre>
	 * 
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
						// �桼����������ץ���󡣽񼰡� -x <name> <value>
						String name  = i+1<args.length ? args[i+1] : "";
						String value = i+2<args.length ? args[i+2] : "";
						Env.extendedOption.put(name, value);
						i+=2;
						break;
					case 'L':
						// ����饤�󥳡��ɤΥ���ѥ������ -classpath ���ץ����ˤʤ���ʬ���ɲä��뤳�Ȥ��Ǥ��롣
						Inline.classPath.add(0, new File(args[++i]));
						break;
					case 'I':
						// LMNtal �饤�֥��θ����ѥ����ɲä��뤳�Ȥ��Ǥ��롣
						compile.Module.libPath.add(new File(args[++i]));
						break;
					case 'c':
						if (args[i].equals("-cgi")) {
							Env.fCGI = true;
						}
						break;
					case 'd':
						if (args[i].matches("-d[0-9]")) {
							Env.debug = args[i].charAt(2) - '0';
						} else {
							Env.debug = Env.DEBUG_DEFAULT;
						}
//						System.out.println("debug level " + Env.debug);
						break;
					case 'v':
						if (args[i].matches("-v[0-9]")) {
							Env.verbose = args[i].charAt(2) - '0';
						} else {
							Env.verbose = Env.VERBOSE_DEFAULT;
						}
//						System.out.println("verbose level " + Env.verbose);
						break;
					case 'g':
						Env.fGUI = true;
						break;
					case 't':
						Env.fTrace = true;
						break;
					case 's':
						if (args[i].matches("-s[0-9]")) {
							Env.shuffle = args[i].charAt(2) - '0';
						} else {
							Env.shuffle = Env.SHUFFLE_DEFAULT;
						}
						System.out.println("shuffle level " + Env.shuffle);
						break;
					case 'e':
						// lmntal -e 'a,(a:-b)'
						// �����Ǽ¹ԤǤ���褦�ˤ��롣like perl
						Env.oneLiner = args[++i];
						break;
					case 'O':
						if (args[i].length() == 2) {
							Env.optimize = 5;
						} else if (args[i].matches("-O[0-9]")) {
							Env.optimize = args[i].charAt(2) - '0';
						} else {
							System.out.println("�����ʥ��ץ����:" + args[i]);
							System.exit(-1);
						}
						break;
					case '-': // ʸ���󥪥ץ����
						if(args[i].equals("--help")){
							System.out.println("usage: FrontEnd [options...] [filenames...]");
						} else if(args[i].equals("--demo")){
							Env.fDEMO = true;
						} else {
							System.out.println("�����ʥ��ץ����:" + args[i]);
							System.exit(-1);
						}
						break;
					default:
						System.out.println("�����ʥ��ץ����:" + args[i]);
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
		
		/// �¹�
		
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
			System.out.println(e.getMessage());
			System.exit(-1);
		} catch(SecurityException e) {
			System.out.println(e.getMessage());
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
			
			compile.structure.Membrane m = lp.parse();
//			if (Env.debug >= Env.DEBUG_SYSDEBUG) {
//				Env.d("");
//				Env.d( "Parse Result: " + m.toStringWithoutBrace() );
//			}
			
			Ruleset rs = RulesetCompiler.compileMembrane(m, unitName);
			Inline.makeCode();
			((InterpretedRuleset)rs).showDetail();
			m.showAllRules();
			
			// �¹�
			MasterLMNtalRuntime rt = new MasterLMNtalRuntime();
			LMNtalRuntimeManager.init();

			Membrane root = rt.getGlobalRoot();
			Env.initGUI(root);
			//root.blockingLock();
			rs.react(root); // TODO ������֤ǻҥ��������ä����ˤɤ��ʤ뤫�ͤ���
			if (Env.gui != null) {
				Env.gui.lmnPanel.getGraphLayout().calc();
				Env.gui.onTrace();
			}
			//root.blockingUnlock();
			((Task)root.getTask()).execAsMasterTask();

			if (!Env.fTrace && Env.verbose > 0) {
				Env.d( "Execution Result:" );
				Env.p( Dumper.dump(rt.getGlobalRoot()) );
			}
			if (Env.gui != null) {
				while(true) Env.gui.onTrace();
			}
			
			LMNtalRuntimeManager.terminateAllNeighbors();
			LMNtalRuntimeManager.disconnectFromDaemon();
			
		} catch (Exception e) {
			Env.e("!! catch !! "+e+"\n"+Env.parray(Arrays.asList(e.getStackTrace()), "\n"));
		}
	}
}
