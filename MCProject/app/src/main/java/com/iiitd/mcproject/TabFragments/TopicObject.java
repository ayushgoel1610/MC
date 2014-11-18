package com.iiitd.mcproject.TabFragments;

/**
 * Created by Vedant on 18-11-2014.
 */
public class TopicObject {
    private int topicId;
    private String topicName;
    private String topicImage;
    private String topicCategory;

    public TopicObject(){

    }

    public int getId(){
        return topicId;
    }
    public String getName(){
        return topicName;
    }
    public String getImage(){
        return topicImage;
    }
    public String getCategory(){
        return topicCategory;
    }

    public void putId(int topicId){
        this.topicId=topicId;
    }
    public void putName(String topicName){
        this.topicName=topicName;
    }
    public void putImage(String topicImage){
        this.topicImage=topicImage;
    }
    public void putCategory(String topicCategory){
        this.topicCategory=topicCategory;
    }
}
