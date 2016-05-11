package org.eclipse.xtend.ide.autoedit;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class RichStringPartionIndentationStrategy extends DefaultIndentLineAutoEditStrategy {
  @Override
  public void customizeDocumentCommand(final IDocument d, final DocumentCommand c) {
    if (((c.text.length() > 1) && (!((List<String>)Conversions.doWrapArray(d.getLegalLineDelimiters())).contains(c.text)))) {
      try {
        final String lineIndentation = this.getLineIndentation(d, c.offset);
        String[] _legalLineDelimiters = d.getLegalLineDelimiters();
        final Function1<String, Integer> _function = new Function1<String, Integer>() {
          @Override
          public Integer apply(final String s) {
            return Integer.valueOf(s.length());
          }
        };
        List<String> _sortBy = IterableExtensions.<String, Integer>sortBy(((Iterable<String>)Conversions.doWrapArray(_legalLineDelimiters)), _function);
        final List<String> legalLineDelimiters = ListExtensions.<String>reverseView(_sortBy);
        final String defaultLineDelimiter = TextUtilities.getDefaultLineDelimiter(d);
        final Function1<String, CharSequence> _function_1 = new Function1<String, CharSequence>() {
          @Override
          public CharSequence apply(final String delimiter) {
            return Pattern.quote(delimiter);
          }
        };
        final String regex = IterableExtensions.<String>join(legalLineDelimiters, "(", ")|(", ")", _function_1);
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(c.text);
        final StringBuilder convertedText = new StringBuilder();
        int currentStart = 0;
        int currentEnd = 0;
        while (matcher.find()) {
          {
            int _start = matcher.start();
            currentEnd = _start;
            if ((currentStart != 0)) {
              convertedText.append(lineIndentation);
            }
            String _substring = c.text.substring(currentStart, currentEnd);
            convertedText.append(_substring);
            convertedText.append(defaultLineDelimiter);
            int _end = matcher.end();
            currentStart = _end;
          }
        }
        int _length = c.text.length();
        boolean _lessThan = (currentStart < _length);
        if (_lessThan) {
          if ((currentStart != 0)) {
            convertedText.append(lineIndentation);
          }
          String _substring = c.text.substring(currentStart);
          convertedText.append(_substring);
        }
        String _string = convertedText.toString();
        c.text = _string;
      } catch (final Throwable _t) {
        if (_t instanceof BadLocationException) {
          final BadLocationException e = (BadLocationException)_t;
          super.customizeDocumentCommand(d, c);
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    }
    super.customizeDocumentCommand(d, c);
  }
  
  public String getLineIndentation(final IDocument document, final int offset) throws BadLocationException {
    String _xblockexpression = null;
    {
      int _xifexpression = (int) 0;
      int _length = document.getLength();
      boolean _tripleEquals = (offset == _length);
      if (_tripleEquals) {
        _xifexpression = (offset - 1);
      } else {
        _xifexpression = offset;
      }
      int adjustedOffset = _xifexpression;
      IRegion line = document.getLineInformationOfOffset(adjustedOffset);
      int start = line.getOffset();
      int end = this.findEndOfWhiteSpace(document, start, offset);
      _xblockexpression = document.get(start, (end - start));
    }
    return _xblockexpression;
  }
}
