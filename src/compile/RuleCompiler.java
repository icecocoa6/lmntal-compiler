package compile;

import java.util.*;
import runtime.Env;
import runtime.Rule;
import runtime.InterpretedRuleset;
import runtime.Instruction;
import runtime.Functor;
import compile.structure.*;

/*
 * ������: 2003/10/24
 *
 */

/**
 * <pre>
 * ����ѥ�����ǡ�����¤����ˤ˥롼�륻�åȤ��ղä��롣
 * 
 * ����Ū�ˤϡ�Membrane -> RuleSet ���ղä��줿 Membrane
 * 
 * ��������ϡ�{ ( :- WORLD ) } �η����ǸƤФ�뤳�Ȥˤʤ롣
 * WORLD �ˤϥ롼�뤬�ޤޤ����⤢��Τǡ�
 * ���Ĥ��ä��롼�뤫��롼�륪�֥������Ȥ��������Ƥ�����Υ롼�륻�åȤ��ɲä���
 * �Ȥ�����Ȥ�Ƶ�Ū�ˤ�뤳�Ȥˤʤ롣
 * 
 * </pre>
 * 
 * @author hara(working)
 */
public class RuleCompiler {
	/**
	 * �Ҥ��äƤ�Ȥߤʤ��ե��󥯥���=/2
	 */
	public static final Functor FUNC_UNIFY = new Functor("=", 2);
	
	/**
	 * ����ѥ��뤵���롼��
	 */
	public RuleStructure rs;
	
	/**
	 * ����ѥ��뤵���롼����б�����롼�륪�֥������ȡ�
	 */
	public Rule theRule;
	
	public List atommatches = new ArrayList();
	public List memmatch = new ArrayList();
	public List body;
	public int varcount;
	
	public List lhsfreemems;
	
	public List rhsatoms;
	public Map  rhsatompath;
	public Map  rhsmempaths;
	
	public List lhsatoms;
	public Map  lhsatompath;
	public Map  lhsmempaths;
	
	public Map lhsatomids;
	public Map lhsatomidpath;
	
	HeadCompiler hc;
	
	/**
	 * ���ꤵ�줿 RuleStructure �ѤΥ롼�륳��ѥ����Ĥ���
	 * 
	 * @param rs �롼��
	 */
	RuleCompiler(RuleStructure rs) {
		Env.n("RuleCompiler");
		Env.p(rs);
		this.rs = rs;
	}
	
	/**
	 * ��������˻��ꤵ�줿�롼�빽¤��롼�륪�֥������Ȥ˥���ѥ��뤷��
	 * ��°��Υ롼�륻�åȤ��ɲä��롣
	 */
	public void compile() {
		Env.c("compile");
		
		simplify();
		
		List rules = new ArrayList();
		
		theRule = new Rule(rs.toString());
		
		//@ruleid = rule.ruleid
		
		hc = new HeadCompiler(rs.leftMem);
		hc.enumformals(rs.leftMem);
		
		compile_l();
		compile_r();
		
		//optimize if $optlevel > 0
		optimize();
		
		//showInstructions();
		
		//rule.register(@atommatches,@memmatch,@body)
		theRule.memMatch     = memmatch;
		theRule.atomMatches  = atommatches;
		theRule.body         = body;
		
		theRule.showDetail();
		((InterpretedRuleset)rs.parent.ruleset).rules.add(theRule);
		
		// �롼��α�����ʲ��˥롼�뤬���뤫�⤷��ʤ��ΤǺƵ�Ū������
		RuleSetGenerator.processMembrane(rs.leftMem); // ������դ�
		RuleSetGenerator.processMembrane(rs.rightMem);
	}
	
	private void compile_l() {
		Env.c("compile_l");
		for(int firstid=0; firstid<=hc.atoms.size(); firstid++) {
			// �������
			hc.prepare();
			
			if(firstid < hc.atoms.size()) {
				atommatches.addAll(hc.match);
				hc.atomidpath.set(firstid, new Integer(1));
				hc.varcount = 1;
				Membrane mem = ((Atom)(hc.atoms.get(firstid))).mem;
				hc.match.add( Instruction.dummy("[:execlevel, mem.memlevel]") );
				hc.match.add( Instruction.dummy("[:func,1,@lhscmp.atoms[firstid].func") );
				
				hc.mempaths.put(mem, "[:memof,1]");
				// ����򤿤ɤ�
				while(mem.mem != null) {
					List l = ((List)(hc.mempaths.get(mem)));
					List ll = new ArrayList();
					for(ListIterator li=l.listIterator();li.hasNext();) {
						ll.add(li.next());
					}
					ll.add(0, ":memof");
					hc.mempaths.put(mem.mem, ll);
					mem = mem.mem;
				}
				hc.compile_group(firstid);
			} else {
				memmatch = hc.match;
				hc.varcount = 0;
				List tmp = new ArrayList();
				tmp.add(new Integer(0));
				hc.mempaths.put(rs.leftMem, tmp);
			}
			hc.compile_mem(rs.leftMem);
			// ȿ������Ȥ���̿��
			hc.match.add( Instruction.react(theRule, hc.getactuals()) );
		}
	}
	
