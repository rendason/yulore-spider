package org.tafia.spider.model;

/**
 * Frequent Search Keyword Model
 */
public class FrequentSearch extends Model{

    private String cityId;

    private String word;

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
