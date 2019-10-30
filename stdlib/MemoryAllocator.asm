; ----------------------------------
; The GB-J Compiler
; Copyright (C) 2019 robbi-blechdose
; Licensed under GNU AGPLv3
; (See LICENSE.txt for full license)
; ----------------------------------
;+--------------------------+
;| Author: robbi-blechdose           |
;| Improved with            |
;| suggestions from ISSOtm  |
;+--------------------------+
INCLUDE "stdlib/hardware.inc"

 SECTION "Heap", WRAMX, BANK[1], ALIGN[8]

; Memory block format:
; 2 byte size (max 32767 bytes per block),
;   also bit 7 of the second byte (little-endian order, so this actually is the first byte but whatever) determines whether this block is in use (0=free, 1=in use)
; 2 bytes pointer to next header/metadata block
; 2 bytes pointer to previous header/metadata block
; Data...

 ; Allocate 2K of memory
Heap:
DS 2 * 1024
.end:

SECTION "MemAllocatorVars", HRAM

hBiggestBlockPtr: DS 2
hBiggestBlockSize: DS 2
hPreviousBlockPtr: DS 2
hCurrentBlockPtr: DS 2
hRequestSize: DS 2
hRequestSizePlusHeader: DS 2
hNextPtr: DS 2

 SECTION "MemAllocator", ROM0

initHeap:
    ld      hl, Heap
    ld      bc, (Heap.end - Heap) - 6 ; Size of first block: entire heap (minus metadata)
    ; Set size
    ld      a, c
    ld      [hl+], a
    ld      a, b
    ld      [hl+], a
    ; Set pointer to next and previous block to NULL since no next block exists
    xor     a
    ld      [hl+], a
    ld      [hl+], a
    ld      [hl+], a
    ld      [hl+], a

; Zero out Heap, only needed for testing purposes
; In reality, data is just overwritten so we don't care if there's garbage there
    ld      bc, (Heap.end - Heap) - 6
    ld      hl, Heap + 6
.zeroByte:
    xor     a
    ld      [hl+], a
    dec     bc
    ld      a, b
    or      c
    jr      nz, .zeroByte

    ret

; Arguments:
; bc: size of block to allocate
; Returns:
; hl: pointer to memory block, if one was found
; carry flag: set if a block was found
malloc:
    ; Set smallest block pointer to NULL
    xor     a
    ldh     [hBiggestBlockPtr], a
    ldh     [hBiggestBlockPtr + 1], a
    ; Set biggest block size to 0, so that any actual block will be put here instead
    ldh     [hBiggestBlockSize], a
    ldh     [hBiggestBlockSize + 1], a
    ; Store the requested size into HRAM
    ld      a, c
    ldh     [hRequestSize], a
    ld      a, b
    ldh     [hRequestSize + 1], a
    ; Compute the full size of the block and store into HRAM
    ld      hl, 6
    add     hl, bc
    ld      a, l
    ldh     [hRequestSizePlusHeader], a
    ld      a, h
    ldh     [hRequestSizePlusHeader + 1], a

    ld      bc, 0
    ld      hl, Heap
.checkBlock:
    ld      c, [hl]
    inc     hl
    ld      a, [hl]
    and     %10000000
    jr      nz, .nextBlock
    ld      a, [hl]
    and     %01111111 ; Mask out the isFree bit
    ld      b, a

    ; Check if we have an exact fit
    ld      a, [hRequestSize]
    cp      c
    jr      nz, .nextBlock
    ld      a, [hRequestSize + 1]
    cp      b
    jr      nz, .nextBlock

    ; Allocate block
    dec     hl
    set     7, [hl] ; Set bit 7 - block is now in use
    scf     ; Set carry flag - we've found a block
    ret

.nextBlock:
    ; Get pointer to next block
    inc     hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    ; Check if h is 0, if so, we have a NULL pointer
    ld      a, h
    and     a
    jr      nz, .checkBlock

; We're at the end of the heap and have found no block - let's try to find the biggest block and split that
    ld      hl, Heap
.checkBlock2:
    ld      a, [hl+]
    ld      b, [hl]
    ld      c, a

    ld      a, [hRequestSizePlusHeader + 1]
    cp      b
    jr      nz, .isLess ; If nz, then the requested size might be less than the size of the block or bigger
    ; If it is the same, check the LSB
    ld      a, [hRequestSizePlusHeader]
    cp      c
.isLess:
    jr      nc, .checkBlock2
    ; Compare against current biggest block
    ld      a, [hBiggestBlockSize + 1]
    cp      b
    jr      z, .checkLowByte
    jr      c, .checkLowByte
    jr      .nextBlock2
