package com.google.sps.data;

import java.util.Date;

/** Class containing information about a comment left on the site. */
public class Comment {

  private final String name;
  private final String content;
  private int numLikes;
  private final Date timestamp;
  private final String key;

  /**
   * Constructs a Comment.
   *
   * @param name name of person who posted comment
   * @param content message left in the comment
   * @param numLikes current number of likes the comment has
   * @param timestamp time that comment was posted
   * @param key unique identifier for the comment
   */
  public Comment(String name, String content, int numLikes, Date timestamp, String key) {
    this.name = name;
    this.content = content;
    this.numLikes = numLikes;
    this.timestamp = timestamp;
    this.key = key;
  }
}
