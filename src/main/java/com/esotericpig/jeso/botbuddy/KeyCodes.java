/*
 * This file is part of Jeso.
 * Copyright (c) 2020-2022 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

import com.esotericpig.jeso.Chars;
import com.esotericpig.jeso.UtilClassException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FIXME: Will this (VK_SHIFT, etc.) work for Mac OS X?
 * <p>
 * FIXME: '#' (VK_NUMBER_SIGN) is 'Shift+3' on US keyboard, but no 'Shift' on UK keyboard
 *        - have to make it generic somehow (classes?) and choose (class)
 *        - default auto-chooses w/ KeyEvent.getKeyModifiersText()
 *
 * @author Jonathan Bradley Whited
 * @see BotBuddy
 * @see BotBuddy#type(String,boolean)
 * @since 0.3.5
 */
public final class KeyCodes {
  private static final Map<Integer,int[]> CHAR_CODES = new HashMap<>();

  static {
    // Generated with #generateAndPrintCharCodes() and then edited by hand.
    // Sorted by char (with editor).
    putCharCodesSafely('\b',new int[]{KeyEvent.VK_BACK_SPACE});
    putCharCodesSafely('\f',new int[]{KeyEvent.VK_PAGE_DOWN});
    putCharCodesSafely('\n',new int[]{KeyEvent.VK_ENTER});
    putCharCodesSafely('\r',new int[]{KeyEvent.VK_HOME});
    putCharCodesSafely('\t',new int[]{KeyEvent.VK_TAB});
    putCharCodesSafely(' ',new int[]{KeyEvent.VK_SPACE});
    putCharCodesSafely('!',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_EXCLAMATION_MARK});
    putCharCodesSafely('"',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_QUOTEDBL});
    putCharCodesSafely('#',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_NUMBER_SIGN});
    putCharCodesSafely('$',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_DOLLAR});
    putCharCodesSafely('%',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_5});
    putCharCodesSafely('&',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_AMPERSAND});
    putCharCodesSafely('\'',new int[]{KeyEvent.VK_QUOTE});
    putCharCodesSafely('(',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_LEFT_PARENTHESIS});
    putCharCodesSafely(')',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_RIGHT_PARENTHESIS});
    putCharCodesSafely('*',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_ASTERISK});
    putCharCodesSafely('+',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_PLUS});
    putCharCodesSafely(',',new int[]{KeyEvent.VK_COMMA});
    putCharCodesSafely('-',new int[]{KeyEvent.VK_MINUS});
    putCharCodesSafely('.',new int[]{KeyEvent.VK_PERIOD});
    putCharCodesSafely('/',new int[]{KeyEvent.VK_SLASH});
    putCharCodesSafely('0',new int[]{KeyEvent.VK_0});
    putCharCodesSafely('1',new int[]{KeyEvent.VK_1});
    putCharCodesSafely('2',new int[]{KeyEvent.VK_2});
    putCharCodesSafely('3',new int[]{KeyEvent.VK_3});
    putCharCodesSafely('4',new int[]{KeyEvent.VK_4});
    putCharCodesSafely('5',new int[]{KeyEvent.VK_5});
    putCharCodesSafely('6',new int[]{KeyEvent.VK_6});
    putCharCodesSafely('7',new int[]{KeyEvent.VK_7});
    putCharCodesSafely('8',new int[]{KeyEvent.VK_8});
    putCharCodesSafely('9',new int[]{KeyEvent.VK_9});
    putCharCodesSafely(':',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_COLON});
    putCharCodesSafely(';',new int[]{KeyEvent.VK_SEMICOLON});
    putCharCodesSafely('<',new int[]{KeyEvent.VK_LESS});
    putCharCodesSafely('=',new int[]{KeyEvent.VK_EQUALS});
    putCharCodesSafely('>',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_GREATER});
    putCharCodesSafely('?',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_SLASH});
    putCharCodesSafely('@',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_AT});
    putCharCodesSafely('A',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_A});
    putCharCodesSafely('B',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_B});
    putCharCodesSafely('C',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_C});
    putCharCodesSafely('D',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_D});
    putCharCodesSafely('E',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_E});
    putCharCodesSafely('F',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_F});
    putCharCodesSafely('G',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_G});
    putCharCodesSafely('H',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_H});
    putCharCodesSafely('I',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_I});
    putCharCodesSafely('J',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_J});
    putCharCodesSafely('K',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_K});
    putCharCodesSafely('L',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_L});
    putCharCodesSafely('M',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_M});
    putCharCodesSafely('N',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_N});
    putCharCodesSafely('O',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_O});
    putCharCodesSafely('P',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_P});
    putCharCodesSafely('Q',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_Q});
    putCharCodesSafely('R',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_R});
    putCharCodesSafely('S',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_S});
    putCharCodesSafely('T',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_T});
    putCharCodesSafely('U',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_U});
    putCharCodesSafely('V',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_V});
    putCharCodesSafely('W',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_W});
    putCharCodesSafely('X',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_X});
    putCharCodesSafely('Y',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_Y});
    putCharCodesSafely('Z',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_Z});
    putCharCodesSafely('[',new int[]{KeyEvent.VK_OPEN_BRACKET});
    putCharCodesSafely('\\',new int[]{KeyEvent.VK_BACK_SLASH});
    putCharCodesSafely(']',new int[]{KeyEvent.VK_CLOSE_BRACKET});
    putCharCodesSafely('^',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_CIRCUMFLEX});
    putCharCodesSafely('_',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_UNDERSCORE});
    putCharCodesSafely('`',new int[]{KeyEvent.VK_BACK_QUOTE});
    putCharCodesSafely('a',new int[]{KeyEvent.VK_A});
    putCharCodesSafely('b',new int[]{KeyEvent.VK_B});
    putCharCodesSafely('c',new int[]{KeyEvent.VK_C});
    putCharCodesSafely('d',new int[]{KeyEvent.VK_D});
    putCharCodesSafely('e',new int[]{KeyEvent.VK_E});
    putCharCodesSafely('f',new int[]{KeyEvent.VK_F});
    putCharCodesSafely('g',new int[]{KeyEvent.VK_G});
    putCharCodesSafely('h',new int[]{KeyEvent.VK_H});
    putCharCodesSafely('i',new int[]{KeyEvent.VK_I});
    putCharCodesSafely('j',new int[]{KeyEvent.VK_J});
    putCharCodesSafely('k',new int[]{KeyEvent.VK_K});
    putCharCodesSafely('l',new int[]{KeyEvent.VK_L});
    putCharCodesSafely('m',new int[]{KeyEvent.VK_M});
    putCharCodesSafely('n',new int[]{KeyEvent.VK_N});
    putCharCodesSafely('o',new int[]{KeyEvent.VK_O});
    putCharCodesSafely('p',new int[]{KeyEvent.VK_P});
    putCharCodesSafely('q',new int[]{KeyEvent.VK_Q});
    putCharCodesSafely('r',new int[]{KeyEvent.VK_R});
    putCharCodesSafely('s',new int[]{KeyEvent.VK_S});
    putCharCodesSafely('t',new int[]{KeyEvent.VK_T});
    putCharCodesSafely('u',new int[]{KeyEvent.VK_U});
    putCharCodesSafely('v',new int[]{KeyEvent.VK_V});
    putCharCodesSafely('w',new int[]{KeyEvent.VK_W});
    putCharCodesSafely('x',new int[]{KeyEvent.VK_X});
    putCharCodesSafely('y',new int[]{KeyEvent.VK_Y});
    putCharCodesSafely('z',new int[]{KeyEvent.VK_Z});
    putCharCodesSafely('{',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_BRACELEFT});
    putCharCodesSafely('|',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_BACK_SLASH});
    putCharCodesSafely('}',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_BRACERIGHT});
    putCharCodesSafely('~',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_BACK_QUOTE});
    putCharCodesSafely('¡',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_INVERTED_EXCLAMATION_MARK});
    putCharCodesSafely('€',new int[]{KeyEvent.VK_SHIFT,KeyEvent.VK_EURO_SIGN});
  }

  public static int[] getCharCodes(int keyChar) {
    return getCharCodes(keyChar,true);
  }

  /**
   * @param keyChar      code point to look up
   * @param getExKeyCode true to call KeyEvent#getExtendedKeyCodeForChar() and
   *                     try that way if null; may still return null
   * @return null if not found, else int[] of keyChar's key codes
   */
  public static int[] getCharCodes(int keyChar,boolean getExKeyCode) {
    int[] keyCodes = CHAR_CODES.get(keyChar);

    if(keyCodes == null && getExKeyCode) {
      int keyCode = KeyEvent.getExtendedKeyCodeForChar(keyChar);

      if(keyCode != KeyEvent.VK_UNDEFINED) {
        List<Integer> keyCodeList = new ArrayList<>();
        String[] keyModsText = InputEvent.getModifiersExText(keyCode)
            .trim().toLowerCase(Locale.ENGLISH).split("\\s+")[0].split("\\+");

        for(String keyModText: keyModsText) {
          int kc;

          switch(keyModText) {
            case "alt": kc = KeyEvent.VK_ALT; break;
            case "button1": kc = KeyEvent.BUTTON1_DOWN_MASK; break;
            case "button2": kc = KeyEvent.BUTTON2_DOWN_MASK; break;
            case "button3": kc = KeyEvent.BUTTON3_DOWN_MASK; break;
            case "ctrl": kc = KeyEvent.VK_CONTROL; break;
            case "graph": kc = KeyEvent.VK_ALT_GRAPH; break;
            case "meta": kc = KeyEvent.VK_META; break;
            case "shift": kc = KeyEvent.VK_SHIFT; break;
            default: kc = KeyEvent.VK_UNDEFINED; break;
          }

          if(kc != KeyEvent.VK_UNDEFINED) {
            keyCodeList.add(kc);
          }
        }

        keyCodeList.add(keyCode);

        keyCodes = new int[keyCodeList.size()];

        for(int i = 0; i < keyCodes.length; ++i) {
          keyCodes[i] = keyCodeList.get(i);
        }
      }
    }

    return keyCodes;
  }

  public static int[] putCharCodes(int keyChar,int[] keyCodes) {
    return CHAR_CODES.put(keyChar,keyCodes);
  }

  /**
   * This is only meant to be used for developing this class (or your own).
   *
   * @param keyChar  code point key
   * @param keyCodes of KeyEvent value
   * @return result of Map#put(...)
   */
  public static int[] putCharCodesSafely(int keyChar,int[] keyCodes) {
    if(CHAR_CODES.containsKey(keyChar)) {
      String keyStr = Chars.toString(keyChar);
      String keyText = Arrays.stream(keyCodes)
                             .mapToObj(KeyEvent::getKeyText)
                             .collect(Collectors.joining(", "));

      System.err.println("Warning: keyChar[" + keyStr + "] already exists; ignoring keyCodes["
          + keyText + "]");
    }

    return putCharCodes(keyChar,keyCodes);
  }

  /**
   * Don't use Java reflection to build/generate CODES because it can possibly
   * throw SecurityException, IllegalAccessException, etc. Instead, use this
   * method to see what it would generate and then copy &amp; paste the results.
   *
   * @throws IllegalAccessException if can't do reflection
   */
  public static void generateAndPrintCharCodes() throws IllegalAccessException {
    Map<String,String> codes = new LinkedHashMap<>();
    Map<String,String> ignored = new LinkedHashMap<>();
    Map<String,String> newCodes = new LinkedHashMap<>();

    for(Field field: KeyEvent.class.getFields()) {
      int mods = field.getModifiers();
      String vkName = field.getName();

      if(Modifier.isPublic(mods) && Modifier.isStatic(mods) && field.getType().equals(int.class)
          && vkName.startsWith("VK_")) {
        List<String[]> code = new ArrayList<>();
        int keyChar = -1;
        int keyCode = field.getInt(null);
        String keyText = KeyEvent.getKeyText(keyCode);

        vkName = "KeyEvent." + vkName;

        if(keyText.length() == 1) {
          keyChar = keyText.codePointAt(0);

          if(Character.isLetter(keyChar)) {
            keyText = keyText.toLowerCase(Locale.ENGLISH);

            code.add(new String[]{keyText,"'" + keyText + "'",vkName});

            keyText = keyText.toUpperCase(Locale.ENGLISH);

            code.add(new String[]{keyText,"'" + keyText + "'","KeyEvent.VK_SHIFT," + vkName});
          }
          else {
            code.add(new String[]{keyText,"'" + keyText + "'",vkName});
          }
        }
        else {
          code.add(new String[]{"\0","'" + keyText + "'|" + vkName,vkName});
        }

        boolean hasKeyCode = CHAR_CODES.values().stream()
            .anyMatch(kcs -> Arrays.stream(kcs).anyMatch(kc -> kc == keyCode));

        for(String[] c: code) {
          keyChar = c[0].codePointAt(0);
          keyText = c[1];
          vkName = c[2];

          if(hasKeyCode || CHAR_CODES.containsKey(keyChar)) {
            codes.put(vkName,keyText);
          }
          else {
            newCodes.put(vkName,keyText);
          }
        }
      }
      else {
        ignored.put(vkName,"");
      }
    }

    List<Map<String,String>> maps = new ArrayList<>();
    String[] titles = new String[]{"[Ignored]","[Char Codes]","[New Char Codes]"};

    maps.add(ignored);
    maps.add(codes);
    maps.add(newCodes);

    System.out.println();

    for(int i = 0; i < maps.size(); ++i) {
      System.out.println(titles[i]);

      for(Map.Entry<String,String> entry: maps.get(i).entrySet()) {
        String keyText = entry.getValue();
        String vkName = entry.getKey();

        System.out.println("    putCharCodesSafely(" + keyText + ",new int[]{" + vkName + "});");
      }

      System.out.println();
    }
  }

  public static void main(String[] args) {
    try {
      generateAndPrintCharCodes();
    }
    // Bad, but this main method is just for developing this class, not production.
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private KeyCodes() {
    throw new UtilClassException();
  }
}
