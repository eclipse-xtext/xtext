/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.typesystem.override;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypesSwitch;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.typesystem.override.IOverrideCheckResult.OverrideCheckDetails;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Lorenzo Bettini - https://bugs.eclipse.org/bugs/show_bug.cgi?id=454786
 */
public class ResolvedFeatures extends AbstractResolvedFeatures {

	private List<IResolvedField> declaredFields;
	private List<IResolvedConstructor> declaredConstructors;
	private List<IResolvedOperation> allOperations;
	private List<IResolvedOperation> declaredOperations;
	private ListMultimap<String, IResolvedOperation> allOperationsPerErasure;
	private ListMultimap<String, IResolvedOperation> declaredOperationsPerErasure;

	/**
	 * @deprecated the {@code targetVersion} parameter is unused - every supported {@link JavaVersion} is
	 *             at least Java 8, use {@link #ResolvedFeatures(LightweightTypeReference, OverrideTester)} instead
	 */
	@Deprecated
	public ResolvedFeatures(LightweightTypeReference type, OverrideTester overrideTester, JavaVersion targetVersion) {
		super(type, overrideTester);
	}

	public ResolvedFeatures(LightweightTypeReference type, OverrideTester overrideTester) {
		super(type, overrideTester);
	}
	
	public ResolvedFeatures(LightweightTypeReference type) {
		this(type, new OverrideTester());
	}
	
	public List<IResolvedOperation> getAllOperations() {
		if (allOperations != null) {
			return allOperations;
		}
		return allOperations = computeAllOperations();
	}
	
	public IResolvedOperation getResolvedOperation(JvmOperation operation) {
		return createResolvedOperation(operation);
	}
	
	public List<IResolvedField> getDeclaredFields() {
		if (declaredFields != null) {
			return declaredFields;
		}
		return declaredFields = computeDeclaredFields();
	}
	
	public List<IResolvedConstructor> getDeclaredConstructors() {
		if (declaredConstructors != null) {
			return declaredConstructors;
		}
		return declaredConstructors = computeDeclaredConstructors();
	}
	
	public List<IResolvedOperation> getDeclaredOperations() {
		if (declaredOperations != null) {
			return declaredOperations;
		}
		return declaredOperations = computeDeclaredOperations();
	}
	
	public List<IResolvedOperation> getDeclaredOperations(String erasedSignature) {
		if (declaredOperationsPerErasure != null) {
			return declaredOperationsPerErasure.get(erasedSignature);
		}
		return (declaredOperationsPerErasure = computeIndex(getDeclaredOperations())).get(erasedSignature);
	}
	
	public List<IResolvedOperation> getAllOperations(String erasedSignature) {
		if (allOperationsPerErasure != null) {
			return allOperationsPerErasure.get(erasedSignature);
		}
		return (allOperationsPerErasure = computeIndex(getAllOperations())).get(erasedSignature);
	}
	
	protected ListMultimap<String, IResolvedOperation> computeIndex(List<IResolvedOperation> operations) {
		// produces an immutable index which is what we want to have
		return Multimaps.index(operations, new Function<IResolvedOperation, String>() {
			@Override
			public String apply(IResolvedOperation input) {
				return input.getResolvedErasureSignature();
			}
		});
	}

	protected List<IResolvedOperation> computeAllOperations() {
		JvmType rawType = getRawType();
		if (!(rawType instanceof JvmDeclaredType)) {
			return Collections.emptyList();
		}
		Multimap<String, AbstractResolvedOperation> processedOperations = LinkedHashMultimap.create();
		for (IResolvedOperation resolvedOperation : getDeclaredOperations()) {
			processedOperations.put(resolvedOperation.getDeclaration().getSimpleName(), (AbstractResolvedOperation) resolvedOperation);
		}
		computeAllOperationsFromSortedSuperTypes((JvmDeclaredType) rawType, processedOperations);
		// make sure the declared operations are the first in the list
		List<IResolvedOperation> result = new ArrayList<IResolvedOperation>(processedOperations.size());
		result.addAll(getDeclaredOperations());
		for (AbstractResolvedOperation operation : processedOperations.values()) {
			if (operation.getDeclaration().getDeclaringType() != rawType) {
				result.add(operation);
			}
		}
		return Collections.unmodifiableList(result);
	}

