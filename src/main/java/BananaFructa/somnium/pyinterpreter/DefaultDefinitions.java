package BananaFructa.somnium.pyinterpreter;

import BananaFructa.somnium.pyinterpreter.objects.*;
import BananaFructa.somnium.pyinterpreter.objects.Python_Number;

import java.util.Collections;

public class DefaultDefinitions {

    private static void throwBadOperator() {
        throw new RuntimeException("Runtime LID Python: Bad types on operator.");
    }

    private static void throwParameterBadCount() {
        throw new RuntimeException("Runtime LID Python: Wrong number of operators for function call.");
    }

    private static void throwBadParsing() {
        throw new RuntimeException("Runtime LID Python: Hanging operator or improper parsing of operator caught at runtime! Please report this issue!");
    }

    private static void throwBadInputType() {
        throw new RuntimeException("Runtime LID Python: Wrong type for function call.");
    }

    public static Python_Object print(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        System.out.println(objects[0].toString());
        return Python_NoneType.None;
    }

    public static Python_Object toInt(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwBadParsing();
        if (!(objects[0] instanceof Python_Number)) throwBadOperator();
        Python_Number n = ((Python_Number)objects[0]);
        if (n.mode == NumberMode.INT) return n.copy();
        return new Python_Number((int)n.f);
    }

    public static Python_Object add(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return ((Python_Number)objects[0]).add(((Python_Number) objects[1]));
    }

