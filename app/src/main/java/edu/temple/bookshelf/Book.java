package edu.temple.bookshelf;

public class Book {
    private int id;
    private String title;
    private String author;
    private String coverURL;

    public Book(int id, String title, String author, String coverURL){
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverURL = coverURL;
    }

    public int getId(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getCoverURL(){
        return this.coverURL;
    }
}
