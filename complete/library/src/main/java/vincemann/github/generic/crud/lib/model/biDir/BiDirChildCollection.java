package vincemann.github.generic.crud.lib.model.biDir;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BiDirChildCollection {

    /**
     *
     * @return generic type of annotated collection
     */
    Class<? extends BiDirChild> value();
}
