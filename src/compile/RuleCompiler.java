package compile;

//import java.util.*;
import runtime.Env;

/*
 * ������: 2003/10/24
 *
 */

/**
 * <pre>
 * ����ѥ�����ǡ�����¤��롼�륪�֥������Ȥ��Ѵ����롣
 * �롼�륪�֥������Ȥ�̿�������ġ�
 * 
 * �Τǡ���ǽ�ϥǡ�����¤�ʥ��󥹥��󥹤��ڡ� -> ̿����
 * 
 * ��������ϡ�( :- WORLD ) �η����ǸƤФ�뤳�Ȥˤʤ롣
 * WORLD �ˤϥ롼�뤬�ޤޤ����⤢��ΤǺƵ�Ū��
 * �롼��򥳥�ѥ��뤹�뤳�Ȥˤʤ롣
 * 
 * </pre>
 * 
 * @author hara(working)
 */
public class RuleCompiler {
	public RuleStructure rs;
	
	/**
	 * rs �ѤΥ롼�륳��ѥ����Ĥ���
	 * 
	 * @param rs �롼��
	 */
	RuleCompiler(RuleStructure rs) {
		Env.n("RuleCompiler");
		Env.p(rs);
		this.rs = rs;
	}
	
	/**
	 * ����ѥ��뤹�롣
	 * �����֤�����̤��
	 * 
	 * @return Rule
	 */
	public Rule compile() {
		Env.c("compile");
		Rule r = new Rule();
		//r.text = "( "+l.toString()+" :- "+r.toString()+" )";
		//@ruleid = rule.ruleid
		
		HeadCompiler hc = new HeadCompiler(rs.leftMem);
		hc.enumformals();
		if(false /* @lhs.natoms + @lhs.nmems == 0 */) {
			hc.freemems.add(rs.leftMem);
		}
		compile_l();
		compile_r();
		
		//optimize if $optlevel > 0
		optimize();
		
		//rule.register(@atommatches,@memmatch,@body)
		return r;
	}
	
	private void compile_l() {
		Env.c("compile_l");
		/*
		@atommatches = []
		for firstid in 0..(@lhscmp.atoms.length)
			@lhscmp.prepare
			if firstid < @lhscmp.atoms.length
				@atommatches.push @lhscmp.match
				@lhscmp.atomidpath[firstid] = @lhscmp.varcount = 1
				mem = @lhscmp.atoms[firstid].mem
				@lhscmp.match.push [:execlevel, mem.memlevel]
				@lhscmp.match.push [:func,1,@lhscmp.atoms[firstid].func]
				@lhscmp.mempaths[mem] = [:memof,1]
				while mem.mem != nil
					@lhscmp.mempaths[mem.mem] = @lhscmp.mempaths[mem].dup.unshift :memof
					mem = mem.mem
				end
				@lhscmp.compile_group firstid
			else
				@memmatch = @lhscmp.match
				@lhscmp.mempaths[@lhs] = [@lhscmp.varcount = 0]
			end
			
			# 
			@lhscmp.compile_mem  @lhs
			@lhscmp.compile_negs @negs
			# ȿ������Ȥ���̿��
			@lhscmp.match.push [:react, @ruleid, @lhscmp.getactuals]
		end
		 */
	}
	
	private void compile_r() {
		Env.c("compile_r");
		/*
		@lhsatoms 	 = @lhscmp.atoms
		@lhsfreemems = @lhscmp.freemems
		@lhsatomids  = @lhscmp.atomids
		@varcount = @lhsatoms.length + @lhsfreemems.length
		@body = [[:spec,@varcount]]
		genlhsmempaths
		@rhsatoms 	 = []
		@rhsatompath = {}
		@rhsmempaths = {}
		@rhsmempaths[@rhs] = @lhsmempaths[@lhs]
		
		remove_lhsatoms
		remove_lhsmem 			@lhs
		
		build_rhsmem				@rhs
		inherit_rhsrules		@rhs
		inherit_builtins		@rhs
		
		build_rhsatoms			@rhs
		free_lhsmem 				@lhs
		
		@body.first.push @varcount
		update_links
		 */
	}
	
	private void optimize() {
		Env.c("optimize");
	}
}

