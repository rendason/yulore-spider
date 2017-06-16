package org.tafia.spider.model;

/**
 * City Model
 */
public class City extends Model{

    private String name;

    private String parentId;

    private String areaCode;

    private int hot;

    private String shortPinyin;

    private String fullPinyin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public String getShortPinyin() {
        return shortPinyin;
    }

    public void setShortPinyin(String shortPinyin) {
        this.shortPinyin = shortPinyin;
    }

    public String getFullPinyin() {
        return fullPinyin;
    }

    public void setFullPinyin(String fullPinyin) {
        this.fullPinyin = fullPinyin;
    }
}
