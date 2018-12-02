package server.model;

public class File {
    private String name;
    private long size;
    private boolean isPublic;
    private boolean isReadonly;
    private String owner;

    public File(String name, long size, boolean isPublic, boolean isReadonly, String owner) {
        this.name = name;
        this.size = size;
        this.isPublic = isPublic;
        this.isReadonly = isReadonly;
        this.owner = owner;
    }

    public File() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean readOnly) {
        isReadonly = readOnly;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
