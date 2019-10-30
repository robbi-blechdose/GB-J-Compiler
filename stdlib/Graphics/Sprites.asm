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

 SECTION "Shadow_OAM", WRAM0, ALIGN[8]

shadowOAM::
    DS $A0

; Format:
; Spr Y
; Spr X
; Spr Tile ID
; Spr Attributes

 SECTION "DMA Routine", HRAM

dmaTransfer:
    DS 10

 SECTION "DMA", ROM0

DMATransfer:
    ld      a, HIGH(shadowOAM)
    ld      [rDMA], a
    ld      a, $28
.wait:
    dec     a
    jr      nz, .wait
    ret
DMATransferEnd:

copyDMARoutine:
    ld      hl, DMATransfer
    ld      de, dmaTransfer
    ld      bc, DMATransferEnd - DMATransfer
    ; call - ret pairs can be optimized away
    ;call    memcpy
    ;ret

memcpy:
.copyLoop:
    ld      a, [hl+]
    ld      [de], a
    inc     de
    dec     bc
    ld      a, b
    or      c
    jr      nz, .copyLoop
    ret

clearOAM:
    ld      hl, shadowOAM
    ld      b, 160
    xor     a
.loop:
    ld      [hl+], a
    dec     b
    jr      nz, .loop
    ret

 SECTION "StdSprites", ROM0

; Usage:
; ld a, spriteIndex
; ld b, xPosition
; ld c, yPosition
; call setSpritePosition
setSpritePosition:
    ; Each entry is 4 bytes big (also, we can do this in a without overflowing since there are only 40 sprites)
    add     a, a
    add     a, a
    ld      d, 0
    ld      e, a
    ld      hl, shadowOAM
    add     hl, de

    ld      [hl], c
    inc     hl
    ld      [hl], b

    ret

; Usage:
; ld a, spriteIndex
; ld b, tileId
; call setSpriteTile
setSpriteTile:
    ; Each entry is 4 bytes big (also, we can do this in a without overflowing since there are only 40 sprites)
    add     a, a
    add     a, a
    ; We want the 3rd byte (tile ID)
    add     a, 2

    ld      h, 0
    ld      l, a

    ld      de, shadowOAM
    add     hl, de
    
    ld      [hl], b

    ret

; Usage:
; ld a, spriteIndex
; ld b, spriteAttributes
; call setSpriteAttributes
setSpriteAttributes:
    ; Each entry is 4 bytes big (also, we can do this in a without overflowing since there are only 40 sprites)
    add     a, a
    add     a, a
    ; We want the 4th byte (attributes)
    add     a, 3

    ld      h, 0
    ld      l, a

    ld      de, shadowOAM
    add     hl, de
    
    ld      [hl], b

    ret