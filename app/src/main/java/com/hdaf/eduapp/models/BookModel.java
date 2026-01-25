package com.hdaf.eduapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a book within a class.
 * Gson annotations map to Supabase table column names.
 */
public class BookModel {

    @SerializedName("id")
    private String id;

    @SerializedName("class_id")
    private String classId;

    @SerializedName("name")
    private String name;

    @SerializedName("order_index")
    private int orderIndex;

    public BookModel() {
        // Required empty constructor for Gson
    }

    public BookModel(String id, String classId, String name, int orderIndex) {
        this.id = id;
        this.classId = classId;
        this.name = name;
        this.orderIndex = orderIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    /**
     * Returns sample books for a given class (mock data for testing)
     */
    public static List<BookModel> getSampleBooks(String classId) {
        List<BookModel> books = new ArrayList<>();

        switch (classId) {
            case "class_1":
                books.add(new BookModel("book_1_1", classId, "MY ENGLISH BOOK ONE", 1));
                books.add(new BookModel("book_1_2", classId, "BALBHARTI", 2));
                break;
            case "class_2":
                books.add(new BookModel("book_2_1", classId, "MY ENGLISH BOOK TWO", 1));
                books.add(new BookModel("book_2_2", classId, "BALBHARTI", 2));
                books.add(new BookModel("book_2_3", classId, "MATHEMATICS", 3));
                break;
            case "class_3":
                books.add(new BookModel("book_3_1", classId, "ENGLISH READER", 1));
                books.add(new BookModel("book_3_2", classId, "MARATHI SULABHBHARTI", 2));
                books.add(new BookModel("book_3_3", classId, "MATHEMATICS", 3));
                books.add(new BookModel("book_3_4", classId, "EVS PART 1", 4));
                break;
            default:
                // Default books for other classes
                books.add(new BookModel("book_" + classId + "_1", classId, "ENGLISH", 1));
                books.add(new BookModel("book_" + classId + "_2", classId, "BALBHARTI", 2));
                books.add(new BookModel("book_" + classId + "_3", classId, "MATHEMATICS", 3));
                books.add(new BookModel("book_" + classId + "_4", classId, "SCIENCE", 4));
                break;
        }

        return books;
    }
}
