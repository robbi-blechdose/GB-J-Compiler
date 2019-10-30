; ----------------------------------
; The GB-J Compiler
; Copyright (C) 2019 robbi-blechdose
; Licensed under GNU AGPLv3
; (See LICENSE.txt for full license)
; ----------------------------------
;+--------------------------+
;| Author: robbi-blechdose           |
;| Header file used for     |
;| every ROM                |
;+--------------------------+
INCLUDE "stdlib/hardware.inc"

 SECTION "GBPrinterVars", HRAM

hReceived: DS 1

 SECTION "UtilsVars", HRAM

hPad: DS 1
hPadLast: DS 1

 SECTION "Stack", WRAM0

wStack:
    DS 256
wStackEnd:

 SECTION "Header", ROM0[$0000]

; $0000 - $003F: RST handlers.
waitVBlank:
    ld      a, [rLY]
    cp      145 ; Check LCDC y coordinate, see if we're in VBlank
    jr      nz, waitVBlank ; If we're not in VBlank, wait until we are
    ret
nop

; $0008
ret
REPT 7
    nop
ENDR
; $0010
ret
REPT 7
    nop
ENDR
; $0018
ret
REPT 7
    nop
ENDR
; $0020
ret
REPT 7
    nop
ENDR
; $0028
ret
REPT 7
    nop
ENDR
; $0030
ret
REPT 7
    nop
ENDR
; $0038 - Error handler (TODO)
ret
REPT 7
    nop
ENDR

; $0040 - $0067: Interrupt handlers.
jp VBlank
REPT 5
    nop
ENDR
; $0048
;jp stat
REPT 8
    nop
ENDR
; $0050
;jp timer
REPT 8
    nop
ENDR
; $0058
serial:
    push    af
    ldh     a, [rSB]
    ldh     [hReceived], a
    pop     af
    reti
    nop
; $0060
;jp joypad
REPT 8
    nop
ENDR

; $0068 - $00FF: Free space.
DS $98

; $0100 - $0103: Startup handler.
nop
jp      startup

REPT 76
    DB 0
ENDR

; Startup routine sets up important things
; -> Stack
; -> Interrupts
; -> 0ing out variables
; -> Initializing the heap
startup:
    ld      sp, wStackEnd
    ld      a, IEF_SERIAL | IEF_VBLANK
    ld      [rIE], a
    ld      a, $FF
    ld      [hReceived], a
    xor     a
    ld      [hPad], a
    ld      [hPadLast], a
    call    initHeap
    jp      main
        
INCLUDE "stdlib/Maths.asm"
INCLUDE "stdlib/MemoryAllocator.asm"

; Written by ISSOtm
; Arguments:
; a: Index
; c: Maximum index of the table
; de: Base address of the table
jumpTableCaller:
    cp      c
    ret     nc
    add     a, a
    ; Required for 8-bit indices
    jr      nc, .noCarry
    inc     d
.noCarry:
    ; End
    add     a, e
    ld      l, a
    adc     a, d
    sub     l
    ld      h, a
    ld      a, [hli]
    ld      h, [hl]
    ld      l, a
    or      h
    ret     z
    jp      hl