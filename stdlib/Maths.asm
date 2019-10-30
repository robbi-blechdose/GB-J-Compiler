; ----------------------------------
; The GB-J Compiler
; Copyright (C) 2019 robbi-blechdose
; Licensed under GNU AGPLv3
; (See LICENSE.txt for full license)
; ----------------------------------
;+--------------------------+
;| Author: robbi-blechdose           |
;+--------------------------+

 SECTION "Temp", HRAM

hTemp: DS 8
hOverflow: DS 2

 SECTION "Maths", ROM0

; Taken from http://wikiti.brandonw.net/index.php?title=Z80_Routines:Math:Division
; Modified to work on LR35902 by robbi-blechdose (with some tips from ISSOtm)
; Arguments:
; d: first number
; e: second number
; Returns:
; d: quotient
; a: remainder
_div8_8:
    xor     a
    ld      b, 8
.loop:
    sla     d
    rla
    cp      e
    jr      c, .skip
    sub     e
    inc     d
.skip:
    dec     b
    jr      nz, .loop

    ret

; Taken from http://wikiti.brandonw.net/index.php?title=Z80_Routines:Math:Division
; Modified to work on LR35902 by robbi-blechdose (with some tips from ISSOtm)
; Arguments:
; hl: first number
; c: second number
; Returns:
; hl: quotient
; a: remainder
_div16_8:
    xor     a
    ld      b, 16
.loop:
    add     hl, hl
    rla
    jr      c, .skip
    cp      c
    jr      c, .skip2
.skip:
    sub     c
    inc     l
.skip2:
    dec     b
    jr      nz, .loop
   
    ret

; Taken from: https://github.com/NovaSquirrel/GameBoyFALSE/blob/master/game.z80
; Arguments:
; bc: first number
; de: second number
; Returns:
; bc: quotient
; de: remainder
_div16_16:
    ld      hl, hTemp
    ld      [hl], e
    inc     hl
    ld      [hl], d
    inc     hl
    ld      [hl], 17
    ld      de, 0
.nxtbit:
    ld      hl, hTemp + 2
    ld      a, c
    rla
    ld      c, a
    ld      a, b
    rla
    ld      b, a
    dec     [hl]
    ret     z
    ld      a, e
    rla
    ld      e, a
    ld      a, d
    rla
    ld      d, a
    dec     hl
    dec     hl
    ld      a, e
    sub     [hl]
    ld      e, a
    inc     hl
    ld      a, d
    sbc     a, [hl]
    ld      d, a
    jr      nc, .noadd

    dec     hl
    ld      a, e
    add     a, [hl]
    ld      e, a
    inc     hl
    ld      a, d
    adc     a, [hl]
    ld      d, a
.noadd:
    ccf
    jr      .nxtbit

; Taken from http://wikiti.brandonw.net/index.php?title=Z80_Routines:Math:Multiplication
; Modified to work on LR35902 by robbi-blechdose
; Arguments:
; h: first number
; e: second number
; Returns:
; l: result
; h: overflow byte (also stored in HRAM)
_mul8_8:
    ld      l, 0
    ld      d, l

    sla     h
    jr      nc, .skip
    ld      l, e
.skip:
    ld      b, 7
.loop:
    add     hl, hl          
    jr      nc, .skip2
    add     hl, de
.skip2:
    dec     b
    jr      nz, .loop
    ; Store overflow byte into HRAM
    ld      a, h
    ld      [hOverflow], a
    ret

; Taken from http://wikiti.brandonw.net/index.php?title=Z80_Routines:Math:Multiplication
; Modified to work on LR35902 by robbi-blechdose
; Arguments:
; de: first number
; a: second number
; Returns:
; hl: result
; a: overflow byte (also stored in HRAM)
_mul16_8:
    ld      c, 0
    ld      h, c
    ld      l, h

    add     a, a ; optimised 1st iteration
    jr      nc, .skip
    ld      h, d
    ld      l, e
.skip:
    ld      b, 7
.loop:
    add     hl, hl
    rla
    jr      nc, .skip2
    add     hl, de
    adc     a, c ; yes this is actually adc a, 0 but since c is free we set it to zero and so we can save 1 byte and up to 3 T-states per iteration
.skip2:
    dec     b
    jr      nz, .loop
    ; Store overflow byte into HRAM
    ld      [hOverflow], a
    ret

; Taken from http://wikiti.brandonw.net/index.php?title=Z80_Routines:Math:Multiplication
; Modified to work on LR35902 by robbi-blechdose
; Arguments:
; bc: first number
; de: second number
; Returns:
; hl: result
; de: overflow bytes (also stored in HRAM)
_mul16_6:
    ld      hl, 0

    sla     e ; optimised 1st iteration
    rl      d
    jr      nc, .skip
    ld      h, b
    ld      l, c
.skip:
    ld      a, 15
.loop:
    add     hl, hl
    rl      e
    rl      d
    jr      nc, .skip2
    add     hl, bc
    jr      nc, .skip2
    inc     de
.skip2:
    dec     a
    jr      nz, .loop
    ; Store overflow bytes into HRAM
    ld      a, e
    ld      [hOverflow], a
    ld      a, d
    ld      [hOverflow + 1], a
    ret