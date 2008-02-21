;; lmntal.el --- LMNtal mode for Emacs.   -*- coding: euc-jp -*-
;; $Id: lmntal.el,v 1.4 2008/02/21 07:46:10 taisuke Exp $

;;  Author: Taisuke HORI (taisuke@ueda.info.waseda.ac.jp)
;;  Version 0.2.3.4
;;
;;  Bug reports, suggestions, donations etc. to above E-Mailaddress or
;;  "lang@ueda.info.waseda.ac.jp".

;; Feature:
;;   * syntactic highlighting
;;   * filling of comments
;;   * indentation (use prolog mode)
;;   * running lmntal with the current buffer
;;   * communication with lmntal interactive session

;; Installation:

;;
;; 1. prolog.el is required to use lmntal.el. Put prolog.el in a
;;    directory where Emacs can find int.
;;
;; 2. Put this file in a directory where Emacs can find it . Then add
;;    the following lines to your Emacs initialization file:
;; 
;;    (load "lmntal")
;;    (setq auto-mode-alist
;;       (append auto-mode-alist
;;               '(("\\.lmn$"  . lmntal-mode))))

;;
;; 2. Set variables in your Emacs initialization file:
;;    ;; Set the path to the LMNtal runtime
;;    (setq lmntal-runtime-name  "/path/to/your/lmntal/runtime")
;;    ;; Set runtime options to run lmntal programs. You can specify options
;;    ;; when you run a program.
;;    (setq lmntal-interpret-mode-arguments
;;          '("--interpret"
;;            "--use-source-library"))
;;    ;; Set runtime options to run lmntal interactive mode.

;;    (setq lmntal-interactive-mode-arguments
;;          '("--interactive"
;;            "--use-source-library"))

;; Usage:
;;
;;  The LMNtal major mode is triggered by visiting a file with
;;  extension .lmn or manually by M-x lmntal-mode. 
;; 
;; Command and Key binding:
;;
;; * Running program

;; 
;;  C-c C-x C-e (lmntal-run-current-buffer) :
;;     Run LMNtal runtime with the current buffer.
;;     If with prefix argument, you can specify options to run runtime.
;;  C-c C-x C-r (lmntal-run-region) :
;;     Run LMNtal runtime with the current region.
;;     If with prefix argument, you can specify options to run runtime.
;;
;; * Interactive mode
;;
;;  C-c C-i (run-interactive-lmntal) :
;;     Run a LMNtal interactive session.
;;     If with prefix argument, you can specify options to run runtime.
;;  C-c C-q (kill-interactive-lmntal) :
;;     Kill the buffer of the interactive session
;;  C-c C-r (lmntal-eval-region) :
;;     Send region to the interactive session.

;;     If with prefix argument, blank lines are removed.
;;  C-c C-s C-d (lmntal-send-debug) :
;;     Send ":debug" to the interactive session.
;;     The level of debug is specified with the argument.
;;  C-c C-s C-o (lmntal-send-optimize) :
;;     Send ":optimize" to the interactive session.
;;     If with negative prefix argument, send ":nooptimize".
;;  C-c C-s C-v (lmntal-send-verbose) :
;;     Send ":verbose" to the interactive session.

;;     If with negative prefix argument, send ":noverbose".
;;  C-c C-s C-s (lmntal-send-shuffle) :
;;     Send ":shuffle" to the interactive session.
;;     If with nevative prefix argument, send ":noshuffle".
;;  C-c C-s C-t (lmntal-send-trace) :
;;     Send ":trace" to the interactive session.
;;     If with negative prefix argument, send ":notrace".

;;  C-c C-s C-r (lmntal-send-remain) :
;;     Send ":remain" to the interactive session.
;;     If with prefix argument, send ":noremain".
;;  C-c C-s C-c (lmntal-send-gui) :
;;     Send ":gui" to the interactive session.
;;     If with prefix argument, send ":nogui".
;;  C-c C-s C-c (lmntal-send-clear) :

;;     Send ":clear" to the interactive session.

