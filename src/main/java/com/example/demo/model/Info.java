package com.example.demo.model;

public class Info {

    private String name;
    private String version;
    private String timestamp;
    private long build;

    public Info() {

    }

    public Info(String name, String version, String timestamp, long build) {
        this.name = name;
        this.version = version;
        this.timestamp = timestamp;
        this.build = build;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getBuild() {
        return build;
    }

    public void setBuild(long build) {
        this.build = build;
    }

    @Override
    public String toString() {
        return "Info [name=" + name + ", version=" + version + ", timestamp=" + timestamp + ", build=" + build +"]";
    }

}
