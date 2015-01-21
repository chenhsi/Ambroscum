.data
  stringTrue: .asciiz "true"
  stringFalse: .asciiz "false"
  stringNewline: .asciiz "\n"
  string0: .asciiz "\\n"
  string1: .asciiz " "
  string2: .asciiz "["
  string3: .asciiz "]"

.text

_b1:
  li $v0, 1
  li $a0, 1
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 16
  li $v0, 9
  syscall
  move $17, $v0
  li $t8, 3
  sw $t8, 0($17)
  li $t9, 4
  add $16, $17, $t9
  li $t8, 1
  sw $t8, 0($16)
  li $t9, 8
  add $16, $17, $t9
  li $t8, 2
  sw $t8, 0($16)
  li $t9, 12
  add $16, $17, $t9
  li $t8, 3
  sw $t8, 0($16)
  li $v0, 1
  move $a0, $17
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 4
  li $v0, 9
  syscall
  move $16, $v0
  li $t8, 0
  sw $t8, 0($16)
  li $v0, 1
  move $a0, $16
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 16
  li $v0, 9
  syscall
  move $18, $v0
  li $t8, 3
  sw $t8, 0($18)
  li $t9, 4
  add $17, $18, $t9
  li $t8, 1
  sw $t8, 0($17)
  li $t9, 8
  add $16, $18, $t9
  li $t8, 2
  sw $t8, 0($16)
  li $t9, 12
  add $16, $18, $t9
  li $t8, 3
  sw $t8, 0($16)
  move $16, $17
  lw $16, 0($16)
  li $v0, 1
  move $a0, $16
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, -10
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 23
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 1
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 3
  syscall
  li $v0, 4
  la $a0, string1
  syscall
  li $v0, 1
  li $a0, -3
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 0
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  la $a0, stringTrue
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  la $a0, stringTrue
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  la $a0, stringFalse
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 4
  la $a0, string2
  syscall
  li $v0, 4
  la $a0, string1
  syscall
  li $v0, 1
  li $a0, 3
  syscall
  li $v0, 4
  la $a0, string1
  syscall
  li $v0, 4
  la $a0, string3
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
_b2:
  li $v0 10
  syscall
