/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.core.macro;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtend.lib.macro.ValidationContext;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.Element;
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableEnumerationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.file.FileLocations;
import org.eclipse.xtend.lib.macro.file.FileSystemSupport;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtend.lib.macro.services.AnnotationReferenceBuildContext;
import org.eclipse.xtend.lib.macro.services.AnnotationReferenceProvider;
import org.eclipse.xtend.lib.macro.services.Problem;
import org.eclipse.xtend.lib.macro.services.ProblemSupport;
import org.eclipse.xtend.lib.macro.services.Tracability;
import org.eclipse.xtend.lib.macro.services.TypeLookup;
import org.eclipse.xtend.lib.macro.services.TypeReferenceProvider;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class ValidationContextImpl implements ValidationContext {
  @Accessors
  private CompilationUnitImpl unit;

  @Delegate
  public ProblemSupport getProblemSupport() {
    return this.unit.getProblemSupport();
  }

  @Delegate
  public TypeLookup getTypeLookup() {
    return this.unit.getTypeLookup();
  }

  @Delegate
  public TypeReferenceProvider getTypeReferenceProvider() {
    return this.unit.getTypeReferenceProvider();
  }

  @Delegate
  public AnnotationReferenceProvider getAnnotationReferenceProvider() {
    return this.unit.getAnnotationReferenceProvider();
  }

  @Delegate
  public FileSystemSupport getFileSystemSupport() {
    return this.unit.getFileSystemSupport();
  }

  @Delegate
  public FileLocations getFileLocations() {
    return this.unit.getFileLocations();
  }

  @Delegate
  public Tracability getTracability() {
    return this.unit.getTracability();
  }

  @Pure
  public CompilationUnitImpl getUnit() {
    return this.unit;
  }

  public void setUnit(final CompilationUnitImpl unit) {
    this.unit = unit;
  }

  public void addError(final Element element, final String message) {
    this.getProblemSupport().addError(element, message);
  }

  public void addWarning(final Element element, final String message) {
    this.getProblemSupport().addWarning(element, message);
  }

  public List<? extends Problem> getProblems(final Element element) {
    return this.getProblemSupport().getProblems(element);
  }

  public void validateLater(final Procedure0 validationCallback) {
    this.getProblemSupport().validateLater(validationCallback);
  }

  public MutableAnnotationTypeDeclaration findAnnotationType(final String qualifiedName) {
    return this.getTypeLookup().findAnnotationType(qualifiedName);
  }

  public MutableClassDeclaration findClass(final String qualifiedName) {
    return this.getTypeLookup().findClass(qualifiedName);
  }

  public MutableEnumerationTypeDeclaration findEnumerationType(final String qualifiedName) {
    return this.getTypeLookup().findEnumerationType(qualifiedName);
  }

  public MutableInterfaceDeclaration findInterface(final String qualifiedName) {
    return this.getTypeLookup().findInterface(qualifiedName);
  }

  public Type findTypeGlobally(final Class<?> clazz) {
    return this.getTypeLookup().findTypeGlobally(clazz);
  }

  public Type findTypeGlobally(final String typeName) {
    return this.getTypeLookup().findTypeGlobally(typeName);
  }

  public TypeReference getAnyType() {
    return this.getTypeReferenceProvider().getAnyType();
  }

  public TypeReference getList(final TypeReference param) {
    return this.getTypeReferenceProvider().getList(param);
  }

  public TypeReference getObject() {
    return this.getTypeReferenceProvider().getObject();
  }

  public TypeReference getPrimitiveBoolean() {
    return this.getTypeReferenceProvider().getPrimitiveBoolean();
  }

  public TypeReference getPrimitiveByte() {
    return this.getTypeReferenceProvider().getPrimitiveByte();
  }

  public TypeReference getPrimitiveChar() {
    return this.getTypeReferenceProvider().getPrimitiveChar();
  }

  public TypeReference getPrimitiveDouble() {
    return this.getTypeReferenceProvider().getPrimitiveDouble();
  }

  public TypeReference getPrimitiveFloat() {
    return this.getTypeReferenceProvider().getPrimitiveFloat();
  }

  public TypeReference getPrimitiveInt() {
    return this.getTypeReferenceProvider().getPrimitiveInt();
  }

  public TypeReference getPrimitiveLong() {
    return this.getTypeReferenceProvider().getPrimitiveLong();
  }

  public TypeReference getPrimitiveShort() {
    return this.getTypeReferenceProvider().getPrimitiveShort();
  }

  public TypeReference getPrimitiveVoid() {
    return this.getTypeReferenceProvider().getPrimitiveVoid();
  }

  public TypeReference getSet(final TypeReference param) {
    return this.getTypeReferenceProvider().getSet(param);
  }

  public TypeReference getString() {
    return this.getTypeReferenceProvider().getString();
  }

  public TypeReference newArrayTypeReference(final TypeReference componentType) {
    return this.getTypeReferenceProvider().newArrayTypeReference(componentType);
  }

  public TypeReference newSelfTypeReference(final Type typeDeclaration) {
    return this.getTypeReferenceProvider().newSelfTypeReference(typeDeclaration);
  }

  public TypeReference newTypeReference(final Class<?> clazz, final TypeReference... typeArguments) {
    return this.getTypeReferenceProvider().newTypeReference(clazz, typeArguments);
  }

  public TypeReference newTypeReference(final String typeName, final TypeReference... typeArguments) {
    return this.getTypeReferenceProvider().newTypeReference(typeName, typeArguments);
  }

  public TypeReference newTypeReference(final Type typeDeclaration, final TypeReference... typeArguments) {
    return this.getTypeReferenceProvider().newTypeReference(typeDeclaration, typeArguments);
  }

  public TypeReference newWildcardTypeReference() {
    return this.getTypeReferenceProvider().newWildcardTypeReference();
  }

  public TypeReference newWildcardTypeReference(final TypeReference upperBound) {
    return this.getTypeReferenceProvider().newWildcardTypeReference(upperBound);
  }

  public TypeReference newWildcardTypeReferenceWithLowerBound(final TypeReference lowerBound) {
    return this.getTypeReferenceProvider().newWildcardTypeReferenceWithLowerBound(lowerBound);
  }

  public AnnotationReference newAnnotationReference(final AnnotationReference annotationReference) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationReference);
  }

  public AnnotationReference newAnnotationReference(final AnnotationReference annotationReference, final Procedure1<AnnotationReferenceBuildContext> initializer) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationReference, initializer);
  }

  public AnnotationReference newAnnotationReference(final Class<?> annotationClass) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationClass);
  }

  public AnnotationReference newAnnotationReference(final Class<?> annotationClass, final Procedure1<AnnotationReferenceBuildContext> initializer) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationClass, initializer);
  }

  public AnnotationReference newAnnotationReference(final String annotationTypeName) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationTypeName);
  }

  public AnnotationReference newAnnotationReference(final String annotationTypeName, final Procedure1<AnnotationReferenceBuildContext> initializer) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationTypeName, initializer);
  }

  public AnnotationReference newAnnotationReference(final Type annotationTypeDelcaration) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationTypeDelcaration);
  }

  public AnnotationReference newAnnotationReference(final Type annotationTypeDelcaration, final Procedure1<AnnotationReferenceBuildContext> initializer) {
    return this.getAnnotationReferenceProvider().newAnnotationReference(annotationTypeDelcaration, initializer);
  }

  public boolean exists(final Path path) {
    return this.getFileSystemSupport().exists(path);
  }

  public String getCharset(final Path path) {
    return this.getFileSystemSupport().getCharset(path);
  }

  public Iterable<? extends Path> getChildren(final Path path) {
    return this.getFileSystemSupport().getChildren(path);
  }

  public CharSequence getContents(final Path path) {
    return this.getFileSystemSupport().getContents(path);
  }

  public InputStream getContentsAsStream(final Path path) {
    return this.getFileSystemSupport().getContentsAsStream(path);
  }

  public long getLastModification(final Path path) {
    return this.getFileSystemSupport().getLastModification(path);
  }

  public boolean isFile(final Path path) {
    return this.getFileSystemSupport().isFile(path);
  }

  public boolean isFolder(final Path path) {
    return this.getFileSystemSupport().isFolder(path);
  }

  public URI toURI(final Path path) {
    return this.getFileSystemSupport().toURI(path);
  }

  public Path getProjectFolder(final Path path) {
    return this.getFileLocations().getProjectFolder(path);
  }

  public Set<Path> getProjectSourceFolders(final Path path) {
    return this.getFileLocations().getProjectSourceFolders(path);
  }

  public Path getSourceFolder(final Path path) {
    return this.getFileLocations().getSourceFolder(path);
  }

  public Path getTargetFolder(final Path sourceFolder) {
    return this.getFileLocations().getTargetFolder(sourceFolder);
  }

  public Element getPrimaryGeneratedJavaElement(final Element source) {
    return this.getTracability().getPrimaryGeneratedJavaElement(source);
  }

  public Element getPrimarySourceElement(final Element target) {
    return this.getTracability().getPrimarySourceElement(target);
  }

  public boolean isExternal(final Element element) {
    return this.getTracability().isExternal(element);
  }

  public boolean isGenerated(final Element element) {
    return this.getTracability().isGenerated(element);
  }

  public boolean isSource(final Element element) {
    return this.getTracability().isSource(element);
  }

  public boolean isThePrimaryGeneratedJavaElement(final Element target) {
    return this.getTracability().isThePrimaryGeneratedJavaElement(target);
  }
}
