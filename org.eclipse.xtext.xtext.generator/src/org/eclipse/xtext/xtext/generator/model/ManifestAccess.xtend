/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.model

import com.google.inject.Injector
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Set
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.util.MergeableManifest2
import org.eclipse.xtext.util.Strings
import org.eclipse.xtext.xtext.generator.IGuiceAwareGeneratorComponent
import org.apache.log4j.Logger

/**
 * Configuration object for MANIFEST.MF files for use in Eclipse.
 */
@Accessors
class ManifestAccess extends TextFileAccess implements IGuiceAwareGeneratorComponent {
	
	static val Logger LOG = Logger.getLogger(ManifestAccess)
	
	String bundleName
	
	String symbolicName
	
	String version = '0.0.1.qualifier'
	
	boolean merge = true
	
	val Set<String> exportedPackages = newHashSet
	
	val Set<String> requiredBundles = newHashSet
	
	val Set<String> importedPackages = newHashSet
	
	TypeReference activator
	
	String lineDelimiter = Strings.newLine();
	
	new() {
		path = 'MANIFEST.MF'
	}
	
	/**
	 * Merge the contents of the given manifest into this one.
	 */
	def merge(ManifestAccess other) {
		if (this.path != other.path) {
			LOG.warn('Merging manifest files with different paths: ' + this.path + ', ' + other.path)
		}
		if (this.bundleName != other.bundleName) {
			if (this.bundleName === null)
				this.bundleName = other.bundleName
			else if (other.bundleName !== null)
				LOG.warn('Merging manifest files with different bundle names: ' + this.bundleName + ', ' + other.bundleName)
		}
		if (this.symbolicName != other.symbolicName) {
			if (this.symbolicName === null)
				this.symbolicName = other.symbolicName
			else if (other.symbolicName !== null)
				LOG.warn('Merging manifest files with different symbolic names: ' + this.symbolicName + ', ' + other.symbolicName)
		}
		if (this.activator != other.activator) {
			if (this.activator === null)
				this.activator = other.activator
			else if (other.activator !== null)
				LOG.warn('Merging manifest files with different activators: ' + this.activator + ', ' + other.activator)
		}
		if (this.version != other.version) {
			LOG.warn('Merging manifest files with different versions: ' + this.version + ', ' + other.version)
		}
		if (this.merge != other.merge) {
			LOG.warn('Merging manifest files with different merging settings.')
		}
		this.exportedPackages.addAll(other.exportedPackages)
		this.requiredBundles.addAll(other.requiredBundles)
		if (symbolicName !== null) {
			this.requiredBundles.remove(effectiveSymbolicName)
		}
		this.importedPackages.addAll(other.importedPackages)
	}
	
	def getEffectiveSymbolicName() {
		if (symbolicName === null) {
			return null
		}
		val idx = symbolicName.indexOf(';')
		if (idx < 0) {
			return symbolicName
		}
		return symbolicName.substring(0, idx)
	}
	
	override setContent(StringConcatenationClient content) {
		throw new UnsupportedOperationException("cannot directly set 'content' on a manifest.mf. Use the individual properties instead.")
	}
	
	override getContent() '''
		Manifest-Version: 1.0
		Bundle-ManifestVersion: 2
		Bundle-Name: «bundleName»
		Bundle-SymbolicName: «symbolicName ?: bundleName»;singleton:=true
		«IF !version.nullOrEmpty»
			Bundle-Version: «version»
		«ENDIF»
		Bundle-RequiredExecutionEnvironment: JavaSE-21
		Bundle-ActivationPolicy: lazy
		«IF !exportedPackages.empty»
			Export-Package: «FOR pack : exportedPackages.sort SEPARATOR ',\n '»«pack»«ENDFOR»
		«ENDIF»
		«IF !requiredBundles.empty»
			Require-Bundle: «FOR bundle : requiredBundles.sort.filter[it != effectiveSymbolicName] SEPARATOR ',\n '»«bundle»«ENDFOR»
		«ENDIF»
		«IF !importedPackages.empty»
			Import-Package: «FOR pack : importedPackages.sort SEPARATOR ',\n '»«pack»«ENDFOR»
		«ENDIF»
		«IF activator !== null»
			Bundle-Activator: «activator»
		«ENDIF»
		Automatic-Module-Name: «symbolicName ?: bundleName»
	'''
	
	override void writeTo(IFileSystemAccess2 fileSystemAccess) {
		if (fileSystemAccess !== null) {
			val contentToWrite = MergeableManifest2.make512Safe(new StringBuffer(content), lineDelimiter)
			// make sure all the constraints for the manifest are respected
			val mergeableManifest = new MergeableManifest2(new ByteArrayInputStream(contentToWrite.getBytes("UTF-8")))
			mergeableManifest.lineDelimiter = lineDelimiter
			var bout = new ByteArrayOutputStream()
			mergeableManifest.write(bout)
			fileSystemAccess.generateFile(path, new ByteArrayInputStream(bout.toByteArray))
		}
	}
	
	override initialize(Injector injector) {
		injector.injectMembers(this)
	}
	
}
