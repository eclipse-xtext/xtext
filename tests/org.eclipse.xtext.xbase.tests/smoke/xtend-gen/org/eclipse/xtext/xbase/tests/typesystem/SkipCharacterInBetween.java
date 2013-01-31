/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.tests.typesystem;

import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.tests.typesystem.AbstractSmokeTest;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("all")
public class SkipCharacterInBetween extends AbstractSmokeTest {
  public void assertNonSmoking(final String input) throws Exception {
    int _length = input.length();
    boolean _greaterThan = (_length > 1);
    if (_greaterThan) {
      int _length_1 = input.length();
      int _minus = (_length_1 - 2);
      IntegerRange _upTo = new IntegerRange(0, _minus);
      for (final Integer i : _upTo) {
        String _substring = input.substring(0, (i).intValue());
        int _plus = ((i).intValue() + 1);
        String _substring_1 = input.substring(_plus);
        String _plus_1 = (_substring + _substring_1);
        this.processExpression(_plus_1);
      }
    }
  }
  
  @Ignore(value = "Disables since it is pretty much the same as testFeatureCall_25_a")
  @Test
  public void testFeatureCall_25_b() throws Exception {
    super.testFeatureCall_25_b();
  }
  
  @Ignore(value = "Disables since it is pretty much the same as testFeatureCall_25_a")
  @Test
  public void testFeatureCall_25_c() throws Exception {
    super.testFeatureCall_25_c();
  }
  
  @Ignore(value = "Disables since it is pretty much the same as testFeatureCall_25_a")
  @Test
  public void testFeatureCall_25_d() throws Exception {
    super.testFeatureCall_25_d();
  }
}
