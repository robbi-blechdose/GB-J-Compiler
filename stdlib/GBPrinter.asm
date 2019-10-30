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

 SECTION "GBPrinterVars", HRAM

hChecksum: DS 2
hMapTilePointer: DS 2
hTileBytePointer: DS 2

 SECTION "GBPrinter", ROM0

; Sends one byte out via the serial port, and simultaneously receives one
sendByte:
    ldh     [rSB], a
    ld      a, $81
    ldh     [rSC], a
.waitSend:
    ldh     a, [rSC]
    and     %10000000
    jr      nz, .waitSend
    ret

initData:
DB $88, $33, $01, $00, $00, $00, $01, $00, $00, $00

; Sends the init packet
; And then checks if the printer is connected (response $81 $00 = connected ; $FF $FF = not connected)
; Returns 1 in bc if connection is OK, 0 if it's not
initPrinter:
    ld      hl, initData
    ld      b, 9
.loop:
    ld      a, [hl+]
    call    sendByte
    dec     b
    jr      nz, .loop
; Last byte sent was a $00 that the printer responds to
; We check the response here
.response1:
    ld      a, [hReceived]
    cp      $FF
    jr      z, .connFailed
    cp      $81
    jr      nz, .error
; We send another $00 that the printer again responds to
; This response is also checked
.send2:
    ld      a, [hl+]
    call    sendByte
.response2:
    ld      a, [hReceived]
    cp      $FF
    jr      z, .connFailed
    cp      $00
    jr      nz, .error
    ld      bc, 1
    ret
.connFailed:
    ld      bc, 0
    ret
.error:
    ld      b, 0
    ld      c, a
    ret

dataHeader:
DB $88, $33, $04, $00, $80, $02

dataEndPacket:
DB $88, $33, $04, $00, $00, $00, $04, $00, $00, $00

; Transfers data from the window layer to the printer
transferData:
    ld      hl, _SCRN1
    ld      a, l
    ld      [hMapTilePointer], a
    ld      a, h
    ld      [hMapTilePointer + 1], a

    ld      b, 9 ; Send 9 data packets
    ; Send packet
.sendPacket:
    push    bc
    ; Reset checksum
    ld      hl, $86
    ld      a, l
    ld      [hChecksum], a
    ld      a, h
    ld      [hChecksum + 1], a

    ; Send header
    ld      hl, dataHeader
    ld      c, 6
.sendHeaderByte:
    ld      a, [hl+]
    call    sendByte
    dec     c
    jr      nz, .sendHeaderByte
    
    ld      c, 40 ; 40 tiles per packet

.sendTile:
    ld      a, [hMapTilePointer]
    ld      e, a
    ld      a, [hMapTilePointer + 1]
    ld      d, a

    ld hl,  rSTAT
.waitVRAM:
    bit     1,  [hl]
    jr      nz, .waitVRAM

    ; Get the pointer to the tile data
    push    de
    ld      a, [de]
    ld      l, a
    ld      h, 0
    add     hl, hl
    add     hl, hl
    add     hl, hl
    add     hl, hl
    ld      de, _VRAM
    add     hl, de

    pop     de
    inc     de
    ld      a, e
    ld      [hMapTilePointer], a
    ld      a, d
    ld      [hMapTilePointer + 1], a

    ld      a, l
    ld      [hTileBytePointer], a
    ld      a, h
    ld      [hTileBytePointer + 1], a

    ; Send tile data
    ld      b, 16

    ld      a, [hTileBytePointer]
    ld      e, a
    ld      a, [hTileBytePointer + 1]
    ld      d, a
.sendByte:

    ld hl,  rSTAT
.waitVRAM2:
    bit 1,  [hl]
    jr      nz, .waitVRAM2

    ; Send the byte out
    ld      a, [de]
    push    de
    ld      e, a
    call    sendByte
    ; Add it to the checksum
    ldh     a, [hChecksum]
    ld      l, a
    ldh     a, [hChecksum + 1]
    ld      h, a
    ld      d, 0
    add     hl, de
    ld      a, l
    ldh     [hChecksum], a
    ld      a, h
    ldh     [hChecksum + 1], a
    pop     de
    inc     de
    ld      a, e
    ld      [hTileBytePointer], a
    ld      a, d
    ld      [hTileBytePointer + 1], a

    dec     b
    jr      nz, .sendByte

    ld      a, c
    cp      21
    call    z, .nextLine
    cp      1
    call    z, .nextLine

    dec     c
    jr      nz, .sendTile

    ; Send checksum and dummy
    ld      a, [hChecksum]
    call    sendByte
    ld      a, [hChecksum + 1]
    call    sendByte

    xor     a
    call    sendByte
    xor     a
    call    sendByte

    pop     bc
    dec     b
    jp      nz, .sendPacket

    ; Send data end packet
    ld      hl, dataEndPacket
    ld      b, 10
.sendDataEndByte:
    ld      a, [hl+]
    call    sendByte
    dec     b
    jr      nz, .sendDataEndByte
    ret

.nextLine:
    push    hl
    push    bc
    ld      a, [hMapTilePointer]
    ld      l, a
    ld      a, [hMapTilePointer + 1]
    ld      h, a
    ld      bc, 12
    add     hl, bc
    ld      a, l
    ld      [hMapTilePointer], a
    ld      a, h
    ld      [hMapTilePointer + 1], a
    pop     bc
    pop     hl
    ret

NULPacket:
DB $88, $33, $0F, $00, $00, $00, $0F, $00, $00, $00

; Checks the printer's status
getStatus:
    ld      hl, NULPacket
    ld      b, 10
.loop:
    ld      a, [hl+]
    call    sendByte
    dec     b
    jr      nz, .loop
    
    ld      a, [hReceived]
    ld      c, a
    ld      b, 0

    ret

startPrintPacket:
DB $88, $33, $02, $00, $04, $00, $01, $13, $E4, $40, $3E, $01, $00, $00

; Sends a "start print" packet to the printer
startPrint:
    ld      hl, startPrintPacket
    ld      b, 14
.loop:
    ld      a, [hl+]
    call    sendByte
    dec     b
    jr      nz, .loop

    ld      a, [hReceived]
    ld      c, a
    ld      b, 0

    ret