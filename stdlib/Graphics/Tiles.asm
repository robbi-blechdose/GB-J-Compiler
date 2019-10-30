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

 SECTION "StdTiles", ROM0

; Usage:
; push vramBank
; push tileStart (offset from tile 0 in tiles)
; push numberOfTiles
; push pointerToTileData
; call loadTiles
; add sp, 6
loadTiles:
    ld      hl, sp + 8
    ld      a, [hl]
    ld      [rVBK], a

    ld      hl, sp + 4
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a

    ; Multiply by 16, we want the number of bytes to copy (16 bytes per tile)
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    ld      b, h
    ld      c, l

    ld      hl, sp + 6
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a

    ; Multiply by 16, we want the number of bytes (16 bytes per tile)
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    ld      de, _VRAM
    add     hl, de
    ld      d, h
    ld      e, l

    ld      hl, sp + 2
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
.loadTile:
    ld      a, [hl+]
    ld      [de], a
    inc     de

    dec     bc
    ld      a, b
    or      c
    jr      nz, .loadTile

    ret

; Places a single tile of the given ID at the given position in the BG map
; Usage:
; ld a, vramBank
; ld b, tileID
; ld c, xPos
; ld d, yPos
; call placeBGTile
placeBGTile:
    ld      l, d
    ld      de, _SCRN0
    jr      placeTile

; Places a single tile of the given ID at the given position in the window map
; Usage:
; ld a, vramBank
; ld b, tileID
; ld c, xPos
; ld d, yPos
; call placeWinTile
placeWinTile:
    ld      l, d
    ld      de, _SCRN1
    ;jr      setTile ; Unnecessary

; Places a single tile of the given ID at the given position
; Never call this directly!
; Usage:
; ld a, vramBank
; ld b, tileID
; ld c, xPos
; ld l, yPos
; ld de, VRAM location (_SCRN0 or _SCRN1)
; call placeTile
placeTile:
    ld      [rVBK], a

    ; Put the tile ID into a
    ld      a, b

    ; c is already loaded with the X pos
    ld      b, 0
    ; l is already loaded with the Y pos
    ld      h, 0
    ; Multiply by 32
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    ; Add x position
    add     hl, bc
    add     hl, de

    ld      [hl], a

    ret

loadBGMap:
    ld      de, _SCRN0
    jr      loadMap

loadWinMap:
    ld      de, _SCRN1
    ;jr      loadMap ; Unnecessary

; Loads a map
; The VRAM bank parameter allows repurposing this as attribute loading function
; Don't call this directly, use loadBGMap and loadWinMap instead!
; Usage:
; push vramBank
; push pointerToMapData
; push xPos
; push yPos
; push xSize
; push ySize
; call loadMap
; add sp, 12
loadMap:
    ld      hl, sp + 12
    ld      a, [hl]
    ld      [rVBK], a

    ld      hl, sp + 8
    ld      c, [hl]
    ld      b, 0

    ld      hl, sp + 6
    ld      l, [hl]
    ld      h, 0
    ; Multiply by 32
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl

    add     hl, bc
    add     hl, de
    ld      d, h
    ld      e, l

    ld      hl, sp + 4
    ld      b, [hl]

    ld      hl, sp + 2
    ld      c, [hl]

    ld      hl, sp + 10
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a

.loadTile:
    ld      a, [hl+]
    ld      [de], a
    inc     de
    dec     b
    jr      nz, .loadTile
    dec     c
    ret     z
    push    hl

    ld      hl, sp + 6 ; +2 because of the push before!
    ld      b, [hl]

    ld      hl, 32
    add     hl, de

    ld      a, l
    sub     b
    ld      l, a
    ld      a, h
    sbc     0
    ld      h, a

    ld      d, h
    ld      e, l

    pop     hl
    jr      .loadTile

; Usage:
; ld a, xScroll
; ld b, yScroll
; call setBGScroll
setBGScroll:
    ld      [rSCX], a
    ld      a, b
    ld      [rSCY], a
    ret

; Usage:
; ld a, xPos
; ld b, yPos
; call setWinPosition
setWinPosition:
    ld      [rWX], a
    ld      a, b
    ld      [rWY], a
    ret