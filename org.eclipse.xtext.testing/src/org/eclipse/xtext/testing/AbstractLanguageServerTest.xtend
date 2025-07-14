/*******************************************************************************
 * Copyright (c) 2016, 2022 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Module
import com.google.inject.Singleton
import java.io.File
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.List
import java.util.Map
import java.util.concurrent.CompletableFuture
import org.eclipse.emf.common.util.URI
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLens
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlight
import org.eclipse.lsp4j.DocumentHighlightKind
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.DocumentSymbol
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.FileChangeType
import org.eclipse.lsp4j.FileEvent
import org.eclipse.lsp4j.FoldingRange
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.ResourceOperation
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.SymbolInformation
import org.eclipse.lsp4j.TextDocumentEdit
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.Endpoint
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.LanguageInfo
import org.eclipse.xtext.ide.server.Document
import org.eclipse.xtext.ide.server.LanguageServerImpl
import org.eclipse.xtext.ide.server.ServerModule
import org.eclipse.xtext.ide.server.UriExtensions
import org.eclipse.xtext.ide.server.concurrent.AbstractRequestManager
import org.eclipse.xtext.ide.server.concurrent.IRequestManager
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.service.OperationCanceledManager
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.util.Files
import org.eclipse.xtext.util.Modules2
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import static extension org.eclipse.lsp4j.util.Ranges.containsRange
import static extension org.eclipse.xtext.util.Strings.*
import org.eclipse.lsp4j.WorkspaceSymbol
import org.eclipse.lsp4j.SemanticTokensParams
import com.google.common.annotations.Beta
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.WorkspaceClientCapabilities

/**
 * @author Sven Efftinge - Initial contribution and API
 *         Rubén Porras Campo - Semantic Tokens Full
 
 */
@FinalFieldsConstructor
abstract class AbstractLanguageServerTest implements Endpoint {

	@Accessors
	protected val String fileExtension

	protected static val TEST_PROJECT_PATH = "/test-data/test-project"

	@Before @BeforeEach
	def void setup() {
		val injector = Guice.createInjector(getServerModule())
		injector.injectMembers(this)

		val resourceServiceProvider = resourceServerProviderRegistry.extensionToFactoryMap.get(fileExtension)
		if (resourceServiceProvider instanceof IResourceServiceProvider)
			languageInfo = resourceServiceProvider.get(LanguageInfo)

		// register notification callbacks
		languageServer.connect(ServiceEndpoints.toServiceObject(this, languageClientClass))
		// initialize
		languageServer.supportedMethods()

		// create workingdir
		root = new File(new File("").absoluteFile, TEST_PROJECT_PATH)
	}

	@After @AfterEach
	def void cleanup() {
		if (root.exists) {
			Files.cleanFolder(root, null, true, true)
		}
	}

	/**
	 * @since 2.18
	 */
	protected def Class<? extends LanguageClient> getLanguageClientClass() {
		return LanguageClient;
	}
	
	/**
	 * A request manager that will run the given read and write actions in the same thread immediately, sequentially.
	 */
	@Singleton
	static class DirectRequestManager extends AbstractRequestManager {
	
		@Inject
		new(OperationCanceledManager operationCanceledManager) {
			super(operationCanceledManager)
		}
		
		override synchronized <V> runRead((CancelIndicator)=>V request) {
			val result = new CompletableFuture()
			try {
				result.complete(request.apply [ false ])
			} catch (Throwable t) {
				if (isCancelException(t)) {
					result.cancel(true)
				} else {
					result.completeExceptionally(t)
				}
			}
			return result
		}

		override synchronized <U, V> runWrite(()=>U nonCancellable, (CancelIndicator, U)=>V request) {
			val result = new CompletableFuture()
			try {
				result.complete(request.apply(CancelIndicator.NullImpl, nonCancellable.apply()))
			} catch (Throwable t) {
				if (isCancelException(t)) {
					result.cancel(true)
				} else {
					result.completeExceptionally(t)
				}
			}
			return result
		}
		
		override shutdown() {
			// no shutdown
		}
		
	}

	protected def Module getServerModule() {
		return Modules2.mixin(new ServerModule) [
			bind(IRequestManager).to(DirectRequestManager)
		]
	}

