/*
 * ������: 2004/11/04
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
//import runtime.Functor;
//import runtime.Env;

//import runtime.Rule;

/**
 * @author sakurai
 *
 * �����ɴط��κ�Ŭ����Ԥ��᥽�åɤ���ĥ��饹�� 
 *
 * ���ߺ�Ŭ�����оݤȤʤ�̿��
 * ALLOCATOM,DEREFATOM
 * ISINT,ISFLOAT,ISSTRING,ISUNARY
 * FADD,FSUB,FMUL,FDIV,IADD,ISUB,IMUL,IDIV,IMOD
 * FLT,FLE,FGT,FGE,ILT,ILE,IGT,IGE,FEQ,FNQ,IEQ,INQ
 * SAMEFUNC,GETFUNC,ALLOCATOMINDIRECT
 */
public class GuardOptimizer {
	/** ���ȥ॰�롼�פ�ޤ����ʤ�������̿���
     *  ������̿��˴ط����륢�ȥ॰�롼����˰�ư������
     *  �ޤ�������Ϥ���2�ĤΥ��ȥ॰�롼�פ��⡢��Ǹ��줿���ȥ॰�롼�ײ��˰�ư
     */
	public static void guardMove(List head, List guard){
		if(guard == null) return;
		
		// ��Ƭ��SPEC���ĺǸ�����JUMP�Ǥʤ���缺��
		Instruction headspec = (Instruction)head.get(0);
		if (headspec.getKind() != Instruction.SPEC) return;
		int headformal = headspec.getIntArg1();
		int headvarcount = headspec.getIntArg2();
		
		int headsize = head.size();
		Instruction headjump = (Instruction)head.get(headsize-1);
		if (headjump.getKind() != Instruction.JUMP) return;
		
		Instruction guardspec = (Instruction)guard.get(0);
		if (guardspec.getKind() != Instruction.SPEC) return;
		int guardformal = guardspec.getIntArg1();
		int guardvarcount = guardspec.getIntArg2();
		
		int guardsize = guard.size();
		Instruction guardjump = (Instruction)guard.get(guardsize-1);
		if (guardjump.getKind() != Instruction.JUMP) return;
		
		//�إå�̿�����jump̿��ΰ���
		//�Ǹ�˹�������
		InstructionList headlabel = (InstructionList)headjump.getArg1(); 
		List headmemargs = (List)headjump.getArg2();
		List headatomargs = (List)headjump.getArg3();
		List headvarargs = (List)headjump.getArg4();
		
		//�����ɤ�X<Y,X+Y<Z�Τ褦�ʼ���񤯤�isint��isfloat����ʣ����Τ�;ʬ��̿�����
		//̵����礫�ʤ�̵�̤ˤʤ�ΤǤʤ�Ȥ��ʤ�ʤ���Τ�������
		for(int gid=1; gid<guardsize-1; gid++){
			Instruction instg = (Instruction)guard.get(gid);
			int instid = instg.getKind();
			if(instid == Instruction.ISINT 
			 || instid == Instruction.ISFLOAT
			 || instid == Instruction.ISUNARY 
			 || instid == Instruction.ISSTRING){
			 	for(int gid2=gid+1; gid2<guardsize; gid2++){
			 		Instruction instg2 = (Instruction)guard.get(gid2);
					if(instid == instg2.getKind() 
					  && instg.getIntArg1() == instg2.getIntArg1()){
					  	guard.remove(gid2);
						gid2 -= 1;
						guardsize -= 1; 
					  }
			 	}
			 }
		}
		
		for(int gid=1; gid<guardsize-1; gid++){
			Instruction instg = (Instruction)guard.get(gid);
			boolean expflag = false;
			switch(instg.getKind()){
				case Instruction.ALLOCATOM:
				//ALLOCATOM�ϼ������Թ礬�����Τ�head��SPEC��ľ��˰�ư�����Ƥ���
				//�롼�뤬Ŭ�ѤǤ��ʤ����㴳̵�̤������뤬�礷������ʤ�����?
					head.add(1,instg);
					headsize += 1;
					headvarcount += 1;
					headatomargs.add(new Integer(instg.getIntArg1()));
					guard.remove(gid);
					guardsize -= 1;
					gid -= 1;
					break;
                //====ALLOCATOM�Ϥ����ޤ�====//
                
                //DEREFATOM���ư������������
                //DEREFATOM��ɬ���إå�¦�˰�ư������
                //TODO �����ˤ��б����������������ΤǤ��ޤ��Ԥ��ʤ�����Ϻ���
                case Instruction.DEREFATOM:
                	//DEREFATOM����1�����������ͤ򥭡��Ȥ��륢�ȥ�򿷤��˥إå�̿����ǰ���
                	int atomvar = instg.getIntArg1();
                	//DEREFATOM����2����������DEREFATOM����³��Υ��ȥ�򼨤���
                	//���Υ��ȥ���ն��DEREFATOM���ư��������
           	        int srcatom = instg.getIntArg2();
           	        //�롼��˽и�������ο����ֹ�0����Ͽ�������ʤ��Ǥ���
           	        int memnum = headmemargs.size() - 1;
           	        //target����1�����˻���FINDATOM�ޤ���DEREF(FUNC�Ǥ��?)��õ��
 		   	        //�إå�̿������Υ��ȥ�ꥹ�Ȥ������
           	        int target = headatomargs.get(srcatom-memnum-1).hashCode();
     	        
                    for(int hid=1; hid<headsize; hid++){
                		Instruction insth = (Instruction)head.get(hid);
                		switch(insth.getKind()){
                			case Instruction.FINDATOM:
                			case Instruction.DEREF:
                				if(insth.getIntArg1() == target){
									head.add(hid+1, instg);
									headvarcount += 1;
									headatomargs.add(new Integer(atomvar));
									headsize += 1;
									guard.remove(gid);
									guardsize -= 1;
									gid -= 1;
									hid = headsize;
									break;                    					
                				}
                				break;
                				
                			default: break;
                		}
                		
                	}
                	break;
                //====DEREFATOM�Ϥ����ޤ�====//
                
				case Instruction.ISINT:
				case Instruction.ISFLOAT:
				case Instruction.ISSTRING:
				case Instruction.ISUNARY:
					//̿����оݤȤʤ륢�ȥ���ѿ��ֹ�
					atomvar = instg.getIntArg1();
					//atomvar����1�����ˤȤ�DEREFATOM�β��˰�ư������
					//���λ������ФȤʤ�DEREFATOM�ϥإå�¦�˰�ư���Ƥ���Ϥ�
					//�ɤ����DEREFATOM��¾�ˤ�ALLOCATOMINDIRECT���ư��θ���ˤʤꤽ��
					for(int hid=1; hid<headsize-1; hid++){
						Instruction insth = (Instruction)head.get(hid);
						switch(insth.getKind()){
							case Instruction.DEREFATOM:
							case Instruction.ALLOCATOMINDIRECT:
								if(insth.getIntArg1() == atomvar){
									head.add(hid+1, instg);
									headsize += 1;
									guard.remove(gid);
									guardsize -= 1;
									gid -= 1;
									hid = headsize; //for�롼�פ���æ��
									break;
								}
							default: break;
						}
					}
					break;
				//====ISINT,ISFLOAT,ISSTRING,ISUNARY�Ϥ����ޤ�====//
				
				//�黻�ط���̿�����Ӵط���̿��Ϥۤ�Ʊ���������ɤ�����
				//�黻�ξ��Ϸ׻���̤Υ��ȥ��ʬ�����ѿ������䤹ɬ�פ�����Τ�expflag�Ƕ���
				case Instruction.FADD: expflag = true;
				case Instruction.FSUB: expflag = true;
				case Instruction.FMUL: expflag = true;
				case Instruction.FDIV: expflag = true;
				case Instruction.IADD: expflag = true;
				case Instruction.ISUB: expflag = true;
				case Instruction.IMUL: expflag = true;
				case Instruction.IDIV: expflag = true;
				case Instruction.IMOD: expflag = true;
				
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
				
     				//̿�����1��������2���������
               	    //��:FLT [atomvar1, atomvar2]
               	    //   FADD [resultvar, atomvar1, atomvar2]
					int atomvar1 = 0;
					int atomvar2 = 0;
					int resultvar = 0;		
					if(expflag){
						resultvar = instg.getIntArg1();
						atomvar1 = instg.getIntArg2();
						atomvar2 = instg.getIntArg3();
					} else {
						atomvar1 = instg.getIntArg1();
						atomvar2 = instg.getIntArg2();
					}
					//atomvar1,atomvar2�Υ��ȥ����1�Ĥ򸫤Ĥ�����true�ˤ���
					boolean flag = false;
                	//DEREFATOM,ALLOCATOM,��§�黻̿��θ���
					//���Ǥ˥إå�¦�˰�ư���Ƥ���ȤߤƤ����Ϥ�
					for(int hid=1; hid<headsize-1; hid++){
						Instruction insth = (Instruction)head.get(hid);
						int instid = insth.getKind();
						//��§�黻̿��Ϸ������Υ��ȥ������ΤǤ����Ǥ�ALLOCATOM�Ȱ�����Ʊ��
						//��������1������atomvar1,atomvar2����٤�Ȥ��������ˤ����ư㤤�Ϥʤ�
						switch(insth.getKind()){
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
								int dstatom = insth.getIntArg1();
								//X*X<Y�Τ褦��̿�᤬���ä����atomvar1=atomvar2�Ȥʤ�
								if(dstatom == atomvar1 && dstatom == atomvar2) {
									head.add(hid+1, instg);
									headsize += 1;
									if(expflag){
										headvarcount += 1;
										headatomargs.add(new Integer(resultvar));
									}
									guard.remove(gid);
									guardsize -= 1;
									gid -= 1;
									hid = headsize;
									break;									
								}
								else if(dstatom == atomvar1 || dstatom == atomvar2){
									if(!flag){
										flag = true;
										break;
									} else {
										head.add(hid+1, instg);
										headsize += 1;
										if(expflag){
											headvarcount += 1;
											headatomargs.add(new Integer(resultvar));
										}
										guard.remove(gid);
										guardsize -= 1;
										gid -= 1;
										hid = headsize;
										break;
									}
								}
								break;
							default: break;
						}
					}
					break;		
				//====FLT,FLE,FGT,FGE,ILT,ILE,IGT,IGE,IEQ,INE,FEQ,FNE====//
				//====FADD,FSUB,FMUL,FDIV,IADD,ISUB,IMUL,IDIV,IMOD�Ϥ����ޤ�====//
				
				case Instruction.SAMEFUNC:
					//õ���Τϥإå�¦�˰�ư����DEREFATOM,ALLOCATOM,ALLOCATOMINDIRECT
					atomvar1 = instg.getIntArg1();
					atomvar2 = instg.getIntArg2();
					flag = false;
					for(int hid = 1; hid<headsize-1; hid++){
						Instruction insth = (Instruction)head.get(hid);
						switch(insth.getKind()){
							case Instruction.DEREFATOM:
							case Instruction.ALLOCATOM:
							case Instruction.ALLOCATOMINDIRECT:
								int dstatom = insth.getIntArg1();
								if(dstatom == atomvar1 || dstatom == atomvar2){
									if(!flag) flag = true;
									else {
										head.add(hid+1, instg);
										headsize += 1;
										guard.remove(gid);
										guardsize -= 1;
										gid -= 1;
										hid = headsize;
										break;
									}
								}
							default: break;
						}
					}
					break;
				//====SAMEFUNC�Ϥ����ޤ�====//
				
				//GETFUNC�ϥ����ɤ���ǰ��Ū���ѿ���Ȥ��������Ѥ����
				//�㡧a(X),b(Y) :- A=X+Y,A<10 | ok.
				//GETFUNC��ALLOCATOMINDIRECT�ϥ��åȤΤ褦�ˤ�פ��뤬�Ȥꤢ����ʬ���ư�ư������
				case Instruction.GETFUNC:
					atomvar1 = instg.getIntArg1();
					atomvar2 = instg.getIntArg2();
					flag = true;
					//õ���Τ�ALLOCATOM ��:a(X) :- A=4,X<A | ok. (��̵̣����������������)
					//ALLOCATOMINDIRECT a(X) :- A=X,B=A,B<3 | ok.
					//DEREFATOM �㡧a(X) :- A=X,A<4 | ok.
					//��§�黻�Ϥ�̿�ᡡ�㡧a(X) :- A=X+5,A<10 | ok.
					//¾�ˤ���ޤ���?
					//�ɤ�ˤ����Ȥ���Τ���1����
					for(int hid = 1; hid<headsize-1; hid++){
						Instruction insth = (Instruction)head.get(hid);
						switch(insth.getKind()){
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
								if(dstatom == atomvar1 || dstatom == atomvar2){
									if(!flag) flag = true;
									else {
										head.add(hid+1, instg);
										headsize += 1;
										headvarcount += 1; //���ȥ���������ʤ����ѿ���������?
										guard.remove(gid);
										guardsize -= 1;
										gid -= 1;
										hid = headsize;
										break;																				
									}
								}
								break;
							default: break;
						}
					}
					break;
					//====GETFUNC�Ϥ����ޤ�====//
					
					//ALLOCATOMINDIRECT��GETFUNC�ȥ��å�?
					//���Ȥ����GETFUNC��ư�κݤ˰��˻��äƤ��ä�����®��
					case Instruction.ALLOCATOMINDIRECT:
						atomvar1 = instg.getIntArg1(); //��ư��ˤ����ֹ�Υ��ȥ���ɲä���
						atomvar2 = instg.getIntArg2(); //�����ͤ���1�����˻���GETFUNC��ľ��˰�ư
						for(int hid=1; hid<headsize-1; hid++){
							Instruction insth = (Instruction)head.get(hid);
							if(insth.getKind() == Instruction.GETFUNC
							  && insth.getIntArg1() == atomvar2){
								head.add(hid+1, instg);
								headsize += 1;
								headvarcount += 1;
								headatomargs.add(new Integer(atomvar1));
								guard.remove(gid);
								guardsize -= 1;
								gid -= 1;
								break;			
							  }
						}
						break;
					//====ALLOCATOMINDIRECT�Ϥ����ޤ�====//
					
				default: break;
			}

		}
		
		//spec̿��ι���
		headspec.updateSpec(headformal, headvarcount);
		
		//jump̿��ι���
        //������̿����SPEC��JUMP�����ˤʤä����
        //jump���ܥǥ�̿����ˤ���
		if(guardsize == 2) {
			headlabel = (InstructionList)guardjump.getArg1();
			//guard.remove(0);
			//guard.remove(0);
			//SPEC,JUMP�Ͼä��٤�?
		}
		head.set(0, headspec);
		head.set(headsize-1, Instruction.jump(headlabel,headmemargs,headatomargs,headvarargs));
	}
	
}