	private void compile_r() {
		Env.c("compile_r");
		
		lhsatoms = hc.atoms;
		lhsfreemems = hc.freemems;
		lhsatomids = hc.atomids;
		varcount = lhsatoms.size() + lhsfreemems.size();
		body = new ArrayList();
		body.add( Instruction.dummy("[:spec,"+varcount) );
		
		genlhsmempaths();
		rhsatoms = new ArrayList();
		rhsatompath = new HashMap();
		rhsmempaths = new HashMap();
		rhsmempaths.put(rs.rightMem, lhsmempaths.get(rs.leftMem));
		
		remove_lhsatoms();
		remove_lhsmem(rs.leftMem);
		build_rhsmem(rs.rightMem);
		inherit_rhsrules(rs.rightMem);
		inherit_builtins(rs.rightMem);
		
		build_rhsatoms(rs.rightMem);
		free_lhsmem(rs.leftMem);
		
		// spec �ΰ������ɲä��Ƥ��롣̿�᥯�饹�λ��ͤˤΤäȤäƽ񤯡�
		//body.first.push @varcount
		update_links();
	}
	
	public void simplify() {
		Env.c("RuleCompiler::simplify");
		static_unify(rs.leftMem);
		static_unify(rs.rightMem);
	}
	
	public void static_unify(Membrane mem) {
		Env.c("RuleCompiler::static_unify");
		/*
		mem.each_mem do | submem | static_unify submem end
		removedatoms = []
		mem.each_atomoffunc($FUNC_UNIFY) do | atom |
			link1 = atom.args[1]
			link2 = atom.args[2]
			if link1.atom.mem.getmem(0) != mem.getmem(0) and \
				 link2.atom.mem.getmem(0) != mem.getmem(0)
				if mem.getmem(0) == @lhs
					$nerrors += 1
					print "Compile error: head contains body unification\n"
				end
				next
			end
			link1.relink link2
			link2.relink link1
			removedatoms.push atom
		end
		removedatoms.each do | atom | atom.remove end
		 */
	}
	
	private void genlhsmempaths() {
		Env.c("RuleCompiler::genlhsmempaths");
		
		Membrane mem = null;
		int atomid;
		
		List rootmems = new ArrayList();
		lhsmempaths = new HashMap();
		lhsatomidpath = new HashMap();
		
		for(int i=0;i<lhsatoms.size();i++) {
			Atom atom = (Atom)(lhsatoms.get(i));
			atomid = ((Integer)(lhsatomids.get( (Object)atom) )).intValue();
			lhsatomidpath.put(new Integer(atomid), new Integer(atomid + 1));
			if(lhsmempaths.get(atom.mem)!=null) {
				varcount++;
				lhsmempaths.put((Object)(atom.mem), new Integer(varcount));
				body.add( Instruction.dummy("[:getmem, @varcount, atomid + 1]") );
				rootmems.add(atom.mem);
			}
		}
		for(int i=0;i<lhsfreemems.size();i++) {
			mem = (Membrane)(lhsfreemems.get(i));
			lhsmempaths.put(mem, new Integer(i + lhsatoms.size() + 1));
			rootmems.add(mem);
		}
		if(mem != null)
		while( mem.mem != null && ! lhsmempaths.containsValue(mem.mem) ) {
			varcount++;
			lhsmempaths.put((Object)(mem.mem), new Integer(varcount));
			body.add( Instruction.dummy("[:getparent, @varcount, @lhsmempaths[mem]]") );
			mem = mem.mem;
		}
	}
	
	private void optimize() {
		Env.c("optimize");
	}
	
	private void remove_lhsatoms() {
		Env.c("RuleCompiler::remove_lhsatoms");
		for(int i=0;i<lhsatoms.size();i++) {
			Atom atom = (Atom)(lhsatoms.get(i));
			body.add( Instruction.dummy("[:removeatom, "+(i+1)+", "+atom.functor) );
			body.add( Instruction.dummy("[:freeatom, "+(i+1)+", "+atom.functor) );
		}
	}
	
