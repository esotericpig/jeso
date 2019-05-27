/**
 * This file is part of jeso.
 * Copyright (c) 2019 Jonathan Bradley Whited (@esotericpig)
 * 
 * jeso is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jeso is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jeso. If not, see <http://www.gnu.org/licenses/>.
 */

package com.esotericpig.jeso.io;

import java.io.IOException;
import java.io.Reader;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class StringListReader extends Reader {
  protected int charIndex = 0;
  protected int charMark = 0;
  protected ListIterator<String> iterator = null;
  protected String line = null;
  protected int lineIndex = 0;
  protected int lineMark = 0;
  protected List<String> list = null;
  
  public StringListReader(List<String> list) {
    super();
    
    init(list);
  }
  
  protected StringListReader(List<String> list,Object lock) {
    super(lock);
    
    init(list);
  }
  
  private void init(List<String> list) {
    if(list == null) {
      throw new IllegalArgumentException("List cannot be null");
    }
    
    this.iterator = list.listIterator();
    this.line = iterator.hasNext() ? iterator.next() : null;
    this.list = list;
  }
  
  @Override
  public void close() {
    synchronized(lock) {
      iterator = null;
      line = null;
      list = null;
    }
  }
  
  protected void checkIfClosed() throws IOException {
    if(list == null) {
      throw new IOException("Reader has been closed");
    }
  }
  
  public void mark() throws IOException {
    mark(0);
  }
  
  @Override
  public void mark(int readAheadLimit) throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      // I guess ignore readAheadLimit like StringReader also does?
      charMark = charIndex;
      lineMark = lineIndex;
    }
  }
  
  @Override
  public int read() throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      if(lineIndex >= list.size()) {
        return -1;
      }
      
      char result = 0;
      
      if(charIndex < line.length()) {
        result = line.charAt(charIndex++);
      }
      else {
        charIndex = 0;
        result = '\n';
        
        if(++lineIndex >= list.size() || !iterator.hasNext()) {
          line = null;
        }
        else {
          line = iterator.next();
        }
      }
      
      return (((int)result) & 0xFFFF);
    }
  }
  
  @Override
  public int read(char[] cbuf,int off,int len) throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      // Could put this outside of the sync block,
      //   but the contract is to throw an IOException if closed.
      // Don't do ">=" on cbuf check.
      if(off < 0 || len < 0 || (off + len) > cbuf.length) {
        throw new IndexOutOfBoundsException();
      }
      if(lineIndex >= list.size()) {
        return -1;
      }
      if(len == 0) {
        return 0;
      }
      
      int i = off;
      
      while(len > 0) {
        if(charIndex >= line.length()) {
          charIndex = 0;
          cbuf[i++] = '\n';
          
          if(++lineIndex >= list.size() || !iterator.hasNext()) {
            line = null;
            
            break;
          }
          
          line = iterator.next();
          --len;
          
          continue;
        }
        
        int minOffLen = Math.min(line.length(),charIndex + len);
        
        line.getChars(charIndex,minOffLen,cbuf,i);
        
        int minLen = minOffLen - charIndex;
        
        charIndex = minOffLen;
        i += minLen;
        len -= minLen;
      }
      
      return (i - off);
    }
  }
  
  @Override
  public void reset() throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      if(lineMark >= list.size()) {
        return; // Just ignore
      }
      
      charIndex = charMark;
      lineIndex = lineMark;
      
      iterator = list.listIterator(lineIndex);
      line = iterator.next();
    }
  }
  
  @Override
  public long skip(long n) throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      if(n < 0L) {
        throw new IllegalArgumentException("Number of chars cannot be negative");
      }
      if(n == 0L || lineIndex >= list.size()) {
        return 0L;
      }
      
      long skipped = 0L;
      
      for(; n > 0L; --n,++skipped) {
        if(charIndex++ >= line.length()) {
          charIndex = 0;
          
          if(++lineIndex >= list.size() || !iterator.hasNext()) {
            line = null;
            
            break;
          }
          
          line = iterator.next();
        }
      }
      
      return skipped;
    }
  }
  
  @Override
  public boolean markSupported() {
    return true;
  }
  
  @Override
  public boolean ready() throws IOException {
    synchronized(lock) {
      checkIfClosed();
      
      return true;
    }
  }
}
