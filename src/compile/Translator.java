package compile;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import runtime.Env;
import runtime.Functor;
import runtime.Inline;
import runtime.InlineUnit;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.IntegerFunctor;
import runtime.InterpretedRuleset;
import runtime.ObjectFunctor;
import runtime.Rule;
import runtime.StringFunctor;

/**
 * ���̿���󤫤�Java�ؤ��Ѵ���Ԥ����饹��
 * 1 �ĤΥ롼�륻�åȤ� 1 �ĤΥ��饹���Ѵ����롣
 * TODO �ü�ե��󥯥��ν���
 * @author mizuno
 */
public class Translator {
	private String className;
	private File outputFile;
	private BufferedWriter writer;
	private InterpretedRuleset ruleset;
	/**���Ϥ��� InstructionList �ν��硣��ʣ���ɤ���������Ѥ��롣*/
	private HashSet instLists = new HashSet();
	/**�������٤� InstructionList*/
	private ArrayList instListsToTranslate = new ArrayList();
	/**���� Ruleset ������Ѥ��Ƥ��� Functor �ˤĤ��ƤΡ�Functor -> �ѿ�̾*/
	private HashMap funcVarMap = new HashMap();

	/** ����Ѱ���ǥ��쥯�ȥ�*/
	private static File baseDir;
	/** �⥸�塼�륯�饹����������ǥ��쥯�ȥ� */
	private static File moduleDir;
	/** �Ѵ������ե�����򤪤��ǥ��쥯�ȥ� */
	private static File dir;
	/** LMNtal�������ե�����̾ */
	private static String sourceName;

	//////////////////////////////////////////////////////////////////
	// static �᥽�å�
	
	/**
	 * ���ꤵ�줿�롼�륻�åȤ��б����륯�饹̾��������롣
	 * @param ruleset �롼�륻�å�
	 * @return �Ѵ���Υ��饹̾
	 */
	public static String getClassName(InterpretedRuleset ruleset) {
		return "Ruleset" + ruleset.getId();
	}
	
