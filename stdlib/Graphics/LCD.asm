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

 SECTION "Std_LCD", ROM0

lcdOff:
    rst     $00 ; Wait for VBlank
    xor     a
    ld      [rLCDC], a
    ret

; Usage:
; ld a, isOBJPal (0 = BG pal, 1 = OBJ Pal)
; ld hl, pointerToPalettes
; call loadPalettes
loadPalettes:
    ; Check palette type to be written
    and     a ; Faster than "cp 0"
    jr      z, .bgPal
.objPal:
    ld      de, rOCPD
    jr      .done
.bgPal:
    ld      de, rBCPD
.done:
    ld      a, %10000000 ; Set both palette index registers to auto-increment after each write
    ld      [rOCPS], a
    ld      [rBCPS], a
    ld      b, 8 * 4 * 2 ; Number of bytes (8 palettes, 4 colours per palette, 2 bytes per colour)

.loadByte:
    ld      a, [rSTAT] ; LCDC Status
    and     STATF_BUSY ; Compare Status with busy value (if they're equal, LCDC is busy, zero flag is not set)
    jr      nz, .loadByte ; If zero flag is not set (LCDC is busy), jump to .loadByte -> wait until it's safe to access VRAM
    ld      a, [hl+]; Load byte from palette into a, then increment hl (points to the next byte afterwards)
    ld      [de], a ; Load a byte of the palette into palette memory
    dec     b ; One byte is done, decrease b
    jr      nz, .loadByte ; If b is not 0 (zero flag is not set), do the next palette

    ret