package org.eclipse.xtext.builder.standalone

import static extension org.eclipse.emf.common.util.URI.createFileURI
import com.google.common.io.Files
import com.google.inject.Inject
import com.google.inject.Provider
import java.io.File
import java.io.IOException
import java.net.URLClassLoader
import java.util.ArrayList
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.regex.Pattern
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.builder.standalone.compiler.IJavaCompiler
import org.eclipse.xtext.builder.standalone.compiler.IJavaCompiler.CompilationResult
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess
import org.eclipse.xtext.generator.AbstractFileSystemAccess
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.mwe.NameBasedFilter
import org.eclipse.xtext.mwe.PathTraverser
import org.eclipse.xtext.parser.IEncodingProvider
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode

class StandaloneBuilder {
	static final Logger LOG = Logger.getLogger(StandaloneBuilder);

	/**  Map key is a file extension provided by Language FileExtensionProvider   */
	@Property Map<String, LanguageAccess> languages
	@Property Iterable<String> sourceDirs
	@Property Iterable<String> classPathEntries
	@Property File tempDir = Files.createTempDir
	@Property String encoding
	@Property String classPathLookUpFilter
	@Property boolean failOnValidationError = true

	@Inject IndexedJvmTypeAccess jvmTypeAccess
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject AbstractFileSystemAccess commonFileAccess
	@Inject IIssueHandler issueHandler
	@Inject IEncodingProvider.Runtime encodingProvider
	@Inject IJavaCompiler compiler

	def void setTempDir(String pathAsString) {
		if (pathAsString != null) {
			_tempDir = new File(pathAsString)
		}
	}

	def launch() {
		val needsJava = languages.values.exists[linksAgainstJava]
		if (needsJava) {
			LOG.info("Using common types.")
		}

		val resourceSet = resourceSetProvider.get

		if (encoding != null) {
			LOG.debug("Setting encoding.")
			fileEncodingSetup(languages.values, encoding)
		}

		LOG.info("Collecting source models.")
		val startedAt = System.currentTimeMillis
		var rootsToTravers = classPathEntries
		if (classPathLookUpFilter != null) {
			LOG.info("Class path look up filter is active.")
			val cpLookUpFilter = Pattern.compile(classPathLookUpFilter)
			rootsToTravers = classPathEntries.filter[root|cpLookUpFilter.matcher(root).matches]
			LOG.info(
				"Investigating " + rootsToTravers.length + " of " + classPathEntries.length + " class path entries.");
		}
		collectResources((sourceDirs + rootsToTravers), resourceSet)
		LOG.debug("Finished collecting source models. Took: " + (System.currentTimeMillis - startedAt) + " ms.")

		val allClassPathEntries = (sourceDirs + classPathEntries)
		if (needsJava) {
			LOG.info("Installing type provider.")
			installTypeProvider(allClassPathEntries, resourceSet, null)
		}
		val index = fillIndex(resourceSet)
		val sourceResources = collectResources(sourceDirs, resourceSet)
		if (needsJava) {
			val stubsClasses = compileStubs(generateStubs(index, sourceResources))
			LOG.info("Installing type provider for stubs.")
			installTypeProvider(allClassPathEntries + newArrayList(stubsClasses), resourceSet, jvmTypeAccess)
		}
		sourceResources.forEach[contents] // full initialize
		sourceResources.forEach[EcoreUtil2.resolveLazyCrossReferences(it, CancelIndicator.NullImpl)]

		val isErrorFree = validate(sourceResources)
		if (failOnValidationError && !isErrorFree) {
			return isErrorFree
		}
		generate(sourceResources)
		return isErrorFree
	}

	def fileEncodingSetup(Collection<LanguageAccess> langs, String encoding) {
		for (lang : langs) {
			switch provider : lang.encodingProvider {
				IEncodingProvider.Runtime: {
					provider.setDefaultEncoding(encoding)
				}
				default: {
					LOG.debug(
						"Couldn't set encoding '" + encoding + "' for provider '" + provider +
							"'. Only subclasses of IEncodingProvider.Runtime are supported.")
				}
			}
		}
	}

	def protected fillIndex(XtextResourceSet set) {
		val descriptions = new ArrayList<Resource>(set.resources).map [
			languageAccess(it).resourceDescriptionManager.getResourceDescription(it)
		]
		val index = new ResourceDescriptionsData(descriptions)
		ResourceDescriptionsData.ResourceSetAdapter.installResourceDescriptionsData(set, index)
		return index
	}

