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
  li $8, 3
  sw $8, 0($17)
  li $9, 4
  add $16, $17, $9
  li $10, 1
  sw $10, 0($16)
  li $11, 8
  add $16, $17, $11
  li $12, 2
  sw $12, 0($16)
  li $13, 12
  add $16, $17, $13
  li $14, 3
  sw $14, 0($16)
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
  li $15, 0
  sw $15, 0($16)
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
  move $17, $v0
  li $18, 3
  sw $18, 0($17)
  li $19, 4
  add $16, $17, $19
  li $20, 1
  sw $20, 0($16)
  li $21, 8
  add $16, $17, $21
  li $22, 2
  sw $22, 0($16)
  li $23, 12
  add $16, $17, $23
  li $24, 3
  sw $24, 0($16)
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
