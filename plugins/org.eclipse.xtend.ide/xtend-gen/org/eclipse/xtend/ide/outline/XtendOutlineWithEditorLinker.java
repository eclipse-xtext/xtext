/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.outline;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.actions.OutlineWithEditorLinker;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.TextRegion;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class XtendOutlineWithEditorLinker extends OutlineWithEditorLinker {
  public void selectInTreeView(final ISelection selection) {
    boolean _and = false;
    if (!(selection instanceof ITextSelection)) {
      _and = false;
    } else {
      Tree _tree = this.treeViewer.getTree();
      boolean _isDisposed = _tree.isDisposed();
      boolean _not = (!_isDisposed);
      _and = _not;
    }
    if (_and) {
      final ITextSelection textSelection = ((ITextSelection) selection);
      int _offset = textSelection.getOffset();
      int _length = textSelection.getLength();
      final TextRegion selectedTextRegion = new TextRegion(_offset, _length);
      final Object input = this.treeViewer.getInput();
      if ((input instanceof IOutlineNode)) {
        final List<IOutlineNode> matchingNodes = CollectionLiterals.<IOutlineNode>newArrayList();
        this.findNodesInRange(((IOutlineNode)input), selectedTextRegion, matchingNodes);
        final Function1<IOutlineNode, Integer> _function = new Function1<IOutlineNode, Integer>() {
          public Integer apply(final IOutlineNode it) {
            ITextRegion _fullTextRegion = it.getFullTextRegion();
            return Integer.valueOf(_fullTextRegion.getLength());
          }
        };
        List<Integer> _map = ListExtensions.<IOutlineNode, Integer>map(matchingNodes, _function);
        final Integer smallestMatch = IterableExtensions.<Integer>min(_map);
        final Function1<IOutlineNode, Boolean> _function_1 = new Function1<IOutlineNode, Boolean>() {
          public Boolean apply(final IOutlineNode it) {
            ITextRegion _fullTextRegion = it.getFullTextRegion();
            int _length = _fullTextRegion.getLength();
            return Boolean.valueOf((_length == (smallestMatch).intValue()));
          }
        };
        final Iterable<IOutlineNode> nodesToBeSelected = IterableExtensions.<IOutlineNode>filter(matchingNodes, _function_1);
        boolean _isEmpty = IterableExtensions.isEmpty(nodesToBeSelected);
        boolean _not_1 = (!_isEmpty);
        if (_not_1) {
          List<IOutlineNode> _list = IterableExtensions.<IOutlineNode>toList(nodesToBeSelected);
          Object[] _array = _list.toArray();
          StructuredSelection _structuredSelection = new StructuredSelection(_array);
          this.treeViewer.setSelection(_structuredSelection);
        }
      }
    }
  }
  
  protected void findNodesInRange(final IOutlineNode input, final ITextRegion selectedTextRegion, final List<IOutlineNode> nodes) {
    final ITextRegion textRegion = input.getFullTextRegion();
    boolean _or = false;
    boolean _equals = Objects.equal(textRegion, null);
    if (_equals) {
      _or = true;
    } else {
      boolean _contains = textRegion.contains(selectedTextRegion);
      _or = _contains;
    }
    if (_or) {
      nodes.add(input);
    }
    List<IOutlineNode> _children = input.getChildren();
    for (final IOutlineNode child : _children) {
      this.findNodesInRange(child, selectedTextRegion, nodes);
    }
  }
}
