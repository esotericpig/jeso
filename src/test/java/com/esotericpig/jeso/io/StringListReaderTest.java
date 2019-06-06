/**
 * This file is part of Jeso.
 * Copyright (c) 2019 Jonathan Bradley Whited (@esotericpig)
 * 
 * Jeso is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Jeso is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jeso. If not, see <http://www.gnu.org/licenses/>.
 */

package com.esotericpig.jeso.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <pre>
 * </pre>
 * 
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class StringListReaderTest {
  protected String expectedOutput = null;
  protected List<String> list = null;
  protected Random rand = null;
  
  @BeforeEach
  public void setUpEach() {
    list = new LinkedList<>();
    rand = new Random();
    
    list.add("");
    list.add("Hello World");
    list.add(" Hello World ");
    list.add("");
    list.add("     Hello World     ");
    list.add("");
    
    StringBuilder sb = new StringBuilder();
    
    for(String s: list) {
      sb.append(s).append('\n');
    }
    
    expectedOutput = sb.toString();
  }
  
  @AfterEach
  public void tearDownEach() {
    expectedOutput = null;
    rand = null;
    
    if(list != null) {
      list.clear();
      list = null;
    }
  }
  
  @Test
  public void testMarkResetSkip() throws IOException {
    try(StringListReader in = new StringListReader(list)) {
      StringBuilder output = new StringBuilder();
      long skipped = 0L;
      
      in.mark();
      for(int i = 0; i < 13; ++i) {
        output.append((char)in.read());
      }
      
      in.reset();
      skipped = in.skip(13L);
      assertEquals(13L,skipped);
      
      in.mark();
      for(int i = 0; i < 5; ++i) {
        output.append((char)in.read());
      }
      
      in.reset();
      skipped = in.skip(5L);
      assertEquals(5L,skipped);
      
      int readChar = -1;
      
      while((readChar = in.read()) != -1) {
        output.append((char)readChar);
      }
      
      assertEquals(expectedOutput,output.toString());
    }
  }
  
  @Test
  public void testRead() throws IOException {
    try(StringListReader in = new StringListReader(list)) {
      StringBuilder output = new StringBuilder();
      int readChar = -1;
      
      while((readChar = in.read()) != -1) {
        output.append((char)readChar);
      }
      
      assertEquals(expectedOutput,output.toString());
    }
  }
  
  @Test
  public void testReadBuffer() throws IOException {
    System.out.println("[testReadBuffer()]");
    
    try(StringListReader in = new StringListReader(list);
        StringReader sin = new StringReader(expectedOutput)) {
      final int BUFFER_LEN = 13;
      
      StringBuilder output = new StringBuilder();
      
      while(true) {
        char[] buffer = new char[BUFFER_LEN];
        char[] bufferS = new char[BUFFER_LEN];
        int offset = rand.nextInt(BUFFER_LEN);
        int length = rand.nextInt(BUFFER_LEN - offset);
        
        int readCount = in.read(buffer,offset,length);
        int readCountS = sin.read(bufferS,offset,length);
        
        if(readCount == -1) {
          break;
        }
        
        System.out.print("" + readCount + ": '");
        for(int i = offset; i < (offset + readCount); ++i) {
          char readChar = buffer[i];
          
          System.out.print((readChar == '\n') ? "\\n" : readChar);
          output.append(readChar);
        }
        System.out.println("'");
        
        assertArrayEquals(bufferS,buffer);
      }
      
      assertEquals(expectedOutput,output.toString());
    }
  }
}
