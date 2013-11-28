/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.tests.builder;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend.ide.tests.builder.JavaEditorExtension;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.builder.impl.javasupport.JavaChangeQueueFiller;
import org.eclipse.xtext.builder.impl.javasupport.JdtQueuedBuildData;
import org.eclipse.xtext.builder.impl.javasupport.UnconfirmedStructuralChangesDelta;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;

/**
 * @author Anton Kosyakov - Initial contribution and API
 */
@SuppressWarnings("all")
public abstract class AbstractQueuedBuildDataTest extends AbstractXtendUITestCase {
  /**
   * <p>
   * To print events into the console set <code>VERBOSE</code> flag of <code>JavaEditorExtension</code> as <code>true</code>.
   * </p>
   */
  @Inject
  @Extension
  protected JavaEditorExtension _javaEditorExtension;
  
  @Inject
  @Extension
  protected WorkbenchTestHelper testHelper;
  
  @Inject
  protected JdtQueuedBuildData queuedBuildData;
  
  @Inject
  protected JavaChangeQueueFiller javaChangeQueueFiller;
  
  public void tearDown() throws Exception {
    this.testHelper.tearDown();
    super.tearDown();
  }
  
  public Collection<? extends IResourceDescription.Delta> assertThereAreDeltas(final Procedure0 producer, final String... expectedExportedNames) {
    Collection<IResourceDescription.Delta> _assertDeltas = this.assertDeltas(producer);
    Collection<? extends IResourceDescription.Delta> _assertThereAreDeltas = this.assertThereAreDeltas(_assertDeltas, expectedExportedNames);
    return _assertThereAreDeltas;
  }
  
  public Collection<? extends IResourceDescription.Delta> assertThereAreDeltas(final Collection<? extends IResourceDescription.Delta> deltas, final String... expectedExportedNames) {
    Collection<? extends IResourceDescription.Delta> _xblockexpression = null;
    {
      int _size = deltas.size();
      boolean _notEquals = (0 != _size);
      Assert.assertTrue("There are not deltas", _notEquals);
      int _length = expectedExportedNames.length;
      boolean _notEquals_1 = (_length != 0);
      if (_notEquals_1) {
        final HashSet<String> remainingExportedNames = CollectionLiterals.<String>newHashSet(expectedExportedNames);
        final HashSet<String> unexpectedExportedNames = CollectionLiterals.<String>newHashSet();
        HashSet<String> _exportedNames = this.getExportedNames(deltas);
        for (final String exportedName : _exportedNames) {
          {
            final Function1<String,Boolean> _function = new Function1<String,Boolean>() {
              public Boolean apply(final String it) {
                boolean _equals = Objects.equal(exportedName, it);
                return Boolean.valueOf(_equals);
              }
            };
            final String qualifiedName = IterableExtensions.<String>findFirst(((Iterable<String>)Conversions.doWrapArray(expectedExportedNames)), _function);
            boolean _equals = Objects.equal(qualifiedName, null);
            if (_equals) {
              unexpectedExportedNames.add(exportedName);
            } else {
              remainingExportedNames.remove(qualifiedName);
            }
          }
        }
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("There are unexpected exported names: ");
        _builder.append(unexpectedExportedNames, "");
        int _length_1 = ((Object[])Conversions.unwrapArray(unexpectedExportedNames, Object.class)).length;
        Assert.assertEquals(_builder.toString(), 0, _length_1);
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("There are not expected exported names ");
        _builder_1.append(remainingExportedNames, "");
        int _length_2 = ((Object[])Conversions.unwrapArray(remainingExportedNames, Object.class)).length;
        Assert.assertEquals(_builder_1.toString(), 0, _length_2);
      }
      _xblockexpression = (deltas);
    }
    return _xblockexpression;
  }
  
  public Collection<? extends IResourceDescription.Delta> assertThereAreNotDeltas(final Procedure0 producer) {
    Collection<? extends IResourceDescription.Delta> _xblockexpression = null;
    {
      final Collection<IResourceDescription.Delta> deltas = this.assertDeltas(producer);
      Collection<? extends IResourceDescription.Delta> _assertThereAreNotDeltas = this.assertThereAreNotDeltas(deltas);
      _xblockexpression = (_assertThereAreNotDeltas);
    }
    return _xblockexpression;
  }
  
