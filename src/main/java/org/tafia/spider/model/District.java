package org.tafia.spider.model;

import java.util.List;

/**
 * District Model
 */
public class District extends Model {

    private String name;

    private List<Category> categories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