    public static Python_Object sub(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2 && objects.length != 1) throwBadParsing();
        if (!(objects[0] instanceof Python_Number)) throwBadOperator();
        if (objects.length == 2) {
            if (!(objects[1] instanceof Python_Number)) throwBadOperator();
            return ((Python_Number) objects[0]).sub(((Python_Number) objects[1]));
        } else {
            return ((Python_Number)objects[0]).invert();
        }
    }

    public static Python_Object mul(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return ((Python_Number)objects[0]).mul(((Python_Number) objects[1]));
    }

    public static Python_Object div(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return ((Python_Number)objects[0]).div(((Python_Number) objects[1]));
    }

    public static Python_Object mod(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return ((Python_Number)objects[0]).mod(((Python_Number) objects[1]));
    }

    public static Python_Object pow(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return ((Python_Number)objects[0]).pow(((Python_Number) objects[1]));
    }

    public static Python_Object equals(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        return new Python_Bool(objects[0].equals(objects[1]));
    }

    public static Python_Object notEquals(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        return new Python_Bool(!objects[0].equals(objects[1]));
    }

    public static Python_Object greater(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return new Python_Bool(((Python_Number)objects[0]).greaterThan((Python_Number)objects[1]));
    }

    public static Python_Object greaterOrEqual(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return new Python_Bool(((Python_Number)objects[0]).greaterThan((Python_Number)objects[1]) || ((Python_Number)objects[0]).equals(((Python_Number)objects[1])));
    }

    public static Python_Object lesser(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return new Python_Bool(!((Python_Number)objects[0]).greaterThan((Python_Number)objects[1]) && !((Python_Number)objects[0]).equals(((Python_Number)objects[1])));
    }

    public static Python_Object lesserOrEqual(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Number) || !(objects[1] instanceof Python_Number)) throwBadOperator();
        return new Python_Bool(!((Python_Number)objects[0]).greaterThan((Python_Number)objects[1]));
    }

    public static Python_Object not(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwBadParsing();
        if (!(objects[0] instanceof Python_Bool)) objects[0] = new Python_Bool(objects[0]);
        return new Python_Bool(!((Python_Bool)objects[0]).b);
    }

    public static Python_Object and(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Bool)) objects[0] = new Python_Bool(objects[0]);
        if (!(objects[1] instanceof Python_Bool)) objects[1] = new Python_Bool(objects[1]);
        return new Python_Bool(((Python_Bool)objects[0]).b && ((Python_Bool)objects[1]).b);
    }

    public static Python_Object or(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[0] instanceof Python_Bool)) objects[0] = new Python_Bool(objects[0]);
        if (!(objects[1] instanceof Python_Bool)) objects[1] = new Python_Bool(objects[1]);
        return new Python_Bool(((Python_Bool)objects[0]).b || ((Python_Bool)objects[1]).b);
    }

    public static Python_Object in(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwBadParsing();
        if (!(objects[1] instanceof Python_List)) throw new RuntimeException("Runtime LID Python: in Must search in a list.");
        Python_List l = (Python_List)objects[1];
        for (int i = 0;i < l.elements.size();i++) {
            if (((Python_Bool)equals(caller,new Python_Object[]{objects[0],l.elements.get(i)})).b) return new Python_Bool(true);
        }
        return new Python_Bool(false);
    }

    public static Python_Object append(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        l.elements.add(objects[1]);
        return Python_NoneType.None;
    }

    public static Python_Object clear(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        l.elements.clear();
        return Python_NoneType.None;
    }

    public static Python_Object copy(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        return l.copy();
    }

    public static Python_Object count(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        int count = 0;
        for (Python_Object t : l.elements) {
            if (((Python_Bool)equals(caller,new Python_Object[]{t,objects[1]})).b) {
                count++;
            }
        }
        return new Python_Number(count);
    }

    public static Python_Object extend(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwParameterBadCount();
        if (!(objects[1] instanceof Python_List)) throw new RuntimeException("Runtime LID Python: Expected to extend from a list.");
        Python_List l = (Python_List)(objects[0]);
        Python_List l2 = (Python_List)(objects[1]);
        l.elements.addAll(l2.elements);
        return Python_NoneType.None;
    }

    public static Python_Object index(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        for (int i = 0;i < l.elements.size();i++) {
            if (((Python_Bool)equals(caller, new Python_Object[]{l.elements.get(i),objects[1]})).b) {
                return new Python_Number(i);
            }
        }
        throw new RuntimeException("Runtime LID Python: Element not found");
    }

    public static Python_Object insert(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 3) throwParameterBadCount();
        if (!(objects[1] instanceof Python_Number)) throw new RuntimeException("Runtime LID Python: Expected index to be an integer.");
        Python_Number index = (Python_Number)objects[1];
        if (index.mode != NumberMode.INT) throw  new RuntimeException("Runtime LID Python: Expected index to be an integer.");
        Python_List l = (Python_List)(objects[0]);
        l.elements.add(index.i,objects[2]);
        return Python_NoneType.None;
    }

    public static Python_Object pop(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        l.elements.remove(l.elements.size() - 1);
        return Python_NoneType.None;
    }

    public static Python_Object remove(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 2) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        for (int i = 0;i < l.elements.size();i++) {
            if (((Python_Bool)equals(caller, new Python_Object[]{l.elements.get(i),objects[1]})).b) {
                l.elements.remove(i);
                return Python_NoneType.None;
            }
        }
        throw new RuntimeException("Runtime LID Python: Element not found");
    }

    public static Python_Object reverse(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        Collections.reverse(l.elements);
        return Python_NoneType.None;
    }

    public static Python_Object sort(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        Collections.sort(l.elements,(a,b) -> {
            if (((Python_Bool)lesser(caller, new Python_Object[]{a,b})).b) return -1;
            return 1;
        });
        return Python_NoneType.None;
    }

    public static Python_Object listAccess(Python_Object listO, Python_Object indexO) {
        if (!(listO instanceof Python_List)) throw new RuntimeException("Runtime LID Python: Only lists can be indexed.");
        if (!(indexO instanceof Python_Number)) throw new RuntimeException("Runtime LID Python: Index has to be a number.");
        Python_List list = (Python_List)listO;
        Python_Number index = (Python_Number) indexO;
        if (index.mode != NumberMode.INT) throw new RuntimeException("Runtime LID Python: List index must be an integer.");
        if (index.i >= 0) {
            if (list.elements.size() <= index.i) throw new RuntimeException("Runtime LID Pythin: Index out of range.");
            return list.elements.get(index.i);
        } else {
            if (list.elements.size() < -index.i) throw new RuntimeException("Runtime LID Pythin: Index out of range.");
            return list.elements.get(list.elements.size() + index.i);
        }
    }

    public static Python_Object listAccess(Python_Object listO, Python_Object index1O, Python_Object index2O) {
        if (!(listO instanceof Python_List)) throw new RuntimeException("Runtime LID Python: Only lists can be indexed.");
        if (!(index1O instanceof Python_Number) && index1O != null) throw new RuntimeException("Runtime LID Python: Index has to be a number.");
        if (!(index2O instanceof Python_Number) && index2O != null) throw new RuntimeException("Runtime LID Python: Index has to be a number.");
        Python_List list = (Python_List)listO;
        Python_Number index1 = (Python_Number) index1O;
        Python_Number index2 = (Python_Number) index2O;
        if (index1 != null && index1.mode != NumberMode.INT || index2 != null && index2.mode != NumberMode.INT) throw new RuntimeException("Runtime LID Python: List index must be an integer.");
        if ((index1 != null && index2 != null) && (index1.i >= 0 && index2.i < 0 || index1.i < 0 && index2.i >= 0)) return new Python_List();
        Python_List l = new Python_List();
        // Note that the parses catches if both were to have been null
        if (index1 != null ? index1.i >= 0 : index2.i >= 0) {
            if (index1 == null) index1 = new Python_Number(0);
            if (index2 == null) index2 = new Python_Number(list.elements.size());
            if (index1.i >= index2.i) return l;
            for (int i = index1.i; i < Math.min(index2.i,list.elements.size());i++) l.elements.add(list.elements.get(i));
        } else {
            if (index1 == null) index1 = new Python_Number(-list.elements.size());
            if (index2 == null) index2 = new Python_Number(-1);
            if (index1.i >= index2.i) return l;
            for (int i = Math.max(index1.i,-list.elements.size()); i < index2.i;i++) l.elements.add(list.elements.get(list.elements.size()+i));
        }
        return l;
    }

    public static Python_Object range(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length == 0 || objects.length > 3) throwParameterBadCount();
        if (!(objects[0] instanceof Python_Number)) throwBadInputType();
        Python_List l = new Python_List();
        switch (objects.length) {
            case 1: {
                Python_Number stop = (Python_Number) objects[0];
                if (stop.mode != NumberMode.INT) throwBadInputType();
                for (int i = 0; i < stop.i; i++) {
                    l.elements.add(new Python_Number(i));
                }
                return l;
            }
            case 2: {
                if (!(objects[1] instanceof Python_Number)) throwBadInputType();
                Python_Number start = (Python_Number) objects[0];
                Python_Number stop = (Python_Number) objects[1];
                if (stop.mode != NumberMode.INT) throwBadInputType();
                if (start.mode != NumberMode.INT) throwBadInputType();
                for (int i = start.i; i < stop.i; i++) {
                    l.elements.add(new Python_Number(i));
                }
                return l;

            }
            case 3: {
                if (!(objects[1] instanceof Python_Number)) throwBadInputType();
                if (!(objects[2] instanceof Python_Number)) throwBadInputType();
                Python_Number start = (Python_Number) objects[0];
                Python_Number stop = (Python_Number) objects[1];
                Python_Number step = (Python_Number) objects[2];
                if (stop.mode != NumberMode.INT) throwBadInputType();
                if (start.mode != NumberMode.INT) throwBadInputType();
                if (step.mode != NumberMode.INT) throwBadInputType();
                for (int i = start.i; i < stop.i; i+=step.i) {
                    l.elements.add(new Python_Number(i));
                }
                return l;
            }
            default:
                return new Python_List(); // this never happens but java won't shut up
        }
    }

    public static Python_Object len(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        Python_List l = (Python_List)(objects[0]);
        return new Python_Number(l.elements.size());
    }

    public static Python_Object abs(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length != 1) throwParameterBadCount();
        if (!(objects[0] instanceof Python_Number)) throwBadInputType();
        Python_Number n = (Python_Number) objects[0];
        if (n.mode == NumberMode.INT) return new Python_Number(Math.abs(n.i));
        else return new Python_Number(Math.abs(n.f));
    }

    // Math

    public static void assertType(Python_Object[] objects, Class<?>... types) {
        if (objects.length != types.length) throwParameterBadCount();
        for (int i = 0;i < objects.length;i++) {
            if (!types[i].isInstance(objects[i])) throwBadInputType();
        }
    }

    public static Python_Object ceil (ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n = (Python_Number) objects[0];
        return new Python_Number((float) Math.ceil(n.anyAsFloat()));
    }

    public static Python_Object fabs (ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n = (Python_Number) objects[0];
        return new Python_Number((float) Math.abs(n.anyAsFloat()));
    }

    public static Python_Object floor(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n = (Python_Number) objects[0];
        return new Python_Number((float) Math.floor(n.anyAsFloat()));
    }

    public static Python_Object fma(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class, Python_Number.class, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        Python_Number n3 = (Python_Number) objects[2];
        return new Python_Number((float) Math.fma(n1.anyAsFloat(),n2.anyAsFloat(),n3.anyAsFloat()));
    }

    public static Python_Object fmod(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        // this is not exactly correct but like who uses this stuff
        return new Python_Number((float) n1.anyAsFloat() % n2.anyAsFloat());
    }

    public static Python_Object modf(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n = (Python_Number) objects[0];
        Python_Tuple t = new Python_Tuple();
        float frac = n.anyAsFloat() % 1;
        float whole = n.anyAsFloat() - frac;
        t.elements.add(new Python_Number(frac));
        t.elements.add(new Python_Number(whole));
        return t;
    }


    // Not sure if this is correct it is too late in the night
    public static Python_Object remainder(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        if (n1.mode != NumberMode.INT) throwBadInputType();
        if (n2.mode != NumberMode.INT) throwBadInputType();
        return new Python_Number((int)(n1.i % n2.i));
    }

    public static Python_Object trunc(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n = (Python_Number) objects[0];
        if (n.anyAsFloat() >= 0) return new Python_Number((float) Math.floor(n.anyAsFloat()));
        else return new Python_Number((float) Math.ceil(n.anyAsFloat()));
    }

    public static Python_Object copysign(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        return new Python_Number(Math.copySign(n1.anyAsFloat(),n2.anyAsFloat()));
    }

    // TODO: implement frexp
    // TODO: implement isClose

    public static Python_Object isfinite(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Bool(Float.isFinite(n1.anyAsFloat()));
    }

    public static Python_Object isinf(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Bool(Float.isInfinite(n1.anyAsFloat()));
    }

    public static Python_Object isnan(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Bool(Float.isNaN(n1.anyAsFloat()));
    }

    // TODO: implement ldexp
    // TODO: implement nextafter
    // TODO: implement ulp

    public static Python_Object cbrt(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.cbrt(n1.anyAsFloat()));
    }

    public static Python_Object exp(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.exp(n1.anyAsFloat()));
    }

    public static Python_Object exp2(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.pow(2,n1.anyAsFloat()));
    }

    public static Python_Object expm1(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.expm1(n1.anyAsFloat()));
    }

    public static Python_Object log(ShadowedPythonCode caller, Python_Object[] objects) {
        if (objects.length == 1) {
            assertType(objects, Python_Number.class);
            Python_Number n1 = (Python_Number) objects[0];
            return new Python_Number((float) Math.log(n1.anyAsFloat()));
        } else {
            assertType(objects, Python_Number.class, Python_Number.class);
            Python_Number n1 = (Python_Number) objects[0];
            Python_Number n2 = (Python_Number) objects[1];
            return new Python_Number((float) Math.log(n1.anyAsFloat()) / (float) Math.log(n2.anyAsFloat()));
        }
    }

    public static Python_Object log2(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float) Math.log(n1.anyAsFloat()) / (float) Math.log(2));
    }

    public static Python_Object log10(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float) Math.log(n1.anyAsFloat()) / (float) Math.log(10));
    }

    public static Python_Object sqrt(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float) Math.sqrt(n1.anyAsFloat()));
    }

    public static Python_Object dist(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_List.class, Python_List.class);
        Python_List n1 = (Python_List) objects[0];
        Python_List n2 = (Python_List) objects[1];
        if (n1.elements.size() != n2.elements.size()) throw new RuntimeException("Runtime LID Python: Distance can only be calculated between lists of equal sizes.");
        Python_Object sum = null;
        for (int i = 0;i < n1.elements.size();i++) {
            Python_Object sub = sub(caller,new Python_Object[]{n1.elements.get(i),n2.elements.get(i)});
            Python_Object square = pow(caller,new Python_Object[]{sub,new Python_Number(2)});
            if (sum == null) sum = square;
            else sum = add(caller,new Python_Object[]{sum,square});
        }
        return sqrt(caller,new Python_Object[]{sum});
    }

    public static Python_Object fsum(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_List.class);
        Python_List n1 = (Python_List) objects[0];
        Python_Object sum = null;
        for (Python_Object e : n1.elements) {
            if (sum == null) sum = e;
            else sum = add(caller, new Python_Object[]{sum,e});
        }
        return sum;
    }

    public static Python_Object hypot(ShadowedPythonCode caller, Python_Object[] objects) {
        Python_Object sum = null;
        for (int i = 0;i < objects.length;i++) {
            Python_Object square = pow(caller, new Python_Object[]{objects[i],new Python_Number(2)});
            if (sum == null) sum = square;
            else sum = add(caller,new Python_Object[]{sum,square});
        }
        return sqrt(caller,new Python_Object[]{sum});
    }

    public static Python_Object prod(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_List.class, java.lang.Number.class);
        Python_List n1 = (Python_List) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        Python_Object prod = n2;
        for (int i = 0; i < n1.elements.size();i++) {
            prod = mul(caller,new Python_Object[]{prod,n1.elements.get(i)});
        }
        return prod;
    }

    public static Python_Object sumprod(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_List.class, Python_List.class);
        Python_List n1 = (Python_List) objects[0];
        Python_List n2 = (Python_List) objects[1];
        if (n1.elements.size() != n2.elements.size()) throw new RuntimeException("Runtime LID Python: Distance can only be calculated between lists of equal sizes.");
        Python_Object sum = null;
        for (int i = 0;i < n1.elements.size();i++) {
            Python_Object prod = mul(caller,new Python_Object[]{n1.elements.get(i),n2.elements.get(i)});
            if (sum == null) sum = prod;
            else sum = add(caller,new Python_Object[]{sum,prod});
        }
        return sum;
    }

    public static Python_Object degrees(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.toDegrees(n1.anyAsFloat()));
    }

    public static Python_Object radians(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.toRadians(n1.anyAsFloat()));
    }

    public static Python_Object acos(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.acos(n1.anyAsFloat()));
    }
    public static Python_Object asin(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.asin(n1.anyAsFloat()));
    }
    public static Python_Object atan(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.atan(n1.anyAsFloat()));
    }
    public static Python_Object atan2(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        Python_Number n2 = (Python_Number) objects[1];
        return new Python_Number((float)Math.atan2(n1.anyAsFloat(),n2.anyAsFloat()));
    }
    public static Python_Object cos(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.cos(n1.anyAsFloat()));
    }
    public static Python_Object sin(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.sin(n1.anyAsFloat()));
    }
    public static Python_Object tan(ShadowedPythonCode caller, Python_Object[] objects) {
        assertType(objects, Python_Number.class);
        Python_Number n1 = (Python_Number) objects[0];
        return new Python_Number((float)Math.tan(n1.anyAsFloat()));
    }


    // TODO: hyperbolics
    // TODO: special functions






}
