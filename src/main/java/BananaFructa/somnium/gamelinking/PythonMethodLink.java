package BananaFructa.somnium.gamelinking;

import javax.annotation.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PythonMethodLink {

    public String docs();
    // Important as it keeps the order of the function always the same which makes the model responese more easy to debug
    public MethodLinkOrder order();
    @Nullable
    public String desc();

}
