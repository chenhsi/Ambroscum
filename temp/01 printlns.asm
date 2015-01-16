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
  move $10, $v0
  li $8, 3
  sw $8, 4($10)
  li $12, 4
  add $8, $10, $12
  li $13, 1
  sw $13, 4($8)
  li $14, 8
  add $8, $10, $14
  li $15, 2
  sw $15, 4($8)
  li $16, 12
  add $8, $10, $16
  li $17, 3
  sw $17, 4($8)
  li $v0, 1
  move $a0, $10
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 4
  li $v0, 9
  syscall
  move $8, $v0
  li $18, 0
  sw $18, 4($8)
  li $v0, 1
  move $a0, $8
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 16
  li $v0, 9
  syscall
  move $10, $v0
  li $19, 3
  sw $19, 4($10)
  li $20, 4
  add $8, $10, $20
  li $21, 1
  sw $21, 4($8)
  li $22, 8
  add $8, $10, $22
  li $23, 2
  sw $23, 4($8)
  li $24, 12
  add $8, $10, $24
  li $25, 3
  sw $25, 4($8)
  li $v0, 1
  li $a0, 1
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
  li $12, 0
  sub $8, $12, $10
  li $v0, 1
  move $a0, $8
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $13, 0
  sub $8, $13, $10
  add $8, $10, $8
  li $v0, 1
  move $a0, $8
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
  li $a0, 1
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
  li $v0, 4
  la $a0, string2
  syscall
  li $v0, 4
  la $a0, string1
  syscall
  li $v0, 1
  move $a0, $10
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
