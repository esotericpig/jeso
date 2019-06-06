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

package com.esotericpig.jeso;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// TODO: primitives:    boolean,byte,char,double,float,int,long,short
// TODO: join():        <primitives>
// TODO: joins():       <primitives>
// TODO: sample():      <primitives>
// TODO: samples():     <primitives>
// TODO: unique():      <primitives>
// TODO: uniqueMut():   <primitives>
// TODO: tests

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public final class Arys {
  public static final String DEFAULT_JOIN_SEPARATOR = "";
  
  @SafeVarargs
  public static <T> T[] compact(T... ary) {
    if(ary.length < 1) {
      return ary;
    }
    
    List<T> compact = new ArrayList<>();
    
    for(T t: ary) {
      if(t != null) {
        compact.add(t);
      }
    }
    
    return toArray(ary,compact);
  }
  
  public static <T> T[] compactMut(T[] ary) {
    if(ary.length < 2) {
      return ary;
    }
    
    int aryIndex = 0;
    
    for(int i = 0; i < ary.length; ++i) {
      T t = ary[i];
      
      ary[i] = null;
      
      if(t != null) {
        ary[aryIndex++] = t;
      }
    }
    
    return ary;
  }
  
  @SafeVarargs
  public static <T> String join(T... ary) {
    return joins(DEFAULT_JOIN_SEPARATOR,ary);
  }
  
  @SafeVarargs
  public static <T> String joins(char separator,T... ary) {
    if(ary.length < 1) {
      return "";
    }
    if(ary.length == 1) {
      return ary[0].toString();
    }
    
    StringBuilder sb = new StringBuilder();
    
    for(int i = 0;;) {
      T t = ary[i];
      
      sb.append(t.toString());
      
      if((++i) >= ary.length) {
        break;
      }
      
      sb.append(separator);
    }
    
    return sb.toString();
  }
  
  @SafeVarargs
  public static <T> String joins(CharSequence separator,T... ary) {
    if(ary.length < 1) {
      return "";
    }
    if(ary.length == 1) {
      return ary[0].toString();
    }
    
    StringBuilder sb = new StringBuilder();
    
    for(int i = 0;;) {
      T t = ary[i];
      
      sb.append(t.toString());
      
      if((++i) >= ary.length) {
        break;
      }
      
      sb.append(separator);
    }
    
    return sb.toString();
  }
  
  /**
   * <pre>
   * java.util.Arrays#copyOfRange(...) does the same thing.
   * 
   * This does not modify the {@code ary} arg.
   * </pre>
   * 
   * @param <T>    the type of array to create
   * @param ary    the array to use for getting the Class data and component type
   * @param length the length of the new array
   * 
   * @return the new array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(T[] ary,int length) {
    Class<? extends T[]> clazz = (Class<? extends T[]>)ary.getClass();
    
    return (T[])Array.newInstance(clazz.getComponentType(),length);
  }
  
  @SafeVarargs
  public static <T> T sample(T... ary) {
    return sample(Rand.rand,ary);
  }
  
  @SafeVarargs
  public static <T> T sample(Random rand,T... ary) {
    if(ary.length < 1) {
      return null;
    }
    
    return ary[rand.nextInt(ary.length)];
  }
  
  @SafeVarargs
  public static <T> T[] samples(int count,T... ary) {
    return samples(count,Rand.rand,ary);
  }
  
  @SafeVarargs
  public static <T> T[] samples(int count,Random rand,T... ary) {
    if(count < 1) {
      return newArray(ary,0);
    }
    if(count > ary.length) {
      count = ary.length;
    }
    
    List<T> options = new ArrayList<>(Arrays.asList(ary));
    T[] samples = newArray(ary,count);
    
    for(int i = 0; i < count; ++i) {
      samples[i] = options.remove(rand.nextInt(options.size()));
    }
    
    return samples;
  }
  
  @SafeVarargs
  public static <T> T[] unique(T... ary) {
    if(ary.length < 2) {
      return ary;
    }
    
    Set<T> dups = new HashSet<>();
    List<T> uniqs = new ArrayList<>();
    
    for(T t: ary) {
      if(!dups.contains(t)) {
        dups.add(t);
        uniqs.add(t);
      }
    }
    
    return toArray(ary,uniqs);
  }
  
  public static <T> T[] uniqueMut(T[] ary) {
    if(ary.length < 2) {
      return ary;
    }
    
    int aryIndex = 0;
    Set<T> dups = new HashSet<>();
    
    for(int i = 0; i < ary.length; ++i) {
      T t = ary[i];
      
      ary[i] = null;
      
      if(t != null && !dups.contains(t)) {
        ary[aryIndex++] = t;
        dups.add(t);
      }
    }
    
    return ary;
  }
  
  public static <T> T[] toArray(T[] ary,List<T> list) {
    return list.toArray(newArray(ary,list.size()));
  }
  
  private Arys() {
    throw new UtilClassException();
  }
  
  public static final class Rand {
    public static final Random rand = new Random();
  }
}
