package org.eclipse.xtend.lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.eclipse.xtend.lib.annotations.DataProcessor;
import org.eclipse.xtend.lib.macro.Active;

/**
 * Turns this class into a read-only Data object.
 * 
 * <p>All non-static, non-transient fields ("data fields") become final and a getter is created for each one. For primitive boolean properties, the "is"-prefix is used.
 * The generation of getters can be customized using the {@link Accessors} annotation.</p>
 * <p>If there is no user-defined constructor, a constructor taking all data fields will be generated.
 * If there already is a constructor, but you want the default one on top of that, you can use the {@link FinalFieldsConstructor} annotation.</p>
 * <p>Default implementations for {@link Object#equals(Object) equals} and {@link Object#hashCode hashCode} are added if they don't exist yet. See {@link EqualsHashCode} for details.
 * A {@link Object#toString toString} method is added if it doesn't exist yet. See {@link ToString} for details and customization options.</p>
 * @since 2.7
 */
@Target(ElementType.TYPE)
@Active(DataProcessor.class)
public @interface Data {
}
