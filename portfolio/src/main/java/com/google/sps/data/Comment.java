package com.google.sps.data;

import java.util.Date;

/** Class containing information about a comment left on the site. */
public class Comment {
  
  private final String NAME;
  private final String CONTENT;
  private int numLikes;
  private final Date TIMESTAMP;
  private final int ID;
  private static int numComments = 0;

  /**
  * Constructs a Comment with a specified name and message.
  * 
  * @param name name of person who posted comment
  * @param content message left in the comment
  */
  public Comment(String name, String content) {
    NAME = name;
    CONTENT = content;
    numLikes = 0;
    TIMESTAMP = new Date();
    ID = numComments++;
  }

  /**
  * Constructs an anonymous Comment with a specified message.
  * 
  * @param content message left in the comment
  */
  public Comment(String content) {
    this(/* name= */"anonymous", content);
  }

  /** Increments number of likes that the comment has. */
  public void addLike() {
    numLikes++;
  }

}