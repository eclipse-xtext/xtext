package org.eclipse.xtend.lib;

import org.eclipse.xtend.lib.annotations.AccessorsProcessor;
import org.eclipse.xtend.lib.macro.AbstractFieldProcessor;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.StringExtensions;

/**
 * @since 2.7
 */
@Deprecated
@SuppressWarnings("all")
public class PropertyProcessor extends AbstractFieldProcessor {
  public void doTransform(final MutableFieldDeclaration it, @Extension final TransformationContext context) {
    @Extension
    final AccessorsProcessor.Util util = new AccessorsProcessor.Util(context);
    boolean _hasGetter = util.hasGetter(it);
    boolean _not = (!_hasGetter);
    if (_not) {
      util.addGetter(it, Visibility.PUBLIC);
    }
    boolean _and = false;
    boolean _isFinal = it.isFinal();
    boolean _not_1 = (!_isFinal);
    if (!_not_1) {
      _and = false;
    } else {
      boolean _hasSetter = util.hasSetter(it);
      boolean _not_2 = (!_hasSetter);
      _and = _not_2;
    }
    if (_and) {
      util.addSetter(it, Visibility.PUBLIC);
    }
    String _simpleName = it.getSimpleName();
    String _firstLower = StringExtensions.toFirstLower(_simpleName);
    String _plus = ("_" + _firstLower);
    it.setSimpleName(_plus);
  }
}
