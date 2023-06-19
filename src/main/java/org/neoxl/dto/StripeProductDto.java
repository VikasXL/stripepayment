package org.neoxl.dto;

import java.util.List;

public class StripeProductDto
{
  public final String name;
  public final String description;
  public final List<String> imageUrlList;
  public final List<String> featureList;

  public StripeProductDto(String name, String description, List<String> imageUrlList, List<String> featureList)
  {
    this.name = name;
    this.description = description;
    this.imageUrlList = imageUrlList;
    this.featureList = featureList;
  }
}