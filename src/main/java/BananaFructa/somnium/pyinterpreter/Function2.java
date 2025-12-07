package BananaFructa.somnium.pyinterpreter;

@FunctionalInterface
public interface Function2<T1,T2,R> {

    public R apply(T1 t1,T2 t2);

}
