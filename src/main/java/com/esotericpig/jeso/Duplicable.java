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
public interface Duplicable<T> {
  public abstract T dup();
}
