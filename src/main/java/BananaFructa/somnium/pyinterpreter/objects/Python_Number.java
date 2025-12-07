package BananaFructa.somnium.pyinterpreter.objects;

public class Python_Number extends Python_Object {

    public static Python_Number ZERO = new Python_Number(0);

    public NumberMode mode;

    public int i;
    public float f;

    public Python_Number(int i) {
        mode = NumberMode.INT;
        this.i = i;
    }

    public Python_Number(float f) {
        mode = NumberMode.FLOAT;
        this.f = f;
    }

    public float anyAsFloat() {
        if (mode == NumberMode.FLOAT) return f;
        else return (float) i;
    }

    public int anyAsInt() {
        if (mode == NumberMode.INT) return i;
        else return (int) f;
    }

    public Python_Number add(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number(i + other.i); // if anything that this is used then yikes
            else return new Python_Number(i + other.f);
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number(f + other.i);
            else return new Python_Number(f + other.f);
        }
    }

    public Python_Number sub(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number(i - other.i); // if anything that this is used then yikes
            else return new Python_Number(i - other.f);
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number(f - other.i);
            else return new Python_Number(f - other.f);
        }
    }

    public Python_Number invert() {
        if (mode == NumberMode.INT) {
            return new Python_Number(-i);
        } else {
            return new Python_Number(-f);
        }
    }

    public Python_Number mul(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number(i * other.i); // if anything that this is used then yikes
            else return new Python_Number(i * other.f);
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number(f * other.i);
            else return new Python_Number(f * other.f);
        }
    }

    public Python_Number div(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number((float)i / other.i); // if anything that this is used then yikes
            else return new Python_Number(i / other.f);
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number(f / other.i);
            else return new Python_Number(f / other.f);
        }
    }

    public Python_Number mod(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number(i % other.i); // if anything that this is used then yikes
            else return new Python_Number(i % other.f);
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number(f % other.i);
            else return new Python_Number(f % other.f);
        }
    }
    public Python_Number pow(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return new Python_Number((float)Math.pow(i , other.i)); // if anything that this is used then yikes
            else return new Python_Number((float)Math.pow(i , other.f));
        } else {
            if (other.mode == NumberMode.INT) return new Python_Number((float)Math.pow(f , other.i));
            else return new Python_Number((float)Math.pow(f , other.f));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Python_Number)) return false;
        Python_Number other = (Python_Number)obj;
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return i == other.i; // if anything that this is used then yikes
            else return i == other.f;
        } else {
            if (other.mode == NumberMode.INT) return f == other.i;
            else return f == other.f;
        }
    }

    public boolean greaterThan(Python_Number other) {
        if (mode == NumberMode.INT) {
            if (other.mode == NumberMode.INT) return i > other.i;
            else return i > other.f;
        } else {
            if (other.mode == NumberMode.INT) return f > other.i;
            else return f > other.f;
        }
    }

    public String toString() {
        if (mode == NumberMode.INT) {
            return Integer.toString(i);
        } else {
            return Float.toString(f);
        }
    }

    public void convert(NumberMode newMode) {
        if (this.mode == newMode) return;
        if (newMode == NumberMode.INT) {
            mode = newMode;
            i = (int)f;
        }
        if (newMode == NumberMode.FLOAT) {
            mode = newMode;
            f = (float)i;
        }
    }

    @Override
    public Python_Object copy() {
        if (mode == NumberMode.INT) return new Python_Number(i);
        else return new Python_Number(f);
    }
}
