/*
 * ������: 2004/11/10
 */
package compile;

import java.util.*;
import runtime.Instruction;
import runtime.InstructionList;
/**
 * @author sakurai
 *
 * �إå�̿����򥢥ȥ॰�롼�פ��Ȥ�ʬ����
 * group[ findatom
 *        deref
 *        func
 *        insint ]
 * group [������]
 * �Τ褦�ʷ��ˤʤ�
 * ���ΤȤ�������̿�᤬���ȥ॰�롼�פ��⳰�ˤ��ޤ���ư���Ƥ��뤫��ǧ���뤯�餤�������Ѳ��ͤʤ�
 */
public class Grouping {
	
	public static void atomGroup(List head){
		//���ʤ��ߤ�spec,jump����
		//�����ɺ�Ŭ�����˽���äƤ���Τ��פ�ʤ�?
		Instruction headspec = (Instruction)head.get(0);
		if (headspec.getKind() != Instruction.SPEC) return;
		
		int headsize = head.size();
		Instruction headjump = (Instruction)head.get(headsize-1);
		if (headjump.getKind() != Instruction.JUMP) return;
				
		//FINDATOM�θ���
		int firstgrouppoint = 0;
		for(int hid=1; hid<headsize; hid++){
			Instruction insth = (Instruction)head.get(hid);
			int subinstssize = 0;
			if(insth.getKind() == Instruction.FINDATOM){
				//���ȥ॰�롼�׳��Υ�����̿����
				List outinsts = new ArrayList();
				//�ǽ�Υ��롼�פΤ�����֡��������ˤ�allocatom�ʤɤ�����
				if(firstgrouppoint == 0) firstgrouppoint = hid;
				//���ȥ॰�롼����Υ�����̿����
				InstructionList subinsts = new InstructionList();
				subinstssize += 1;
				
				//GROUP�ν����򸡺�
				for(int hid2=hid+1; hid2<headsize; hid2++){
					Instruction insth2 = (Instruction)head.get(hid2);
					boolean expflag = false;
					switch(insth2.getKind()){
						//FINDATOM��JUMP�򸫤Ĥ�����GROUP�����
						case Instruction.FINDATOM:
						case Instruction.JUMP:
							hid2 = headsize; //̵������for�롼�פ���æ��
						break; //���ʤߤˤ���break��switchʸ�ѡ�
						
						//2�����ʾ�Υ�����̿��ξ�祰�롼�פ�٤뤫�����å�����ɬ�פ�����
						//�ޤ��ϻ�§�黻����ӱ黻��
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
						case Instruction.SAMEFUNC:
							//�㡧FADD[resultvar,atom1,atom2]
							//    FLT[atom1,atom2]
							int atom1 = 0;
							int atom2 = 0;
							if(expflag){
								atom1 = insth2.getIntArg2();
								atom2 = insth2.getIntArg3();
							} else {
								atom1 = insth2.getIntArg1();
								atom2 = insth2.getIntArg2();
							}
							//GROUP���atom1,atom2�����뤫�ɤ������ǧ����
							//2�ĤȤ⤢�롢�⤷���Ϥɤ��餫�����Ǥ�����Υ��ȥ�ʤ饢�ȥ॰�롼�פ�٤뤳�ȤϤʤ�
							boolean flag1 = false;
							boolean flag2 = false;

							//ALLOCATOM��SPEC̿���ľ��˸ǤޤäƤ��� (GROUP�γ�)
							//�ޤ�FADD�ʤɤ���������ॢ�ȥ�⤳���ն�ˤ����ǽ���⤢��
							//(�����ɤ�1+1<3�Ȥ��񤤤����ʤɡ��񤯤ʤȸ��������Ȥ������)
							//�����Ǥθ����Ǥ�atom1,atom2�Τɤ��餫�����롼�פȤ�̵�ط���������ȥ�Ǥ��뤳�Ȥ��ǧ����
							//�⤷�����ʤ�insth2�����ȥ॰�롼�פ�٤�̿��Ǥ��뤳�ȤϤʤ�
							//atom1,atom2��ξ����������ƤϤޤ�ʤ�insth2�Ϻǽ��GROUP̿�������ˤ���Ϥ�
							for(int hid2b=1; hid2b<firstgrouppoint; hid2b++){
								Instruction insth2b = (Instruction)head.get(hid2b);
								switch(insth2b.getKind()){
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
										int atomid = insth2b.getIntArg1();
										if(atomid == atom1 || atomid == atom2){
											if(!flag1) {
												flag1 = true;
												hid2b = firstgrouppoint;
												break;
											}
										} 
										break;
									
									default: break;
								}
							}

							//GROUP��θ���
							for(int hid2b=hid2-1; hid2b>=hid; hid2b--){
								Instruction insth2b = (Instruction)head.get(hid2b);
								switch(insth2b.getKind()){
									case Instruction.ALLOCATOMINDIRECT:
									case Instruction.ISINT:
									case Instruction.ISFLOAT:
									case Instruction.FADD:
									case Instruction.FSUB:
									case Instruction.FMUL:
									case Instruction.FDIV:
									case Instruction.IADD:
									case Instruction.ISUB:
									case Instruction.IMUL:
									case Instruction.IDIV:
									case Instruction.IMOD:	
										int atomid = insth2b.getIntArg1();
										if(atomid == atom1 && atomid == atom2){
											subinstssize += 1;
											hid2b = hid;	
											break;	
										}
										else if(atomid == atom1 || atomid == atom2){
											if(!flag1) flag1 = true;
											else {
												flag2 = true;
												subinstssize += 1;
												hid2b = hid;	
												break;							
											}
										}
										break;
										
										default: break;
								}
							}
							//���롼�׳��˰ܤ�̿�᤬���ä����
							if(!flag2) {
								outinsts.add(insth2);
								head.remove(hid2);
								headsize -= 1;
								hid2 -= 1;
							}
							break;
							//====��§�黻����ӱ黻�ϤϤ����ޤ�====//
							
							//GETFUNC̿��ξ��Ʊ�����롼�����
							//GETFUNC̿�����2��������1�����˻���̿��(IADD��)������Ф���
							case Instruction.GETFUNC:
								flag2 = false;
								for(int hid2b=hid2-1; hid2b>=hid; hid2b--){
									Instruction insth2b = (Instruction)head.get(hid2b);
									switch(insth2b.getKind()){
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
											if(insth2b.getIntArg1() == insth2.getIntArg2()){
												subinstssize += 1;
												hid2b = hid;
												flag2 = true;	
												break;											
											}
										default: break;
									}
								}
								if(!flag2){
									outinsts.add(insth2);
									head.remove(hid2);
									headsize -= 1;
									hid2 -= 1;
								}
								break;
							//====GETFUNC̿��Ϥ����ޤ�====//
							
							//ALLOCATOMINDIRECT��GETFUNC�ȥ��åȤ�ư�����Ƥ⤤�����⤷��ʤ�
							case Instruction.ALLOCATOMINDIRECT:
								flag2 = false;
								for(int hid2b=hid2-1; hid2b<hid; hid2b--){
									Instruction insth2b = (Instruction)head.get(hid2b);
									if(insth2b.getKind() == Instruction.GETFUNC
									   && insth2b.getIntArg1() == insth2.getIntArg2()){
									   		subinstssize += 1;
									   		flag2 = true;
									   		break;
									   }
								}
								if(!flag2){
									outinsts.add(insth2);
									head.remove(hid2);
									headsize -= 1;
									hid2 -= 1;
								}
								break;
							//====ALLOCATOMINDIRECT�Ϥ����ޤ�====//
								
							//GROUP���̿����ɲ�
							default:
								subinstssize += 1;
								break;
					}									
					
				}
				
				//FINDATOM�Τ��ä����֤�GROUP̿�����������
				for(int s=0; s<subinstssize; s++){
					subinsts.add((Instruction)head.get(hid));
					head.remove(hid);
					headsize -= 1;
				}
				head.add(hid, new Instruction(Instruction.GROUP, subinsts));
				headsize += 1;
				//���롼�׳��˥�����̿���ܤ����
				if(outinsts != null){
					int outinstssize = outinsts.size();
					for(int i=0; i<outinstssize; i++){
						head.add(hid+1+i, outinsts.get(i));
						headsize += 1;
					}
				}
				
			}
			//�����ޤǤ�1GROUP�����ν�����λ
			
		}
		
	}

}
