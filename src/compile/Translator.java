package compile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import runtime.Functor;
import runtime.Instruction;
import runtime.InstructionList;
import runtime.InterpretedRuleset;
import runtime.Rule;
import runtime.Ruleset;

/**
 * ���̿���󤫤�Java�ؤ��Ѵ���Ԥ����饹��
 * 1 �ĤΥ롼�륻�åȤ� 1 �ĤΥ��饹���Ѵ����롣
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
	/**���� Ruleset ������Ѥ��Ƥ��� Functor �ν���*/
	private HashSet functors = new HashSet();

	/**
	 * ���ꤵ�줿 InterpretedRuleset �� Java ���Ѵ����뤿��Υ��󥹥��󥹤��������롣
	 * @param ruleset �Ѵ�����롼�륻�å�
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	public Translator(InterpretedRuleset ruleset) throws IOException{
		className = "Ruleset" + ruleset.getId();
		outputFile = new File(className + ".java");
		writer = new BufferedWriter(new FileWriter(outputFile));
		this.ruleset = ruleset;
	}
	/**
	 * Java����������Ϥ��롣
	 * @param genMain main �ؿ�������������� true������ǡ��������롼����Ф������Ѥ��롣
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	public void translate(boolean genMain) throws IOException {
		writer.write("import runtime.*;\n");
		writer.write("import java.util.*;\n");
		writer.write("import daemon.IDConverter;\n");
		writer.write("\n");
		writer.write("public class " + className + " extends Ruleset {\n");
		writer.write("	private int id = " + ruleset.getId() + ";\n");
		writer.write("	private String globalRulesetID;\n");
		writer.write("	public String getGlobalRulesetID() {\n");
		writer.write("		if (globalRulesetID == null) {\n");
		writer.write("			globalRulesetID = Env.theRuntime.getRuntimeID() + \":\" + id;\n");
		writer.write("			IDConverter.registerRuleset(globalRulesetID, this);\n");
		writer.write("		}\n");
		writer.write("		return globalRulesetID;\n");
		writer.write("	}\n");
		writer.write("	public String toString() {\n");
		writer.write("		return \"@\" + id;\n");
		writer.write("	}\n");

		writer.write("	public boolean react(Membrane mem, Atom atom) {\n");
		writer.write("		boolean result = false;\n");
		Iterator it = ruleset.rules.iterator();
		while (it.hasNext()) {
			Rule rule = (Rule) it.next();
			writer.write("		if (exec" + rule.atomMatchLabel.label + "(mem, atom)) {\n");
			writer.write("			result = true;\n");
			writer.write("			return true;\n");
			//writer.write("			if (!mem.isCurrent()) return true;\n");
			writer.write("		}\n");
		}
		writer.write("		return result;\n");
		writer.write("	}\n");
		writer.write("	public boolean react(Membrane mem) {\n");
		writer.write("		boolean result = false;\n");
		it = ruleset.rules.iterator();
		while (it.hasNext()) {
			Rule rule = (Rule) it.next();
			writer.write("		if (exec" + rule.memMatchLabel.label + "(mem)) {\n");
			writer.write("			result = true;\n");
			writer.write("			return true;\n");
			//writer.write("			if (!mem.isCurrent()) return true;\n");
			writer.write("		}\n");
		}
		writer.write("		return result;\n");
		writer.write("	}\n");
		
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

		it = functors.iterator();
		while (it.hasNext()) {
			Functor func = (Functor)it.next();
			writer.write("	private static final Functor func_" + func
					+ " = new Functor(\"" + func.getName() + "\", " + func.getArity() + ");\n");
		}
		
		if (genMain) {
			writer.write("	public static void main(String[] args) {\n");
			writer.write("		runtime.FrontEnd.run(new " + className + "());\n"); //todo �����ν���
			writer.write("	}\n");
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
	 * ���ꤵ�줿 InstructionList ��Java�����ɤ��Ѵ����롣
	 * @param instList �Ѵ�����InstructionList
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	private void translate(InstructionList instList) throws IOException {
		writer.write("	public boolean exec" + instList.label + "(");
		Instruction spec = (Instruction)instList.insts.get(0);
		int formals = spec.getIntArg1();
		int locals = spec.getIntArg2();
		if (formals > 0) {
			writer.write("Object var0");
			for (int i = 1; i < formals; i++) {
				writer.write(", Object var" + i);
			}
		}
		writer.write(") {\n");
		
		if (locals > formals) {
			writer.write("		Object var" + formals);
			for (int i = formals + 1; i < locals; i++) {
				writer.write(", var" + i);
			}
			writer.write(";\n");
		}
		
		writer.write("		Atom atom;\n");
		
		Iterator it = instList.insts.iterator();
		if (!translate(it, "		", 1)) {
			writer.write("		return false;\n");
		}
		
		writer.write("	}\n");
	}
	/**
	 * ���ꤵ�줿 Iterator �ˤ�ä�������̿����� Java �����ɤ��Ѵ����롣
	 * @param it �Ѵ�����̿����� Iterator
	 * @param tabs ���ϻ������Ѥ��륤��ǥ�ȡ��̾�� N �ĤΥ���ʸ������ꤹ�롣
	 * @param iteratorNo ���Ϥ��륳������Ǽ������Ѥ��� Iterator ���ֹ档�������ѿ��ν�ʣ���ɤ������ɬ�ס�
	 * @return return ʸ����Ϥ��ƽ�λ�������ˤ� true������ѥ��륨�顼���ɤ����ᡢtrue ���֤�������ľ���"}"�ʳ��Υ����ɤ���Ϥ��ƤϤʤ�ʤ���
	 * @throws IOException Java �������ν��Ϥ˼��Ԥ������
	 */
	private boolean translate(Iterator it, String tabs, int iteratorNo) throws IOException {
		while (it.hasNext()) {
			Functor func;
			Instruction inst = (Instruction)it.next();
			switch (inst.getKind()) {
			case Instruction.SPEC:
				break;
			case Instruction.FINDATOM:
				func = (Functor)inst.getArg3();
				functors.add(func);
				writer.write(tabs + "Iterator it" + iteratorNo + 
						" = ((AbstractMembrane)var" + inst.getIntArg2() + ").atomIteratorOfFunctor(func_" + func + ");\n");
				writer.write(tabs + "while (it" + iteratorNo + ".hasNext()) {\n");
				writer.write(tabs + "	var" + inst.getIntArg1() + " = (Atom)it" + iteratorNo + ".next();\n");
				translate(it, tabs + "	", iteratorNo + 1);
				writer.write(tabs + "}\n");
				break;
			case Instruction.JUMP:
				InstructionList label = (InstructionList)inst.getArg1();
				add(label);
				writer.write(tabs + "return exec" + label.label + "(");
				boolean fFirst = true;
				Iterator it2 = ((List)inst.getArg2()).iterator();
				while (it2.hasNext()) {
					if (!fFirst) {
						writer.write(",");
					}
					fFirst = false;
					writer.write("var" + it2.next());
				}
				it2 = ((List)inst.getArg3()).iterator();
				while (it2.hasNext()) {
					if (!fFirst) {
						writer.write(",");
					}
					fFirst = false;
					writer.write("var" + it2.next());
				}
				it2 = ((List)inst.getArg4()).iterator();
				while (it2.hasNext()) {
					if (!fFirst) {
						writer.write(",");
					}
					fFirst = false;
					writer.write("var" + it2.next());
				}
				writer.write(");\n");
				return true;
			case Instruction.COMMIT:
				break;
			case Instruction.DEQUEUEATOM:
				writer.write(tabs + "atom = (Atom)var" + inst.getIntArg1() + ";\n");
				writer.write(tabs + "atom.dequeue();\n");
				break;
			case Instruction.REMOVEATOM:
				writer.write(tabs + "atom = (Atom)var" + inst.getIntArg1() + ";\n");
				writer.write(tabs + "atom.getMem().removeAtom(atom);\n");
				break;
			case Instruction.NEWATOM:
				func = (Functor)inst.getArg3();
				functors.add(func);
				writer.write(tabs + "var" + inst.getIntArg1()
						+ " = ((AbstractMembrane)var" + inst.getIntArg2() + ").newAtom(func_" + func + ");\n");
				break;
			case Instruction.ENQUEUEATOM:
				writer.write(tabs + "atom = (Atom)var" + inst.getIntArg1() + ";\n");
				writer.write(tabs + "atom.getMem().enqueueAtom(atom);\n");
				break;
			case Instruction.FREEATOM:
				break;
			case Instruction.PROCEED:
				writer.write(tabs + "return true;\n");
				return true;
			case Instruction.LOADRULESET:
				InterpretedRuleset rs = (InterpretedRuleset)inst.getArg2();
				Translator t = new Translator(rs);
				t.translate(false);
				writer.write(tabs + "((AbstractMembrane)var" + inst.getIntArg1() + ").loadRuleset(new " + t.className + "());\n"); 
				break;
			default:
				throw new RuntimeException("Unsupported Instruction : " + inst);
			}
		}
		return false;
	}
}
