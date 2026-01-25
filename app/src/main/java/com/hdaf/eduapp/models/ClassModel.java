package com.hdaf.eduapp.models;

/**
 * Represents a class/standard (1st to 9th)
 */
public class ClassModel {
    private String id;
    private String name;
    private int standardNum;

    public ClassModel() {
        // Required empty constructor for Firebase
    }

    public ClassModel(String id, String name, int standardNum) {
        this.id = id;
        this.name = name;
        this.standardNum = standardNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStandardNum() {
        return standardNum;
    }

    public void setStandardNum(int standardNum) {
        this.standardNum = standardNum;
    }

    /**
     * Creates a list of all standard classes (1st to 9th)
     */
    public static ClassModel[] getAllClasses() {
        return new ClassModel[] {
                new ClassModel("class_1", "1st", 1),
                new ClassModel("class_2", "2nd", 2),
                new ClassModel("class_3", "3rd", 3),
                new ClassModel("class_4", "4th", 4),
                new ClassModel("class_5", "5th", 5),
                new ClassModel("class_6", "6th", 6),
                new ClassModel("class_7", "7th", 7),
                new ClassModel("class_8", "8th", 8),
                new ClassModel("class_9", "9th", 9)
        };
    }
}