  public Collection<? extends IResourceDescription.Delta> assertThereAreNotDeltas(final Collection<? extends IResourceDescription.Delta> deltas) {
    Collection<? extends IResourceDescription.Delta> _xblockexpression = null;
    {
      HashSet<String> _exportedNames = this.getExportedNames(deltas);
      String _plus = ("There are deltas: " + _exportedNames);
      int _size = deltas.size();
      Assert.assertEquals(_plus, 0, _size);
      _xblockexpression = (deltas);
    }
    return _xblockexpression;
  }
  
  public HashSet<String> getExportedNames(final Collection<? extends IResourceDescription.Delta> deltas) {
    final Function1<IResourceDescription.Delta,HashSet<String>> _function = new Function1<IResourceDescription.Delta,HashSet<String>>() {
      public HashSet<String> apply(final IResourceDescription.Delta it) {
        HashSet<String> _xblockexpression = null;
        {
          final HashSet<String> names = CollectionLiterals.<String>newHashSet();
          boolean _haveEObjectDescriptionsChanged = it.haveEObjectDescriptionsChanged();
          boolean _not = (!_haveEObjectDescriptionsChanged);
          if (_not) {
            return names;
          }
          IResourceDescription _new = it.getNew();
          Iterable<IEObjectDescription> _exportedObjects = null;
          if (_new!=null) {
            _exportedObjects=_new.getExportedObjects();
          }
          if (_exportedObjects!=null) {
            final Procedure1<IEObjectDescription> _function = new Procedure1<IEObjectDescription>() {
              public void apply(final IEObjectDescription it) {
                QualifiedName _name = it.getName();
                String _string = _name.toString();
                names.add(_string);
              }
            };
            IterableExtensions.<IEObjectDescription>forEach(_exportedObjects, _function);
          }
          IResourceDescription _old = it.getOld();
          Iterable<IEObjectDescription> _exportedObjects_1 = null;
          if (_old!=null) {
            _exportedObjects_1=_old.getExportedObjects();
          }
          if (_exportedObjects_1!=null) {
            final Procedure1<IEObjectDescription> _function_1 = new Procedure1<IEObjectDescription>() {
              public void apply(final IEObjectDescription it) {
                QualifiedName _name = it.getName();
                String _string = _name.toString();
                names.add(_string);
              }
            };
            IterableExtensions.<IEObjectDescription>forEach(_exportedObjects_1, _function_1);
          }
          _xblockexpression = (names);
        }
        return _xblockexpression;
      }
    };
    Iterable<HashSet<String>> _map = IterableExtensions.map(deltas, _function);
    final Function2<HashSet<String>,HashSet<String>,HashSet<String>> _function_1 = new Function2<HashSet<String>,HashSet<String>,HashSet<String>>() {
      public HashSet<String> apply(final HashSet<String> t, final HashSet<String> t2) {
        HashSet<String> _xblockexpression = null;
        {
          t.addAll(t2);
          _xblockexpression = (t);
        }
        return _xblockexpression;
      }
    };
    HashSet<String> _reduce = IterableExtensions.<HashSet<String>>reduce(_map, _function_1);
    return _reduce;
  }
  
  public Collection<IResourceDescription.Delta> assertDeltas(final Procedure0 producer) {
    Collection<IResourceDescription.Delta> _xblockexpression = null;
    {
      this.reset();
      producer.apply();
      this.confirmDeltas();
      Collection<IResourceDescription.Delta> _andRemovePendingDeltas = this.queuedBuildData.getAndRemovePendingDeltas();
      _xblockexpression = (_andRemovePendingDeltas);
    }
    return _xblockexpression;
  }
  
  public void reset() {
    IResourcesSetupUtil.waitForAutoBuild();
    this.queuedBuildData.reset();
  }
  
  public void confirmDeltas() {
    final boolean result = this.tryConfirmDeltas();
    final Collection<UnconfirmedStructuralChangesDelta> deltas = this.queuedBuildData.getUnconfirmedDeltas();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("There are unconfirmed changes: ");
    HashSet<String> _exportedNames = this.getExportedNames(deltas);
    _builder.append(_exportedNames, "");
    Assert.assertTrue(_builder.toString(), result);
  }
  
  public boolean tryConfirmDeltas() {
    boolean _xblockexpression = false;
    {
      IResourcesSetupUtil.waitForAutoBuild();
      IProject _project = this.testHelper.getProject();
      boolean _needRebuild = this.queuedBuildData.needRebuild(_project);
      boolean _not = (!_needRebuild);
      _xblockexpression = (_not);
    }
    return _xblockexpression;
  }
}