	/**
	 * Translator ���������롣
	 * Ʊ��Υ��������Ф����Ϣ�� Translator �����˸ƤӽФ�ɬ�פ����롣
	 * @param unitName LMNtal�������ե�����̾
	 */
	public static void init(String unitName) throws IOException {
		if (unitName.equals(InlineUnit.DEFAULT_UNITNAME)) {
			sourceName = "a";
		} else {
			sourceName = new File(unitName).getName();
			if (sourceName.startsWith(".")) {
				sourceName = sourceName.substring(1);
			}
			int pos = sourceName.indexOf('.');
			if (pos >= 0) {
				sourceName = sourceName.substring(0, pos);
			}
		}
		//����ѥǥ��쥯�ȥ����
		String s = System.getProperty("java.io.tmpdir") + "lmn_translate";
		int i = 1;
		while (true) {
			baseDir = new File(s + i).getCanonicalFile();
			if (baseDir.mkdir()) {
				break;
			}
			i++;
		}
		moduleDir = new File(baseDir, "translated");
		moduleDir.mkdir();
		if (Env.fLibrary) {
			dir = new File(moduleDir, sourceName);
			dir.mkdir();
		} else {
			dir = moduleDir;
		}
	}
	/**
	 * �ᥤ��ؿ����������롣
	 * @param initialRuleset ����ǡ��������롼�륻�å�
	 * @throws IOException IO���顼��ȯ���������
	 */
	public static void genMain(InterpretedRuleset initialRuleset) throws IOException {
		if (Env.fLibrary) return;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(baseDir, "Main.java")));
		writer.write("public class Main {\n");
		writer.write("	public static void main(String[] args) {\n");
		writer.write("		runtime.FrontEnd.run(translated." + getClassName(initialRuleset) + ".getInstance());\n"); //todo �����ν���
		writer.write("	}\n");
		writer.write("}\n");
		writer.close();
	}
	/**
	 * �⥸�塼�륯�饹���������롣
	 * @throws IOException IO���顼��ȯ���������
	 */
	public static void genModules() throws IOException {
		Iterator moduleIterator = Module.memNameTable.keySet().iterator();
		while (moduleIterator.hasNext()) {
			String moduleName = (String)moduleIterator.next();
			if (Env.fLibrary && !moduleName.equals(sourceName)) {
				continue;
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(moduleDir, "Module_" + moduleName + ".java")));
			writer.write("package translated;\n");
			if (Env.fLibrary) {
				writer.write("import translated." + sourceName + ".*;\n");
			}
			writer.write("import runtime.Ruleset;\n");
			writer.write("public class Module_" + moduleName + "{\n");
			writer.write("	private static Ruleset[] rulesets = {");
			boolean first = true;
			Iterator rulesetIterator = ((compile.structure.Membrane)Module.memNameTable.get(moduleName)).rulesets.iterator();
			while (rulesetIterator.hasNext()) {
				if (!first) writer.write(", ");
				writer.write(getClassName((InterpretedRuleset)rulesetIterator.next()) + ".getInstance()");
			}
			writer.write("};\n");
			writer.write("	public static Ruleset[] getRulesets() {\n");
			writer.write("		return rulesets;\n");
			writer.write("	}\n");
			writer.write("}\n");
			writer.close();
		}
	}

	/**
	 * �Ѵ����� Java �������ե�����򥳥�ѥ��뤷��JAR�ե�������������롣
	 * @throws IOException IO���顼��ȯ���������
	 */
	public static void genJAR() throws IOException {
		//����ѥ���
		if (!Env.fLibrary) {
			if (!compile(new File(baseDir, "Main.java"))) {
				return;
			}
		}
		Iterator moduleIterator = Module.memNameTable.keySet().iterator();
		while (moduleIterator.hasNext()) {
			String moduleName = (String)moduleIterator.next();
			if (Env.fLibrary && !moduleName.equals(sourceName)) {
				continue;
			}
			if (!compile(new File(moduleDir, "Module_" + moduleName + ".java"))) {
				return;
			}
		}

		//JAR������
		Manifest mf = new Manifest();
		Attributes att = mf.getMainAttributes();
		att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(sourceName + ".jar")), mf);
		putToJar(out, "", baseDir);
		out.close();
		
		//����ե�����κ��
		if (!delete(baseDir)) {
			Env.warning("failed to delete temprary files");
		}
	}
	/**
	 * �Ѵ������ե�����򥳥�ѥ��뤹�롣
	 * @param file ����ѥ��뤹��ե�����
	 * @throws IOException IO���顼��ȯ���������
	 * @return ����ѥ���������������true
	 */
	private static boolean compile(File file) throws IOException {
		String classpath = System.getProperty("java.class.path");
		//TODO ���饹�ѥ��˶��򤬴ޤޤ�Ƥ�����ؤ��н�
		String command = "javac -cp " + classpath + " -sourcepath \"" + baseDir + "\" \"" + file.getCanonicalPath() + "\"";
		Env.d(command);
		Process javac = Runtime.getRuntime().exec(command);
		javac.getInputStream().close();
		javac.getErrorStream().close();
		try {
			if (javac.waitFor() != 0) {
				Env.e("Failed to compile the translated files."); 
				return false;
			}
			return true;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * �ǥ��쥯�ȥ���Υե����� Jar �ե�����˽��Ϥ��롣�ǥ��쥯�ȥ�ϺƵ�Ū�˽�������롣
	 * @param out ���Ϥ��� JarOutputStream
	 * @param relativeDir "/"�Ƕ��ڤ�줿�����Хѥ���Jar �ؤν��Ϥ����Ѥ��롣
	 * @param directory ��������ǥ��쥯�ȥ��ɽ�� File ���󥹥��󥹡�
	 * @throws IOException IO���顼��ȯ���������
	 */
	private static void putToJar(JarOutputStream out, String relativeDir, File directory) throws IOException {
		String[] files = directory.list();
		byte[] buf = new byte[8192];
		for (int i = 0; i < files.length; i++) {
			File f = new File(directory, files[i]);
			if (f.isDirectory()) {
				out.putNextEntry(new JarEntry(relativeDir + files[i] + "/"));
				putToJar(out, relativeDir + files[i] + "/", f);
			} else {
				out.putNextEntry(new JarEntry(relativeDir + files[i]));
				FileInputStream in = new FileInputStream(f);
				int size;
				while ((size = in.read(buf)) != -1) {
					out.write(buf, 0, size);
				}
				in.close();
			}
		}
	}
	/**
	 * ���ꤷ���ǥ��쥯�ȥ�������롣
	 * �ҥǥ��쥯�ȥꡦ�ե�����򤹤٤ƺ�����Ƥ��顢���Υǥ��쥯�ȥ�������롣
	 * @param directory �������ǥ��쥯�ȥ�
	 * @return ����������������� true
	 */
	private static boolean delete(File directory) {
		String[] files = directory.list();
		for (int i = 0; i < files.length; i++) {
			File f = new File(directory, files[i]);
			if (f.isDirectory()) {
				delete(f);
			} else {
				f.delete();
			}
		}
		return directory.delete();
	}
	
	////////////////////////////////////////////////////////////////////////
	// ���󥹥��󥹥᥽�å�
	
	/**
	 * ���ꤵ�줿 InterpretedRuleset �� Java ���Ѵ����뤿��Υ��󥹥��󥹤��������롣
	 * @param ruleset �Ѵ�����롼�륻�å�
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	public Translator(InterpretedRuleset ruleset) throws IOException{
		className = getClassName(ruleset);
		outputFile = new File(dir, className + ".java");
		writer = new BufferedWriter(new FileWriter(outputFile));
		this.ruleset = ruleset;
	}
	/**
	 * Java����������Ϥ��롣
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	public void translate() throws IOException {
		if (Env.fLibrary) {
			writer.write("package translated." + sourceName + ";\n");
		} else {
			writer.write("package translated;\n");
		}
		writer.write("import runtime.*;\n");
		writer.write("import java.util.*;\n");
		writer.write("import java.io.*;\n");
		writer.write("import daemon.IDConverter;\n");
		writer.write("import module.*;\n");
		writer.write("\n");
		{
			Iterator il0 = Inline.inlineSet.values().iterator();
			while(il0.hasNext()) {
				runtime.InlineUnit iu = (runtime.InlineUnit)il0.next();
				Iterator il1 = iu.defs.iterator();
				while(il1.hasNext()) {
					writer.write((String)il1.next());
					writer.write("\n");
				}
			}
		}
		writer.write("\n");
		writer.write("public class " + className + " extends Ruleset {\n");
		writer.write("	private static final " + className + " theInstance = new " + className + "();\n");
		writer.write("	private " + className + "() {}\n");
		writer.write("	public static " + className + " getInstance() {\n");
		writer.write("		return theInstance;\n");
		writer.write("	}\n");
		writer.write("	private int id = " + ruleset.getId() + ";\n");
		writer.write("	private String globalRulesetID;\n");
		writer.write("	public String getGlobalRulesetID() {\n");
		writer.write("		if (globalRulesetID == null) {\n");
		String libname = (Env.fLibrary ? sourceName : "");
		writer.write("			globalRulesetID = Env.theRuntime.getRuntimeID() + \":" + libname + "\" + id;\n");
		writer.write("			IDConverter.registerRuleset(globalRulesetID, this);\n");
		writer.write("		}\n");
		writer.write("		return globalRulesetID;\n");
		writer.write("	}\n");
		writer.write("	public String toString() {\n");
		writer.write("		return \"@" + libname + "\" + id;\n");
		writer.write("	}\n");

		//���ư�ƥ���
		writer.write("	public boolean react(Membrane mem, Atom atom) {\n");
		writer.write("		boolean result = false;\n");
		Iterator it = ruleset.rules.iterator();
		while (it.hasNext()) {
			Rule rule = (Rule) it.next();
			writer.write("		if (exec" + rule.atomMatchLabel.label + "(mem, atom)) {\n");
			//writer.write("			result = true;\n");
			writer.write("			return true;\n");
			//writer.write("			if (!mem.isCurrent()) return true;\n");
			writer.write("		}\n");
		}
		writer.write("		return result;\n");
		writer.write("	}\n");
		//���ȥ��ư�ƥ���
		writer.write("	public boolean react(Membrane mem) {\n");
		writer.write("		boolean result = false;\n");
		it = ruleset.rules.iterator();
		while (it.hasNext()) {
			Rule rule = (Rule) it.next();
			writer.write("		if (exec" + rule.memMatchLabel.label + "(mem)) {\n");
			//writer.write("			result = true;\n");
			writer.write("			return true;\n");
			//writer.write("			if (!mem.isCurrent()) return true;\n");
			writer.write("		}\n");
		}
		writer.write("		return result;\n");
		writer.write("	}\n");

		//InstructionList ��᥽�åɤ��Ѵ�
		it = ruleset.rules.iterator();
		while (it.hasNext()) {
			Rule rule = (Rule)it.next();
			add(rule.atomMatchLabel);
			add(rule.memMatchLabel);
		}
		while (instListsToTranslate.size() > 0) {
			InstructionList instList = (InstructionList)instListsToTranslate.remove(instListsToTranslate.size() - 1);
			translate(instList);
		}

		//Functor ����������� new ����Τ��ɤ����ᡢ���饹�ѿ��ˤ��롣
		//������ˡ���� Ruleset ��˺����Τǡ�Functors ���饹�ߤ�����ʪ���ä������褤���⤷��ʤ���
		it = funcVarMap.keySet().iterator();
		while (it.hasNext()) {
			Functor func = (Functor)it.next();
			writer.write("	private static final Functor " + funcVarMap.get(func));
			if (func instanceof StringFunctor) {
				writer.write(" = new StringFunctor(\"" + escapeString((String)func.getValue()) + "\");\n");
			} else if (func instanceof ObjectFunctor) {
				throw new RuntimeException("ObjectFunctor is not supported");
			} else if (func instanceof IntegerFunctor) {
				writer.write(" = new IntegerFunctor(" + ((IntegerFunctor)func).getValue() + ");\n");
			} else {
				String path = "null";
				if (func.getPath() != null) {
					path = "\"" + escapeString(func.getPath()) + "\"";
				}
				writer.write(" = new Functor(\"" + escapeString(func.getName()) + "\", " + func.getArity() + ", " + path + ");\n");
			}
		}
		
		writer.write("}\n");
		writer.close();
	}

	/**
	 * �Ѵ����٤� InstructionList ���ɲä��롣
	 * Ʊ�����󥹥��󥹤��Ф���ʣ����ƤӽФ������ϡ������ܰʹߤϲ��⤷�ʤ���
	 * @param instList �ɲä��� InstructionList
	 */
	private void add(InstructionList instList) {
		if (instLists.contains(instList)) {
			return;
		}
		instLists.add(instList);
		instListsToTranslate.add(instList);
	}
	/**
	 * ʸ�����ƥ���Ѥ˥��������׽����򤹤롣
	 * @param data ��������ʸ����
	 * @return ���������פ���ʸ����
	 */
	private String escapeString(String data) {
		data = data.replaceAll("\r", "\\\\r");
		data = data.replaceAll("\n", "\\\\n");
		data = data.replaceAll("\\\\", "\\\\\\\\"); // \ -> \\
		data = data.replaceAll("\"", "\\\\\"");		// " -> \"
		return data;
	}

	/**
	 * ���ꤵ�줿 InstructionList ��Java�����ɤ��Ѵ����롣
	 * @param instList �Ѵ�����InstructionList
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	private void translate(InstructionList instList) throws IOException {
		writer.write("	public boolean exec" + instList.label + "(");
		Instruction spec = (Instruction)instList.insts.get(0);
		if (spec.getKind() != Instruction.SPEC) {
			throw new RuntimeException("first instructio is not SPEC but " + spec);
		}
		int formals = spec.getIntArg1();
		int locals = spec.getIntArg2();
		if (formals > 0) {
			writer.write("Object var0");
			for (int i = 1; i < formals; i++) {
				writer.write(", Object var" + i);
			}
		}
		writer.write(") {\n");
		
		for (int i = formals; i < locals; i++) {
			writer.write("		Object var" + i + " = null;\n");
		}

		//�ʲ����ѿ��ϡ��Ѵ�������������Ǽ�ͳ�����ѤǤ��롣
		//���������ѿ�̾�ξ��ͤ��򤱤뤿�ᡢ�ǽ��1���������������Ѥ��뤳�Ȥˤ��Ƥ��롣
		//���Ѥ�����ϡ�ɬ���ͤ��������Ƥ���Ȥ����ȡ�
		//�ޤ���translate�᥽�åɤκƵ��ƤӽФ��򤹤�ȡ��ѿ����ͤ��񤭴������Ƥ��뤳�Ȥ�����Τ�
		//�Ƶ��ƤӽФ��θ�ˤϡʺ������������ˡ����Ѥ��ƤϤ����ʤ���
		writer.write("		Atom atom;\n");
		writer.write("		Functor func;\n");
		writer.write("		Link link;\n");
		writer.write("		AbstractMembrane mem;\n");
		writer.write("		int x, y;\n");
		writer.write("		double u, v;\n");
		writer.write("		int isground_ret;");
		writer.write("		boolean eqground_ret;");

		writer.write("		boolean ret = false;\n");
		writer.write(instList.label + ":\n");
		writer.write("		{\n");
		Iterator it = instList.insts.iterator();
		translate(it, "			", 1, locals, instList.label);
		writer.write("		}\n");
		writer.write("		return ret;\n");
//		if (!translate(it, "			", 1, locals)) {
//			writer.write("		return false;\n");
//		}
		
		writer.write("	}\n");
	}
	/**
	 * ���ꤵ�줿 Iterator �ˤ�ä�������̿����� Java �����ɤ��Ѵ����롣
	 * @param it �Ѵ�����̿����� Iterator
	 * @param tabs ���ϻ������Ѥ��륤��ǥ�ȡ��̾�� N �ĤΥ���ʸ������ꤹ�롣
	 * @param iteratorNo ���Ϥ��륳������Ǽ������Ѥ��� Iterator ���ֹ档���������ѿ��ν�ʣ���ɤ������ɬ�ס�
	 * @param breakLabel �������� break ����֥��å��Υ�٥�
	 * @return return ʸ����Ϥ��ƽ�λ�������ˤ� true������ѥ��륨�顼���ɤ����ᡢtrue ���֤�������ľ���"}"�ʳ��Υ����ɤ���Ϥ��ƤϤʤ�ʤ���
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	private void translate(Iterator it, String tabs, int iteratorNo, int varnum, String breakLabel) throws IOException {
		while (it.hasNext()) {
			Functor func;
			InstructionList label; 
			Instruction inst = (Instruction)it.next();

			String a = inst.toString();
			int pos_nl = a.indexOf('\r');
			int pos2 = a.indexOf('\n');
			if (pos_nl == -1 || (pos2 >= 0 && pos2 < pos_nl)) {
				pos_nl = pos2;
			}
			if (pos_nl >= 0) {
				int pos_b = a.indexOf('[');
				if (pos_b > pos_nl) {
					a = a.substring(0, pos_nl) + "...";
				} else {
					a = a.substring(0, pos_b) + "[ ... ]";
				}
			}
			writer.write("// " + a + "\n");

			switch (inst.getKind()) {
				//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====��������====
				case Instruction.DEREF : //[-dstatom, srcatom, srcpos, dstpos]
					writer.write(tabs + "link = ((Atom)var" + inst.getIntArg2() + ").getArg(" + inst.getIntArg3() + ");\n");
					writer.write(tabs + "if (!(link.getPos() != " + inst.getIntArg4() + ")) {\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = link.getAtom();\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.DEREFATOM : // [-dstatom, srcatom, srcpos]
					writer.write(tabs + "link = ((Atom)var" + inst.getIntArg2() + ").getArg(" + inst.getIntArg3() + ");\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = link.getAtom();\n");
					break; //n-kato
				case Instruction.DEREFLINK : //[-dstatom, srclink, dstpos]
					writer.write(tabs + "link = (Link)var" + inst.getIntArg2() + ";\n");
					writer.write(tabs + "if (!(link.getPos() != " + inst.getIntArg3() + ")) {\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = link.getAtom();\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //mizuno
				case Instruction.FINDATOM : // [-dstatom, srcmem, funcref]
					writer.write(tabs + "func = " + getFuncVarName((Functor)inst.getArg3()) + ";\n");
					writer.write(tabs + "Iterator it" + iteratorNo + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").atomIteratorOfFunctor(func);\n");
					writer.write(tabs + "while (it" + iteratorNo + ".hasNext()) {\n");
					writer.write(tabs + "	atom = (Atom) it" + iteratorNo + ".next();\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = atom;\n");
					translate(it, tabs + "\t", iteratorNo + 1, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break;
					//====���ȥ�˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====
					//====��˴ط�������Ϥ�����ܥ�����̿�� ====��������====
				case Instruction.LOCKMEM :
				case Instruction.LOCALLOCKMEM :
					// lockmem [-dstmem, freelinkatom]
					writer.write(tabs + "mem = ((Atom)var" + inst.getIntArg2() + ").getMem();\n");
					writer.write(tabs + "if (mem.lock()) {\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = mem;\n");
					translate(it, tabs + "\t", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "	((AbstractMembrane)var" + inst.getIntArg1() + ").unlock();\n"); //���Ԥ�����ϡ�resetvars̿���¹Ԥ�����Ϥʤ�
					writer.write(tabs + "}\n");
					break;
				case Instruction.ANYMEM :
				case Instruction.LOCALANYMEM : // anymem [-dstmem, srcmem] 
					writer.write(tabs + "Iterator it" + iteratorNo + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").memIterator();\n");
					writer.write(tabs + "while (it" + iteratorNo + ".hasNext()) {\n");
					writer.write(tabs + "	mem = (AbstractMembrane) it" + iteratorNo + ".next();\n");
					writer.write(tabs + "	if (mem.lock()) {\n");
					writer.write(tabs + "		var" + inst.getIntArg1() + " = mem;\n");
					translate(it, tabs + "		", iteratorNo + 1, varnum, breakLabel);
					writer.write(tabs + "		((AbstractMembrane)var" + inst.getIntArg1() + ").unlock();\n");
					writer.write(tabs + "	}\n");
					writer.write(tabs + "}\n");
					break;
				case Instruction.LOCK :
				case Instruction.LOCALLOCK : //[srcmem] 
					writer.write(tabs + "mem = ((AbstractMembrane)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "if (mem.lock()) {\n");
					translate(it, tabs + "\t", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "	((AbstractMembrane)var" + inst.getIntArg1() + ").unlock();\n");
					writer.write(tabs + "}\n");
					break;
				case Instruction.GETMEM : //[-dstmem, srcatom]
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((Atom)var" + inst.getIntArg2() + ").getMem();\n");
					break; //n-kato
				case Instruction.GETPARENT : //[-dstmem, srcmem]
					writer.write(tabs + "mem = ((AbstractMembrane)var" + inst.getIntArg2() + ").getParent();\n");
					writer.write(tabs + "if (!(mem == null)) {\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = mem;\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
					//====��˴ط�������Ϥ�����ܥ�����̿��====�����ޤ�====
					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.TESTMEM : //[dstmem, srcatom]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ") != ((Atom)var" + inst.getIntArg2() + ").getMem())) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NORULES : //[srcmem] 
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ").hasRules())) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NFREELINKS : //[srcmem, count]
					writer.write(tabs + "mem = ((AbstractMembrane)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "if (!(mem.getAtomCountOfFunctor(Functor.INSIDE_PROXY) != " + inst.getIntArg2() + ")) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break;
				case Instruction.NATOMS : //[srcmem, count]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ").getAtomCount() != " + inst.getIntArg2() + ")) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NATOMSINDIRECT : //[srcmem, countfunc]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ").getAtomCount() != ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue())) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //kudo 2004-12-08
				case Instruction.NMEMS : //[srcmem, count]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ").getMemCount() != " + inst.getIntArg2() + ")) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.EQMEM : //[mem1, mem2]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ") != ((AbstractMembrane)var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NEQMEM : //[mem1, mem2]
					writer.write(tabs + "if (!(((AbstractMembrane)var" + inst.getIntArg1() + ") == ((AbstractMembrane)var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.STABLE : //[srcmem] 
					writer.write(tabs + "if (!(!((AbstractMembrane)var" + inst.getIntArg1() + ").isStable())) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
					//====��˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====
					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====��������====
				case Instruction.FUNC : //[srcatom, funcref]
					writer.write(tabs + "if (!(!(" + getFuncVarName((Functor)inst.getArg2()) + ").equals(((Atom)var" + inst.getIntArg1() + ").getFunctor()))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NOTFUNC : //[srcatom, funcref]
					writer.write(tabs + "if (!((" + getFuncVarName((Functor)inst.getArg2()) + ").equals(((Atom)var" + inst.getIntArg1() + ").getFunctor()))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.EQATOM : //[atom1, atom2]
					writer.write(tabs + "if (!(((Atom)var" + inst.getIntArg1() + ") != ((Atom)var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.NEQATOM : //[atom1, atom2]
					writer.write(tabs + "if (!(((Atom)var" + inst.getIntArg1() + ") == ((Atom)var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.SAMEFUNC: //[atom1, atom2]
					writer.write(tabs + "if (!(!((Atom)var" + inst.getIntArg1() + ").getFunctor().equals(((Atom)var" + inst.getIntArg2() + ").getFunctor()))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
					//====���ȥ�˴ط�������Ϥ��ʤ����ܥ�����̿��====�����ޤ�====
					//====�ե��󥯥��˴ط�����̿��====��������====
				case Instruction.DEREFFUNC : //[-dstfunc, srcatom, srcpos]
					writer.write(tabs + "var" + inst.getIntArg1() + " =  ((Atom)var" + inst.getIntArg2() + ").getArg(" + inst.getIntArg3() + ").getAtom().getFunctor();\n");
					break; //nakajima 2003-12-21, n-kato
				case Instruction.GETFUNC : //[-func, atom]
					writer.write(tabs + "var" + inst.getIntArg1() + " =  ((Atom)var" + inst.getIntArg2() + ").getFunctor();\n");
					break; //nakajima 2003-12-21, n-kato
				case Instruction.LOADFUNC : //[-func, funcref]
					writer.write(tabs + "var" + inst.getIntArg1() + " =  " + getFuncVarName((Functor)inst.getArg2()) + ";\n");
					break;//nakajima 2003-12-21, n-kato
				case Instruction.EQFUNC : //[func1, func2]
					writer.write(tabs + "if (!(!var" + inst.getIntArg1() + ".equals(var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //nakajima, n-kato
				case Instruction.NEQFUNC : //[func1, func2]
					writer.write(tabs + "if (!(var" + inst.getIntArg1() + ".equals(var" + inst.getIntArg2() + "))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //nakajima, n-kato
					//====�ե��󥯥��˴ط�����̿��====�����ޤ�====
					//====���ȥ��������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEATOM :
				case Instruction.LOCALREMOVEATOM : //[srcatom, srcmem, funcref]
					writer.write(tabs + "atom = ((Atom)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "atom.getMem().removeAtom(atom);\n");
					break; //n-kato
				case Instruction.NEWATOM :
				case Instruction.LOCALNEWATOM : //[-dstatom, srcmem, funcref]
					writer.write(tabs + "func = " + getFuncVarName((Functor)inst.getArg3()) + ";\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").newAtom(func);\n");
					break; //n-kato
				case Instruction.NEWATOMINDIRECT :
				case Instruction.LOCALNEWATOMINDIRECT : //[-dstatom, srcmem, func]
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").newAtom((Functor)(var" + inst.getIntArg3() + "));\n");
					break; //nakajima 2003-12-27, 2004-01-03, n-kato
				case Instruction.ENQUEUEATOM :
				case Instruction.LOCALENQUEUEATOM : //[srcatom]
					writer.write(tabs + "atom = ((Atom)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "atom.getMem().enqueueAtom(atom);\n");
					break; //n-kato
				case Instruction.DEQUEUEATOM : //[srcatom]
					writer.write(tabs + "atom = ((Atom)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "atom.dequeue();\n");
					break; //n-kato
				case Instruction.FREEATOM : //[srcatom]
					break; //n-kato
				case Instruction.ALTERFUNC :
				case Instruction.LOCALALTERFUNC : //[atom, funcref]
					writer.write(tabs + "atom = ((Atom)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "atom.getMem().alterAtomFunctor(atom," + getFuncVarName((Functor)inst.getArg2()) + ");\n");
					break; //n-kato
				case Instruction.ALTERFUNCINDIRECT :
				case Instruction.LOCALALTERFUNCINDIRECT : //[atom, func]
					writer.write(tabs + "atom = ((Atom)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "atom.getMem().alterAtomFunctor(atom,(Functor)(var" + inst.getIntArg2() + "));\n");
					break; //nakajima 2003-12-27, 2004-01-03, n-kato
					//====���ȥ��������ܥܥǥ�̿��====�����ޤ�====
					//====���ȥ�����뷿�դ���ĥ��̿��====��������====
				case Instruction.ALLOCATOM : //[-dstatom, funcref]
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, " + getFuncVarName((Functor)inst.getArg2()) + ");\n");
					break; //nakajima 2003-12-27, n-kato
				case Instruction.ALLOCATOMINDIRECT : //[-dstatom, func]
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, (Functor)(var" + inst.getIntArg2() + "));\n");
					break; //nakajima 2003-12-27, 2004-01-03, n-kato
				case Instruction.COPYATOM :
				case Instruction.LOCALCOPYATOM : //[-dstatom, mem, srcatom]
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").newAtom(((Atom)var" + inst.getIntArg3() + ").getFunctor());\n");
					break; //nakajima, n-kato
					//case Instruction.ADDATOM:
				case Instruction.LOCALADDATOM : //[dstmem, atom]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").addAtom(((Atom)var" + inst.getIntArg2() + "));\n");
					break; //nakajima 2003-12-27, n-kato
					//====���ȥ�����뷿�դ���ĥ��̿��====�����ޤ�====
					//====���������ܥܥǥ�̿��====��������====
				case Instruction.REMOVEMEM :
				case Instruction.LOCALREMOVEMEM : //[srcmem, parentmem]
					writer.write(tabs + "mem = ((AbstractMembrane)var" + inst.getIntArg1() + ");\n");
					writer.write(tabs + "mem.getParent().removeMem(mem);\n");
					break; //n-kato
				case Instruction.NEWMEM: //[-dstmem, srcmem]
					writer.write(tabs + "mem = ((AbstractMembrane)var" + inst.getIntArg2() + ").newMem();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = mem;\n");
					break; //n-kato
				case Instruction.LOCALNEWMEM : //[-dstmem, srcmem]
					writer.write(tabs + "mem = ((Membrane)((AbstractMembrane)var" + inst.getIntArg2() + ")).newLocalMembrane();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = mem;\n");
					break; //n-kato
				case Instruction.ALLOCMEM: //[-dstmem]
					writer.write(tabs + "mem = ((Task)((AbstractMembrane)var0).getTask()).createFreeMembrane();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = mem;\n");
					break; //n-kato
				case Instruction.NEWROOT : //[-dstmem, srcmem, nodeatom]
					writer.write(tabs + "String nodedesc = ((Atom)var" + inst.getIntArg3() + ").getFunctor().getName();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").newRoot(nodedesc);\n");
					break; //n-kato 2004-09-17
				case Instruction.MOVECELLS : //[dstmem, srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").moveCellsFrom(((AbstractMembrane)var" + inst.getIntArg2() + "));\n");
					break; //nakajima 2004-01-04, n-kato
				case Instruction.ENQUEUEALLATOMS : //[srcmem]
					break;
				case Instruction.FREEMEM : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").free();\n");
					break; //mizuno 2004-10-12, n-kato
				case Instruction.ADDMEM :
				case Instruction.LOCALADDMEM : //[dstmem, srcmem]
					writer.write(tabs + "var" + inst.getIntArg2() + " = ((AbstractMembrane)var" + inst.getIntArg2() + ").moveTo(((AbstractMembrane)var" + inst.getIntArg1() + "));\n");
					break; //nakajima 2004-01-04, n-kato, n-kato 2004-11-10
				case Instruction.ENQUEUEMEM:
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").activate();\n");
					//mems[inst.getIntArg1()].enqueueAllAtoms();
					break;
				case Instruction.UNLOCKMEM :
				case Instruction.LOCALUNLOCKMEM : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").forceUnlock();\n");
					break; //n-kato
				case Instruction.LOCALSETMEMNAME: //[dstmem, name]
				case Instruction.SETMEMNAME: //[dstmem, name]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").setName(\"" + escapeString((String)inst.getArg2()) + "\");\n");
					break; //n-kato
					//====���������ܥܥǥ�̿��====�����ޤ�====
					//====��󥯤˴ط�������Ϥ��륬����̿��====��������====
				case Instruction.GETLINK : //[-link, atom, pos]
					writer.write(tabs + "link = ((Atom)var" + inst.getIntArg2() + ").getArg(" + inst.getIntArg3() + ");\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = link;\n");
					break; //n-kato
				case Instruction.ALLOCLINK : //[-link, atom, pos]
					writer.write(tabs + "link = new Link(((Atom)var" + inst.getIntArg2() + "), " + inst.getIntArg3() + ");\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = link;\n");
					break; //n-kato
					//====��󥯤˴ط�������Ϥ��륬����̿��====�����ޤ�====
					//====��󥯤�����ܥǥ�̿��====��������====
				case Instruction.NEWLINK:		 //[atom1, pos1, atom2, pos2, mem1]
				case Instruction.LOCALNEWLINK:	 //[atom1, pos1, atom2, pos2 (,mem1)]
					writer.write(tabs + "((Atom)var" + inst.getIntArg1() + ").getMem().newLink(\n");
					writer.write(tabs + "	((Atom)var" + inst.getIntArg1() + "), " + inst.getIntArg2() + ",\n");
					writer.write(tabs + "	((Atom)var" + inst.getIntArg3() + "), " + inst.getIntArg4() + " );\n");
					break; //n-kato
				case Instruction.RELINK:		 //[atom1, pos1, atom2, pos2, mem]
				case Instruction.LOCALRELINK:	 //[atom1, pos1, atom2, pos2 (,mem)]
					writer.write(tabs + "((Atom)var" + inst.getIntArg1() + ").getMem().relinkAtomArgs(\n");
					writer.write(tabs + "	((Atom)var" + inst.getIntArg1() + "), " + inst.getIntArg2() + ",\n");
					writer.write(tabs + "	((Atom)var" + inst.getIntArg3() + "), " + inst.getIntArg4() + " );\n");
					break; //n-kato
				case Instruction.UNIFY:		//[atom1, pos1, atom2, pos2, mem]
				case Instruction.LOCALUNIFY:	//[atom1, pos1, atom2, pos2 (,mem)]
					// mem = mems[0]; // �ΤΥ�����
					// mem = (AbstractMembrane)inst.getArg5(); // �����Υ�����
					writer.write(tabs + "mem = ((Atom)var" + inst.getIntArg1() + ").getArg(" + inst.getIntArg2() + ")\n");
					writer.write(tabs + "		.getAtom().getMem(); // ���ѥ�����\n");
					writer.write(tabs + "if (mem != null) {\n");
					writer.write(tabs + "	mem.unifyAtomArgs(\n");
					writer.write(tabs + "		((Atom)var" + inst.getIntArg1() + "), " + inst.getIntArg2() + ",\n");
					writer.write(tabs + "		((Atom)var" + inst.getIntArg3() + "), " + inst.getIntArg4() + " );\n");
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.INHERITLINK:		 //[atom1, pos1, link2, mem]
				case Instruction.LOCALINHERITLINK:	 //[atom1, pos1, link2 (,mem)]
					writer.write(tabs + "((Atom)var" + inst.getIntArg1() + ").getMem().inheritLink(\n");
					writer.write(tabs + "	((Atom)var" + inst.getIntArg1() + "), " + inst.getIntArg2() + ",\n");
					writer.write(tabs + "	(Link)var" + inst.getIntArg3() + " );\n");
					break; //n-kato
				case Instruction.UNIFYLINKS:		//[link1, link2, mem]
				case Instruction.LOCALUNIFYLINKS:	//[link1, link2 (,mem)]
					// mem = (AbstractMembrane)inst.getArg3(); // �����Υ�����
					writer.write(tabs + "mem = ((Link)var" + inst.getIntArg1() + ").getAtom().getMem(); // ���ѥ�����\n");
					writer.write(tabs + "if (mem != null) {\n");
					writer.write(tabs + "	mem.unifyLinkBuddies(\n");
					writer.write(tabs + "		((Link)var" + inst.getIntArg1() + "),\n");
					writer.write(tabs + "		((Link)var" + inst.getIntArg2() + "));\n");
					writer.write(tabs + "}\n");
					break; //n-kato
					//====��󥯤�����ܥǥ�̿��====�����ޤ�====
					//====��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿��====��������====
				case Instruction.REMOVEPROXIES : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").removeProxies();\n");
					break; //nakajima 2004-01-04, n-kato
				case Instruction.REMOVETOPLEVELPROXIES : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").removeToplevelProxies();\n");
					break; //nakajima 2004-01-04, n-kato
				case Instruction.INSERTPROXIES : //[parentmem,childmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").insertProxies(((AbstractMembrane)var" + inst.getIntArg2() + "));\n");
					break;  //nakajima 2004-01-04, n-kato
				case Instruction.REMOVETEMPORARYPROXIES : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").removeTemporaryProxies();\n");
					break; //nakajima 2004-01-04, n-kato
					//====��ͳ��󥯴������ȥ༫ư�����Τ���Υܥǥ�̿��====�����ޤ�====
					//====�롼�������ܥǥ�̿��====��������====
//���Ǽ�ư����
//				case Instruction.LOADRULESET:
//				case Instruction.LOCALLOADRULESET: //[dstmem, ruleset]
//					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset((Ruleset)inst.getArg2() );\n");
//					break; //n-kato
				case Instruction.COPYRULES:
				case Instruction.LOCALCOPYRULES:   //[dstmem, srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").copyRulesFrom(((AbstractMembrane)var" + inst.getIntArg2() + "));\n");
					break; //n-kato
				case Instruction.CLEARRULES:
				case Instruction.LOCALCLEARRULES:  //[dstmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").clearRules();\n");
					break; //n-kato
//���Ǽ�ư����
//				case Instruction.LOADMODULE: //[dstmem, module_name]
//					// �⥸�塼����ľ°�Υ롼�륻�åȤ������ɤ߹���
//					writer.write(tabs + "compile.structure.Membrane m = (compile.structure.Membrane)compile.Module.memNameTable.get(\"" + escapeString((String)inst.getArg2()) + "\");\n");
//					writer.write(tabs + "if(m==null) {\n");
//					writer.write(tabs + "	Env.e(\"Undefined module " + escapeString((String)inst.getArg2()) + "\");\n");
//					writer.write(tabs + "} else {\n");
//					writer.write(tabs + "	Iterator i = m.rulesets.iterator();\n");
//					writer.write(tabs + "	while (i.hasNext()) {\n");
//					writer.write(tabs + "		((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset((Ruleset)i.next() );\n");
//					writer.write(tabs + "	}\n");
//					writer.write(tabs + "}\n");
//					break;
					//====�롼�������ܥǥ�̿��====�����ޤ�====
					//====���դ��Ǥʤ��ץ�����ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====��������====
				case Instruction.RECURSIVELOCK : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").recursiveLock();\n");
					break; //n-kato
				case Instruction.RECURSIVEUNLOCK : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").recursiveUnlock();\n");
					break;//nakajima 2004-01-04, n-kato
				case Instruction.COPYCELLS : //[-dstmap, -dstmem, srcmem]
					// <strike>��ͳ��󥯤�����ʤ���ʤ��λ���ȤΥ�󥯤�OK�ˤΤ�</strike>
					writer.write(tabs + "var" + inst.getIntArg1() + " =  ((AbstractMembrane)var" + inst.getIntArg2() + ").copyCellsFrom(((AbstractMembrane)var" + inst.getIntArg3() + "));\n");
					break; //kudo 2004-09-29
				case Instruction.DROPMEM : //[srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").drop();\n");
					break; //kudo 2004-09-29
				case Instruction.LOOKUPLINK : //[-dstlink, srcmap, srclink]
					writer.write(tabs + "HashMap srcmap = (HashMap)var" + inst.getIntArg2() + ";\n");
					writer.write(tabs + "Link srclink = (Link)var" + inst.getIntArg3() + ";\n");
					writer.write(tabs + "Atom la = (Atom) srcmap.get(srclink.getAtom());\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Link(la, srclink.getPos());\n");
					break; //kudo 2004-10-10
//̤�б����ѿ��ϡ�����˻����ʤ���Фʤ�ʤ��ä��餷����
//				case Instruction.INSERTCONNECTORS : //[-dstset,linklist,mem]
//					writer.write(tabs + "func = " + getFuncVarName(new Functor("=",2) + ";\n");
//					writer.write(tabs + "linklist=(List)var" + inst.getIntArg2() + ";\n");
//					writer.write(tabs + "insset=new HashSet();\n");
//					writer.write(tabs + "mem=((AbstractMembrane)var" + inst.getIntArg3() + ");\n");
//					writer.write(tabs + "for(int i=0;i<linklist.size();i++)\n");
//					writer.write(tabs + "	for(int j=i+1;j<linklist.size();j++){\n");
//					writer.write(tabs + "		Link a=(Link)var((Integerlinklist.get(i)).intValue());\n");
//					writer.write(tabs + "		Link b=(Link)var((Integerlinklist.get(j)).intValue());\n");
//					writer.write(tabs + "		if(a==b.getBuddy()){\n");
//					writer.write(tabs + "			Atom eq=srcmem.newAtom(FUNC_UNIFY);\n");
//					writer.write(tabs + "			srcmem.unifyLinkBuddies(a,new Link(eq,0));\n");
//					writer.write(tabs + "			srcmem.unifyLinkBuddies(b,new Link(eq,1));\n");
//					writer.write(tabs + "			insset.add(eq);\n");
//					writer.write(tabs + "		}\n");
//					writer.write(tabs + "	}\n");
//					writer.write(tabs + "var" + inst.getIntArg1() + " = insset;\n");
//					break; //kudo 2004-12-29
//				case Instruction.DELETECONNECTORS : //[srcset,srcmap,srcmem]
//					writer.write(tabs + "Set delset = (Set)var" + inst.getIntArg1() + ";\n");
//					writer.write(tabs + "Map delmap = (Map)var" + inst.getIntArg2() + ";\n");
//					writer.write(tabs + "srcmem = ((AbstractMembrane)var" + inst.getIntArg3() + ");\n");
//					writer.write(tabs + "Iterator it" + iteratorNo + " = delset.iterator();\n");
//					writer.write(tabs + "while(it" + iteratorNo + ".hasNext()){\n");
//					writer.write(tabs + "	Atom orig=(Atom)it" + iteratorNo + ".next();\n");
//					writer.write(tabs + "	Atom copy=(Atom)delmap.get(orig);//new Integer(orig.id));\n");
//					writer.write(tabs + "	srcmem.unifyLinkBuddies(copy.getArg(0), copy.getArg(1));\n");
////						copy.args[0].getAtom().args[copy.args[0].getPos()]=copy.args[1];
////						copy.args[1].getAtom().args[copy.args[1].getPos()]=copy.args[0];
//					writer.write(tabs + "	srcmem.removeAtom(copy);\n");
//					writer.write(tabs + "}\n");
//					break; //kudo 2004-12-29
					//====���դ��Ǥʤ��ץ�����ʸ̮�򥳥ԡ��ޤ����Ѵ����뤿���̿��====�����ޤ�====
					//====����̿��====��������====
				case Instruction.COMMIT :
					// �ȥ졼������
					break;//
//������̤�����������ϲ������Ǽ�ư����
//				case Instruction.REACT :
//					writer.write(tabs + "Rule rule = (Rule) inst.getArg1();\n");
//					writer.write(tabs + "List bodyInsts = (List) rule.body;\n");
//					writer.write(tabs + "Instruction spec = (Instruction) bodyInsts.get(0);\n");
//					writer.write(tabs + "int formals = spec.getIntArg1();\n");
//					writer.write(tabs + "int locals  = spec.getIntArg2();\n");
//// // ArrayIndexOutOfBoundsException ���Ǥ��Τǰ��Ū���ѹ�
//// if (locals < 10) locals = 10;
//					writer.write(tabs + "InterpretiveReactor ir = new InterpretiveReactor(locals);\n");
//					writer.write(tabs + "ir.reloadVars(this, locals, (List)inst.getArg2(),\n");
//					writer.write(tabs + "	(List)inst.getArg3(), (List)inst.getArg4());\n");
//					writer.write(tabs + "if (ir.interpret(bodyInsts, 0)) return true;\n");
//					writer.write(tabs + "if (Env.debug == 9) Env.p(\"info: body execution failed\");\n");
//				case Instruction.JUMP: {
//					writer.write(tabs + "InstructionList label = (InstructionList) inst.getArg1();\n");
//					writer.write(tabs + "List bodyInsts = (List) label.insts;\n");
//					writer.write(tabs + "Instruction spec = (Instruction) bodyInsts.get(0);\n");
//					writer.write(tabs + "int formals = spec.getIntArg1();\n");
//					writer.write(tabs + "int locals  = spec.getIntArg2();					\n");
//					writer.write(tabs + "InterpretiveReactor ir = new InterpretiveReactor(locals);\n");
//					writer.write(tabs + "ir.reloadVars(this, locals, (List)inst.getArg2(),\n");
//					writer.write(tabs + "	(List)inst.getArg3(), (List)inst.getArg4());\n");
//					writer.write(tabs + "if (ir.interpret(bodyInsts, 0)) return true;\n");
//					writer.write(tabs + "}\n");
//				case Instruction.RESETVARS :
//					writer.write(tabs + "reloadVars(this, vars.size(), (List)inst.getArg1(),\n");
//					writer.write(tabs + "		(List)inst.getArg2(), (List)inst.getArg3());\n");
//					break;
//				case Instruction.CHANGEVARS :
//					writer.write(tabs + "changeVars(this, (List)inst.getArg1(),\n");
//					writer.write(tabs + "		(List)inst.getArg2(), (List)inst.getArg3());\n");
//					break; //n-kato
				case Instruction.PROCEED:
//					writer.write(tabs + "return true; //n-kato\n");
					writer.write(tabs + "ret = true;\n");
					writer.write(tabs + "break " + breakLabel + ";\n");
					return;// true;
//				case Instruction.SPEC://[formals,locals]
//					writer.write(tabs + "extendVector(" + inst.getIntArg2() + ");\n");
//					break;//n-kato
//				case Instruction.BRANCH :
//					writer.write(tabs + "List subinsts;\n");
//					writer.write(tabs + "subinsts = ((InstructionList)inst.getArg1()).insts;\n");
//**					if (interpret(subinsts, 0))
//**						return true;
//					break; //nakajima, n-kato
//				case Instruction.LOOP :
//					writer.write(tabs + "subinsts = (List) ((List) inst.getArg1()).get(0); // reverted by n-kato: remove \".get(0)\" by mizuno\n");
//					writer.write(tabs + "while (interpret(subinsts, 0)) {\n");
//					writer.write(tabs + "}\n");
//					break; //nakajima, n-kato
//				case Instruction.RUN :
//					writer.write(tabs + "subinsts = (List) ((List) inst.getArg1()).get(0);\n");
//					writer.write(tabs + "interpret(subinsts, 0);\n");
//					break; //nakajima
//				case Instruction.NOT :
//					writer.write(tabs + "subinsts = ((InstructionList)inst.getArg1()).insts;\n");
//					writer.write(tabs + "if (!(interpret(subinsts, 0))) {\n");
//					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
//					writer.write(tabs + "}\n");
//					break; //n-kato
					//====����̿��====�����ޤ�====
					//====���դ��ץ�����ʸ̮�򰷤�������ɲ�̿��====��������====
				case Instruction.EQGROUND : //[link1,link2]
					writer.write(tabs + "eqground_ret = ((Link)var" + inst.getIntArg1() + ").eqGround(((Link)var" + inst.getIntArg2() + "),new HashMap());\n");
					writer.write(tabs + "if (!(!eqground_ret)) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //kudo 2004-12-03
				case Instruction.COPYGROUND : //[-dstlink, srclink, dstmem]
					writer.write(tabs + "var" + inst.getIntArg1() + " = ((AbstractMembrane)var" + inst.getIntArg3() + ").copyGroundFrom(((Link)var" + inst.getIntArg2() + "),new HashMap());\n");
					break; //kudo 2004-12-03
				case Instruction.REMOVEGROUND : //[srclink,srcmem]
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg2() + ").removeGround(((Link)var" + inst.getIntArg1() + "),new HashSet());\n");
					break; //kudo 2004-12-08
				case Instruction.FREEGROUND : //[srclink]
					break; //kudo 2004-12-08
					//====���դ��ץ�����ʸ̮�򰷤�������ɲ�̿��====�����ޤ�====
					//====�������Τ���Υ�����̿��====��������====
				case Instruction.ISGROUND : //[-natomsfunc,srclink,srcset]
					writer.write(tabs + "isground_ret = ((Link)var" + inst.getIntArg2() + ").isGround(new HashSet(),((Set)var" + inst.getIntArg3() + "));\n");
					writer.write(tabs + "if (!(isground_ret == -1)) {\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = new IntegerFunctor(isground_ret);\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //kudo 2004-12-03
				case Instruction.ISUNARY: // [atom]
					writer.write(tabs + "func = ((Atom)var" + inst.getIntArg1() + ").getFunctor();\n");
					// �ޤ���Ķ������󥯤� unary ���ɤ�����Ƚ�ǤǤ��ʤ���OUTSIDE_PROXY �򸫤Ƥ�
					// DEREF �⡩
					// (n-kato)
					// ���٤ƻ��ͤǤ����Ȥ������������Ͽ���ˤ��뤫�⤷��ʤ��櫓�Ǥ�����
					// ����ο���ˤ��륢�ȥ��Ĵ�٤뤳�Ȥϵ�����Ƥ��ޤ���
					// (hara) ���㤽�������Ȥ��ϡּ��ԡפȤ������ȤǤ����Ǥ����ͤ�
					// (n-kato) �Ϥ������Ԥ��Ʋ����������ʤߤ�$in,$out��arity��2�ʤΤǼ���2�ԤϾ�ά���ޤ�����
					//if(f.equals(Functor.OUTSIDE_PROXY)) return false;
					//if(f.equals(Functor.INSIDE_PROXY)) return false;
					writer.write(tabs + "if (!(func.getArity() != 1)) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
//				case Instruction.ISUNARYFUNC: // [func]
//					break;
				case Instruction.ISINT : //[atom]
					writer.write(tabs + "if (!(!(((Atom)var" + inst.getIntArg1() + ").getFunctor() instanceof IntegerFunctor))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.ISFLOAT : //[atom]
					writer.write(tabs + "if (!(!(((Atom)var" + inst.getIntArg1() + ").getFunctor() instanceof FloatingFunctor))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.ISSTRING : //[atom] // todo StringFunctor���Ѥ����CONNECTRUNTIME���
					writer.write(tabs + "if (((Atom)var" + inst.getIntArg1() + ").getFunctor() instanceof ObjectFunctor &&\n");
					writer.write(tabs + "    ((ObjectFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).getObject() instanceof String) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.ISINTFUNC : //[func]
					writer.write(tabs + "if (!(!(var" + inst.getIntArg1() + " instanceof IntegerFunctor))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
//				case Instruction.ISFLOATFUNC : //[func]
//					break;
//				case Instruction.ISSTRINGFUNC : //[func]
//					break;
				case Instruction.GETCLASS: //[-stringatom, atom]
					writer.write(tabs + "if (!(!(((Atom)var" + inst.getIntArg2() + ").getFunctor() instanceof ObjectFunctor))) {\n");
					writer.write(tabs + "	{\n");
					writer.write(tabs + "		Object obj = ((ObjectFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).getObject();\n");
					writer.write(tabs + "		var" + inst.getIntArg1() + " = new Atom(null, new StringFunctor( obj.getClass().toString().substring(6) ));\n");
					writer.write(tabs + "	}\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
					//====�������Τ���Υ�����̿��====�����ޤ�====
					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====��������====
//̤����
				case Instruction.INLINE : //[atom, inlineref]
					writer.write(tabs + "do{ Atom me = (Atom)var" + inst.getIntArg1() + ";\n");
					writer.write(tabs + "  mem = (AbstractMembrane)var0;\n");
					writer.write(tabs + Inline.getCode(inst.getIntArg1(), (String)inst.getArg2(), inst.getIntArg3()));
					writer.write(tabs + "}while(false);\n"); // ����饤�󥳡��ɤ� switch ����ˤ�������ǽ񤫤�Ƥ��롣
					
					break;
//					writer.write(tabs + "Inline.callInline( ((Atom)var" + inst.getIntArg1() + "), \"" + escapeString((String)inst.getArg2()) + "\", " + inst.getIntArg3() + " );\n");
//					break; //hara
					//====�Ȥ߹��ߵ�ǽ�˴ؤ���̿��====�����ޤ�====
//ʬ����ǽ��̤����
//					//====ʬ����ĥ�Ѥ�̿��====��������====
//				case Instruction.CONNECTRUNTIME: //[srcatom] // todo StringFunctor���Ѥ����ISSTRING���
//					writer.write(tabs + "func = ((Atom)var" + inst.getIntArg1() + ").getFunctor();\n");
//					writer.write(tabs + "if (!(!(func instanceof ObjectFunctor))) {\n");
//					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
//					writer.write(tabs + "}\n");
//					writer.write(tabs + "if (!(!(((ObjectFunctor)func).getObject() instanceof String))) {\n");
//					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
//					writer.write(tabs + "}\n");
//					writer.write(tabs + "if (func.getName().equals(\"\")) break; // ��ʸ����ξ��ϤĤͤ������Ȥ���\n");
//					writer.write(tabs + "if (!(LMNtalRuntimeManager.connectRuntime(func.getName()) == null)) {\n");
//					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
//					writer.write(tabs + "}\n");
//					break; //n-kato
//				case Instruction.GETRUNTIME: //[-dstatom,srcmem] // todo StringFunctor���Ѥ����ISSTRING���
//					writer.write(tabs + "String hostname = \"\";\n");
//**					if (mems[inst.getIntArg2()].isRoot())
//**						hostname = mems[inst.getIntArg2()].getTask().getMachine().hostname;
//					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new StringFunctor(hostname));\n");
//					break; //n-kato
//					//====ʬ����ĥ�Ѥ�̿��====�����ޤ�====
					//====���ȥॻ�åȤ����뤿���̿��====��������====
				case Instruction.NEWSET : //[-dstset]
					writer.write(tabs + "var" + inst.getIntArg1() + " = new HashSet();\n");
					break; //kudo 2004-12-08
				case Instruction.ADDATOMTOSET : //[srcset,atom]
					writer.write(tabs + "((Set)var" + inst.getIntArg1() + ").add(((Atom)var" + inst.getIntArg2() + "));\n");
					break; //kudo 2004-12-08
					//====���ȥॻ�åȤ����뤿���̿��====�����ޤ�====
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.IADD : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x+y));\n");
					break; //n-kato
				case Instruction.ISUB : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x-y));	\n");
					break; //nakajima 2004-01-05
				case Instruction.IMUL : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x * y));	\n");
					break; //nakajima 2004-01-05
				case Instruction.IDIV : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "if (!(y == 0)) {\n");
					writer.write(tabs + "	func = new IntegerFunctor(x / y);\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = new Atom(null, func);				\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					//if (y == 0) func = new Functor("NaN",1);
					break; //nakajima 2004-01-05, n-kato
				case Instruction.INEG : //[-dstintatom, intatom]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(-x));				\n");
					break;
				case Instruction.IMOD : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "if (!(y == 0)) {\n");
					writer.write(tabs + "	func = new IntegerFunctor(x % y);\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " = new Atom(null, func);						\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					//if (y == 0) func = new Functor("NaN",1);
					break; //nakajima 2004-01-05
				case Instruction.INOT : //[-dstintatom, intatom]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(~x));	\n");
					break; //nakajima 2004-01-21
				case Instruction.IAND : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x & y));	\n");
					break; //nakajima 2004-01-21
				case Instruction.IOR : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x | y));	\n");
					break; //nakajima 2004-01-21
				case Instruction.IXOR : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x ^ y));	\n");
					break; //nakajima 2004-01-21
				case Instruction.ISAL : //[-dstintatom, intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x << y));	\n");
					break; //nakajima 2004-01-21
				case Instruction.ISAR : //[-dstintatom, intatom1, intatom2] 
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x >> y));	\n");
					break; //nakajima 2004-01-21					
				case Instruction.ISHR : //[-dstintatom, intatom1, intatom2] 
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor(x >>> y));	\n");
					break; //nakajima 2004-01-21	
				case Instruction.IADDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x+y);\n");
					break; //n-kato
				case Instruction.ISUBFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x-y);\n");
					break; //nakajima 2003-01-05
				case Instruction.IMULFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x*y);\n");
					break; //nakajima 2003-01-05
				case Instruction.IDIVFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();\n");
					writer.write(tabs + "if (!(y == 0)) {\n");
					writer.write(tabs + "	func = new IntegerFunctor(x / y);\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " =  func;\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					//if (y == 0) func = new Functor("NaN",1);
					break; //nakajima 2003-01-05
				case Instruction.INEGFUNC : //[-dstintfunc, intfunc]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(-x);\n");
					break;
				case Instruction.IMODFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();\n");
					writer.write(tabs + "if (!(y == 0)) {\n");
					writer.write(tabs + "	func = new IntegerFunctor(x % y);\n");
					writer.write(tabs + "	var" + inst.getIntArg1() + " =  func;\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					//if (y == 0) func = new Functor("NaN",1);
					break; //nakajima 2003-01-05
				case Instruction.INOTFUNC : //[-dstintfunc, intfunc]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(~x);\n");
					break; //nakajima 2003-01-21
				case Instruction.IANDFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x & y);\n");
					break; //nakajima 2003-01-21
				case Instruction.IORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x | y);\n");
					break; //nakajima 2003-01-21
				case Instruction.IXORFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x ^ y);\n");
					break; //nakajima 2003-01-21
				case Instruction.ISALFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x << y);\n");
					break; //nakajima 2003-01-21
				case Instruction.ISARFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x >> y);\n");
					break; //nakajima 2003-01-21
				case Instruction.ISHRFUNC : //[-dstintfunc, intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg3() + ").intValue();	\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new IntegerFunctor(x >>> y);\n");
					break; //nakajima 2003-01-21
					//====�����Ѥ��Ȥ߹��ߥܥǥ�̿��====�����ޤ�====
					//====�����Ѥ��Ȥ߹��ߥ�����̿��====��������====
				case Instruction.ILT : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x < y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.ILE : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x <= y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.IGT : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x > y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.IGE : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x >= y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.IEQ : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x == y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.INE : //[intatom1, intatom2]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();	\n");
					writer.write(tabs + "if (!(!(x != y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.ILTFUNC : //[intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg1() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "if (!(!(x < y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.ILEFUNC : //[intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg1() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "if (!(!(x <= y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.IGTFUNC : //[intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg1() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "if (!(!(x > y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.IGEFUNC : //[intfunc1, intfunc2]
					writer.write(tabs + "x = ((IntegerFunctor)var" + inst.getIntArg1() + ").intValue();\n");
					writer.write(tabs + "y = ((IntegerFunctor)var" + inst.getIntArg2() + ").intValue();\n");
					writer.write(tabs + "if (!(!(x >= y))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				// IEQFUNC INEFUNC FEQFUNC FNEFUNC FNEFUNC... INT2FLOATFUNC...
					//====�����Ѥ��Ȥ߹��ߥ�����̿��====�����ޤ�====
					//====��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿��====��������====
				case Instruction.FADD : //[-dstfloatatom, floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new FloatingFunctor(u+v));\n");
					break; //n-kato
				case Instruction.FSUB : //[-dstfloatatom, floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new FloatingFunctor(u-v));	\n");
					break; // n-kato
				case Instruction.FMUL : //[-dstfloatatom, floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new FloatingFunctor(u * v));	\n");
					break; // n-kato
				case Instruction.FDIV : //[-dstfloatatom, floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg3() + ").getFunctor()).floatValue();\n");
					//if (v == 0.0) func = new Functor("NaN",1);
					//else
					writer.write(tabs + "func = new FloatingFunctor(u / v);\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, func);				\n");
					break; // n-kato
				case Instruction.FNEG : //[-dstfloatatom, floatatom]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new FloatingFunctor(-u));\n");
					break; //nakajima 2004-01-23
				case Instruction.FADDFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg3() + ").floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new FloatingFunctor(u + v);\n");
					break; //nakajima 2004-01-23			
				case Instruction.FSUBFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg3() + ").floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new FloatingFunctor(u - v);\n");
					break; //nakajima 2004-01-23
				case Instruction.FMULFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg3() + ").floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new FloatingFunctor(u * v);\n");
					break; //nakajima 2004-01-23
				case Instruction.FDIVFUNC : //[-dstfloatfunc, floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg3() + ").floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new FloatingFunctor(u / v);\n");
					break; //nakajima 2004-01-23
				case Instruction.FNEGFUNC : //[-dstfloatfunc, floatfunc]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " =  new FloatingFunctor(-u);\n");
					break; //nakajima 2004-01-23
					//====��ư���������Ѥ��Ȥ߹��ߥܥǥ�̿��====�����ޤ�====
					//====��ư���������Ѥ��Ȥ߹��ߥ�����̿��====��������====	
				case Instruction.FLT : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u < v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FLE : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u <= v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FGT : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u > v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FGE : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u >= v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FEQ : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u == v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FNE : //[floatatom1, floatatom2]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg1() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();	\n");
					writer.write(tabs + "if (!(!(u != v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; // n-kato
				case Instruction.FLTFUNC : //[floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg1() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "if (!(!(u < v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //nakajima 2003-01-23
				case Instruction.FLEFUNC : //[floatfunc1, floatfunc2]	
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg1() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "if (!(u <= v)) return false;		\n");
					break; //nakajima 2003-01-23
				case Instruction.FGTFUNC : //[floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg1() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "if (!(u > v)) return false;		\n");
					break; //nakajima 2003-01-23
				case Instruction.FGEFUNC : //[floatfunc1, floatfunc2]
					writer.write(tabs + "u = ((FloatingFunctor)var" + inst.getIntArg1() + ").floatValue();\n");
					writer.write(tabs + "v = ((FloatingFunctor)var" + inst.getIntArg2() + ").floatValue();\n");
					writer.write(tabs + "if (!(!(u >= v))) {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //nakajima 2003-01-23
					//====��ư���������Ѥ��Ȥ߹��ߥ�����̿��====�����ޤ�====
				case Instruction.FLOAT2INT: //[-intatom, floatatom]
					writer.write(tabs + "u = ((FloatingFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).floatValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new IntegerFunctor((int)u));\n");
					break; // n-kato
				case Instruction.INT2FLOAT: //[-floatatom, intatom]
					writer.write(tabs + "x = ((IntegerFunctor)((Atom)var" + inst.getIntArg2() + ").getFunctor()).intValue();\n");
					writer.write(tabs + "var" + inst.getIntArg1() + " = new Atom(null, new FloatingFunctor((double)x));\n");
					break; // n-kato
//̤����
//				case Instruction.GROUP:
//					writer.write(tabs + "subinsts = ((InstructionList)inst.getArg1()).insts;\n");
//					writer.write(tabs + "if(!interpret(subinsts, 0)){\n");
//						//�����Ǥ�ɬ������������
//						//GROUP���̿�᤬�������뤳�ȤϤʤ�
//						//System.out.println("failed");
//					writer.write(tabs + "}\n");
//					break;
//					//�����ǤϤޤȤ��ư���ʤ���
//					//sakurai
//					writer.write(tabs + "				default :\n");
//					writer.write(tabs + "System.out.println(\n");
//					writer.write(tabs + "	\"SYSTEM ERROR: Invalid instruction: \" + inst);\n");
//					break;
//					writer.write(tabs + "			}\n");
//					writer.write(tabs + "		}\n");
//					writer.write(tabs + "	}\n");
//					writer.write(tabs + "}\n");

//�ʲ��ϼ�ư����������
				case Instruction.RESETVARS: {
					writer.write(tabs + "{\n");
					int i = 0;
					List l = (List)inst.getArg1();
					for (int j = 0; j < l.size(); j++) {
						writer.write(tabs + "	Object t" + (i++) + " = var" + l.get(j) + ";\n");
					}
					l = (List)inst.getArg2();
					for (int j = 0; j < l.size(); j++) {
						writer.write(tabs + "	Object t" + (i++) + " = var" + l.get(j) + ";\n");
					}
					l = (List)inst.getArg3();
					for (int j = 0; j < l.size(); j++) {
						writer.write(tabs + "	Object t" + (i++) + " = var" + l.get(j) + ";\n");
					}
					for (int j = 0; j < i; j++) {
						writer.write(tabs + "	var" + j + " = t" + j + ";\n");
					}
					writer.write(tabs + "}\n");
					break;
				}
				case Instruction.LOADMODULE:
					writer.write(tabs + "	try {\n");
					writer.write(tabs + "		Class c = Class.forName(\"translated.Module_" + inst.getArg2() + "\");\n");
					writer.write(tabs + "		java.lang.reflect.Method method = c.getMethod(\"getRulesets\", null);\n");
					writer.write(tabs + "		Ruleset[] rulesets = (Ruleset[])method.invoke(null, null);\n");
					writer.write(tabs + "		for (int i = 0; i < rulesets.length; i++) {\n");
					writer.write(tabs + "			((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset(rulesets[i]);\n");
					writer.write(tabs + "		}\n");
					writer.write(tabs + "	} catch (ClassNotFoundException e) {\n");
					writer.write(tabs + "		Env.e(\"Undefined module " + inst.getArg2() + "\");\n");
					writer.write(tabs + "	} catch (NoSuchMethodException e) {\n");
					writer.write(tabs + "		Env.e(\"Undefined module " + inst.getArg2() + "\");\n");
					writer.write(tabs + "	} catch (IllegalAccessException e) {\n");
					writer.write(tabs + "		Env.e(\"Undefined module " + inst.getArg2() + "\");\n");
					writer.write(tabs + "	} catch (java.lang.reflect.InvocationTargetException e) {\n");
					writer.write(tabs + "		Env.e(\"Undefined module " + inst.getArg2() + "\");\n");
					writer.write(tabs + "	}\n");
//					writer.write(tabs + "{\n");
//					writer.write(tabs + "	Ruleset[] rulesets = Module" + inst.getArg2() + ".getRulesets();\n");
//					writer.write(tabs + "	for (int i = 0; i < rulesets.length; i++) {\n");
//					writer.write(tabs + "		((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset(rulesets[i]);\n");
//					writer.write(tabs + "	}\n");
//					writer.write(tabs + "}\n");
					break;
				case Instruction.JUMP:
					label = (InstructionList)inst.getArg1();
					add(label);
					writer.write(tabs + "if (exec" + label.label + "(");
					genArgList((List)inst.getArg2(), (List)inst.getArg3(), (List)inst.getArg4());
					writer.write(")) {\n");
					writer.write(tabs + "	ret = true;\n");
					writer.write(tabs + "	break " + breakLabel + ";\n");
					writer.write(tabs + "}\n");
					return;// false;
				case Instruction.SPEC://[formals,locals]
					break;//n-kato
				case Instruction.BRANCH :
					label = (InstructionList)inst.getArg1();
					add(label);
					Instruction in_spec = (Instruction)label.insts.get(0);
					if (in_spec.getKind() != Instruction.SPEC) {
						throw new RuntimeException("the first instruction is not spec but " + in_spec);
					}
					writer.write(tabs + "if (exec" + label.label + "(var0");
					for (int i = 1; i < in_spec.getIntArg1(); i++) {
						writer.write(", var" + i);
					}
					writer.write(")) {\n");
					writer.write(tabs + "	ret = true;\n");
					writer.write(tabs + "	break " + breakLabel + ";\n");
					writer.write(tabs + "}\n");
					break; //nakajima, n-kato
				case Instruction.LOOP :
//					label = (InstructionList)inst.getArg1();
					List list = (List)((List)inst.getArg1()).get(0);
					writer.write(tabs + "while (true) {\n");
//					writer.write(label.label + ":\n");
					writer.write("LL:\n");
					writer.write(tabs + "	{\n");
					translate(list.iterator(), tabs + "\t\t", iteratorNo, varnum, "LL");
					writer.write(tabs + "		break;\n");
					writer.write(tabs + "	}\n");
					writer.write(tabs + "	ret = false;\n");
					writer.write(tabs + "}\n");
					break; //nakajima, n-kato
				case Instruction.NOT :
//					label = (InstructionList)inst.getArg1();
//					add(label);
//					writer.write(tabs + "if (!exec" + label.label + "(var0");
//					for (int i = 1; i < varnum; i++) {
//						writer.write(", var" + i);
//					}
//					writer.write(")) {\n");
//					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
//					writer.write(tabs + "}\n");
//					break; //n-kato
					label = (InstructionList)inst.getArg1();
					writer.write(label.label + ":\n");
					writer.write(tabs + "{\n");
					translate(label.insts.iterator(), tabs + "	", iteratorNo, varnum, label.label);
					writer.write(tabs + "}\n");
					writer.write(tabs + "if (ret) {\n");
					writer.write(tabs + "	ret = false;\n");
					writer.write(tabs + "} else {\n");
					translate(it, tabs + "	", iteratorNo, varnum, breakLabel);
					writer.write(tabs + "}\n");
					break; //n-kato
				case Instruction.LOADRULESET:
				case Instruction.LOCALLOADRULESET:
					InterpretedRuleset rs = (InterpretedRuleset)inst.getArg2();
//					Translator t = new Translator(rs);
//					t.translate();
					String name = getClassName(rs);
					writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset(" + name + ".getInstance());\n"); 
					break;
				default:
					Env.e("Unsupported Instruction : " + inst);
			}
		}
//		return false;
	}
	private int nextFuncVarNum = 0;
	private String getFuncVarName(Functor func) {
		if (func.equals(Functor.INSIDE_PROXY)) {
			return "Functor.INSIDE_PROXY";
		} else if (func.equals(Functor.OUTSIDE_PROXY)) {
			return "Functor.OUTSIDE_PROXY";
		} else if (func.equals(Functor.STAR)) {
			return "Functor.STAR";
		}
		
		if (funcVarMap.containsKey(func)) {
			return (String)funcVarMap.get(func);
		} else {
			String varname = "f" + nextFuncVarNum++;
			funcVarMap.put(func, varname);
			return varname;
		}
	}
	
	private void genArgList(List l1, List l2, List l3) throws IOException {
		boolean fFirst = true;
		Iterator it = l1.iterator();
		while (it.hasNext()) {
			if (!fFirst) {
				writer.write(",");
			}
			fFirst = false;
			writer.write("var" + it.next());
		}
		it = l2.iterator();
		while (it.hasNext()) {
			if (!fFirst) {
				writer.write(",");
			}
			fFirst = false;
			writer.write("var" + it.next());
		}
		it = l3.iterator();
		while (it.hasNext()) {
			if (!fFirst) {
				writer.write(",");
			}
			fFirst = false;
			writer.write("var" + it.next());
		}
	}
}