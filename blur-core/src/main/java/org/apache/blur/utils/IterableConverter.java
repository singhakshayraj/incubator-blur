package org.apache.blur.utils;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.blur.manager.results.BlurIterable;
import org.apache.blur.manager.results.BlurIterator;

public class IterableConverter<F, T, E extends Exception> implements BlurIterable<T, E> {

  private Converter<F, T> converter;
  private Iterable<F> iterable;

  public IterableConverter(Iterable<F> iterable, Converter<F, T> converter) {
    this.converter = converter;
    this.iterable = iterable;
  }

  @Override
  public BlurIterator<T, E> iterator() {
    return new IteratorConverter<F, T, E>(iterable.iterator(), converter);
  }

}
