/** 
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.idea.build

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.google.inject.Provider
import com.intellij.ProjectTopics
import com.intellij.compiler.ModuleCompilerUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentAdapter
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootAdapter
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileAdapter
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileMoveEvent
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.util.Alarm
import com.intellij.util.graph.Graph
import com.intellij.util.messages.MessageBusConnection
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.builder.standalone.incremental.BuildRequest
import org.eclipse.xtext.builder.standalone.incremental.IncrementalBuilder
import org.eclipse.xtext.builder.standalone.incremental.IndexState
import org.eclipse.xtext.builder.standalone.incremental.TypeResourceDescription.ChangedDelta
import org.eclipse.xtext.idea.resource.IdeaResourceSetProvider
import org.eclipse.xtext.idea.resource.IdeaResourceSetProvider.VirtualFileBasedUriHandler
import org.eclipse.xtext.idea.shared.IdeaSharedInjectorProvider
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.IResourceDescription.Delta
import org.eclipse.xtext.resource.IResourceDescription.Event
import org.eclipse.xtext.resource.IResourceDescription.Event.Listener
import org.eclipse.xtext.resource.IResourceDescriptions
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.util.internal.Log

import static org.eclipse.xtext.idea.build.BuildEvent.Type.*
import static org.eclipse.xtext.idea.build.XtextAutoBuilderComponent.*

import static extension org.eclipse.xtext.idea.resource.VirtualFileURIUtil.*

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
@Log class XtextAutoBuilderComponent extends AbstractProjectComponent implements IResourceDescription.Event.Source, Disposable {
	
	static interface AutoBuilderListener {
		def void aboutToBuild(List<BuildEvent> events)
		def void finishedBuild()
	}
	
	boolean disposed
		
	BlockingQueue<BuildEvent> queue = new LinkedBlockingQueue<BuildEvent>()

	Alarm alarm 

	Project project
	
	@Inject Provider<IncrementalBuilder> builderProvider	
	
	@Inject Provider<BuildProgressReporter> buildProgressReporterProvider
	 
	@Inject IdeaResourceSetProvider resourceSetProvider
	
	@Inject IResourceServiceProvider.Registry resourceServiceProviderRegistry
	
	@Inject IQualifiedNameConverter qualifiedNameConverter
	
	IndexState indexState
	
	List<Listener> resourceDeltaListeners = newArrayList()
	List<AutoBuilderListener> autoBuildListeners = newArrayList()
	
	new(Project project) {
		super(project)
		TEST_MODE = ApplicationManager.application.isUnitTestMode
		IdeaSharedInjectorProvider.injector.injectMembers(this)
		this.project = project
		alarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD, this)
		disposed = false
		Disposer.register(project, this)
	
		EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentAdapter() {
			override void documentChanged(DocumentEvent event) {
				var file = FileDocumentManager.getInstance().getFile(event.getDocument())
				if (file != null) {
					fileModified(file)
				} else {
					LOG.info("No virtual file for document. Contents was "+event.document)
				}
			}
		}, project)
		
		VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
			override void contentsChanged(VirtualFileEvent event) {
				fileModified(event.getFile())
			}

			override void fileCreated(VirtualFileEvent event) {
				fileAdded(event.getFile())
			}

			override void fileDeleted(VirtualFileEvent event) {
				fileDeleted(event.getFile())
			}
			
			override void fileMoved(VirtualFileMoveEvent event) {
				// TODO deal with that!
			}
		}, project)
		
		val MessageBusConnection connection = project.getMessageBus().connect(project);
         connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
										
			override rootsChanged(ModuleRootEvent event) {
				doCleanBuild
			}
         	
         });
		
		alarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD, project)
	}
	
	override dispose() {
		alarm.cancelAllRequests
		queue.clear
		indexState = null
		disposed = true
	}
	
	protected def getProject() {
		return myProject
	}
	
	override addListener(Listener listener) {
		resourceDeltaListeners.add(listener)
	}
	
	override notifyListeners(Event event) {
		for (listener : resourceDeltaListeners) {
			listener.descriptionsChanged(event)
		}
	}
	
	override removeListener(Listener listener) {
		resourceDeltaListeners.remove(resourceDeltaListeners)
	}
	
	def void addAutoBuilderListener(AutoBuilderListener listener) {
		autoBuildListeners.add(listener)
	}
	
	def void removeAutoBuilderListener(AutoBuilderListener listener) {
		autoBuildListeners.remove(listener)
	}
	
	def void fileModified(VirtualFile file) {
		enqueue(file, MODIFIED)
	}

	def void fileDeleted(VirtualFile file) {
		enqueue(file, DELETED)
	}

	def void fileAdded(VirtualFile file) {
		if (!file.directory && file.length > 0) {
			enqueue(file, ADDED)
		} else {
			if (LOG.infoEnabled)
				LOG.info("Ignoring new empty file "+file.path+". Waiting for content.")
		}
	}
	
	/**
	 * For testing purposes! When set to <code>true</code>, the builds are not running asynchronously and delayed, but directly when the event comes in
	 */
	public static boolean TEST_MODE = false

	protected def enqueue(VirtualFile file, BuildEvent.Type type) {
		if (isExcluded(file)) {
			return;
		}
		if (!disposed && !isLoaded()) {
			queueAllResources()
		}
		if (LOG.isInfoEnabled) {
			LOG.info("Queuing "+type+" - "+file.URI+".")
		}
		if (file != null && !disposed) {
			queue.put(new BuildEvent(file, type))
			doRunBuild()
		}
	}
	
	protected def doCleanBuild() {
		indexState = null
		queueAllResources
		doRunBuild
	}
	
	protected def doRunBuild() {
		if (TEST_MODE) {
			(PsiManager.getInstance(getProject()).getModificationTracker() as PsiModificationTrackerImpl).incCounter();
			build
		} else {
			alarm.cancelAllRequests
			alarm.addRequest([build], 200)
		}
	}
	
	protected def boolean isExcluded(VirtualFile file) {
		if (ignoreIncomingEvents) {
			if (LOG.isDebugEnabled) 
				LOG.debug("Ignoring transitive file change "+file.path)
			return true;
		}
		return file == null 
			|| file.isDirectory 
	}
	
	protected def boolean isLoaded() {
		if (indexState != null || !queue.isEmpty)
			return true;
		return false;
	}
	
	protected def queueAllResources() {
		val baseFile = project.baseDir
		baseFile.visitFileTree[ file |
			if (!file.isDirectory && file.exists) {
				queue.put(new BuildEvent(file, BuildEvent.Type.ADDED))
			}
		]
	}
	
	def void visitFileTree(VirtualFile file, (VirtualFile)=>void handler) {
		if (file.isDirectory) {
			for (child : file.children) {
				visitFileTree(child, handler)
			}
		}
		handler.apply(file)
	}
	
	private volatile boolean ignoreIncomingEvents = false
	
	protected def void build() {
		if (disposed) {
			return
		}
		val allEvents = newArrayList
		queue.drainTo(allEvents)
		internalBuild(allEvents)
	}

	protected def void internalBuild(List<BuildEvent> allEvents) {
		val app = ApplicationManager.application
		val moduleManager = ModuleManager.getInstance(getProject)
		val buildProgressReporter = buildProgressReporterProvider.get 
		buildProgressReporter.project = project
		try {
			autoBuildListeners.forEach[
				aboutToBuild(allEvents)
			]
			val fileIndex = ProjectFileIndex.SERVICE.getInstance(project)
			val moduleGraph = app.<Graph<Module>>runReadAction[moduleManager.moduleGraph]
			// deltas are added over the whole build
			val deltas = <IResourceDescription.Delta>newArrayList
			val sortedModules = new ArrayList(moduleGraph.nodes)
			ModuleCompilerUtil.sortModules(project, sortedModules)
			for (module: sortedModules) {
				val changedUris = newHashSet
				val deletedUris = newHashSet
				val contentRoots = ModuleRootManager.getInstance(module).contentRoots
				val events = allEvents.filter[event| event.findModule(fileIndex) == module].toSet
				if (contentRoots.empty 
					|| events.isEmpty && deltas.isEmpty) {
					LOG.info("Skipping module '"+module.name+"'. Nothing to do here.")		
				} else {
					collectChanges(events, module, changedUris, deletedUris, deltas)
					
					val entries = OrderEnumerator.orderEntries(module)
					val request = new BuildRequest => [
						resourceSet = resourceSetProvider.get(module)
						dirtyFiles += changedUris
						deletedFiles += deletedUris
						externalDeltas += deltas
						classPath += entries.withoutSdk.classes.pathsList.virtualFiles.filter[/* HACK! we need to properly exclude the out put dir */!isDirectory].map[URI]
						baseDir = contentRoots.head.URI
						// outputs = ??
						previousState = indexState ?: new IndexState()
	
						afterValidate = buildProgressReporter
						afterDeleteFile = [
							buildProgressReporter.markAsAffected(it)
						]
						belongsToThisBuildRun = [ uri |
							val file = uri.virtualFile
							val thisModule = file.findModule(fileIndex)
							return module == thisModule
						]
					]
					val result = app.<IncrementalBuilder.Result>runReadAction [
						builderProvider.get().build(request, resourceServiceProviderRegistry)
					]
					app.invokeAndWait([
						app.runWriteAction [
							try {
								ignoreIncomingEvents = true
								val handler = VirtualFileBasedUriHandler.find(request.resourceSet)
								handler.flushToDisk
							} finally {
								ignoreIncomingEvents = false
							}
						]
					], ModalityState.any)
					indexState = result.indexState
					notifyListeners [
						ImmutableList.copyOf(result.affectedResources)
					]
					deltas.addAll(result.affectedResources)
				}
			}
		} catch(ProcessCanceledException exc) {
			queue.addAll(allEvents)
		} finally {
			buildProgressReporter.clearProgress
			autoBuildListeners.forEach[
				finishedBuild()
			]
		}
	}
	
	protected def collectChanges(Set<BuildEvent> events, Module module, HashSet<URI> changedUris, HashSet<URI> deletedUris, ArrayList<Delta> deltas) {
		val app = ApplicationManager.application
		for (event : events) {
			switch event.type {
				case MODIFIED,
				case ADDED: {
					val uri = event.file.URI
					val sourceUris = indexState?.fileMappings?.getSource(uri)
					if (sourceUris != null && !sourceUris.isEmpty) {
						for (sourceUri : sourceUris) {
							changedUris += sourceUri
						}									
					} else if (isJavaFile(event.file)) {
						deltas += app.<Set<IResourceDescription.Delta>>runReadAction [
							return getJavaDeltas(event.file, module)
						]
					} else {
						changedUris += uri
					}
				}
				case DELETED : {
					val uri = event.file.URI
					val sourceUris = indexState?.fileMappings?.getSource(uri)
					if (sourceUris != null && !sourceUris.isEmpty) {
						for (sourceUri : sourceUris) {
							changedUris += sourceUri
						}									
					} else if (isJavaFile(event.file)) {
						deltas += app.<Set<IResourceDescription.Delta>>runReadAction [
							getJavaDeltas(event.file, module)
						]
					} else {
						deletedUris += uri
					}
				}
			}
		}
	}
	
	def boolean isJavaFile(VirtualFile file) {
		file.extension == 'java'
	}
	
	def Set<IResourceDescription.Delta> getJavaDeltas(VirtualFile file, Module module) {
		if (!file.isValid) {
			return emptySet
		}
		val psiFile = PsiManager.getInstance(module.project).findFile(file)
		val result = <IResourceDescription.Delta>newLinkedHashSet
		if (psiFile instanceof PsiJavaFile) {
			for (clazz : psiFile.classes) {
				result += new ChangedDelta(qualifiedNameConverter.toQualifiedName(clazz.qualifiedName)) 
			}
		}
		return result
	}
	
	protected def getIndexState() {
		if (indexState == null) {
			if (!isLoaded()) {
				queueAllResources
				if (TEST_MODE) {
					build
				} else {
					alarm.cancelAllRequests
					alarm.addRequest([build], 200)
				}
			}
			return new IndexState()
		}
		return indexState		
	}
	
	public def IResourceDescriptions getResourceDescriptions() {
		getIndexState.resourceDescriptions
	}

	protected def findModule(BuildEvent it, ProjectFileIndex fileIndex) {
		if (type == DELETED)
			file.findModule(fileIndex)
		else
			fileIndex.getModuleForFile(file, true)
	}
	
	protected def Module findModule(VirtualFile file, ProjectFileIndex fileIndex) {
		if (file == null) {
			return null
		}
		val module = fileIndex.getModuleForFile(file, true)
		if (module != null)
			return module
		return file.parent.findModule(fileIndex)
	}
	
	override String getComponentName() {
		return "Xtext Compiler Component"
	}
	
}
