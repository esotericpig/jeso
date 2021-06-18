/*
 * This file is part of Jeso.
 * Copyright (c) 2019-2021 Jonathan Bradley Whited
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package com.esotericpig.jeso;

/**
 * @author Jonathan Bradley Whited
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
