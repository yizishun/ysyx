#include <am.h>
#include <klib-macros.h>
#include "ysyxSoC.h"
static int keymap[512] = {};

#define SCANCODE_KEYS(_) \
  _(ESCAPE, 0x76) _(F1, 0x05) _(F2, 0x06) _(F3, 0x04) _(F4, 0x0C) _(F5, 0x03) _(F6, 0x0B) _(F7, 0x83) _(F8, 0x0A) _(F9, 0x01) _(F10, 0x09) _(F11, 0x78) _(F12, 0x07) \
  _(GRAVE, 0x0E) _(1, 0x16) _(2, 0x1E) _(3, 0x26) _(4, 0x25) _(5, 0x2E) _(6, 0x36) _(7, 0x3D) _(8, 0x3E) _(9, 0x46) _(0, 0x45) _(MINUS, 0x4E) _(EQUALS, 0x55) _(BACKSPACE, 0x66) \
  _(TAB, 0x0D) _(Q, 0x15) _(W, 0x1D) _(E, 0x24) _(R, 0x2D) _(T, 0x2C) _(Y, 0x35) _(U, 0x3C) _(I, 0x43) _(O, 0x44) _(P, 0x4D) _(LEFTBRACKET, 0x54) _(RIGHTBRACKET, 0x5B) _(BACKSLASH, 0x5D) \
  _(CAPSLOCK, 0x58) _(A, 0x1C) _(S, 0x1B) _(D, 0x23) _(F, 0x2B) _(G, 0x34) _(H, 0x33) _(J, 0x3B) _(K, 0x42) _(L, 0x4B) _(SEMICOLON, 0x4C) _(APOSTROPHE, 0x52) _(RETURN, 0x5A) \
  _(LSHIFT, 0x12) _(Z, 0x1A) _(X, 0x22) _(C, 0x21) _(V, 0x2A) _(B, 0x32) _(N, 0x31) _(M, 0x3A) _(COMMA, 0x41) _(PERIOD, 0x49) _(SLASH, 0x4A) _(RSHIFT, 0x59) \
  _(LCTRL, 0x14) _(APPLICATION, 0x11) _(LALT, 0x11) _(SPACE, 0x29) _(RALT, 0x111) _(RCTRL, 0x114) \
  _(UP, 0x175) _(DOWN, 0x172) _(LEFT, 0x16B) _(RIGHT, 0x174) _(INSERT, 0x170) _(DELETE, 0x171) _(HOME, 0x16C) _(END, 0x169) _(PAGEUP, 0x17D) _(PAGEDOWN, 0x17A)

#define SCANCODE_KEYS_MAP(k, c) keymap[c] = AM_KEY_##k;

void __am_keymap_init() {
  SCANCODE_KEYS(SCANCODE_KEYS_MAP)
}

uint16_t getScan(uint16_t scanMid);
void __am_input_keybrd(AM_INPUT_KEYBRD_T *kbd) {
  uint16_t scanMid = getScan(0);
  if((scanMid >> 9) == 1)
    kbd->keydown = 0;
  scanMid = scanMid & 0x1FF;
  kbd->keycode = keymap[scanMid];
}

uint16_t getScan(uint16_t scanMid) {
  uint8_t code = inb(PS2);
  putch(code);
  switch (code)
  {
  case 0xE0:
    scanMid |= (1 << 8);
    scanMid = getScan(scanMid);
    break;
  case 0xF0:
    scanMid |= (1 << 9);
    scanMid = getScan(scanMid);
    break;
  default:
    scanMid = (scanMid & 0xFF00)| code; 
    break;
  }
  return scanMid;
}
