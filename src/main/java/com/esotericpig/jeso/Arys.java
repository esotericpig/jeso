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

package com.esotericpig.jeso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// TODO: primitives:    boolean,byte,char,double,float,int,long,short
// TODO: compact():     <primitives>
// TODO: compact_mut(): <primitives>
// TODO: join():        <all>
// TODO: sample():      <primitives>
// TODO: samples():     <primitives>
// TODO: uniq():        <primitives>
// TODO: uniq_mut():    <primitives>
// TODO: tests

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class Arys {
  @SafeVarargs
  public static <T> List<T> compact(T... ary) {
    List<T> compact = new ArrayList<>();
    
    for(T t: ary) {
      if(t != null) {
        compact.add(t);
      }
    }
    
    return compact;
  }
  
  public static <T> T[] compact_mut(T[] ary) {
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
  public static <T> T sample(T... ary) {
    if(ary.length < 1) {
      return null;
    }
    
    return ary[(int)(Math.random() * ary.length)];
  }
  
  @SafeVarargs
  public static <T> T sample(Random rand,T... ary) {
    if(ary.length < 1) {
      return null;
    }
    
    return ary[rand.nextInt(ary.length)];
  }
  
  @SafeVarargs
  public static <T> List<T> samples(int count,T... ary) {
    if(count > ary.length) {
      count = ary.length;
    }
    
    List<T> samples = new ArrayList<>();
    
    if(count < 1) {
      return samples;
    }
    
    List<T> options = new ArrayList<>(Arrays.asList(ary));
    
    for(; count > 0; --count) {
      samples.add(options.remove((int)(Math.random() * options.size())));
    }
    
    return samples;
  }
  
  @SafeVarargs
  public static <T> List<T> samples(int count,Random rand,T... ary) {
    if(count > ary.length) {
      count = ary.length;
    }
    
    List<T> samples = new ArrayList<>();
    
    if(count < 1) {
      return samples;
    }
    
    List<T> options = new ArrayList<>(Arrays.asList(ary));
    
    for(; count > 0; --count) {
      samples.add(options.remove(rand.nextInt(options.size())));
    }
    
    return samples;
  }
  
  @SafeVarargs
  public static <T> List<T> uniq(T... ary) {
    List<T> uniqs = new ArrayList<>();
    
    if(ary.length == 1) {
      uniqs.add(ary[0]);
    }
    else if(ary.length > 1) {
      Set<T> dups = new HashSet<>();
      
      for(T t: ary) {
        if(!dups.contains(t)) {
          dups.add(t);
          uniqs.add(t);
        }
      }
    }
    
    return uniqs;
  }
  
  public static <T> T[] uniq_mut(T[] ary) {
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
  
  public static void main(String[] args) {
  }
  
  private Arys() {
  }
}
