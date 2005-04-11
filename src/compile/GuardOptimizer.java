/*
 * ������: 2004/11/04
 */
package compile;

import java.util.*;
import runtime.Instruction;
//import runtime.InstructionList;


/**
 * @author sakurai
 *
 * �����ɴط��κ�Ŭ����Ԥ��᥽�åɤ���ĥ��饹�� 
 *
 */
public class GuardOptimizer {
	/** 
	 * ������̿����ǽ�ʸ¤����˰�ư������.
	 * ����������̿����ˤ��ä�̿��Τ߰�ư������
     */
	public static void guardMove(List head){
		int guardinstsstart = 0;
		for(int hid=1; hid<head.size()-1; hid++){
			Instruction insth = (Instruction) head.get(hid);
			switch(insth.getKind()){
				//������̿�������Ƭ�ˤ��ꤽ����̿��
				case Instruction.ALLOCATOM:
				case Instruction.DEREFATOM:
				case Instruction.GETLINK:
				case Instruction.NATOMS:
				case Instruction.NMEMS:
				case Instruction.NORULES:
					guardinstsstart = hid;
					hid = head.size();
					break;
				default: break;
			}
		}
		if(guardinstsstart != 0) //������̿���󤬶��ξ��ϼ¹Ԥ��ʤ�.
			for(int hid=guardinstsstart; hid<head.size()-1; hid++){
				Instruction insth = (Instruction)head.get(hid);
				ArrayList list = insth.getVarArgs();
				boolean moveOk = true;
				//ALLOCATOM̿�����Ƭ��SPEC��ľ��˰�ư
				if(insth.getKind() == Instruction.ALLOCATOM){
					head.remove(hid);
					head.add(1, insth);
				}
				else if(insth.getKind() == Instruction.NOT)
					continue;
				else 
					for(int hid2=hid-1; hid2>0; hid2--){
						Instruction insth2 = (Instruction)head.get(hid2);
						ArrayList list2 = insth2.getVarArgs();
						for(int i=0; i<list.size(); i++){
							if((insth2.getOutputType() != -1
							   && list.get(i).equals(insth2.getArg1()))
							   || !(orderConstraintCheck(insth, insth2, list.get(i)))){
							   	moveOk = false;
							   	break;
							   }
						}
						if(moveOk){
							head.remove(hid2+1); //��ư�оݤ�̿��insth�ΰ��֤�hid2+1
							head.add(hid2, insth); //hid2�˰�ư
						}
						else break;
					}
			}
		}
	
	//inst1��1�ľ��̿��inst2��, inst1��Ʊ���ѿ��ֹ�var����Ѥ��Ƥ�����,
	//inst1��inst2������˽Ф��뤫������å�����.
	//�Ф�����true, �Ф��ʤ����false���֤�.
	//ISINT, ISFLOAT��Ʊ���ѿ��ֹ��Ȥ�̿�����Ǥ�ͥ���̤��⤤.
	//(IGT�ʤɤ�ISINT����ˤʤ�)
	private static boolean orderConstraintCheck(Instruction inst1, Instruction inst2, Object var){
		ArrayList list = inst2.getVarArgs();
		if(list.contains(var)){
			switch(inst2.getKind()){
				case Instruction.ISINT:
				case Instruction.ISFLOAT:
					return false;
				default: return true;
			}
		}
		else return true;
	}
	