;; Customize:
;;
;; �����Х���ɤΥ������ޥ���
;; (global-set-key "\C-c\M-e" 'lmntal-run-current-buffer)
;; (global-set-key "\C-c\M-r" 'lmntal-run-region)
;;

;; (defun lmntal-initialize ()
;;   ;; nil�ʤ��minor mode��Ȥ�ʤ�
;;   (setq lmntal-use-minormode-in-interpret t)
;;   (setq lmntal-use-minormode-in-interactive t)
;;  
;;   ;; interactive mode��������ʬ����Τˤ���
;;   ;; (set-face-italic-p 'comint-highlight-input t)
;;   ;; ��������
;;   ;; list-colors-display�ǿ��ΰ������Ф�Τǡ������ʿ�������
;;   (if window-system
;;       (progn
;;         (set-face-foreground 'lmntal-atom-face "LightGoldenrod")
;;         (set-face-foreground 'lmntal-link-face "YellowGreen")

;;         (set-face-foreground 'lmntal-membrane-face "green")
;;         (set-face-foreground 'lmntal-membrane-name-face "MediumSpringGreen")
;;         (set-face-foreground 'lmntal-rule-name-face "aquamarine")
;;         (set-face-foreground 'lmntal-rule-atmark-face "CadetBlue")
;;         (set-face-foreground 'lmntal-arrow-face "cyan")

;;         (set-face-foreground 'lmntal-equal-face "cyan")
;;         (set-face-foreground 'lmntal-integer-face "PeachPuff1")
;;         (set-face-foreground 'lmntal-float-face "PeachPuff1")
;;         (set-face-foreground 'lmntal-error-face "red")
;;         (set-face-foreground 'lmntal-warning-face "red")

;;         (set-face-foreground 'lmntal-process-end-face "blue")
;;         (set-face-foreground 'lmntal-rule-representation-face "aquamarine")
;;         ))
;;   )
;; (add-hook 'lmntal-mode-hook 'lmntal-initialize)
;; (add-hook 'lmntal-minor-mode-hook 'lmntal-initialize)



;; ʻ�Ѥ���������ʤ��:
;;   mmm-mode(http://mmm-mode.sourceforge.net/)��Ȥ��ȡ�java�Υ���饤�󥳡��ɤǤ�
;;   java-mode���ڤ��ؤ��뤳�Ȥ��Ǥ��ޤ�����ϰʲ��Τ褦�����ꤷ�Ƥ��ޤ���
;;     (require 'mmm-mode)
;;     (setq mmm-global-mode 'maybe)
;;     ;; �����ꡥ����ϡ����ߤǡ�����Ĥ������ʤ��ʤ� nil �ˤ��ޤ���
;;     ;(set-face-background 'mmm-default-submode-face nil)
;;     (set-face-background 'mmm-default-submode-face "black")
;;     (mmm-add-classes
;;      '((inline-java-code

;;         :submode java-mode
;;         :front "\\[:/\\*inline"
;;         :back ":\\]"
;;         :insert ((?t inline-java-code nil @ "[:/*inline*/" @ "\n" _ "\n" @ ":]" @))

;;        )))
;;     (mmm-add-mode-ext-class nil "\\.lmn?\\'" 'inline-java-code)
;;   
;;

;; Restriction:
;;   - ����ǥ�Ȥϡ�prolog-mode�˼㴳�ѹ���ä�����Τ����Ѥ��Ƥ��ޤ���
;;   - mmm-mode��Ȥ��ȡ�����饤�󥳡��ɤγ��Ϥ�Ʊ���Ԥ�LMNtal�Υ����ɤ�
;;     font-lock�������ʤ��褦�Ǥ���
;;   - imenu�ι��ܡʼ����̾)������饤�󥳡��ɤ�ȿ�����ޤ���
;;   - interactive mode��ʸ������ü���ܤ��Ƥ���ȿ����դ��ʤ���
;;     ���ڡ������������п����դ��褦�ˤʤ롣

;; Memo:

;;    eclipse preference, Rebuild class files modified by others
;;       �����ꤷ�Ȥ��Ȥ褤?
;;    (set-face-foreground 'font-lock-keyword-face "pink")
;;     �Ȥ��ǡ������Ѥ����롣hook�ǰ��ä��������ꤹ�롩

;; Changelog:
;; Version 0.2.3.4 (2008/02/21):
;;   * Bug fix
;;   * English Document
;; Version 0.2.3.3 (2007/05/29):
;;   * Bug fix
;; Version 0.2.3.2 (2007/05/28):
;;   * Bug fix
;; Version 0.2.3.1 (2007/05/27):

;;   * Bug fix
;; Version 0.2.3(2007/05/25):
;;   * rule̾������ɽ������
;; Version 0.2.2(2007/05/22):
;;   * interactive�⡼�ɤ�send�ϤΥ��ޥ�ɤ��ɲ�
;; Version 0.2.1.1(2007/05/21):
;;   * minor�⡼�ɤ�font-lock�� '��:.*' ���ɲ�
;;   * �������ɲä�������Խ����ѹ�
;; Version 0.2.1(2007/05/20):
;;   * font-lock�ζ���
;;   * interpret�⡼�ɤǤ�minor�⡼�ɤ�ON��
;;   * interactive�⡼�ɤǥ꡼����������ɾ����ǽ
;;   * ư��β���
;; Version 0.2(2007/05/20):

;;   * LMNtal��interactive�⡼�ɤȤ�Ϣ��, minor mode�κ���
;;   * font-lock�ζ���
;;   * runtime�Υѥ�������λ�����ˡ���ѹ�
;;   * �꡼�������˸��ꤷ�ƥץ�����¹ԤǤ���褦�ˤ���
;;   * runtime�¹Ի��˰������ɲäǤ���褦�ˤ���
;; Version 0.1(2007/05/18): 
;;   * font-lock, imenu, indent(prolog-mode������),
;;     ���ߤΥХåե���LMNtal runtime�Ǽ¹�

;;; TODO:
;;;

;;; BUGS:
;;;

;;; code
 

(require 'cl)
(require 'prolog)

;; �⥸�塼��ǻȤ��빭��γ�̤�ȿ�����ơ�����ǥ�Ȥ����ޤ������ʤ��Τǡ� '{}'�ˤ�롢
;; ����ǥ�Ȥ�̵���ˤ��롣
(defconst prolog-left-paren "[[(]" 
  "The characters used as left parentheses for the indentation code.")
(defconst prolog-right-paren "[])]"

  "The characters used as right parentheses for the indentation code.")

(defvar lmntal-mode-map nil
  "Keymap for LMNtal major mode")
(if lmntal-mode-map
    ()  ; do nothing if lmntal-mode-map exists
  (setq lmntal-mode-map (make-sparse-keymap))
  (define-key lmntal-mode-map "\C-c\C-c" 'compile)

  (define-key lmntal-mode-map "\C-c\C-x\C-e" 'lmntal-run-current-buffer)
  (define-key lmntal-mode-map "\C-c\C-x\C-r" 'lmntal-run-region)
  (define-key lmntal-mode-map "\C-c\C-r" 'lmntal-eval-region)
  (define-key lmntal-mode-map "\C-c\C-s\C-c" 'lmntal-send-clear)

  (define-key lmntal-mode-map "\C-c\C-s\C-r" 'lmntal-send-remain)
  (define-key lmntal-mode-map "\C-c\C-s\C-g" 'lmntal-send-gui)
  (define-key lmntal-mode-map "\C-c\C-s\C-s" 'lmntal-send-shuffle)
  (define-key lmntal-mode-map "\C-c\C-s\C-t" 'lmntal-send-trace)

  (define-key lmntal-mode-map "\C-c\C-s\C-v" 'lmntal-send-verbose)
  (define-key lmntal-mode-map "\C-c\C-s\C-d" 'lmntal-send-debug)
  (define-key lmntal-mode-map "\C-c\C-s\C-o" 'lmntal-send-optimize)

  (define-key lmntal-mode-map "\C-c\C-i" 'run-interactive-lmntal)

  (define-key lmntal-mode-map "\C-c\C-q" 'kill-interactive-lmntal)
  )

(defvar lmntal-mode-hook nil
  "List of functions to call when entering lmntal mode.")
(defvar lmntal-minor-mode-hook nil
  "List of functions to call when entering lmntal minor mode.")


(defvar lmntal-comment-start-skip
  "/\\*+ *")

(defvar lmntal-use-minormode-in-interpret nil
  "If value is non-nil, use LMNtal minor mode in interpret mode")
(if lmntal-use-minormode-in-interpret
    ()
  (setq lmntal-use-minormode-in-interpret t))

(defvar lmntal-use-minormode-in-interactive nil
  "If value is non-nil, use LMNtal minor mode in interactive mode")

(if lmntal-use-minormode-in-interactive
    ()
  (setq lmntal-use-minormode-in-interactive t))

(defvar lmntal-mode-syntax-table
  (let ((table (make-syntax-table)))
    (modify-syntax-entry ?\  " " table)
    (modify-syntax-entry ?\t " " table)

    (modify-syntax-entry ?_ "w" table)
    (modify-syntax-entry ?.  "_" table)
    (modify-syntax-entry ?\( "()" table)
    (modify-syntax-entry ?\) ")(" table)

    (modify-syntax-entry ?\[  "(]" table)
    (modify-syntax-entry ?\]  ")[" table)
    (modify-syntax-entry ?\{  "(}" table)
    (modify-syntax-entry ?\}  "){" table)

    (modify-syntax-entry ?\" "\"" table)
    (modify-syntax-entry ?\' "\"" table)

    (modify-syntax-entry ?/  ". 124b" table)

    (modify-syntax-entry ?*  ". 23" table)
    (modify-syntax-entry ?\n "> b" table)
    (modify-syntax-entry ?% "< b" table)

    (modify-syntax-entry ?# "< b" table)

    (modify-syntax-entry ?\\ "\\" table)
    table)
  "Syntax table in use in LMNtal mode buffers.")


(defvar lmntal-imenu-generic-expression nil
  "Imenu expression for LMNtal major mode")
(if lmntal-imenu-generic-expression
    ()
  (setq lmntal-imenu-generic-expression
   '(("Rules" "\\(^\\|[^_]\\)\\b\\([_a-zA-Z]\\([a-zA-Z0-9_]*\\)\\(\\.[a-zA-Z-09_]+\\)?\\)\\\(\\s \\|\n\\)*\\(@@\\)\\([^@]\\|$\\)" 2)

     ("Membranes" "\\(^\\|[^_]\\)\\b\\([a-zA-Z]\\(\\w\\|_\\)*\\)\\(\\s \\|\n\\)*{" 2))))

(require 'font-lock)

(defvar lmntal-font-lock-keywords
  ()
  "Font-Lock patterns for LMNtal mode.")

(if lmntal-font-lock-keywords
    ()
  (setq
   lmntal-font-lock-keywords
   (list
    ;; '='
    '("\\(^\\|\\s \\|[A-Za-z0-9_]\\)\\(=\\)\\([A-Za-z_0-9$]\\|\\s \\|$\\)"

       2 'lmntal-equal-face nil nil)
    ;; ':-'
    '("\\(:-\\)"
      1 'lmntal-arrow-face nil nil)
    ;; rule name
     '("\\(^\\|[^_]\\)\\b\\([_a-zA-Z]\\([a-zA-Z0-9_]*\\)\\(\\.[a-zA-Z-09_]+\\)?\\)\\\(\\s \\|\n\\)*\\(@@\\)\\([^@]\\|$\\)"

       2 'lmntal-rule-name-face)
    ;; rule '@@'
    '("\\(^\\|[^_]\\)\\b\\([a-zA-Z]\\(\\w\\|\\d\\|_\\)*\\)\\(\\s \\|\n\\)*\\(@@\\)\\([^@]\\|$\\)"
      5 'lmntal-rule-atmark-face)
    ;; membrane name
    '("\\(^\\|[^_]\\)\\b\\([a-zA-Z]\\(\\w\\|_\\)*\\)\\(\\s \\|\n\\)*{"

      2 'lmntal-membrane-name-face)
    ;; membrane
    '("{"
      0 'lmntal-membrane-face)
    '("}"
      0 'lmntal-membrane-face)

    ;; link«, X*Y�ΤȤ������ϡ����̤˰�����
    ;; ������link�������֤�!!
    '("\\(^\\|[^A-Za-z_*]\\)\\(\\*\\b[A-Z]+\\(\\w\\|_\\)*\\)"
      2 'lmntal-link-face)
    ;; link
    '("\\b\\([A-Z]+\\(\\w\\|_\\)*\\)"

      1 'lmntal-link-face)
    ;; atom
    '("\\(^\\|[^A-Za-z$]\\)\\b\\(\\$?[a-z]+\\(\\w\\|_\\)*\\)"
      2 'lmntal-atom-face)

    ;; float, X-100 �Τ褦�ʾ������̤˰���
    '("\\(^\\|[^A-Za-z_0-9]\\)\\(\\(-\\|\\+\\)?[0-9]+\\.[0-9]+\\)"

      2 'lmntal-float-face)
    ;; float,
    '("\\(^\\|[^\\-]\\)\\b\\(\\(-\\|\\+\\)?[0-9]+\\.[0-9]+\\)"
      2 'lmntal-float-face)

    ;; integer, X-100 �Τ褦�ʾ������̤˰���
    '("\\(^\\|[^A-Za-z_0-9]\\)\\(\\(-\\|\\+\\)?[0-9]+\\)"

      2 'lmntal-integer-face)
    ;; integer,
    '("\\(^\\|[^\\-]\\)\\b\\(\\(-\\|\\+\\)?[0-9]+\\)"
      2 'lmntal-integer-face)

    )))

(defvar lmntal-minor-font-lock-keywords

  ()
  "Font-Lock patterns for LMNtal minor mode.")
(if lmntal-minor-font-lock-keywords
    ()
  (setq
   lmntal-minor-font-lock-keywords
   (append (list
            ;; rule
            '("\\(^\\|[^@]\\)\\(@[0-9]+\\)" 2 'lmntal-rule-representation-face)

            '("\\(^\\|[^@]\\)\\(@[A-Za-z_0-9]+@\\)" 2 'lmntal-rule-representation-face)
            ;; �����ϥ��顼���Ϥ��ɤ����ǿ���ʬ���������ɡ���
            '("^ *Warning :.*" 0 'lmntal-warning-face)
            '("^ *WARNING:.*" 0 'lmntal-warning-face)

            '("^ERROR:.*" 0 'lmntal-error-face)
            '("^Note:.*" 0 'lmntal-error-face)
            '("^ *SYNTAX ERROR:.*" 0 'lmntal-error-face)

            '("^ *Syntax errorr :.*" 0 'lmntal-error-face)
            '("^ *Syntax error:.*" 0 'lmntal-error-face)
            '("Couldn't.*" 0 'lmntal-error-face)

            '("^Can't.*" 0 'lmntal-error-face)
            '("^FEATURE NOT IMPLEMENTED:.*" 0 'lmntal-error-face)
            '("^COMPILE ERROR:.*" 0 'lmntal-error-face)

            '("^Compilation Failed" 0 'lmntal-error-face)
            '("^��:.*" 0 'lmntal-warning-face)
            ;; process����λ�����Ȥ���emacs����Υ�å�����
            '("^Process lmntal process finished" 0 'lmntal-process-end-face))

           lmntal-font-lock-keywords)))


(defvar lmntal-runtime-name nil
  "LMNtal runtime name.
   You can use environment variables.")
(if lmntal-runtime-name
    ()
  (setq lmntal-runtime-name "lmntal"))


(defvar lmntal-interpret-mode-arguments nil
  "Argument list for LMNtal interpret mode")
(if lmntal-interpret-mode-arguments
    ()
  (setq '("--interpret"
          "--use-source-library")))


(defvar lmntal-interactive-mode-arguments nil
  "Argument list for LMNtal interactive mode")
(if lmntal-interactive-mode-arguments
    ()
  (setq lmntal-interactive-mode-arguments
        '("--interactive"
          "--use-source-library")))
  

(defvar lmntal-home-directory nil
  "Directory where LMNtal use to run.
   If nil current directory used")
(if lmntal-home-directory
    ()
  (setq lmntal-home-directory nil))

;; not use
;; (defvar lmntal-runtime-arguments nil
;;   "List of arguments for LMNtal runtime")

;; (if lmntal-runtime-arguments
;;     ()
;;   (setq lmntal-runtime-arguments
;;         '("--interpret"
;;           "--use-source-library")))

(defvar lmntal-interpret-buffer-name nil
  "LMNtal buffer name for interpret output")
(if lmntal-interpret-buffer-name
    ()

  (setq lmntal-interpret-buffer-name "*LMNtal*"))

(defvar lmntal-interactive-buffer-name nil
  "Buffer name for interactive LMNtal")
(if lmntal-interactive-buffer-name
    ()
  (setq lmntal-interactive-buffer-name "*I-LMNtal*"))


(defvar lmntal-interactive-process nil
  "LMNtal interactive process")

;;; Faces
(copy-face 'font-lock-variable-name-face 'lmntal-atom-face)
(copy-face 'font-lock-type-face 'lmntal-link-face)
(copy-face 'font-lock-keyword-face 'lmntal-membrane-face)
(copy-face 'font-lock-function-name-face 'lmntal-membrane-name-face)

(copy-face 'font-lock-function-name-face 'lmntal-rule-name-face)
(copy-face 'font-lock-keyword-face 'lmntal-rule-atmark-face)
(copy-face 'font-lock-keyword-face 'lmntal-arrow-face)
(copy-face 'font-lock-keyword-face 'lmntal-equal-face)
(make-face 'lmntal-integer-face)
(make-face 'lmntal-float-face)
(copy-face 'font-lock-warning-face 'lmntal-error-face)

(copy-face 'font-lock-warning-face 'lmntal-warning-face)
(make-face 'lmntal-process-end-face)
(set-face-foreground 'lmntal-process-end-face "skyblue")
(copy-face 'font-lock-function-name-face 'lmntal-rule-representation-face)
  
(defun lmntal-run-file (file arg-list)
  "Run LMNtal with specified file with additional lmntal options opt-args."

  (let ((old_dir default-directory))
    (if lmntal-home-directory
        (cd lmntal-home-directory))
    ;; �ʤ���ХХåե�����
    (if (not (get-buffer lmntal-interpret-buffer-name))
        (generate-new-buffer lmntal-interpret-buffer-name))
    ;; �Хåե��Υƥ����Ȥ�õ�
    (save-current-buffer
      (set-buffer lmntal-interpret-buffer-name)

      (erase-buffer)
      (if lmntal-use-minormode-in-interpret
          (lmntal-minor-mode 1))
      (apply 'start-process "lmntal process"
             lmntal-interpret-buffer-name
             (substitute-in-file-name lmntal-runtime-name)
             (append arg-list

                     (list file)))
      (beginning-of-buffer))
    (let ((w (selected-window)))
      (switch-to-buffer-other-window lmntal-interpret-buffer-name)
      (select-window w))
    (cd old_dir)))

(defun lmntal-read-additional-argument (arg-list)
  (split-string (read-string "Input options: "

                             (apply 'concat (mapcar '(lambda (s) (concat s " "))
                                                    arg-list)))))

(defun lmntal-run-current-buffer (&optional arg)
  "Run LMNtal with file of current buffer.
   If arg is non-nil, you can input additional lmntal options."
  (interactive "P")

  (if arg
      (lmntal-run-file (buffer-file-name)
                       (lmntal-read-additional-argument lmntal-interpret-mode-arguments))
    (lmntal-run-file (buffer-file-name) lmntal-interpret-mode-arguments)))

(defun lmntal-run-region (&optional arg)
  "Run LMNtal with current region.
   If arg is non-nil, you can input additional lmntal options."
  (interactive "P")

  ;; 'temporary-file-directory' ��temporary�ե��������
  (let ((file (make-temp-file "LMNTAL-MODE-TEMP-FILE" nil ".lmn")))
    (write-region (region-beginning) (region-end) file)
    (print file)
    (if arg

        (lmntal-run-file file
                         (lmntal-read-additional-argument lmntal-interpret-mode-arguments))
      (lmntal-run-file file lmntal-interpret-mode-arguments))))


(defun run-interactive-lmntal (&optional arg)
  "Run interactive LMNtal process.
   If arg is non-nil, you can input additional lmntal options."
  (interactive "P")

  ;; �Ȥꤢ���������Ƥ���
  (kill-interactive-lmntal)
  (let ((w (selected-window)))
    (shell lmntal-interactive-buffer-name)
    (setq lmntal-interactive-process (get-buffer-process lmntal-interactive-buffer-name))
    ;;    (set-process-sentinel lmntal-interactive-process 'lmntal-interactive-process-handler)
    (erase-buffer)
    (if lmntal-use-minormode-in-interactive

        (lmntal-minor-mode 1))
    (select-window w)
    )
  ;; shell�ν������Ǥ���ޤǾ����Ԥ�
  (sleep-for 0.5)
  (lmntal-eval-string
   (apply 'concat lmntal-runtime-name
          (mapcar '(lambda (s) (concat " " s))

                  (if arg
                      (lmntal-read-additional-argument lmntal-interactive-mode-arguments)
                    lmntal-interactive-mode-arguments)))))

(defun lmntal-interactive-process-handler (process event)
  "Kill LMNtal interactive process when signal occur."
  (if (or (compare-strings "finished" 0 7
                           event 0 7)

          (compare-string "exited" 0 5
                          event 0 5))
      (kill-interactive-lmntal)))

(defun lmntal-eval-string (str)
  "Evaluate string in interactive LMNtal process"
  (if (not lmntal-interactive-process)
      (run-interactive-lmntal))

  (let ((w (selected-window)))
    (save-current-buffer
      (set-buffer (process-buffer lmntal-interactive-process))
      ;(switch-to-buffer-other-window lmntal-interactive-buffer-name)
    (insert str)
    (comint-send-input))
    (select-window w)))

(defun remove-blank-lines (s)
  "Remove blank lines from s."

  (apply 'concat (mapcar '(lambda (s) (concat s "\n"))
                      (split-string s "[ \f\t\n\r]+\n"))))

(defun lmntal-eval-region (&optional arg)
  "Evaluate region in interactive LMNtal process.
   If arg is non-nil, blank lines are removed."

  (interactive "P")
  (if (not lmntal-interactive-process)
      (run-interactive-lmntal))
  (let* ((s (buffer-substring (region-beginning)
                              (region-end))))
    (if arg
        (setq s (remove-blank-lines s)))
    (lmntal-eval-string s)))


(defun lmntal-send-remain (&optional arg)
  "Evaluate \":remain\" in interactive LMNtal process"
  (interactive "P")
  (if lmntal-interactive-process
      (lmntal-eval-string (concat ":" (if arg "no" "") "remain"))))


(defun lmntal-send-trace (&optional arg)
  "Evaluate \":trace\" in interactive LMNtal process"
  (interactive "P")
  (if lmntal-interactive-process
      (lmntal-eval-string (concat ":" (if arg "no" "") "trace"))))


(defun lmntal-send-gui (&optional arg)
  "Evaluate \":gui\" in interactive LMNtal process"
  (interactive "P")
  (if lmntal-interactive-process
      (lmntal-eval-string (concat ":" (if arg "no" "") "gui"))))


(defun lmntal-compose-leveled-string (s arg)
  (concat ":" (if (and arg (< arg 0)) "no" "")
          s
          (if (and arg (>= arg 0))

              (concat " " (number-to-string arg)))))

(defun lmntal-send-shuffle (&optional arg)
  "Evaluate \":shuffle\" in interactive LMNtal process"
  (interactive "N")

  (if lmntal-interactive-process
      (lmntal-eval-string (lmntal-compose-leveled-string "shuffle" arg) )))

(defun lmntal-send-optimize (&optional arg)
  "Evaluate \":optimize\" in interactive LMNtal process"
  (interactive "N")

  (if lmntal-interactive-process
      (lmntal-eval-string (lmntal-compose-leveled-string "optimize" arg) )))

(defun lmntal-send-debug (&optional arg)
  "Evaluate \":debug\" in interactive LMNtal process"
  (interactive "N")

  (if lmntal-interactive-process
      (lmntal-eval-string (lmntal-compose-leveled-string "debug" arg) )))

(defun lmntal-send-verbose (&optional arg)
  "Evaluate \":verbose\" in interactive LMNtal process"
  (interactive "N")

  (if lmntal-interactive-process
      (lmntal-eval-string (lmntal-compose-leveled-string "verbose" arg) )))

(defun lmntal-send-clear ()
  "Evaluate \":clear\" in interactive LMNtal process"
  (interactive)

  (if lmntal-interactive-process
      (lmntal-eval-string ":clear")))

(defun kill-interactive-lmntal ()
  "Kill interactive LMNtal process."
  (interactive)
  (if (and lmntal-interactive-process
           (get-buffer lmntal-interactive-buffer-name))

      (progn
        (set-buffer lmntal-interactive-buffer-name)
        (kill-buffer-and-window)))
  (setq lmntal-interactive-process nil))

(defun install-lmntal-mode ()
  "�ޥ��ʡ��⡼�ɤȡ��᥸�㡼�⡼�ɤǶ��̤Υ��󥹥ȡ������"
  (set-syntax-table lmntal-mode-syntax-table)

  ;; Make local variables

  (make-local-variable 'font-lock-defaults)
  (make-local-variable 'case-fold-search)
  (make-local-variable 'paragraph-start)
  (make-local-variable 'paragraph-separate)
  (make-local-variable 'paragraph-ignore-fill-prefix)
  (make-local-variable 'indent-line-function)

  (make-local-variable 'indent-region-function)
  (make-local-variable 'parse-sexp-ignore-comments)
  (make-local-variable 'comment-start)
  (make-local-variable 'comment-end)
  (make-local-variable 'comment-column)
  (make-local-variable 'comment-start-skip)

  (make-local-variable 'comment-indent-hook)
  (make-local-variable 'defun-prompt-regexp)
  (make-local-variable 'compile-command)
  (make-local-variable 'imenu-generic-expression)
  
  ;; Set local variables
  (setq imenu-generic-expression lmntal-imenu-generic-expression)

  (setq case-fold-search 		t
        paragraph-start 		(concat "^$\\|" page-delimiter) ; ����?
        paragraph-separate 		paragraph-start
        paragraph-ignore-fill-prefix 	t ; �褯�狼��ʤ��������ꤷ�Ƥ���
        indent-line-function 		'prolog-indent-line
        parse-sexp-ignore-comments 	t
        comment-start 			"// "

        comment-end 			""
        comment-column 			32
        comment-start-skip 		lmntal-comment-start-skip))

(defun lmntal-mode ()
  "Major mode for editing LMNtal code."
  (interactive)
  (kill-all-local-variables)
  (setq major-mode 'lmntal-mode)

  (setq mode-name "LMNtal")
  (setq major-mode 'lmntal-mode)
  (set-syntax-table lmntal-mode-syntax-table)
  (use-local-map lmntal-mode-map)
  (install-lmntal-mode)
  ;; font lock
  (setq font-lock-defaults

	(list
	 'lmntal-font-lock-keywords nil nil))
  (run-hooks 'lmntal-mode-hook))

(defvar lmntal-minor-mode nil
  "Mode variable for LMNtal minor mode.")
(make-variable-buffer-local 'lmntal-minor-mode)

(defun lmntal-minor-mode (&optional arg)

  "LMNtal minor mode."
  (interactive "P")
  (setq lmntal-minor-mode
        (if (null arg)
            (not lmntal-minor-mode)
          (> (prefix-numeric-value arg) 0)))

  (if lmntal-minor-mode
      (progn
        (if (not (assq 'lmntal-minor-mode minor-mode-alist))
            (setq minor-mode-alist
                  (cons '(lmntal-minor-mode " LMNtal")
                        minor-mode-alist)))

        (install-lmntal-mode)
         ;; font lock
        (setq font-lock-defaults
              (list 'lmntal-minor-font-lock-keywords nil nil))
        ;; Fundamental�ξ�硢font-lock��ͭ���ˤʤ�ʤ��ä��Τ�����Ū��ON�ˤ���
        (if global-font-lock-mode
            (font-lock-mode t))
        ;; LMNtal������� '#' �������Ȥ�Ƚ�Ǥ���Ƥ��ޤ��Τ�OFF�ˤ��롣

        (modify-syntax-entry ?# ".")
        (run-hooks 'lmntal-mode-hook))
    ()))

(provide 'lmntal)
