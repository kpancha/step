package com.google.sps.data;

import java.util.Date;

/** Class containing information about a comment left on the site. */
public class Comment {
  
  private final String NAME;
  private final String CONTENT;
  private long numLikes;
  private final Date TIMESTAMP;
  private final String KEY;

  /**
  * Constructs a Comment.
  * 
  * @param name name of person who posted comment
  * @param content message left in the comment
  */
  public Comment(String name, String content, long numLikes, Date timestamp, String key) {
    NAME = name;
    CONTENT = content;
    this.numLikes = numLikes;
    TIMESTAMP = timestamp;
    KEY = key;
  }
}