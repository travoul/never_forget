/*      Copyright 2016 Marcello de Paula Ferreira Costa

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License. */
package mchs.neverforget.model;

public class Book {
    private String author;
    private String title;
    private String returnDate;
    private String library;
    private int drawableId;

    //represents a html attribute. Used to implement the "renew selected" feature
    private String nameAttribute;

    public Book(String author, String title, String returnDate, String library,
                String nameAttribute) {
        this.author = author;
        this.title = title;
        this.returnDate = returnDate;
        this.library = library;
        this.nameAttribute = nameAttribute;
    }

    public Book(String author, String title, String returnDate, String library) {
        this.author = author;
        this.title = title;
        this.returnDate = returnDate;
        this.library = library;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getNameAttribute() {
        return nameAttribute;
    }

    public void setNameAttribute(String nameAttribute) {
        this.nameAttribute = nameAttribute;
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }
}
