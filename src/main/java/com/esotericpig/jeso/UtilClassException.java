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

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class UtilClassException extends UnsupportedOperationException {
  public UtilClassException() {
    super("Cannot construct a utility class");
  }
  
  public UtilClassException(Throwable cause) {
    super(cause);
  }
  
  public UtilClassException(String message) {
    super(message);
  }
  
  public UtilClassException(String message,Throwable cause) {
    super(message,cause);
  }
}
