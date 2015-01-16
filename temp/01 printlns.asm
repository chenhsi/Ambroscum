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
  move $9, $v0
  li $8, 3
  sw $8, 4($9)
  li $10, 4
  add $8, $9, $10
  li $11, 1
  sw $11, 4($8)
  li $12, 8
  add $8, $9, $12
  li $13, 2
  sw $13, 4($8)
  li $14, 12
  add $8, $9, $14
  li $15, 3
  sw $15, 4($8)
  li $v0, 1
  move $a0, $9
  syscall
  li $v0, 4
  la $a0, stringNewline
  syscall
  li $v0, 1
  li $a0, 4
  li $v0, 9
  syscall
  move $8, $v0
  li $16, 0
  sw $16, 4($8)
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
  move $9, $v0
  li $17, 3
  sw $17, 4($9)
  li $18, 4
  add $8, $9, $18
  li $19, 1
  sw $19, 4($8)
  li $20, 8
  add $8, $9, $20
  li $21, 2
  sw $21, 4($8)
  li $22, 12
  add $8, $9, $22
  li $23, 3
  sw $23, 4($8)
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
