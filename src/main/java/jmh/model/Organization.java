package jmh.model;

import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexType;

public class Organization {

    private Integer id;
    private String name;

    @SpaceId
    public Integer getId() {
        return id;
    }
    public Organization setId(Integer id) {
        this.id = id;
        return this;
    }


    @SpaceIndex(type = SpaceIndexType.EQUAL)
    public String getName() {
        return name;
    }
    public Organization setName(String firstName) {
        this.name = firstName;
        return this;
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
