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

package com.esotericpig.jeso.botbuddy;

/**
 * @author Jonathan Bradley Whited (@esotericpig)
 */
public class UserIsActiveException extends RuntimeException {
  public UserIsActiveException() {
    this("User is active; stopping automatic operations");
  }
  
  public UserIsActiveException(Throwable cause) {
    super(cause);
  }
  
  public UserIsActiveException(String message) {
    super(message);
  }
  
  public UserIsActiveException(String message,Throwable cause) {
    super(message,cause);
  }
}
