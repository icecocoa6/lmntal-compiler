package util;

/**
 * Stack�ɐς܂��v�f�̂��߂̐e�N���X�B
 * ���̃N���X�̃C���X�^���X��head/tail������ȗp�r�̗v�f�̂��߂ɂ̂ݎg�p���A
 * ���ۂ̗v�f�ɂ͎q�N���X�̃C���X�^���X���g�p����B
 */
public class QueuedEntity {
	/** �q�N���X������͂����̕ϐ��ɒ��ڃA�N�Z�X���Ȃ� */
	QueuedEntity next, prev;
	/** ���̃N���X�̃C���X�^���X�𒼐ڐ�������͓̂���p�b�P�[�W���̂� */
	protected QueuedEntity() {
		next = prev = null;
	}
	/** �X�^�b�N�ɐς܂�Ă���ꍇ��true�B */
	public boolean isQueued() {
		return next == null && prev == null;
	}
	/** �X�^�b�N���珜�� */
	public void dequeue() {
		next.prev = prev;
		prev.next = next;
	}
}