.checkLowByte:
    ld      a, [hBiggestBlockSize]
    cp      c
    jr      nc, .nextBlock2
    ; Current block is bigger than the biggest block, set biggest block ptr and size
    dec     hl
    ld      a, l
    ld      [hBiggestBlockPtr], a
    ld      a, h
    ld      [hBiggestBlockPtr + 1], a
    ld      a, c
    ld      [hBiggestBlockSize], a
    ld      a, b
    ld      [hBiggestBlockSize + 1], a
    inc     hl
.nextBlock2:
    ; Check next block
    inc     hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    ; Check if h is 0, if so, we have a NULL pointer
    ld      a, h
    and     a
    jr      z, .done
    jr      .checkBlock2

.done:
    ld      a, [hBiggestBlockPtr + 1]
    and     a
    jr      nz, .split
    ; We didn't find a block - tell the caller we failed :(
    xor     a ; Clear carry flag - we did not find a block
    ret

.split:
    ld      a, [hBiggestBlockPtr]
    ld      l, a
    ld      a, [hBiggestBlockPtr + 1]
    ld      h, a
    ; Store pointer to this block
    ld      a, l
    ldh     [hPreviousBlockPtr], a
    ld      a, h
    ldh     [hPreviousBlockPtr + 1], a
    ; Compute new size of the block
    ldh     a, [hRequestSizePlusHeader]
    ld      e, a ; Some inefficient register juggling but whatever
    ld      a, c
    sub     e
    ld      e, a
    ld      [hl+], a
    ldh     a, [hRequestSizePlusHeader + 1]
    ld      d, a
    ld      a, b
    sub     d
    ld      d, a
    ld      [hl+], a
    ; Store pointer to next block
    ld      a, [hl+]
    ldh     [hNextPtr], a
    ld      a, [hl]
    ldh     [hNextPtr + 1], a
    dec     hl
    ; Compute pointer to the new block
    ldh     a, [hPreviousBlockPtr]
    add     e
    ld      [hl+], a
    ld      e, a
    ldh     a, [hPreviousBlockPtr + 1]
    add     d
    ld      [hl+], a
    ; Go to new block
    ld      h, a
    ld      l, e
    ; Write size
    ldh     a, [hRequestSize]
    set     7, a ; This block is now in use
    ld      [hl+], a
    ldh     a, [hRequestSize + 1]
    ld      [hl+], a
    ; Write pointer to next block
    ldh     a, [hNextPtr]
    ld      [hl+], a
    ldh     a, [hNextPtr + 1]
    ld      [hl+], a
    ; Write pointer to previous block
    ldh     a, [hPreviousBlockPtr]
    ld      [hl+], a
    ldh     a, [hPreviousBlockPtr + 1]
    ld      [hl], a
    
    scf     ; Set carry flag - we've found a block
    ret

; Old TODO: Check next pointer as NULL pointer - I don't think this is still relevant, but maybe it should be checked again just to make sure
; hl: Pointer to block to be freed
free:
    push    hl
    ; Set the block itself to be free
    ld      a, [hl]
    and     %01111111
    ld      [hl+], a
    ; Check next block
    inc     hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    ; Check if this block even exists
    ld      a, h
    and     a
    jr      z, .prevBlock
    ; Check if block is free
    ld      a, [hl]
    and     %10000000
    jr      z, .prevBlock

    ; Merge blocks
    ld      c, [hl]
    inc     hl
    ld      b, [hl]
    inc     hl
    ld      e, [hl]
    inc     hl
    ld      d, [hl]

    pop     hl
    push    hl

    inc     hl
    inc     hl
    ld      [hl], e
    inc     hl
    ld      [hl], d
    dec     hl
    dec     hl
    dec     hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    add     hl, bc
    ld      b, h
    ld      c, l

    pop     hl
    push    hl

    ld      [hl], c
    inc     hl
    ld      [hl], b
    
    ; Check previous block
.prevBlock:
    pop     hl
    push    hl
    ; Save size and pointer to next block
    ld      c, [hl]
    inc     hl
    ld      b, [hl]
    inc     hl
    ld      e, [hl]
    inc     hl
    ld      d, [hl]

    pop     hl
    inc     hl
    inc     hl
    inc     hl
    inc     hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    ; Check if this block even exists
    ld      a, h
    and     a
    ret     z
    ; Check if block is free
    ld      a, [hl]
    and     %10000000
    ret     z

    ; Merge blocks
    push    hl
    ld      a, [hl+]
    ld      h, [hl]
    ld      l, a
    add     hl, bc
    ld      b, h
    ld      c, l
    pop     hl
    ld      [hl], c
    inc     hl
    ld      [hl], b
    inc     hl
    ld      [hl], e
    inc     hl
    ld      [hl], d
    
    ret