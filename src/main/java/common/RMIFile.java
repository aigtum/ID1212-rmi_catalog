package common;

import java.io.Serializable;

public class RMIFile implements Serializable {

    private String name;
    private long size;
    private boolean isPublic, isReadonly;

    public RMIFile(String name, long size, boolean isPublic, boolean isReadonly) {
        this.name = name;
        this.size = size;
        this.isPublic = isPublic;
        this.isReadonly = isReadonly;
    }

    public RMIFile(String name, long size) {
        this(name, size, false, true);
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isReadonly() {
        return isReadonly;
    }
}
