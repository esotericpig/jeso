/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso.botbuddy;

/**
 * @author Jonathan Bradley Whited
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