	def protected compileStubs(File stubsDir) {
		val stubsClasses = createTempDir("classes")
		compiler.setClassPath(classPathEntries)
		LOG.info("Compiling stubs located in " + stubsDir.absolutePath)
		val result = compiler.compile(sourceDirs + newArrayList(stubsDir.absolutePath), stubsClasses)
		switch (result) {
			case CompilationResult.SKIPPED:
				LOG.info("Nothing to compile. Stubs compilation was skipped.")
			case CompilationResult.FAILED:
				LOG.debug("Stubs compilation finished with errors.")
		}
		return stubsClasses.absolutePath
	}

	def protected generateStubs(ResourceDescriptionsData data, List<? extends Resource> resources) {
		val stubsDir = createTempDir("stubs")
		LOG.info("Generating stubs into " + stubsDir.absolutePath)
		if (encoding != null)
			encodingProvider.setDefaultEncoding(encoding)
		commonFileAccess.setOutputPath(IFileSystemAccess.DEFAULT_OUTPUT, stubsDir.absolutePath)
		val generateStubs = resources.filter[languageAccess.linksAgainstJava]
		generateStubs.forEach [
			languageAccess.stubGenerator.doGenerateStubs(commonFileAccess, data.getResourceDescription(URI))
		]
		return stubsDir
	}

	def protected validate(List<Resource> sourceResources) {
		val allIssues = newArrayList()
		for (Resource resource : sourceResources) {
			val resourceValidator = languageAccess(resource).getResourceValidator();
			val validationResult = resourceValidator.validate(resource, CheckMode.ALL, null);
			allIssues.addAll(validationResult)
		}
		return issueHandler.handleIssue(allIssues)
	}

	def protected generate(List<Resource> sourceResources) {
		for (Resource it : sourceResources) {
			LOG.info("Starting generator for input: '" + getURI().lastSegment() + "'");
			registerCurrentSource(it)
			languageAccess.generator.doGenerate(it, languageAccess.fileSystemAccess);
		}
	}
	
	def protected registerCurrentSource(Resource resource) {
		val fsa = resource.languageAccess.fileSystemAccess
		val absoluteSource = sourceDirs
			.map[new File(it).absolutePath.createFileURI.toString]
			.filter[resource.URI.toString.startsWith(it)]
			.reduce[longest, current| if(current.length > longest.length) current else longest]
			?.createFileURI
		if (absoluteSource == null) {
			throw new IllegalStateException(
				'''Resource �resource.URI� is not contained in any of the known source folders �sourceDirs�.''')
		}
		for (output : fsa.outputConfigurations.values) {
			for (relativeSource : output.sourceFolders) {
				if (absoluteSource.toString.endsWith(relativeSource)) {
					fsa.currentSource = relativeSource
				}
			}
		}
	}
	
	def private languageAccess(Resource resource) {
		languages.get(resource.URI.fileExtension)
	}

	def protected createTempDir(String subDir) {
		val file = new File(tempDir, subDir)
		if(!file.mkdirs && !file.exists) throw new IOException("Failed to create directory '" + file.absolutePath + "'")
		return file
	}

	def protected void installTypeProvider(Iterable<String> classPathRoots, XtextResourceSet resSet,
		IndexedJvmTypeAccess typeAccess) {
		val classLoader = createURLClassLoader(classPathRoots)
		new ClasspathTypeProvider(classLoader, resSet, typeAccess)
		resSet.setClasspathURIContext(classLoader);
	}

	def private URLClassLoader createURLClassLoader(Iterable<String> classPathEntries) {
		val classPathUrls = classPathEntries.map[str|new File(str).toURI().toURL()]
		return new URLClassLoader(classPathUrls)
	}

	def protected List<Resource> collectResources(Iterable<String> roots, ResourceSet resourceSet) {
		val extensions = languages.keySet.join("|")
		val nameBasedFilter = new NameBasedFilter

		//TODO test with whitespaced file extensions
		nameBasedFilter.setRegularExpression(".*\\.(?:(" + extensions + "))$");
		val List<Resource> resources = newArrayList();

		new PathTraverser().resolvePathes(
			roots.toList,
			[ input |
				val matches = nameBasedFilter.matches(input)
				if (matches) {
					LOG.debug("Adding file '" + input + "'");
					resources.add(resourceSet.getResource(input, true));
				}
				return matches
			]
		)
		return resources;
	}

	def getCompiler() {
		compiler
	}
}
