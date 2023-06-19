package org.neoxl.dto;

public class StripeCustomerDto
{
  public final String name;
  public final String email;
  public final String phone;

  public StripeCustomerDto(String name, String email, String phone)
  {
    this.name = name;
    this.email = email;
    this.phone = phone;
  }
}