li t0, 69        # Push 69
addi sp, sp, -4
sw t0, (sp)
li t0, 420        # Push 420
addi sp, sp, -4
sw t0, (sp)
lw t0, (sp)    # add
lw t1, 4(sp)
add t0, t1, t0
addi sp, sp, 4
sw t0, (sp)
li t0, 3        # Push 3
addi sp, sp, -4
sw t0, (sp)
lw t0, (sp)    # add
lw t1, 4(sp)
add t0, t1, t0
addi sp, sp, 4
sw t0, (sp)