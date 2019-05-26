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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class StrsTest {
  @BeforeEach
  public void setUpEach() {
  }
  
  @AfterEach
  public void tearDownEach() {
  }
  
  @Test
  public void testTrims() {
    testTrims("");
    testTrims(" ");
    testTrims("     ");
    testTrims("a");
    testTrims(" a");
    testTrims("     a");
    testTrims("a ");
    testTrims("a     ");
    testTrims(" a ");
    testTrims("     a     ");
    testTrims("abc");
    testTrims(" abc");
    testTrims("     abc");
    testTrims("abc ");
    testTrims("abc     ");
    testTrims(" abc ");
    testTrims("     abc     ");
  }
  
  public void testTrims(String str) {
    assertEquals(str.replaceFirst("\\A\\s+",""),Strs.ltrim(new StringBuilder(str)).toString());
    assertEquals(str.replaceFirst("\\s+\\z",""),Strs.rtrim(new StringBuilder(str)).toString());
    assertEquals(str.trim(),Strs.trim(new StringBuilder(str)).toString());
  }
}