	@Inject
	protected IResourceServiceProvider.Registry resourceServerProviderRegistry

	@Inject extension UriExtensions
	@Inject protected LanguageServerImpl languageServer

	protected List<Pair<String, Object>> notifications = newArrayList()
	protected File root
	protected LanguageInfo languageInfo
	protected boolean hierarchicalDocumentSymbolSupport = false;

	protected def Path getTestRootPath() {
		root.toPath().toAbsolutePath().normalize()
	}

	protected def Path relativize(String uri) {
		val path = Paths.get(new java.net.URI(uri))
		testRootPath.relativize(path)
	}

	protected def InitializeResult initialize() {
		return initialize(null)
	}

	protected def InitializeResult initialize((InitializeParams)=>void initializer) {
		initialize(initializer, true)
	}
	
	protected def InitializeResult initialize((InitializeParams)=>void initializer, boolean callInitialized) {
		initialize(initializer, callInitialized, false)
	}
	
	protected def InitializeResult initialize((InitializeParams)=>void initializer, boolean callInitialized, boolean useRootPath) {
		val params = new InitializeParams => [
			processId = 1
			workspaceFolders = #[
				new WorkspaceFolder(root.toURI.normalize.toUriString, '')
			]
		]
		if (!useRootPath) {
			if (params.capabilities === null) {
				params.capabilities = new ClientCapabilities
			}
			if (params.capabilities.workspace === null) {
				params.capabilities.workspace = new WorkspaceClientCapabilities
			}
			params.capabilities.workspace.workspaceFolders = true
		}
		initializer?.apply(params)
		hierarchicalDocumentSymbolSupport = params.capabilities?.textDocument?.documentSymbol?.
			hierarchicalDocumentSymbolSupport ?: false;
		val result = languageServer.initialize(params).get
		if (callInitialized)
			languageServer.initialized(null)
		return result
	}

	protected def void open(String fileUri, String model) {
		open(fileUri, languageInfo.languageName, model)
	}

	protected def void open(String fileUri, String langaugeId, String model) {
		languageServer.didOpen(new DidOpenTextDocumentParams => [
			textDocument = new TextDocumentItem => [
				uri = fileUri
				languageId = langaugeId
				version = 1
				text = model.toUnixLineSeparator
			]
		])
	}

	protected def void didCreateWatchedFiles(String ... fileUris) {
		languageServer.didChangeWatchedFiles(new DidChangeWatchedFilesParams => [
			for (fileUri : fileUris) {
				changes += new FileEvent => [
					uri = fileUri
					it.type = FileChangeType.Created
				]
			}
		])
	}

	protected def void close(String fileUri) {
		languageServer.didClose(new DidCloseTextDocumentParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
		])
	}

	def String writeFile(String path, CharSequence contents) {
		val file = new File(root, path)
		file.parentFile.mkdirs
		file.createNewFile

		val writer = new FileWriter(file)
		writer.write(contents.toString)
		writer.close

		return file.toURI.normalize.toUriString
	}

	def void deleteFile(String path) {
		new File(root, path).delete
	}

	def String getVirtualFile(String path) {
		val file = new File(root, path)
		return file.toURI.normalize.toUriString
	}

	protected def dispatch String toExpectation(List<?> elements) '''
		«FOR element : elements»
			«element.toExpectation»
		«ENDFOR»
	'''
	protected def dispatch String toExpectation(String it) { it }
	
	protected def dispatch String toExpectation(Integer it) { '''«it»''' }
	
	protected def dispatch String toExpectation(Void it) { '' }

	protected dispatch def String toExpectation(Either<?, ?> either) '''
		«IF either.isLeft»
		«either.getLeft.toExpectation»
		«ELSE»
		«either.getRight.toExpectation»
		«ENDIF»
	'''

	protected def dispatch String toExpectation(Location it) '''«uri.relativize» «range.toExpectation»'''

	protected def dispatch String toExpectation(Range it) '''[«start.toExpectation» .. «end.toExpectation»]'''

	protected def dispatch String toExpectation(Position it) '''[«line», «character»]'''

	@Deprecated
	protected def dispatch String toExpectation(SymbolInformation it) '''
		symbol "«name»" {
		    kind: «kind.value»
		    location: «location.toExpectation»
		    «IF !containerName.nullOrEmpty»
		    	container: "«containerName»"
		    «ENDIF»
		}
	'''

	protected def dispatch String toExpectation(WorkspaceSymbol it) '''
		symbol "«name»" {
		    kind: «kind.value»
		    location: «location.toExpectation»
		    «IF !containerName.nullOrEmpty»
		    	container: "«containerName»"
		    «ENDIF»
		}
	'''

	/**
	 * @since 2.16
	 */
	protected def dispatch String toExpectation(DocumentSymbol it) {
		Assert.assertTrue('''selectionRange must be contained in the range: «it»''', range.containsRange(selectionRange))
	'''
		symbol "«name»" {
		    kind: «kind.value»
		    range: «range.toExpectation»
		    selectionRange: «selectionRange.toExpectation»
		    details: «detail»
		    deprecated: «deprecated»
		    «IF !children.nullOrEmpty»
		    children: [
		    	«FOR child : children SEPARATOR'\n'»«child.toExpectation»«ENDFOR»
		    ]
		    «ENDIF»
		}'''
	}

	protected def dispatch String toExpectation(CompletionItem it) '''
		«label»«IF !detail.nullOrEmpty» («detail»)«ENDIF»«IF textEdit !== null» -> «textEdit.toExpectation»«IF !additionalTextEdits.nullOrEmpty»   + «additionalTextEdits.map[toExpectation].join('   + ')»«ENDIF»«ELSEIF insertText !== null && insertText != label» -> «insertText»«ENDIF»
	'''

	protected dispatch def String toExpectation(TextEdit it) '''
		«newText» «range.toExpectation»
	'''

	protected dispatch def String toExpectation(Hover it) '''
		«range.toExpectation»
		«contents.toExpectation»
	'''

	protected dispatch def String toExpectation(SignatureHelp it) {
		if (signatures.size === 0) {
			Assert.
				assertNull('''Signature index is expected to be null when no signatures are available. Was: «activeSignature».''',
					activeSignature);
			return '<empty>';
		}
		Assert.assertNotNull('Active signature index must not be null when signatures are available.', activeSignature);
		val param = if(activeParameter === null) '<empty>' else signatures.get(activeSignature).parameters.get(
				activeParameter).label.getLeft();
		'''«signatures.map[label].join(' | ')» | «param»''';
	}

	protected dispatch def String toExpectation(DocumentHighlight it) {
		val rangeString = '''«IF range === null»[NaN, NaN]:[NaN, NaN]«ELSE»«range.toExpectation»«ENDIF»''';
		'''«IF kind === null»NaN«ELSE»«kind.toExpectation»«ENDIF» «rangeString»'''
	}

	protected dispatch def String toExpectation(DocumentHighlightKind kind) {
		return kind.toString.substring(0, 1).toUpperCase;
	}
	
	protected dispatch def String toExpectation(Map<Object, Object> it) {
		val sb = new StringBuilder;
		entrySet.forEach[
			if (sb.length > 0) {
				sb.append('\n');
			}
			sb.append(key.toExpectation);
			sb.append(' ->');
			if (value instanceof Iterable<?>) {
				(value as Iterable<?>).forEach[
					sb.append('\n * ');
					sb.append(toExpectation);
				]
			} else {
				sb.append(' ');
				sb.append(value.toExpectation);
			}
 		];
		return sb.toString;
	}

	protected dispatch def String toExpectation(CodeLens it) {
		return command.title + " " +range.toExpectation
	}
	
	protected dispatch def String toExpectation(FoldingRange it) {
		return '''[«String.valueOf(kind)» «startLine»..«endLine»]'''
	}

	@Accessors static class TestCodeLensConfiguration extends TextDocumentPositionConfiguration {
		String expectedCodeLensItems = ''
		(List<? extends CodeLens>)=>void assertCodeLenses = null
	}

	protected def void testCodeLens((TestCodeLensConfiguration)=>void configurator) {
		val extension configuration = new TestCodeLensConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		val codeLenses = languageServer.codeLens(new CodeLensParams=>[
			textDocument = new TextDocumentIdentifier(filePath)
		])
		val result = codeLenses.get.map[languageServer.resolveCodeLens(it).get].toList

		if (configuration.assertCodeLenses !== null) {
			configuration.assertCodeLenses.apply(result)
		} else {
			assertEquals(expectedCodeLensItems, result.toExpectation)
		}
	}
	
	protected dispatch def String toExpectation(MarkupContent it) '''
		kind: «kind»
		value: «value»
	'''

	protected dispatch def String toExpectation(Command it) '''
		command : «command»
		title : «title»
		args : 
			«arguments.join(',')[toExpectation]»
	'''

	protected dispatch def String toExpectation(WorkspaceEdit it) '''
		changes :
			«IF changes !== null»
				«FOR entry : changes.entrySet»
					«URI.createURI(entry.key).lastSegment» : «entry.value.toExpectation»
				«ENDFOR»
			«ENDIF»
		documentChanges : 
			«IF !documentChanges.nullOrEmpty»
				«FOR entry: documentChanges.filter[isLeft].map[getLeft]»
					«entry.toExpectation»
				«ENDFOR»
				«FOR entry: documentChanges.filter[isRight].map[getRight]»
					«entry.toExpectation»
				«ENDFOR»
			«ENDIF»
	'''
	
	protected dispatch def String toExpectation(ResourceOperation it) '''
		kind : «kind»
	'''
	
	protected dispatch def String toExpectation(CodeAction it)  '''
		title : «title»
		kind : «kind»
		command : «command»
		«IF !diagnostics.nullOrEmpty»codes : «diagnostics.map[code.get].join(',')»«ENDIF»
		edit : «edit.toExpectation»
	'''
	
	protected def dispatch String toExpectation(TextDocumentEdit e) '''
		«e.textDocument.toExpectation» : «e.edits.toExpectation»
	'''

	protected def dispatch String toExpectation(VersionedTextDocumentIdentifier v) 
		'''«URI.createURI(v.uri).lastSegment» <«v.version»>'''
	
	
	
	@Accessors static class TestCodeActionConfiguration extends TextDocumentPositionConfiguration {
		String expectedCodeActions = ''

		(List<Either<Command, CodeAction>>)=>void assertCodeActions= null
	}

	@Beta
	protected def void testSemanticTokensFull((SemanticTokensFullConfiguration)=>void configurator) {
		val extension configuration = new SemanticTokensFullConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		val result = languageServer.semanticTokensFull(new SemanticTokensParams => [
			textDocument = new TextDocumentIdentifier(filePath)
		])

		Assert.assertArrayEquals(configuration.expected.toArray, result.get.data.toArray)
	}

	protected def void testCodeAction((TestCodeActionConfiguration)=>void configurator) {
		val extension configuration = new TestCodeActionConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		val result = languageServer.codeAction(new CodeActionParams=>[
			textDocument = new TextDocumentIdentifier(filePath)
			range = new Range => [
				start = new Position(configuration.line, configuration.column)
				end = start
			]
			context = new CodeActionContext => [
				diagnostics = this.diagnostics.get(filePath)
			]
		])

		if (configuration.assertCodeActions !== null) {
			configuration.assertCodeActions.apply(result.get)
		} else {
			assertEquals(configuration.expectedCodeActions, result.get.toExpectation)
		}
	}

	protected def void testCompletion((TestCompletionConfiguration)=>void configurator) {
		val extension configuration = new TestCompletionConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val filePath = initializeContext(configuration).uri
		val completionItems = languageServer.completion(new CompletionParams => [
			textDocument = new TextDocumentIdentifier(filePath)
			position = new Position(line, column)
		])

		val result = completionItems.get
		val items = if (result.isLeft) result.getLeft else result.getRight.items
		// assert ordered by sortText
		Assert.assertEquals(items, items.sortBy[sortText].toList)
		if (configuration.assertCompletionList !== null) {
			configuration.assertCompletionList.apply(result.getRight)
		} else {
			val actualCompletionItems = items.toExpectation
			assertEquals(expectedCompletionItems, actualCompletionItems)
		}
	}

	protected def FileInfo initializeContext(TextDocumentConfiguration configuration) {
		initialize(configuration.initializer)
		// create files on disk and notify languageServer
		if (!configuration.filesInScope.isEmpty) {
			val createdFiles = configuration.filesInScope.entrySet.map[key.writeFile(value.toString)]
			didCreateWatchedFiles(createdFiles)

			if (configuration.model === null) {
				return new FileInfo(createdFiles.head, configuration.filesInScope.entrySet.head.value.toString)
			}
		}
		Assert.assertNotNull(configuration.model)
		val filePath = configuration.filePath.writeFile(configuration.model)
		open(filePath, configuration.model)
		return new FileInfo(filePath, configuration.model)
	}

	protected def void testDefinition((DefinitionTestConfiguration)=>void configurator) {
		val extension configuration = new DefinitionTestConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val fileUri = initializeContext(configuration).uri
		val definitionsFuture = languageServer.definition(new DefinitionParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
			position = new Position(line, column)
		])
		val definitions = definitionsFuture.get
		if (configuration.assertDefinitions !== null) {
			configuration.assertDefinitions.apply(definitions.getLeft())
		} else {
			val actualDefinitions = definitions.toExpectation
			assertEquals(expectedDefinitions, actualDefinitions)
		}
	}

	protected def void testHover((HoverTestConfiguration)=>void configurator) {
		val extension configuration = new HoverTestConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val fileUri = initializeContext(configuration).uri

		val hoverFuture = languageServer.hover(new HoverParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
			position = new Position(line, column)
		])
		val hover = hoverFuture.get
		if (configuration.assertHover !== null) {
			configuration.assertHover.apply(hover)
		} else {
			val actualHover = hover.toExpectation
			assertEquals(expectedHover, actualHover)
		}
	}

	protected def testSignatureHelp((SignatureHelpConfiguration)=>void configurator) {
		val extension configuration = new SignatureHelpConfiguration;
		configuration.filePath = 'MyModel.' + fileExtension;
		configurator.apply(configuration);

		val fileUri = initializeContext(configuration).uri

		val signatureHelpFuture = languageServer.signatureHelp(new SignatureHelpParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
			position = new Position(line, column)
		]);
		val signatureHelp = signatureHelpFuture.get
		if (configuration.assertSignatureHelp !== null) {
			configuration.assertSignatureHelp.apply(signatureHelp)
		} else {
			val actualSignatureHelp = signatureHelp.toExpectation
			assertEquals(expectedSignatureHelp.trim, actualSignatureHelp.trim)
		}
	}

	protected def testDocumentHighlight((DocumentHighlightConfiguration)=>void configurator) {
		val extension configuration = new DocumentHighlightConfiguration => [
			filePath = '''MyModel.«fileExtension»''';
		];
		configurator.apply(configuration);

		val fileUri = initializeContext(configuration).uri;
		val highlights = languageServer.documentHighlight(new DocumentHighlightParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
			position = new Position(line, column)
		]);

		val actualDocumentHighlight = highlights.get.map[toExpectation].join(' | ');
		assertEquals(expectedDocumentHighlight, actualDocumentHighlight);
	}

	protected def void testDocumentSymbol((DocumentSymbolConfiguration)=>void configurator) {
		val extension configuration = new DocumentSymbolConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)

		val fileUri = initializeContext(configuration).uri
		val symbolsFuture = languageServer.documentSymbol(new DocumentSymbolParams(new TextDocumentIdentifier(fileUri)))
		val symbols = symbolsFuture.get
		if (configuration.getAssertSymbols !== null) {
			configuration.getAssertSymbols.apply(symbols)
		} else {
			val unwrappedSymbols = symbols.map[getRight]
			val String actualSymbols = unwrappedSymbols.toExpectation
			assertEquals(getExpectedSymbols, actualSymbols)
		}
	}

	protected def void testSymbol((WorkspaceSymbolConfiguration)=>void configurator) {
		val extension configuration = new WorkspaceSymbolConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)

		initializeContext(configuration)
		val symbols = languageServer.symbol(new WorkspaceSymbolParams(query)).get
		if (configuration.assertSymbols !== null || configuration.assertWorkspaceSymbols !== null) {
			if (symbols.isLeft) {
				configuration.assertSymbols.apply(symbols.getLeft)
			} else {
				configuration.assertWorkspaceSymbols.apply(symbols.getRight)
			}
		} else {
			val String actualSymbols = symbols.toExpectation
			assertEquals(expectedSymbols, actualSymbols)
		}
	}

	protected def void testReferences((ReferenceTestConfiguration)=>void configurator) {
		val extension configuration = new ReferenceTestConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val fileUri = initializeContext(configuration).uri
		val referencesFuture = languageServer.references(new ReferenceParams => [
			textDocument = new TextDocumentIdentifier(fileUri)
			position = new Position(line, column)
			context = new ReferenceContext(includeDeclaration)
		])
		val references = referencesFuture.get
		if (configuration.assertReferences !== null) {
			configuration.assertReferences.apply(references)
		} else {
			val actualReferences = references.toExpectation
			assertEquals(expectedReferences, actualReferences)
		}
	}

	def void assertEquals(String expected, String actual) {
		val expectedM = expected.replace(System.lineSeparator, '\n')
		val actualM = actual.replace(System.lineSeparator, '\n')
		Assert.assertEquals(expectedM.replace('\t', '    '), actualM.replace('\t', '    '))
	}

	def void assertEqualsStricter(String expected, String actual) {
		val expectedM = expected.replace(System.lineSeparator, '\n')
		val actualM = actual.replace(System.lineSeparator, '\n')
		Assert.assertEquals(expectedM, actualM)
	}

	protected def testFormatting((FormattingConfiguration)=>void configurator) {
		testFormatting(null, configurator)
	}

	protected def testFormatting((DocumentFormattingParams)=>void paramsConfigurator, (FormattingConfiguration)=>void configurator) {
		val extension configuration = new FormattingConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		val fileInfo = initializeContext(configuration)

		val changes = languageServer.formatting(new DocumentFormattingParams => [
			textDocument = new TextDocumentIdentifier(fileInfo.uri)
			if (paramsConfigurator !== null) {
				paramsConfigurator.apply(it)
			}
		])
		val result = new Document(1, fileInfo.contents).applyChanges(<TextEdit>newArrayList(changes.get()).reverse)
		assertEqualsStricter(configuration.expectedText, result.contents)
	}

	protected def testRangeFormatting((RangeFormattingConfiguration)=>void configurator) {
		testRangeFormatting(null, configurator)
	}

	protected def testRangeFormatting((DocumentRangeFormattingParams)=>void paramsConfigurator, (RangeFormattingConfiguration)=>void configurator) {
		val extension configuration = new RangeFormattingConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)

		val fileInfo = initializeContext(configuration)

		val changes = languageServer.rangeFormatting(new DocumentRangeFormattingParams => [
			textDocument = new TextDocumentIdentifier(fileInfo.uri)
			range = configuration.range
			if (paramsConfigurator !== null) {
				paramsConfigurator.apply(it)
			}
		])
		val result = new Document(1, fileInfo.contents).applyChanges(<TextEdit>newArrayList(changes.get()).reverse)
		assertEqualsStricter(configuration.expectedText, result.contents)
	}
	
	/**
	 * @since 2.26
	 */
	protected def testFolding((FoldingConfiguration)=>void configurator) {
		val extension configuration = new FoldingConfiguration
		configuration.filePath = 'MyModel.' + fileExtension
		configurator.apply(configuration)
		
		val fileInfo = initializeContext(configuration)
		
		val foldings = languageServer.foldingRange(new FoldingRangeRequestParams => [
			textDocument = new TextDocumentIdentifier(fileInfo.uri)
		]).get()
		
		assertEqualsStricter(configuration.expectedFoldings, foldings.toExpectation);
	}

	override notify(String method, Object parameter) {
		this.notifications.add(method -> parameter)
	}

	override request(String method, Object parameter) {
		return CompletableFuture.completedFuture(null)
	}

	protected def Map<String, List<Diagnostic>> getDiagnostics() {
		languageServer.requestManager.runRead[
			val result = <String, List<Diagnostic>>newHashMap
			for (diagnostic : notifications.map[value].filter(PublishDiagnosticsParams)) {
				result.put(diagnostic.uri, diagnostic.diagnostics)
			}
			return result 
		].get
	}

}

