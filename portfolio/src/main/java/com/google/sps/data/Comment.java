package com.google.sps.data;

import java.util.Date;

public class Comment {
  
  private String name;
  private String content;
  private int numLikes;
  private Date timestamp;

  public Comment(String name, String content) {
    this.name = name;
    this.content = content;
    numLikes = 0;
    timestamp = new Date();
  }

  public Comment(String content) {
    this("anonymous", content);
  }

  public void addLike() {
    numLikes++;
  }

}