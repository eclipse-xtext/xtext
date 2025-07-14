/**
 * Copyright (c) 2016, 2022 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.testing;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.util.Ranges;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.LanguageInfo;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.ServerModule;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.eclipse.xtext.ide.server.concurrent.AbstractRequestManager;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Files;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.lib.XbaseGenerated;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Sven Efftinge - Initial contribution and API
 *         Rubén Porras Campo - Semantic Tokens Full
 */
@FinalFieldsConstructor
@SuppressWarnings("all")
public abstract class AbstractLanguageServerTest implements Endpoint {
  /**
   * A request manager that will run the given read and write actions in the same thread immediately, sequentially.
   */
  @Singleton
  public static class DirectRequestManager extends AbstractRequestManager {
    @Inject
    public DirectRequestManager(final OperationCanceledManager operationCanceledManager) {
      super(operationCanceledManager);
    }

    @Override
    public synchronized <V extends Object> CompletableFuture<V> runRead(final Function1<? super CancelIndicator, ? extends V> request) {
      final CompletableFuture<V> result = new CompletableFuture<V>();
      try {
        final CancelIndicator _function = () -> {
          return false;
        };
        result.complete(request.apply(_function));
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          boolean _isCancelException = this.isCancelException(t);
          if (_isCancelException) {
            result.cancel(true);
          } else {
            result.completeExceptionally(t);
          }
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      return result;
    }

    @Override
    public synchronized <U extends Object, V extends Object> CompletableFuture<V> runWrite(final Function0<? extends U> nonCancellable, final Function2<? super CancelIndicator, ? super U, ? extends V> request) {
      final CompletableFuture<V> result = new CompletableFuture<V>();
      try {
        result.complete(request.apply(CancelIndicator.NullImpl, nonCancellable.apply()));
      } catch (final Throwable _t) {
        if (_t instanceof Throwable) {
          final Throwable t = (Throwable)_t;
          boolean _isCancelException = this.isCancelException(t);
          if (_isCancelException) {
            result.cancel(true);
          } else {
            result.completeExceptionally(t);
          }
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
      return result;
    }

    @Override
    public void shutdown() {
    }
  }

  @Accessors
  public static class TestCodeLensConfiguration extends TextDocumentPositionConfiguration {
    private String expectedCodeLensItems = "";

    private Procedure1<? super List<? extends CodeLens>> assertCodeLenses = null;

    @Pure
    public String getExpectedCodeLensItems() {
      return this.expectedCodeLensItems;
    }

    public void setExpectedCodeLensItems(final String expectedCodeLensItems) {
      this.expectedCodeLensItems = expectedCodeLensItems;
    }

    @Pure
    public Procedure1<? super List<? extends CodeLens>> getAssertCodeLenses() {
      return this.assertCodeLenses;
    }

    public void setAssertCodeLenses(final Procedure1<? super List<? extends CodeLens>> assertCodeLenses) {
      this.assertCodeLenses = assertCodeLenses;
    }
  }

  @Accessors
  public static class TestCodeActionConfiguration extends TextDocumentPositionConfiguration {
    private String expectedCodeActions = "";

    private Procedure1<? super List<Either<Command, CodeAction>>> assertCodeActions = null;

    @Pure
    public String getExpectedCodeActions() {
      return this.expectedCodeActions;
    }

    public void setExpectedCodeActions(final String expectedCodeActions) {
      this.expectedCodeActions = expectedCodeActions;
    }

    @Pure
    public Procedure1<? super List<Either<Command, CodeAction>>> getAssertCodeActions() {
      return this.assertCodeActions;
    }

    public void setAssertCodeActions(final Procedure1<? super List<Either<Command, CodeAction>>> assertCodeActions) {
      this.assertCodeActions = assertCodeActions;
    }
  }

  @Accessors
  protected final String fileExtension;

  protected static final String TEST_PROJECT_PATH = "/test-data/test-project";

  @Before
  @BeforeEach
  public void setup() {
    final Injector injector = Guice.createInjector(this.getServerModule());
    injector.injectMembers(this);
    final Object resourceServiceProvider = this.resourceServerProviderRegistry.getExtensionToFactoryMap().get(this.fileExtension);
    if ((resourceServiceProvider instanceof IResourceServiceProvider)) {
      this.languageInfo = ((IResourceServiceProvider)resourceServiceProvider).<LanguageInfo>get(LanguageInfo.class);
    }
    this.languageServer.connect(ServiceEndpoints.toServiceObject(this, this.getLanguageClientClass()));
    this.languageServer.supportedMethods();
    File _absoluteFile = new File("").getAbsoluteFile();
    File _file = new File(_absoluteFile, AbstractLanguageServerTest.TEST_PROJECT_PATH);
    this.root = _file;
  }

  @After
  @AfterEach
  public void cleanup() {
    try {
      boolean _exists = this.root.exists();
      if (_exists) {
        Files.cleanFolder(this.root, null, true, true);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * @since 2.18
   */
  protected Class<? extends LanguageClient> getLanguageClientClass() {
    return LanguageClient.class;
  }

  protected com.google.inject.Module getServerModule() {
    ServerModule _serverModule = new ServerModule();
    final com.google.inject.Module _function = (Binder it) -> {
      it.<IRequestManager>bind(IRequestManager.class).to(AbstractLanguageServerTest.DirectRequestManager.class);
    };
    return Modules2.mixin(_serverModule, _function);
  }

  @Inject
  protected IResourceServiceProvider.Registry resourceServerProviderRegistry;

  @Inject
  @Extension
  private UriExtensions _uriExtensions;

  @Inject
  protected LanguageServerImpl languageServer;

  protected List<Pair<String, Object>> notifications = CollectionLiterals.<Pair<String, Object>>newArrayList();

  protected File root;

  protected LanguageInfo languageInfo;

  protected boolean hierarchicalDocumentSymbolSupport = false;

  protected Path getTestRootPath() {
    return this.root.toPath().toAbsolutePath().normalize();
  }

  protected Path relativize(final String uri) {
    try {
      Path _xblockexpression = null;
      {
        URI _uRI = new URI(uri);
        final Path path = Paths.get(_uRI);
        _xblockexpression = this.getTestRootPath().relativize(path);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected InitializeResult initialize() {
    return this.initialize(null);
  }

  protected InitializeResult initialize(final Procedure1<? super InitializeParams> initializer) {
    return this.initialize(initializer, true);
  }

  protected InitializeResult initialize(final Procedure1<? super InitializeParams> initializer, final boolean callInitialized) {
    return this.initialize(initializer, callInitialized, false);
  }

  protected InitializeResult initialize(final Procedure1<? super InitializeParams> initializer, final boolean callInitialized, final boolean useRootPath) {
    try {
      InitializeParams _initializeParams = new InitializeParams();
      final Procedure1<InitializeParams> _function = (InitializeParams it) -> {
        it.setProcessId(Integer.valueOf(1));
        String _uriString = this._uriExtensions.toUriString(this.root.toURI().normalize());
        WorkspaceFolder _workspaceFolder = new WorkspaceFolder(_uriString, "");
        it.setWorkspaceFolders(Collections.<WorkspaceFolder>unmodifiableList(CollectionLiterals.<WorkspaceFolder>newArrayList(_workspaceFolder)));
      };
      final InitializeParams params = ObjectExtensions.<InitializeParams>operator_doubleArrow(_initializeParams, _function);
      if ((!useRootPath)) {
        ClientCapabilities _capabilities = params.getCapabilities();
        boolean _tripleEquals = (_capabilities == null);
        if (_tripleEquals) {
          ClientCapabilities _clientCapabilities = new ClientCapabilities();
          params.setCapabilities(_clientCapabilities);
        }
        WorkspaceClientCapabilities _workspace = params.getCapabilities().getWorkspace();
        boolean _tripleEquals_1 = (_workspace == null);
        if (_tripleEquals_1) {
          ClientCapabilities _capabilities_1 = params.getCapabilities();
          WorkspaceClientCapabilities _workspaceClientCapabilities = new WorkspaceClientCapabilities();
          _capabilities_1.setWorkspace(_workspaceClientCapabilities);
        }
        WorkspaceClientCapabilities _workspace_1 = params.getCapabilities().getWorkspace();
        _workspace_1.setWorkspaceFolders(Boolean.valueOf(true));
      }
      if (initializer!=null) {
        initializer.apply(params);
      }
      Boolean _elvis = null;
      ClientCapabilities _capabilities_2 = params.getCapabilities();
      TextDocumentClientCapabilities _textDocument = null;
      if (_capabilities_2!=null) {
        _textDocument=_capabilities_2.getTextDocument();
      }
      DocumentSymbolCapabilities _documentSymbol = null;
      if (_textDocument!=null) {
        _documentSymbol=_textDocument.getDocumentSymbol();
      }
      Boolean _hierarchicalDocumentSymbolSupport = null;
      if (_documentSymbol!=null) {
        _hierarchicalDocumentSymbolSupport=_documentSymbol.getHierarchicalDocumentSymbolSupport();
      }
      if (_hierarchicalDocumentSymbolSupport != null) {
        _elvis = _hierarchicalDocumentSymbolSupport;
      } else {
        _elvis = Boolean.valueOf(false);
      }
      this.hierarchicalDocumentSymbolSupport = (_elvis).booleanValue();
      final InitializeResult result = this.languageServer.initialize(params).get();
      if (callInitialized) {
        this.languageServer.initialized(null);
      }
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void open(final String fileUri, final String model) {
    this.open(fileUri, this.languageInfo.getLanguageName(), model);
  }

  protected void open(final String fileUri, final String langaugeId, final String model) {
    DidOpenTextDocumentParams _didOpenTextDocumentParams = new DidOpenTextDocumentParams();
    final Procedure1<DidOpenTextDocumentParams> _function = (DidOpenTextDocumentParams it) -> {
      TextDocumentItem _textDocumentItem = new TextDocumentItem();
      final Procedure1<TextDocumentItem> _function_1 = (TextDocumentItem it_1) -> {
        it_1.setUri(fileUri);
        it_1.setLanguageId(langaugeId);
        it_1.setVersion(1);
        it_1.setText(Strings.toUnixLineSeparator(model));
      };
      TextDocumentItem _doubleArrow = ObjectExtensions.<TextDocumentItem>operator_doubleArrow(_textDocumentItem, _function_1);
      it.setTextDocument(_doubleArrow);
    };
    DidOpenTextDocumentParams _doubleArrow = ObjectExtensions.<DidOpenTextDocumentParams>operator_doubleArrow(_didOpenTextDocumentParams, _function);
    this.languageServer.didOpen(_doubleArrow);
  }

  protected void didCreateWatchedFiles(final String... fileUris) {
    DidChangeWatchedFilesParams _didChangeWatchedFilesParams = new DidChangeWatchedFilesParams();
    final Procedure1<DidChangeWatchedFilesParams> _function = (DidChangeWatchedFilesParams it) -> {
      for (final String fileUri : fileUris) {
        List<FileEvent> _changes = it.getChanges();
        FileEvent _fileEvent = new FileEvent();
        final Procedure1<FileEvent> _function_1 = (FileEvent it_1) -> {
          it_1.setUri(fileUri);
          it_1.setType(FileChangeType.Created);
        };
        FileEvent _doubleArrow = ObjectExtensions.<FileEvent>operator_doubleArrow(_fileEvent, _function_1);
        _changes.add(_doubleArrow);
      }
    };
    DidChangeWatchedFilesParams _doubleArrow = ObjectExtensions.<DidChangeWatchedFilesParams>operator_doubleArrow(_didChangeWatchedFilesParams, _function);
    this.languageServer.didChangeWatchedFiles(_doubleArrow);
  }

  protected void close(final String fileUri) {
    DidCloseTextDocumentParams _didCloseTextDocumentParams = new DidCloseTextDocumentParams();
    final Procedure1<DidCloseTextDocumentParams> _function = (DidCloseTextDocumentParams it) -> {
      TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
      it.setTextDocument(_textDocumentIdentifier);
    };
    DidCloseTextDocumentParams _doubleArrow = ObjectExtensions.<DidCloseTextDocumentParams>operator_doubleArrow(_didCloseTextDocumentParams, _function);
    this.languageServer.didClose(_doubleArrow);
  }

  public String writeFile(final String path, final CharSequence contents) {
    try {
      final File file = new File(this.root, path);
      file.getParentFile().mkdirs();
      file.createNewFile();
      final FileWriter writer = new FileWriter(file);
      writer.write(contents.toString());
      writer.close();
      return this._uriExtensions.toUriString(file.toURI().normalize());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public void deleteFile(final String path) {
    new File(this.root, path).delete();
  }

  public String getVirtualFile(final String path) {
    final File file = new File(this.root, path);
    return this._uriExtensions.toUriString(file.toURI().normalize());
  }

  protected String _toExpectation(final List<?> elements) {
    StringConcatenation _builder = new StringConcatenation();
    {
      for(final Object element : elements) {
        String _expectation = this.toExpectation(element);
        _builder.append(_expectation);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }

  protected String _toExpectation(final String it) {
    return it;
  }

  protected String _toExpectation(final Integer it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(it);
    return _builder.toString();
  }

  protected String _toExpectation(final Void it) {
    return "";
  }

  protected String _toExpectation(final Either<?, ?> either) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isLeft = either.isLeft();
      if (_isLeft) {
        String _expectation = this.toExpectation(either.getLeft());
        _builder.append(_expectation);
        _builder.newLineIfNotEmpty();
      } else {
        String _expectation_1 = this.toExpectation(either.getRight());
        _builder.append(_expectation_1);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }

  protected String _toExpectation(final Location it) {
    StringConcatenation _builder = new StringConcatenation();
    Path _relativize = this.relativize(it.getUri());
    _builder.append(_relativize);
    _builder.append(" ");
    String _expectation = this.toExpectation(it.getRange());
    _builder.append(_expectation);
    return _builder.toString();
  }

  protected String _toExpectation(final Range it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[");
    String _expectation = this.toExpectation(it.getStart());
    _builder.append(_expectation);
    _builder.append(" .. ");
    String _expectation_1 = this.toExpectation(it.getEnd());
    _builder.append(_expectation_1);
    _builder.append("]");
    return _builder.toString();
  }

  protected String _toExpectation(final Position it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[");
    int _line = it.getLine();
    _builder.append(_line);
    _builder.append(", ");
    int _character = it.getCharacter();
    _builder.append(_character);
    _builder.append("]");
    return _builder.toString();
  }

  @Deprecated
  protected String _toExpectation(final SymbolInformation it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("symbol \"");
    String _name = it.getName();
    _builder.append(_name);
    _builder.append("\" {");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("kind: ");
    int _value = it.getKind().getValue();
    _builder.append(_value, "    ");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("location: ");
    String _expectation = this.toExpectation(it.getLocation());
    _builder.append(_expectation, "    ");
    _builder.newLineIfNotEmpty();
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(it.getContainerName());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        _builder.append("    ");
        _builder.append("container: \"");
        String _containerName = it.getContainerName();
        _builder.append(_containerName, "    ");
        _builder.append("\"");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }

  protected String _toExpectation(final WorkspaceSymbol it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("symbol \"");
    String _name = it.getName();
    _builder.append(_name);
    _builder.append("\" {");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("kind: ");
    int _value = it.getKind().getValue();
    _builder.append(_value, "    ");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    _builder.append("location: ");
    String _expectation = this.toExpectation(it.getLocation());
    _builder.append(_expectation, "    ");
    _builder.newLineIfNotEmpty();
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(it.getContainerName());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        _builder.append("    ");
        _builder.append("container: \"");
        String _containerName = it.getContainerName();
        _builder.append(_containerName, "    ");
        _builder.append("\"");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("}");
    _builder.newLine();
    return _builder.toString();
  }

  /**
   * @since 2.16
   */
  protected String _toExpectation(final DocumentSymbol it) {
    String _xblockexpression = null;
    {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("selectionRange must be contained in the range: ");
      _builder.append(it);
      Assert.assertTrue(_builder.toString(), Ranges.containsRange(it.getRange(), it.getSelectionRange()));
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("symbol \"");
      String _name = it.getName();
      _builder_1.append(_name);
      _builder_1.append("\" {");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("    ");
      _builder_1.append("kind: ");
      int _value = it.getKind().getValue();
      _builder_1.append(_value, "    ");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("    ");
      _builder_1.append("range: ");
      String _expectation = this.toExpectation(it.getRange());
      _builder_1.append(_expectation, "    ");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("    ");
      _builder_1.append("selectionRange: ");
      String _expectation_1 = this.toExpectation(it.getSelectionRange());
      _builder_1.append(_expectation_1, "    ");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("    ");
      _builder_1.append("details: ");
      String _detail = it.getDetail();
      _builder_1.append(_detail, "    ");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("    ");
      _builder_1.append("deprecated: ");
      Boolean _deprecated = it.getDeprecated();
      _builder_1.append(_deprecated, "    ");
      _builder_1.newLineIfNotEmpty();
      {
        boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(it.getChildren());
        boolean _not = (!_isNullOrEmpty);
        if (_not) {
          _builder_1.append("    ");
          _builder_1.append("children: [");
          _builder_1.newLine();
          _builder_1.append("    ");
          _builder_1.append("\t");
          {
            List<DocumentSymbol> _children = it.getChildren();
            boolean _hasElements = false;
            for(final DocumentSymbol child : _children) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                _builder_1.appendImmediate("\n", "    \t");
              }
              String _expectation_2 = this.toExpectation(child);
              _builder_1.append(_expectation_2, "    \t");
            }
          }
          _builder_1.newLineIfNotEmpty();
          _builder_1.append("    ");
          _builder_1.append("]");
          _builder_1.newLine();
        }
      }
      _builder_1.append("}");
      _xblockexpression = _builder_1.toString();
    }
    return _xblockexpression;
  }

  protected String _toExpectation(final CompletionItem it) {
    StringConcatenation _builder = new StringConcatenation();
    String _label = it.getLabel();
    _builder.append(_label);
    {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(it.getDetail());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        _builder.append(" (");
        String _detail = it.getDetail();
        _builder.append(_detail);
        _builder.append(")");
      }
    }
    {
      Either<TextEdit, InsertReplaceEdit> _textEdit = it.getTextEdit();
      boolean _tripleNotEquals = (_textEdit != null);
      if (_tripleNotEquals) {
        _builder.append(" -> ");
        String _expectation = this.toExpectation(it.getTextEdit());
        _builder.append(_expectation);
        {
          boolean _isNullOrEmpty_1 = IterableExtensions.isNullOrEmpty(it.getAdditionalTextEdits());
          boolean _not_1 = (!_isNullOrEmpty_1);
          if (_not_1) {
            _builder.append("   + ");
            final Function1<TextEdit, String> _function = (TextEdit it_1) -> {
              return this.toExpectation(it_1);
            };
            String _join = IterableExtensions.join(ListExtensions.<TextEdit, String>map(it.getAdditionalTextEdits(), _function), "   + ");
            _builder.append(_join);
          }
        }
      } else {
        if (((it.getInsertText() != null) && (!Objects.equals(it.getInsertText(), it.getLabel())))) {
          _builder.append(" -> ");
          String _insertText = it.getInsertText();
          _builder.append(_insertText);
        }
      }
    }
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final TextEdit it) {
    StringConcatenation _builder = new StringConcatenation();
    String _newText = it.getNewText();
    _builder.append(_newText);
    _builder.append(" ");
    String _expectation = this.toExpectation(it.getRange());
    _builder.append(_expectation);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final Hover it) {
    StringConcatenation _builder = new StringConcatenation();
    String _expectation = this.toExpectation(it.getRange());
    _builder.append(_expectation);
    _builder.newLineIfNotEmpty();
    String _expectation_1 = this.toExpectation(it.getContents());
    _builder.append(_expectation_1);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final SignatureHelp it) {
    String _xblockexpression = null;
    {
      int _size = it.getSignatures().size();
      boolean _tripleEquals = (_size == 0);
      if (_tripleEquals) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Signature index is expected to be null when no signatures are available. Was: ");
        Integer _activeSignature = it.getActiveSignature();
        _builder.append(_activeSignature);
        _builder.append(".");
        Assert.assertNull(_builder.toString(), 
          it.getActiveSignature());
        return "<empty>";
      }
      Assert.assertNotNull("Active signature index must not be null when signatures are available.", it.getActiveSignature());
      String _xifexpression = null;
      Integer _activeParameter = it.getActiveParameter();
      boolean _tripleEquals_1 = (_activeParameter == null);
      if (_tripleEquals_1) {
        _xifexpression = "<empty>";
      } else {
        _xifexpression = it.getSignatures().get((it.getActiveSignature()).intValue()).getParameters().get(
          (it.getActiveParameter()).intValue()).getLabel().getLeft();
      }
      final String param = _xifexpression;
      StringConcatenation _builder_1 = new StringConcatenation();
      final Function1<SignatureInformation, String> _function = (SignatureInformation it_1) -> {
        return it_1.getLabel();
      };
      String _join = IterableExtensions.join(ListExtensions.<SignatureInformation, String>map(it.getSignatures(), _function), " | ");
      _builder_1.append(_join);
      _builder_1.append(" | ");
      _builder_1.append(param);
      _xblockexpression = _builder_1.toString();
    }
    return _xblockexpression;
  }

  protected String _toExpectation(final DocumentHighlight it) {
    String _xblockexpression = null;
    {
      StringConcatenation _builder = new StringConcatenation();
      {
        Range _range = it.getRange();
        boolean _tripleEquals = (_range == null);
        if (_tripleEquals) {
          _builder.append("[NaN, NaN]:[NaN, NaN]");
        } else {
          String _expectation = this.toExpectation(it.getRange());
          _builder.append(_expectation);
        }
      }
      final String rangeString = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      {
        DocumentHighlightKind _kind = it.getKind();
        boolean _tripleEquals_1 = (_kind == null);
        if (_tripleEquals_1) {
          _builder_1.append("NaN");
        } else {
          String _expectation_1 = this.toExpectation(it.getKind());
          _builder_1.append(_expectation_1);
        }
      }
      _builder_1.append(" ");
      _builder_1.append(rangeString);
      _xblockexpression = _builder_1.toString();
    }
    return _xblockexpression;
  }

  protected String _toExpectation(final DocumentHighlightKind kind) {
    return kind.toString().substring(0, 1).toUpperCase();
  }

  protected String _toExpectation(final Map<Object, Object> it) {
    final StringBuilder sb = new StringBuilder();
    final Consumer<Map.Entry<Object, Object>> _function = (Map.Entry<Object, Object> it_1) -> {
      int _length = sb.length();
      boolean _greaterThan = (_length > 0);
      if (_greaterThan) {
        sb.append("\n");
      }
      sb.append(this.toExpectation(it_1.getKey()));
      sb.append(" ->");
      Object _value = it_1.getValue();
      if ((_value instanceof Iterable<?>)) {
        Object _value_1 = it_1.getValue();
        final Consumer<Object> _function_1 = (Object it_2) -> {
          sb.append("\n * ");
          sb.append(this.toExpectation(it_2));
        };
        ((Iterable<?>) _value_1).forEach(_function_1);
      } else {
        sb.append(" ");
        sb.append(this.toExpectation(it_1.getValue()));
      }
    };
    it.entrySet().forEach(_function);
    return sb.toString();
  }

  protected String _toExpectation(final CodeLens it) {
    String _title = it.getCommand().getTitle();
    String _plus = (_title + " ");
    String _expectation = this.toExpectation(it.getRange());
    return (_plus + _expectation);
  }

  protected String _toExpectation(final FoldingRange it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("[");
    String _valueOf = String.valueOf(it.getKind());
    _builder.append(_valueOf);
    _builder.append(" ");
    int _startLine = it.getStartLine();
    _builder.append(_startLine);
    _builder.append("..");
    int _endLine = it.getEndLine();
    _builder.append(_endLine);
    _builder.append("]");
    return _builder.toString();
  }

  protected void testCodeLens(final Procedure1<? super AbstractLanguageServerTest.TestCodeLensConfiguration> configurator) {
    try {
      @Extension
      final AbstractLanguageServerTest.TestCodeLensConfiguration configuration = new AbstractLanguageServerTest.TestCodeLensConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String filePath = this.initializeContext(configuration).getUri();
      CodeLensParams _codeLensParams = new CodeLensParams();
      final Procedure1<CodeLensParams> _function = (CodeLensParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(filePath);
        it.setTextDocument(_textDocumentIdentifier);
      };
      CodeLensParams _doubleArrow = ObjectExtensions.<CodeLensParams>operator_doubleArrow(_codeLensParams, _function);
      final CompletableFuture<List<? extends CodeLens>> codeLenses = this.languageServer.codeLens(_doubleArrow);
      final Function1<CodeLens, CodeLens> _function_1 = (CodeLens it) -> {
        try {
          return this.languageServer.resolveCodeLens(it).get();
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      };
      final List<CodeLens> result = IterableExtensions.<CodeLens>toList(ListExtensions.map(codeLenses.get(), _function_1));
      if ((configuration.assertCodeLenses != null)) {
        configuration.assertCodeLenses.apply(result);
      } else {
        this.assertEquals(configuration.expectedCodeLensItems, this.toExpectation(result));
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected String _toExpectation(final MarkupContent it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("kind: ");
    String _kind = it.getKind();
    _builder.append(_kind);
    _builder.newLineIfNotEmpty();
    _builder.append("value: ");
    String _value = it.getValue();
    _builder.append(_value);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final Command it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("command : ");
    String _command = it.getCommand();
    _builder.append(_command);
    _builder.newLineIfNotEmpty();
    _builder.append("title : ");
    String _title = it.getTitle();
    _builder.append(_title);
    _builder.newLineIfNotEmpty();
    _builder.append("args : ");
    _builder.newLine();
    _builder.append("\t");
    final Function1<Object, CharSequence> _function = (Object it_1) -> {
      return this.toExpectation(it_1);
    };
    String _join = IterableExtensions.<Object>join(it.getArguments(), ",", _function);
    _builder.append(_join, "\t");
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final WorkspaceEdit it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("changes :");
    _builder.newLine();
    {
      Map<String, List<TextEdit>> _changes = it.getChanges();
      boolean _tripleNotEquals = (_changes != null);
      if (_tripleNotEquals) {
        {
          Set<Map.Entry<String, List<TextEdit>>> _entrySet = it.getChanges().entrySet();
          for(final Map.Entry<String, List<TextEdit>> entry : _entrySet) {
            _builder.append("\t");
            String _lastSegment = org.eclipse.emf.common.util.URI.createURI(entry.getKey()).lastSegment();
            _builder.append(_lastSegment, "\t");
            _builder.append(" : ");
            String _expectation = this.toExpectation(entry.getValue());
            _builder.append(_expectation, "\t");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    _builder.append("documentChanges : ");
    _builder.newLine();
    {
      boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(it.getDocumentChanges());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        {
          final Function1<Either<TextDocumentEdit, ResourceOperation>, Boolean> _function = (Either<TextDocumentEdit, ResourceOperation> it_1) -> {
            return Boolean.valueOf(it_1.isLeft());
          };
          final Function1<Either<TextDocumentEdit, ResourceOperation>, TextDocumentEdit> _function_1 = (Either<TextDocumentEdit, ResourceOperation> it_1) -> {
            return it_1.getLeft();
          };
          Iterable<TextDocumentEdit> _map = IterableExtensions.<Either<TextDocumentEdit, ResourceOperation>, TextDocumentEdit>map(IterableExtensions.<Either<TextDocumentEdit, ResourceOperation>>filter(it.getDocumentChanges(), _function), _function_1);
          for(final TextDocumentEdit entry_1 : _map) {
            _builder.append("\t");
            String _expectation_1 = this.toExpectation(entry_1);
            _builder.append(_expectation_1, "\t");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          final Function1<Either<TextDocumentEdit, ResourceOperation>, Boolean> _function_2 = (Either<TextDocumentEdit, ResourceOperation> it_1) -> {
            return Boolean.valueOf(it_1.isRight());
          };
          final Function1<Either<TextDocumentEdit, ResourceOperation>, ResourceOperation> _function_3 = (Either<TextDocumentEdit, ResourceOperation> it_1) -> {
            return it_1.getRight();
          };
          Iterable<ResourceOperation> _map_1 = IterableExtensions.<Either<TextDocumentEdit, ResourceOperation>, ResourceOperation>map(IterableExtensions.<Either<TextDocumentEdit, ResourceOperation>>filter(it.getDocumentChanges(), _function_2), _function_3);
          for(final ResourceOperation entry_2 : _map_1) {
            _builder.append("\t");
            String _expectation_2 = this.toExpectation(entry_2);
            _builder.append(_expectation_2, "\t");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    return _builder.toString();
  }

  protected String _toExpectation(final ResourceOperation it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("kind : ");
    String _kind = it.getKind();
    _builder.append(_kind);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final CodeAction it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("title : ");
    String _title = it.getTitle();
    _builder.append(_title);
    _builder.newLineIfNotEmpty();
    _builder.append("kind : ");
    String _kind = it.getKind();
    _builder.append(_kind);
    _builder.newLineIfNotEmpty();
    _builder.append("command : ");
    Command _command = it.getCommand();
    _builder.append(_command);
    _builder.newLineIfNotEmpty();
    {
      boolean _isNullOrEmpty = IterableExtensions.isNullOrEmpty(it.getDiagnostics());
      boolean _not = (!_isNullOrEmpty);
      if (_not) {
        _builder.append("codes : ");
        final Function1<Diagnostic, Object> _function = (Diagnostic it_1) -> {
          return it_1.getCode().get();
        };
        String _join = IterableExtensions.join(ListExtensions.<Diagnostic, Object>map(it.getDiagnostics(), _function), ",");
        _builder.append(_join);
      }
    }
    _builder.newLineIfNotEmpty();
    _builder.append("edit : ");
    String _expectation = this.toExpectation(it.getEdit());
    _builder.append(_expectation);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final TextDocumentEdit e) {
    StringConcatenation _builder = new StringConcatenation();
    String _expectation = this.toExpectation(e.getTextDocument());
    _builder.append(_expectation);
    _builder.append(" : ");
    String _expectation_1 = this.toExpectation(e.getEdits());
    _builder.append(_expectation_1);
    _builder.newLineIfNotEmpty();
    return _builder.toString();
  }

  protected String _toExpectation(final VersionedTextDocumentIdentifier v) {
    StringConcatenation _builder = new StringConcatenation();
    String _lastSegment = org.eclipse.emf.common.util.URI.createURI(v.getUri()).lastSegment();
    _builder.append(_lastSegment);
    _builder.append(" <");
    Integer _version = v.getVersion();
    _builder.append(_version);
    _builder.append(">");
    return _builder.toString();
  }

  @Beta
  protected void testSemanticTokensFull(final Procedure1<? super SemanticTokensFullConfiguration> configurator) {
    try {
      @Extension
      final SemanticTokensFullConfiguration configuration = new SemanticTokensFullConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String filePath = this.initializeContext(configuration).getUri();
      SemanticTokensParams _semanticTokensParams = new SemanticTokensParams();
      final Procedure1<SemanticTokensParams> _function = (SemanticTokensParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(filePath);
        it.setTextDocument(_textDocumentIdentifier);
      };
      SemanticTokensParams _doubleArrow = ObjectExtensions.<SemanticTokensParams>operator_doubleArrow(_semanticTokensParams, _function);
      final CompletableFuture<SemanticTokens> result = this.languageServer.semanticTokensFull(_doubleArrow);
      Assert.assertArrayEquals(configuration.getExpected().toArray(), result.get().getData().toArray());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testCodeAction(final Procedure1<? super AbstractLanguageServerTest.TestCodeActionConfiguration> configurator) {
    try {
      @Extension
      final AbstractLanguageServerTest.TestCodeActionConfiguration configuration = new AbstractLanguageServerTest.TestCodeActionConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String filePath = this.initializeContext(configuration).getUri();
      CodeActionParams _codeActionParams = new CodeActionParams();
      final Procedure1<CodeActionParams> _function = (CodeActionParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(filePath);
        it.setTextDocument(_textDocumentIdentifier);
        Range _range = new Range();
        final Procedure1<Range> _function_1 = (Range it_1) -> {
          int _line = configuration.getLine();
          int _column = configuration.getColumn();
          Position _position = new Position(_line, _column);
          it_1.setStart(_position);
          it_1.setEnd(it_1.getStart());
        };
        Range _doubleArrow = ObjectExtensions.<Range>operator_doubleArrow(_range, _function_1);
        it.setRange(_doubleArrow);
        CodeActionContext _codeActionContext = new CodeActionContext();
        final Procedure1<CodeActionContext> _function_2 = (CodeActionContext it_1) -> {
          it_1.setDiagnostics(this.getDiagnostics().get(filePath));
        };
        CodeActionContext _doubleArrow_1 = ObjectExtensions.<CodeActionContext>operator_doubleArrow(_codeActionContext, _function_2);
        it.setContext(_doubleArrow_1);
      };
      CodeActionParams _doubleArrow = ObjectExtensions.<CodeActionParams>operator_doubleArrow(_codeActionParams, _function);
      final CompletableFuture<List<Either<Command, CodeAction>>> result = this.languageServer.codeAction(_doubleArrow);
      if ((configuration.assertCodeActions != null)) {
        configuration.assertCodeActions.apply(result.get());
      } else {
        this.assertEquals(configuration.expectedCodeActions, this.toExpectation(result.get()));
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testCompletion(final Procedure1<? super TestCompletionConfiguration> configurator) {
    try {
      @Extension
      final TestCompletionConfiguration configuration = new TestCompletionConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String filePath = this.initializeContext(configuration).getUri();
      CompletionParams _completionParams = new CompletionParams();
      final Procedure1<CompletionParams> _function = (CompletionParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(filePath);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
      };
      CompletionParams _doubleArrow = ObjectExtensions.<CompletionParams>operator_doubleArrow(_completionParams, _function);
      final CompletableFuture<Either<List<CompletionItem>, CompletionList>> completionItems = this.languageServer.completion(_doubleArrow);
      final Either<List<CompletionItem>, CompletionList> result = completionItems.get();
      List<CompletionItem> _xifexpression = null;
      boolean _isLeft = result.isLeft();
      if (_isLeft) {
        _xifexpression = result.getLeft();
      } else {
        _xifexpression = result.getRight().getItems();
      }
      final List<CompletionItem> items = _xifexpression;
      final Function1<CompletionItem, String> _function_1 = (CompletionItem it) -> {
        return it.getSortText();
      };
      Assert.assertEquals(items, IterableExtensions.<CompletionItem>toList(IterableExtensions.<CompletionItem, String>sortBy(items, _function_1)));
      Procedure1<? super CompletionList> _assertCompletionList = configuration.getAssertCompletionList();
      boolean _tripleNotEquals = (_assertCompletionList != null);
      if (_tripleNotEquals) {
        configuration.getAssertCompletionList().apply(result.getRight());
      } else {
        final String actualCompletionItems = this.toExpectation(items);
        this.assertEquals(configuration.getExpectedCompletionItems(), actualCompletionItems);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected FileInfo initializeContext(final TextDocumentConfiguration configuration) {
    this.initialize(configuration.getInitializer());
    boolean _isEmpty = configuration.getFilesInScope().isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      final Function1<Map.Entry<String, CharSequence>, String> _function = (Map.Entry<String, CharSequence> it) -> {
        return this.writeFile(it.getKey(), it.getValue().toString());
      };
      final Iterable<String> createdFiles = IterableExtensions.<Map.Entry<String, CharSequence>, String>map(configuration.getFilesInScope().entrySet(), _function);
      this.didCreateWatchedFiles(((String[])Conversions.unwrapArray(createdFiles, String.class)));
      String _model = configuration.getModel();
      boolean _tripleEquals = (_model == null);
      if (_tripleEquals) {
        String _head = IterableExtensions.<String>head(createdFiles);
        String _string = IterableExtensions.<Map.Entry<String, CharSequence>>head(configuration.getFilesInScope().entrySet()).getValue().toString();
        return new FileInfo(_head, _string);
      }
    }
    Assert.assertNotNull(configuration.getModel());
    final String filePath = this.writeFile(configuration.getFilePath(), configuration.getModel());
    this.open(filePath, configuration.getModel());
    String _model_1 = configuration.getModel();
    return new FileInfo(filePath, _model_1);
  }

  protected void testDefinition(final Procedure1<? super DefinitionTestConfiguration> configurator) {
    try {
      @Extension
      final DefinitionTestConfiguration configuration = new DefinitionTestConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      DefinitionParams _definitionParams = new DefinitionParams();
      final Procedure1<DefinitionParams> _function = (DefinitionParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
      };
      DefinitionParams _doubleArrow = ObjectExtensions.<DefinitionParams>operator_doubleArrow(_definitionParams, _function);
      final CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definitionsFuture = this.languageServer.definition(_doubleArrow);
      final Either<List<? extends Location>, List<? extends LocationLink>> definitions = definitionsFuture.get();
      Procedure1<? super List<? extends Location>> _assertDefinitions = configuration.getAssertDefinitions();
      boolean _tripleNotEquals = (_assertDefinitions != null);
      if (_tripleNotEquals) {
        configuration.getAssertDefinitions().apply(definitions.getLeft());
      } else {
        final String actualDefinitions = this.toExpectation(definitions);
        this.assertEquals(configuration.getExpectedDefinitions(), actualDefinitions);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testHover(final Procedure1<? super HoverTestConfiguration> configurator) {
    try {
      @Extension
      final HoverTestConfiguration configuration = new HoverTestConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      HoverParams _hoverParams = new HoverParams();
      final Procedure1<HoverParams> _function = (HoverParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
      };
      HoverParams _doubleArrow = ObjectExtensions.<HoverParams>operator_doubleArrow(_hoverParams, _function);
      final CompletableFuture<Hover> hoverFuture = this.languageServer.hover(_doubleArrow);
      final Hover hover = hoverFuture.get();
      Procedure1<? super Hover> _assertHover = configuration.getAssertHover();
      boolean _tripleNotEquals = (_assertHover != null);
      if (_tripleNotEquals) {
        configuration.getAssertHover().apply(hover);
      } else {
        final String actualHover = this.toExpectation(hover);
        this.assertEquals(configuration.getExpectedHover(), actualHover);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testSignatureHelp(final Procedure1<? super SignatureHelpConfiguration> configurator) {
    try {
      @Extension
      final SignatureHelpConfiguration configuration = new SignatureHelpConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      SignatureHelpParams _signatureHelpParams = new SignatureHelpParams();
      final Procedure1<SignatureHelpParams> _function = (SignatureHelpParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
      };
      SignatureHelpParams _doubleArrow = ObjectExtensions.<SignatureHelpParams>operator_doubleArrow(_signatureHelpParams, _function);
      final CompletableFuture<SignatureHelp> signatureHelpFuture = this.languageServer.signatureHelp(_doubleArrow);
      final SignatureHelp signatureHelp = signatureHelpFuture.get();
      Procedure1<? super SignatureHelp> _assertSignatureHelp = configuration.getAssertSignatureHelp();
      boolean _tripleNotEquals = (_assertSignatureHelp != null);
      if (_tripleNotEquals) {
        configuration.getAssertSignatureHelp().apply(signatureHelp);
      } else {
        final String actualSignatureHelp = this.toExpectation(signatureHelp);
        this.assertEquals(configuration.getExpectedSignatureHelp().trim(), actualSignatureHelp.trim());
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testDocumentHighlight(final Procedure1<? super DocumentHighlightConfiguration> configurator) {
    try {
      DocumentHighlightConfiguration _documentHighlightConfiguration = new DocumentHighlightConfiguration();
      final Procedure1<DocumentHighlightConfiguration> _function = (DocumentHighlightConfiguration it) -> {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("MyModel.");
        _builder.append(this.fileExtension);
        it.setFilePath(_builder.toString());
      };
      @Extension
      final DocumentHighlightConfiguration configuration = ObjectExtensions.<DocumentHighlightConfiguration>operator_doubleArrow(_documentHighlightConfiguration, _function);
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      DocumentHighlightParams _documentHighlightParams = new DocumentHighlightParams();
      final Procedure1<DocumentHighlightParams> _function_1 = (DocumentHighlightParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
      };
      DocumentHighlightParams _doubleArrow = ObjectExtensions.<DocumentHighlightParams>operator_doubleArrow(_documentHighlightParams, _function_1);
      final CompletableFuture<List<? extends DocumentHighlight>> highlights = this.languageServer.documentHighlight(_doubleArrow);
      final Function1<DocumentHighlight, String> _function_2 = (DocumentHighlight it) -> {
        return this.toExpectation(it);
      };
      final String actualDocumentHighlight = IterableExtensions.join(ListExtensions.map(highlights.get(), _function_2), " | ");
      this.assertEquals(configuration.getExpectedDocumentHighlight(), actualDocumentHighlight);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testDocumentSymbol(final Procedure1<? super DocumentSymbolConfiguration> configurator) {
    try {
      @Extension
      final DocumentSymbolConfiguration configuration = new DocumentSymbolConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
      DocumentSymbolParams _documentSymbolParams = new DocumentSymbolParams(_textDocumentIdentifier);
      final CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbolsFuture = this.languageServer.documentSymbol(_documentSymbolParams);
      final List<Either<SymbolInformation, DocumentSymbol>> symbols = symbolsFuture.get();
      Procedure1<? super List<Either<SymbolInformation, DocumentSymbol>>> _assertSymbols = configuration.getAssertSymbols();
      boolean _tripleNotEquals = (_assertSymbols != null);
      if (_tripleNotEquals) {
        configuration.getAssertSymbols().apply(symbols);
      } else {
        final Function1<Either<SymbolInformation, DocumentSymbol>, DocumentSymbol> _function = (Either<SymbolInformation, DocumentSymbol> it) -> {
          return it.getRight();
        };
        final List<DocumentSymbol> unwrappedSymbols = ListExtensions.<Either<SymbolInformation, DocumentSymbol>, DocumentSymbol>map(symbols, _function);
        final String actualSymbols = this.toExpectation(unwrappedSymbols);
        this.assertEquals(configuration.getExpectedSymbols(), actualSymbols);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testSymbol(final Procedure1<? super WorkspaceSymbolConfiguration> configurator) {
    try {
      @Extension
      final WorkspaceSymbolConfiguration configuration = new WorkspaceSymbolConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      this.initializeContext(configuration);
      String _query = configuration.getQuery();
      WorkspaceSymbolParams _workspaceSymbolParams = new WorkspaceSymbolParams(_query);
      final Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = this.languageServer.symbol(_workspaceSymbolParams).get();
      if (((configuration.getAssertSymbols() != null) || (configuration.getAssertWorkspaceSymbols() != null))) {
        boolean _isLeft = symbols.isLeft();
        if (_isLeft) {
          configuration.getAssertSymbols().apply(symbols.getLeft());
        } else {
          configuration.getAssertWorkspaceSymbols().apply(symbols.getRight());
        }
      } else {
        final String actualSymbols = this.toExpectation(symbols);
        this.assertEquals(configuration.getExpectedSymbols(), actualSymbols);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testReferences(final Procedure1<? super ReferenceTestConfiguration> configurator) {
    try {
      @Extension
      final ReferenceTestConfiguration configuration = new ReferenceTestConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final String fileUri = this.initializeContext(configuration).getUri();
      ReferenceParams _referenceParams = new ReferenceParams();
      final Procedure1<ReferenceParams> _function = (ReferenceParams it) -> {
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(fileUri);
        it.setTextDocument(_textDocumentIdentifier);
        int _line = configuration.getLine();
        int _column = configuration.getColumn();
        Position _position = new Position(_line, _column);
        it.setPosition(_position);
        boolean _isIncludeDeclaration = configuration.isIncludeDeclaration();
        ReferenceContext _referenceContext = new ReferenceContext(_isIncludeDeclaration);
        it.setContext(_referenceContext);
      };
      ReferenceParams _doubleArrow = ObjectExtensions.<ReferenceParams>operator_doubleArrow(_referenceParams, _function);
      final CompletableFuture<List<? extends Location>> referencesFuture = this.languageServer.references(_doubleArrow);
      final List<? extends Location> references = referencesFuture.get();
      Procedure1<? super List<? extends Location>> _assertReferences = configuration.getAssertReferences();
      boolean _tripleNotEquals = (_assertReferences != null);
      if (_tripleNotEquals) {
        configuration.getAssertReferences().apply(references);
      } else {
        final String actualReferences = this.toExpectation(references);
        this.assertEquals(configuration.getExpectedReferences(), actualReferences);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public void assertEquals(final String expected, final String actual) {
    final String expectedM = expected.replace(System.lineSeparator(), "\n");
    final String actualM = actual.replace(System.lineSeparator(), "\n");
    Assert.assertEquals(expectedM.replace("\t", "    "), actualM.replace("\t", "    "));
  }

  public void assertEqualsStricter(final String expected, final String actual) {
    final String expectedM = expected.replace(System.lineSeparator(), "\n");
    final String actualM = actual.replace(System.lineSeparator(), "\n");
    Assert.assertEquals(expectedM, actualM);
  }

  protected void testFormatting(final Procedure1<? super FormattingConfiguration> configurator) {
    this.testFormatting(null, configurator);
  }

  protected void testFormatting(final Procedure1<? super DocumentFormattingParams> paramsConfigurator, final Procedure1<? super FormattingConfiguration> configurator) {
    try {
      @Extension
      final FormattingConfiguration configuration = new FormattingConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final FileInfo fileInfo = this.initializeContext(configuration);
      DocumentFormattingParams _documentFormattingParams = new DocumentFormattingParams();
      final Procedure1<DocumentFormattingParams> _function = (DocumentFormattingParams it) -> {
        String _uri = fileInfo.getUri();
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(_uri);
        it.setTextDocument(_textDocumentIdentifier);
        if ((paramsConfigurator != null)) {
          paramsConfigurator.apply(it);
        }
      };
      DocumentFormattingParams _doubleArrow = ObjectExtensions.<DocumentFormattingParams>operator_doubleArrow(_documentFormattingParams, _function);
      final CompletableFuture<List<? extends TextEdit>> changes = this.languageServer.formatting(_doubleArrow);
      String _contents = fileInfo.getContents();
      final Document result = new Document(Integer.valueOf(1), _contents).applyChanges(ListExtensions.<TextEdit>reverse(CollectionLiterals.<TextEdit>newArrayList(((TextEdit[])Conversions.unwrapArray(changes.get(), TextEdit.class)))));
      this.assertEqualsStricter(configuration.getExpectedText(), result.getContents());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  protected void testRangeFormatting(final Procedure1<? super RangeFormattingConfiguration> configurator) {
    this.testRangeFormatting(null, configurator);
  }

  protected void testRangeFormatting(final Procedure1<? super DocumentRangeFormattingParams> paramsConfigurator, final Procedure1<? super RangeFormattingConfiguration> configurator) {
    try {
      @Extension
      final RangeFormattingConfiguration configuration = new RangeFormattingConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final FileInfo fileInfo = this.initializeContext(configuration);
      DocumentRangeFormattingParams _documentRangeFormattingParams = new DocumentRangeFormattingParams();
      final Procedure1<DocumentRangeFormattingParams> _function = (DocumentRangeFormattingParams it) -> {
        String _uri = fileInfo.getUri();
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(_uri);
        it.setTextDocument(_textDocumentIdentifier);
        it.setRange(configuration.getRange());
        if ((paramsConfigurator != null)) {
          paramsConfigurator.apply(it);
        }
      };
      DocumentRangeFormattingParams _doubleArrow = ObjectExtensions.<DocumentRangeFormattingParams>operator_doubleArrow(_documentRangeFormattingParams, _function);
      final CompletableFuture<List<? extends TextEdit>> changes = this.languageServer.rangeFormatting(_doubleArrow);
      String _contents = fileInfo.getContents();
      final Document result = new Document(Integer.valueOf(1), _contents).applyChanges(ListExtensions.<TextEdit>reverse(CollectionLiterals.<TextEdit>newArrayList(((TextEdit[])Conversions.unwrapArray(changes.get(), TextEdit.class)))));
      this.assertEqualsStricter(configuration.getExpectedText(), result.getContents());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * @since 2.26
   */
  protected void testFolding(final Procedure1<? super FoldingConfiguration> configurator) {
    try {
      @Extension
      final FoldingConfiguration configuration = new FoldingConfiguration();
      configuration.setFilePath(("MyModel." + this.fileExtension));
      configurator.apply(configuration);
      final FileInfo fileInfo = this.initializeContext(configuration);
      FoldingRangeRequestParams _foldingRangeRequestParams = new FoldingRangeRequestParams();
      final Procedure1<FoldingRangeRequestParams> _function = (FoldingRangeRequestParams it) -> {
        String _uri = fileInfo.getUri();
        TextDocumentIdentifier _textDocumentIdentifier = new TextDocumentIdentifier(_uri);
        it.setTextDocument(_textDocumentIdentifier);
      };
      FoldingRangeRequestParams _doubleArrow = ObjectExtensions.<FoldingRangeRequestParams>operator_doubleArrow(_foldingRangeRequestParams, _function);
      final List<FoldingRange> foldings = this.languageServer.foldingRange(_doubleArrow).get();
      this.assertEqualsStricter(configuration.getExpectedFoldings(), this.toExpectation(foldings));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Override
  public void notify(final String method, final Object parameter) {
    Pair<String, Object> _mappedTo = Pair.<String, Object>of(method, parameter);
    this.notifications.add(_mappedTo);
  }

  @Override
  public CompletableFuture<?> request(final String method, final Object parameter) {
    return CompletableFuture.<Object>completedFuture(null);
  }

  protected Map<String, List<Diagnostic>> getDiagnostics() {
    try {
      final Function1<CancelIndicator, HashMap<String, List<Diagnostic>>> _function = (CancelIndicator it) -> {
        final HashMap<String, List<Diagnostic>> result = CollectionLiterals.<String, List<Diagnostic>>newHashMap();
        final Function1<Pair<String, Object>, Object> _function_1 = (Pair<String, Object> it_1) -> {
          return it_1.getValue();
        };
        Iterable<PublishDiagnosticsParams> _filter = Iterables.<PublishDiagnosticsParams>filter(ListExtensions.<Pair<String, Object>, Object>map(this.notifications, _function_1), PublishDiagnosticsParams.class);
        for (final PublishDiagnosticsParams diagnostic : _filter) {
          result.put(diagnostic.getUri(), diagnostic.getDiagnostics());
        }
        return result;
      };
      return this.languageServer.getRequestManager().<HashMap<String, List<Diagnostic>>>runRead(_function).get();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @XbaseGenerated
  protected String toExpectation(final Object it) {
    if (it instanceof Integer) {
      return _toExpectation((Integer)it);
    } else if (it instanceof List) {
      return _toExpectation((List<?>)it);
    } else if (it instanceof DocumentHighlightKind) {
      return _toExpectation((DocumentHighlightKind)it);
    } else if (it instanceof String) {
      return _toExpectation((String)it);
    } else if (it instanceof VersionedTextDocumentIdentifier) {
      return _toExpectation((VersionedTextDocumentIdentifier)it);
    } else if (it == null) {
      return _toExpectation((Void)null);
    } else if (it instanceof Map) {
      return _toExpectation((Map<Object, Object>)it);
    } else if (it instanceof CodeAction) {
      return _toExpectation((CodeAction)it);
    } else if (it instanceof CodeLens) {
      return _toExpectation((CodeLens)it);
    } else if (it instanceof Command) {
      return _toExpectation((Command)it);
    } else if (it instanceof CompletionItem) {
      return _toExpectation((CompletionItem)it);
    } else if (it instanceof DocumentHighlight) {
      return _toExpectation((DocumentHighlight)it);
    } else if (it instanceof DocumentSymbol) {
      return _toExpectation((DocumentSymbol)it);
    } else if (it instanceof FoldingRange) {
      return _toExpectation((FoldingRange)it);
    } else if (it instanceof Hover) {
      return _toExpectation((Hover)it);
    } else if (it instanceof Location) {
      return _toExpectation((Location)it);
    } else if (it instanceof MarkupContent) {
      return _toExpectation((MarkupContent)it);
    } else if (it instanceof Position) {
      return _toExpectation((Position)it);
    } else if (it instanceof Range) {
      return _toExpectation((Range)it);
    } else if (it instanceof ResourceOperation) {
      return _toExpectation((ResourceOperation)it);
    } else if (it instanceof SignatureHelp) {
      return _toExpectation((SignatureHelp)it);
    } else if (it instanceof SymbolInformation) {
      return _toExpectation((SymbolInformation)it);
    } else if (it instanceof TextDocumentEdit) {
      return _toExpectation((TextDocumentEdit)it);
    } else if (it instanceof TextEdit) {
      return _toExpectation((TextEdit)it);
    } else if (it instanceof WorkspaceEdit) {
      return _toExpectation((WorkspaceEdit)it);
    } else if (it instanceof WorkspaceSymbol) {
      return _toExpectation((WorkspaceSymbol)it);
    } else if (it instanceof Either) {
      return _toExpectation((Either<?, ?>)it);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(it).toString());
    }
  }

  public AbstractLanguageServerTest(final String fileExtension) {
    super();
    this.fileExtension = fileExtension;
  }

  @Pure
  public String getFileExtension() {
    return this.fileExtension;
  }
}
