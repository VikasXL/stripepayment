package org.neoxl.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerCreateParams.Address;
import com.stripe.param.CustomerUpdateParams;
import org.neoxl.dto.StripeCustomerDto;
import org.neoxl.resources.Data;

public class CustomerService
{
  public Customer createCustomer()
  {
    Address address = Data.getCustomerAddress2();
    StripeCustomerDto customerDto = Data.getCustomerDto2();

    CustomerCreateParams params = CustomerCreateParams.builder()
      .setName(customerDto.name)
      .setPhone(customerDto.phone)
      .setEmail(customerDto.email)
      .setAddress(address)
      .build();

    try
    {
      Customer customer = Customer.create(params);
      //System.out.println(customer);
      return customer;
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  public Customer updateCustomer(String customerId)
  {
    try
    {
      StripeCustomerDto customerDto = Data.getCustomerDto2();

      CustomerUpdateParams newParams = CustomerUpdateParams.builder()
        .setEmail(customerDto.email)
        .setPhone(customerDto.phone)
        .build();
      Customer customer = Customer.retrieve(customerId);
      Customer newCustomer = customer.update(newParams);
      System.out.println(newCustomer);
      return newCustomer;
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void deleteCustomer(String customerId)
  {
    try
    {
      Customer customer = Customer.retrieve(customerId);
      customer.delete();
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }
}