	protected void computeAllOperations(JvmDeclaredType type, Multimap<String, AbstractResolvedOperation> processedOperations) {
		for (JvmOperation operation: type.getDeclaredOperations()) {
			boolean addToResult = handleOverridesAndConflicts(operation, processedOperations);
			if (addToResult) {
				BottomResolvedOperation resolvedOperation = createResolvedOperation(operation);
				processedOperations.put(operation.getSimpleName(), resolvedOperation);
			}
		}
	}

	protected void computeAllOperationsFromSortedSuperTypes(JvmDeclaredType rootType,
			final Multimap<String, AbstractResolvedOperation> processedOperations) {
		class SuperTypes extends TypesSwitch<Boolean> {

			private Multiset<JvmType> interfaces = LinkedHashMultiset.create();
			private Set<JvmType> notInterfaces = Sets.newLinkedHashSet();
			
			public SuperTypes(JvmDeclaredType rootType) {
				doSwitch(rootType);
			}

			@Override
			public Boolean doSwitch(EObject theEObject) {
				if (theEObject == null)
					return Boolean.FALSE;
				return super.doSwitch(theEObject);
			}
			
			@Override
			public Boolean caseJvmTypeReference(JvmTypeReference object) {
				return doSwitch(object.getType());
			}
			
			@Override
			public Boolean caseJvmType(JvmType object) {
				return notInterfaces.add(object);
			}
			
			@Override
			public Boolean caseJvmDeclaredType(JvmDeclaredType object) {
				if (notInterfaces.add(object)) {
					for (JvmTypeReference superType : object.getSuperTypes()) {
						doSwitch(superType);
					}
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
			
			@Override
			public Boolean caseJvmGenericType(JvmGenericType object) {
				boolean traverseSuperTypes = false;
				if (object.isInterface()) {
					traverseSuperTypes = interfaces.add(object, 1) == 0;
				} else {
					traverseSuperTypes = notInterfaces.add(object);
				}
				if (traverseSuperTypes) {
					for (JvmTypeReference superType : object.getSuperTypes()) {
						doSwitch(superType);
					}
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
			
			public Collection<JvmType> getSuperTypesNoInterfaces() {
				return notInterfaces;
			}
			
			public int consumeInterfaceOccurrence(JvmGenericType intf) {
				return interfaces.remove(intf, 1);
			}
			
		}
		final SuperTypes superTypes = new SuperTypes(rootType);
		for(JvmType superClass: superTypes.getSuperTypesNoInterfaces()) {
			if (superClass instanceof JvmDeclaredType) {
				computeAllOperations((JvmDeclaredType) superClass, processedOperations);
			}
		}
		
		class SuperInterfaceConsumer extends TypesSwitch<Boolean> {

			private Set<JvmType> seen = Sets.newHashSet();
			
			@Override
			public Boolean doSwitch(EObject theEObject) {
				if (theEObject == null)
					return Boolean.FALSE;
				return super.doSwitch(theEObject);
			}
			
			@Override
			public Boolean defaultCase(EObject object) {
				return Boolean.FALSE;
			}
			
			@Override
			public Boolean caseJvmTypeReference(JvmTypeReference object) {
				return doSwitch(object.getType());
			}
			
			@Override
			public Boolean caseJvmDeclaredType(JvmDeclaredType object) {
				if (seen.add(object)) {
					for (JvmTypeReference superType : object.getSuperTypes()) {
						doSwitch(superType);
					}
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
			
			@Override
			public Boolean caseJvmGenericType(JvmGenericType object) {
				if (object.isInterface()) {
					int was = superTypes.consumeInterfaceOccurrence(object);
					if (was == 0) {
						return Boolean.FALSE;
					}
					if (was == 1) {
						computeAllOperations(object, processedOperations);
					}
					for (JvmTypeReference superType : object.getSuperTypes()) {
						doSwitch(superType);
					}
					return was > 1;
				} else if (seen.add(object)) {
					for (JvmTypeReference superType : object.getSuperTypes()) {
						doSwitch(superType);
					}
					return Boolean.TRUE;
				}
				return Boolean.FALSE;
			}
			
			public void consume(JvmType rootType) {
				doSwitch(rootType);
			}
			
		}
		new SuperInterfaceConsumer().consume(rootType);
	}
	
	protected List<IResolvedOperation> computeDeclaredOperations() {
		JvmType rawType = getRawType();
		if (!(rawType instanceof JvmDeclaredType)) {
			return Collections.emptyList();
		}
		List<IResolvedOperation> result = Lists.newArrayList();
		Iterable<JvmOperation> operations = ((JvmDeclaredType)rawType).getDeclaredOperations();
		for(JvmOperation operation: operations) {
			BottomResolvedOperation resolvedOperation = createResolvedOperation(operation);
			result.add(resolvedOperation);
		}
		return Collections.unmodifiableList(result);
	}
	
	protected List<IResolvedField> computeDeclaredFields() {
		JvmType rawType = getRawType();
		if (!(rawType instanceof JvmGenericType)) {
			return Collections.emptyList();
		}
		List<IResolvedField> result = Lists.newArrayList();
		for(JvmField field: ((JvmGenericType)rawType).getDeclaredFields()) {
			result.add(new ResolvedField(field, getType()));
		}
		return Collections.unmodifiableList(result);
	}
	
	protected List<IResolvedConstructor> computeDeclaredConstructors() {
		JvmType rawType = getRawType();
		if (!(rawType instanceof JvmGenericType)) {
			return Collections.emptyList();
		}
		List<IResolvedConstructor> result = Lists.newArrayList();
		for(JvmConstructor constructor: ((JvmGenericType)rawType).getDeclaredConstructors()) {
			result.add(new ResolvedConstructor(constructor, getType()));
		}
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * When the inherited operations are computed for Java 8, we have to check for conflicting default interface method implementations.
	 */
	private boolean handleOverridesAndConflicts(JvmOperation operation, Multimap<String, AbstractResolvedOperation> processedOperations) {
		String simpleName = operation.getSimpleName();
		if (!processedOperations.containsKey(simpleName)) {
			return true;
		}
		List<AbstractResolvedOperation> conflictingOperations = null;
		for (AbstractResolvedOperation candidate : processedOperations.get(simpleName)) {
			OverrideTester overrideTester = candidate.getOverrideTester();
			IOverrideCheckResult checkResult = overrideTester.isSubsignature(candidate, operation, false);
			if (checkResult.getDetails().contains(OverrideCheckDetails.DEFAULT_IMPL_CONFLICT)) {
				// The current operation conflicts with the candidate
				if (conflictingOperations == null)
					conflictingOperations = Lists.newLinkedList();
				conflictingOperations.add(candidate);
			} else if (checkResult.isOverridingOrImplementing()) {
				return false;
			}
		}
		if (conflictingOperations != null) {
			if (conflictingOperations.size() == 1 && conflictingOperations.get(0) instanceof ConflictingDefaultOperation) {
				// The current operation contributes to the already existing conflict
				ConflictingDefaultOperation conflictingDefaultOperation = (ConflictingDefaultOperation) conflictingOperations.get(0);
				boolean isOverridden = false;
				for (IResolvedOperation conflictingOp : conflictingDefaultOperation.getConflictingOperations()) {
					if (conflictingOp.getResolvedDeclarator().isSubtypeOf(operation.getDeclaringType())) {
						isOverridden = true;
						break;
					}
				}
				if (!isOverridden)
					conflictingDefaultOperation.getConflictingOperations().add(createResolvedOperation(operation));
				return false;
			}
			// A new conflict of default implementations was found
			if (operation.isAbstract()) {
				ConflictingDefaultOperation resolvedOperation = createConflictingOperation(conflictingOperations.get(0).getDeclaration());
				resolvedOperation.getConflictingOperations().add(createResolvedOperation(operation));
				for (AbstractResolvedOperation conflictingOp : conflictingOperations) {
					processedOperations.remove(simpleName, conflictingOp);
					if (conflictingOp.getDeclaration() != resolvedOperation.getDeclaration()) {
						resolvedOperation.getConflictingOperations().add(conflictingOp);
					}
				}
				processedOperations.put(simpleName, resolvedOperation);
			} else {
				ConflictingDefaultOperation resolvedOperation = createConflictingOperation(operation);
				for (AbstractResolvedOperation conflictingOp : conflictingOperations) {
					processedOperations.remove(simpleName, conflictingOp);
					resolvedOperation.getConflictingOperations().add(conflictingOp);
				}
				processedOperations.put(simpleName, resolvedOperation);
			}
			return false;
		}
		return true;
	}

}
