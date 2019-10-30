; ----------------------------------
; The GB-J Compiler
; Copyright (C) 2019 robbi-blechdose
; Licensed under GNU AGPLv3
; (See LICENSE.txt for full license)
; ----------------------------------
;+--------------------------+
;| Author: robbi-blechdose           |
;+--------------------------+
INCLUDE "stdlib/hardware.inc"

 SECTION "Utils", ROM0

readPad:
    ldh     a, [hPad]
    ldh     [hPadLast], a

    ld      a, %00100000 ; Bit 4: 0, bit 5: 1 (D-Pad on, buttons off)
    ld      [rP1], a
    ; Several readings to avoid bouncing
    ld      a, [rP1]
    ld      a, [rP1]
    ld      a, [rP1]
    ld      a, [rP1]
    and     $0F ; Mask out the upper 4 bits (they're irrelevant)
    swap    a ; Exchange lower and upper 4 bits
    ld      b, a ; Backup a into b

    ld      a, %00010000 ; Bit 4: 1, bit 5: 0 (D-Pad off, buttons on)
    ld      [rP1], a
    ; Several readings to avoid bouncing
    ld      a, [rP1]
    ld      a, [rP1]
    ld      a, [rP1]
    ld      a, [rP1]
    and     $0F ; Mask out the upper 4 bits (they're irrelevant)
    or      b ; Set lower 4 bits to b (basically)
    cpl ; Set a to !a (all 1s are changed to 0s, all 0s to 1s)
        ; (because 0 is low, which means the buttons is pressed, but we want pressed to be a 1)
    ldh     [hPad], a ; Store the joypad state
    ld      a, $30
    ld      [rP1], a
    ret

; Usage:
; ld c, button index (bitmask: 7=D, 6=U, 5=L, 4=R, 3=Start, 2=Select, 1=B, 0=A)
; call isButtonPressed
; Returns:
; If button is pressed: c = 1, else c = 0
isButtonPressed:
    ld      a, [hPad]
    and     c
    jr      z, .notPressed
    ld      c, 1
    ret
.notPressed:
    ld      c, 0
    ret

; Usage:
; ld c, button index (bitmask: 7=D, 6=U, 5=L, 4=R, 3=Start, 2=Select, 1=B, 0=A)
; call wasButtonPressed
; Returns:
; If button was pressed: c = 1, else c = 0
wasButtonPressed:
    ld      a, [hPadLast]
    and     c
    jr      z, .notPressed
    ld      c, 1
    ret
.notPressed:
    ld      c, 0
    ret

; Used to switch between single- and double-speed on GBC
switchSpeed:
    ; Save interrupts
    ldh     a, [rIE]
    ld      b, a
    ; Disable interrupts
    xor     a
    ldh     [rIE], a
    ; Disable joypad lines (otherwise STOP may malfunction)
    ld      a, $30
    ldh     [rP1], a
    ; Request speed switch
    ld      a, 1
    ldh     [rKEY1], a
    ; Execute speed switch
    stop
    nop
    ; Restore interrupts
    ld      a, b
    ldh     [rIE], a
    ret