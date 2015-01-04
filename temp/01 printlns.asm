.data
  testString: .asciiz "Hello World\n"

.text
  la $a0 testString
  li $v0 4
  syscall
  li $v0 10
  syscall
