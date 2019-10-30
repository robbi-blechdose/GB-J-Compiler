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
INCLUDE "stdlib/Graphics/Charset.z80"
INCLUDE "stdlib/Graphics/LCD.asm"

 SECTION "Std_Printer", ROM0

; Loads the charset as well as the GBC palettes (required if we are on GBC)
loadCharset:
    ld      a, BANK(GraphicsCharset)
    ld      [rROMB0], a

    ld      hl, _VRAM
    ld      de, GraphicsCharset
    ld      bc, 40 * 16
.loadByte:
    ld      a, [de]
    ld      [hl+], a
    inc     de
    dec     bc
    ld      a, b
    or      c
    jr      nz, .loadByte

;loadTileAttributes:
    ld      a, 1
    ld      [rVBK], a

    ld      hl, _SCRN0
    ld      bc, 32 * 32
.attrLoop:
    xor     a
    ld      [hl+], a
    dec     bc
    ld      a, b
    or      c
    jr      nz, .attrLoop

; load simple palette
    ld      hl, 0
    push    hl
    ld      hl, Palettes
    push    hl
    call    loadPalettes
    add     sp, 4

;clearScreen:
    xor     a
    ld      [rVBK], a

    ld      hl, _SCRN0
    ld      bc, 32 * 32

.clearTile:
    xor     a
    ld      [hl+], a
    dec     bc
    ld      a, b
    or      c
    jr      nz, .clearTile

    ret

; Usage:
; ld e, x
; ld c, y
; ld hl, pointerToData (Data must be ASCII, but charmapped (see charmap.asm))
; call print
print:
    xor     a
    ld      [rVBK], a

    ld      d, 0

    push    hl
    ld      h, 0
    ld      l, c
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, de

    ld      bc, _SCRN0
    add     hl, bc

    ld      b, h
    ld      c, l
    pop     hl

.printChar:
    ld      a, [hl+]
    and     a ; Faster than "cp 0"
    ret     z ; 0 terminator
    ld      [bc], a
    inc     bc
    jr      .printChar

; Usage:
; ld c, x
; ld l, y
; ld d, char
; call printNumber
printNumber:
    xor     a
    ld      [rVBK], a

    ld      b, 0
    ld      h, 0

    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, bc

    ld      bc, _SCRN0
    add     hl, bc

.firstChar:
    and     $F0
    rrc     a
    rrc     a
    rrc     a
    rrc     a
    inc     a
    ld      [hl+], a
.secondChar:
    ld      a, d
    and     $0F
    inc     a
    ld      [hl], a
    ret

Palettes:
; Use DW (B << 10) | (G << 5) | R
; Palette 0
DW (26 << 10) | (26 << 5) | 26
DW (15 << 10) | (15 << 5) | 15
DW (5 << 10) | (5 << 5) | 5
DW (0 << 10) | (0 << 5) | 0