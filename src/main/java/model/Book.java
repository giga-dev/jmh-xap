package model;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class Book {
    private String id;
    private String payload;
    private String author;

    @SpaceId
    public String getId() {
        return id;
    }
    public Book setId(String id) {
        this.id = id;
        return this;
    }

    @SpaceIndex(type = SpaceIndexType.EQUAL)
    public String getAuthor() {
        return author;
    }
    public Book setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getPayload() {
        return payload;
    }
    public Book setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}