	/*
	public static void guardMove(List head){
		//��Ƭ��SPEC���ĺǸ�����JUMP�Ǥʤ���缺��
		Instruction headspec = (Instruction)head.get(0);
		Instruction headjump = (Instruction)head.get(head.size()-1);
		if(headspec.getKind() != Instruction.SPEC) return;
		if(headjump.getKind() != Instruction.JUMP && 
		   headjump.getKind() != Instruction.PROCEED) return;
		
		for(int hid=1; hid<head.size()-1; hid++){
			Instruction insth = (Instruction)head.get(hid);
			boolean expflag = false;
			switch(insth.getKind()){
				//ALLOCATOM��SPEC��ľ��ޤǲ����夲�Ƥ���
				case Instruction.ALLOCATOM:
					head.add(1, insth);
					head.remove(hid+1);
					break;
				//====ALLOCATOM�Ϥ����ޤ�====//
					
				//DEREFATOM
				case Instruction.DEREFATOM:
					for(int hid2=hid-1; hid2>0; hid2--){
						Instruction insth2 = (Instruction)head.get(hid2);
						switch(insth2.getKind()){
							case Instruction.FINDATOM:
							case Instruction.DEREF:
								if(insth.getIntArg2() == insth2.getIntArg1()){
									head.add(hid2+1, insth);
									head.remove(hid+1);
									hid2 = 0;
								}
								break;
							default: break;
						}
					}
					break;
				//====DEREFATOM�Ϥ����ޤ�====//
				
				//ISINT,ISFLOAT,ISSTRING,ISUNARY
				//�������б�����DEREFATOM�β��ޤǰ�ư��ǽ
				case Instruction.ISINT:
				case Instruction.ISFLOAT:
				case Instruction.ISSTRING:
				case Instruction.ISUNARY:
					for(int hid2=hid-1; hid2>0; hid2--){
						Instruction insth2 = (Instruction)head.get(hid2);
						if(insth2.getKind() == Instruction.DEREFATOM && insth.getIntArg1() == insth2.getIntArg1()){
							head.add(hid2+1, insth);
							head.remove(hid+1);
							break;
						}
					}
					break;
				//====ISINT,ISFLOAT,ISSTRING,ISUNARY�Ϥ����ޤ�====//
					
				//���ѱ黻�ط���̿��
				case Instruction.FADD:	expflag = true;
				case Instruction.FSUB:	expflag = true;
				case Instruction.FMUL:	expflag = true;
				case Instruction.FDIV:	expflag = true;
				case Instruction.IADD:	expflag = true;
				case Instruction.ISUB:	expflag = true;
				case Instruction.IMUL:	expflag = true;
				case Instruction.IDIV:	expflag = true;
				case Instruction.IMOD:	expflag = true;
				
				case Instruction.FLT:
				case Instruction.FLE:
				case Instruction.FGT:
				case Instruction.FGE:
				case Instruction.ILT:
				case Instruction.ILE:
				case Instruction.IGT:
				case Instruction.IGE:
				case Instruction.IEQ:
				case Instruction.INE:
				case Instruction.FEQ:
				case Instruction.FNE:
					int atomvar1 = 0;
					int atomvar2 = 0;
					if(expflag){
						atomvar1 = insth.getIntArg2();
						atomvar2 = insth.getIntArg3();
					} else {
						atomvar1 = insth.getIntArg1();
						atomvar2 = insth.getIntArg2();
					}
					//atomvar1,atomvar2�Υ��ȥ����1�ĸ��Ĥ�����true�ˤ���
					boolean flag = false;
					for(int hid2=1; hid2<hid; hid2++){
						Instruction insth2 = (Instruction)head.get(hid2);
						switch(insth2.getKind()){
							case Instruction.ALLOCATOM:
							case Instruction.ALLOCATOMINDIRECT:
							case Instruction.FADD:
							case Instruction.FSUB:
							case Instruction.FMUL:
							case Instruction.FDIV:
							case Instruction.IADD:
							case Instruction.ISUB:
							case Instruction.IMUL:
							case Instruction.IDIV:
							case Instruction.IMOD:
							case Instruction.ISINT:
							case Instruction.ISFLOAT:
								int dstatom = insth2.getIntArg1();
								//X*X<Y�Τ褦��̿�᤬���ä����atomvar1=atomvar2�Ȥʤ�
								if(dstatom == atomvar1 && dstatom == atomvar2){
									head.add(hid2+1, insth);
									head.remove(hid+1);
									hid2 = hid;
									break;
								}
								else if(dstatom == atomvar1 || dstatom == atomvar2){
									if(!flag){
										flag = true;
										break;
									} else {
										head.add(hid2+1, insth);
										head.remove(hid+1);
										hid2 = hid;
										break;
									}
								}
								break;
							default: break;
						}
					}
					break;
					//====���ѱ黻�ط���̿��Ϥ����ޤ�====//
				
				//GETFUNC�ϥ����ɤ���ǰ��Ū���ѿ��򰷤����˻��Ѥ����
				//�㡧a(X),a(Y) :- A=X+Y,A<10 | ok.
				//GETFUNC��ALLOCATOMINDIRECT�ϥ��åȤΤ褦�˻פ��뤬�Ȥꤢ����ʬ���ư�ư������
				case Instruction.GETFUNC:
					//õ���Τ�ALLOCATOM �㡧a(X) :- A=4,X<A | ok.(��̵̣����������)
					//ALLOCATOMINDIRECT a(X) :- A=X,B=A,B<3 | ok.
					//DEREFATOM a(X) :- A=X,A<4 | ok.
					//���ѱ黻 a(X) :- A=X+5,A<10 | ok.
					//�ɤ�ˤ����Ȥ���Τ���1����
					for(int hid2=1; hid2<hid; hid2++){
						Instruction insth2 = (Instruction)head.get(hid2);
						switch(insth2.getKind()){
							case Instruction.ALLOCATOM:
							case Instruction.ALLOCATOMINDIRECT:
							case Instruction.DEREFATOM:
							case Instruction.FADD:
							case Instruction.FSUB:
							case Instruction.FMUL:
							case Instruction.FDIV:
							case Instruction.IADD:
							case Instruction.ISUB:
							case Instruction.IMUL:
							case Instruction.IDIV:
							case Instruction.IMOD:
								int dstatom = insth.getIntArg1();
								if(insth.getIntArg2() == insth2.getIntArg1()){
									head.add(hid2+1, insth);
									head.remove(hid+1);
									hid2 = hid;
								}
								break;
							default: break;
						}
					}
					
					case Instruction.ALLOCATOMINDIRECT:
						for(int hid2=1; hid2<hid; hid2++){
							Instruction insth2 = (Instruction)head.get(hid2);
							if(insth2.getKind() == Instruction.GETFUNC
							  && insth.getIntArg2() == insth2.getIntArg1()){
							  	head.add(hid2+1, insth);
							  	head.remove(hid+1);
							  	break;
							  }
						}
						break;
					//====GETFUNC,ALLOCATOMINDIRECT====//
					
					default: break;
					
			}
			
		}
	}*/
	
}