	private void remove_lhsmem(Membrane mem) {
		Env.c("RuleCompiler::remove_lhsmem");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			
			remove_lhsmem(m);
			body.add( Instruction.dummy("[:removemem"+lhsmempaths.get(m)) );
		}
	}
	
	private void build_rhsmem(Membrane mem) {
		Env.c("RuleCompiler::build_rhsmem");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			
			rhsmempaths.put(m, new Integer(++varcount));
			body.add( Instruction.dummy("[:newmem"+varcount+", "+rhsmempaths.get(m)) );
			build_rhsmem(m); //inside must be enqueued first
		}
		for(int i=0;i<mem.processContexts.size();i++) {
			ProcessContext p = (ProcessContext)(mem.processContexts.get(i));
			
			if(rhsmempaths.get(mem).equals(lhsmempaths.get(p.lhsMem))) continue;
			body.add( Instruction.dummy("[:pour"+rhsmempaths.get(mem)+", "+lhsmempaths.get(p.lhsMem)) );
		}
	}
	
	private void inherit_rhsrules(Membrane mem) {
		Env.c("RuleCompiler::inherit_rhsrules");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			inherit_rhsrules(m);
		}
		for(int i=0;i<mem.ruleContexts.size();i++) {
			RuleContext r = (RuleContext)(mem.ruleContexts.get(i));
			
			if(rhsmempaths.get(mem).equals(lhsmempaths.get(r.lhsMem))) continue;
			body.add( Instruction.dummy("[:inheritrules"+rhsmempaths.get(mem)+", "+lhsmempaths.get(r.lhsMem)) );
		}
	}
	
	private void inherit_builtins(Membrane mem) {
		Env.c("RuleCompiler::inherit_builtins");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			inherit_builtins(m);
		}
		for(int i=0;i<mem.rules.size();i++) {
			RuleStructure r = (RuleStructure)(mem.rules.get(i));
			
			body.add( Instruction.dummy("[:loadruleset"+rhsmempaths.get(mem)+", "+r) );
		}
		/*
		mem.rulesets.each do | ruleset |
			@body.push [:loadruleset, @rhsmempaths[mem], ruleset.rulesetid]
		end
		 */
	}
	
	private void build_rhsatoms(Membrane mem) {
		Env.c("RuleCompiler::build_rhsatoms");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			build_rhsatoms(m);
		}
		for(int i=0;i<mem.atoms.size();i++) {
			Atom atom = (Atom)(mem.atoms.get(i));
			
			if(atom.functor.equals(FUNC_UNIFY)) {
				LinkOccurrence link1 = atom.args[0];
				LinkOccurrence link2 = atom.args[1];
				body.add( Instruction.dummy("[:unify, "
					+lhsatomidpath.get(lhsatomids.get(link1.atom))+", "+link1.pos
					+lhsatomidpath.get(lhsatomids.get(link2.atom))+", "+link2.pos
				) );
			} else {
				rhsatompath.put(atom, new Integer(++varcount));
				rhsatoms.add(atom);
				body.add( Instruction.dummy("[:newatom, "+varcount+", "+rhsmempaths.get(mem)+", "+atom.functor) );
			}
		}
	}
	
	private void free_lhsmem(Membrane mem) {
		Env.c("RuleCompiler::free_lhsmem");
		for(int i=0;i<mem.mems.size();i++) {
			Membrane m = (Membrane)(mem.mems.get(i));
			free_lhsmem(m);
			body.add( Instruction.dummy("[:freemem, "+lhsmempaths.get(m)) );
		}
	}
	
	private void update_links() {
		Env.c("RuleCompiler::update_links");
		for(int i=0;i<rhsatoms.size();i++) {
			Atom atom = (Atom)(rhsatoms.get(i));
			
			for(int pos=1; pos <= atom.functor.getArity(); pos++) {
				LinkOccurrence link = atom.args[pos];
				if(link.atom.mem.mems.get(0).equals(rs.leftMem)) {
					body.add( Instruction.dummy("[:relink, "+rhsatompath.get(atom)+", "+pos+", "
						+lhsatomidpath.get(lhsatomids.get(link.atom))+", "+link.pos) );
				} else {
					body.add( Instruction.dummy("[:newlink, "+rhsatompath.get(atom)+", "+pos+", "
						+rhsatompath.get(link.atom)+", "+link.pos) );
				}
			}
		}
	}
	
	/**
	 * �ǥХå���ɽ��
	 */
	private void showInstructions() {
		Iterator l;
		l = atommatches.listIterator();
		Env.p("--atommatches :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = memmatch.listIterator();
		Env.p("--memmatch :");
		while(l.hasNext()) Env.p((Instruction)l.next());
		
		l = body.listIterator();
		Env.p("--body :");
		while(l.hasNext()) Env.p((Instruction)l.next());
	}
}

