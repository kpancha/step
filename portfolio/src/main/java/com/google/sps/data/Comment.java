package com.google.sps.data;

import java.util.Date;

public class Comment {
  
  private final String NAME;
  private final String CONTENT;
  private int numLikes;
  private final Date TIMESTAMP;

  public Comment(String name, String content) {
    NAME = name;
    CONTENT = content;
    numLikes = 0;
    TIMESTAMP = new Date();
  }

  public Comment(String content) {
    this(/* name= */"anonymous", content);
  }

  public void addLike() {
    numLikes++;
  }

}