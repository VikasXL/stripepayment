package org.neoxl.resources;

import java.util.List;
import com.stripe.model.Address;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams.Shipping;
import org.neoxl.dto.StripeCustomerDto;
import org.neoxl.dto.StripeProductDto;

public class Data
{
  public static Shipping.Address getShippingAddress()
  {
    return new Shipping.Address.Builder()
      .setLine1("123 Main St")
      .setCity("City")
      .setState("State")
      .setPostalCode("12345")
      .setCountry("US")
      .build();
  }

  public static CustomerCreateParams.Address getCustomerAddress1()
  {
    return new CustomerCreateParams.Address.Builder()
      .setCountry("India")
      .setState("Bihar")
      .setCity("Pune")
      .setPostalCode("333090")
      .setLine1("Abc road 001")
      .build();
  }

  public static CustomerCreateParams.Address getCustomerAddress2()
  {
    return new CustomerCreateParams.Address.Builder()
      .setCountry("US")
      .setState("Dallas")
      .setCity("LEGENDARY")
      .setPostalCode("3632")
      .setLine1("Abc road 001")
      .build();
  }

  public static PaymentMethod.BillingDetails getBillingDetailsOutsideIndia()
  {

    Address address = new Address();
    address.setCountry("US");
    address.setState("Dallas");
    address.setCity("LEGENDARY");
    address.setPostalCode("3632");
    address.setLine1("Abc road 001");

    PaymentMethod.BillingDetails billingDetails = new PaymentMethod.BillingDetails();
    billingDetails.setAddress(address);
    billingDetails.setName("Vikas");
    billingDetails.setPhone("7700000000");

    return billingDetails;
  }

  public static StripeCustomerDto getCustomerDto1()
  {
    return new StripeCustomerDto("Vikas Kumar", "vikas@gmail.com", "7700000000");
  }

  public static StripeCustomerDto getCustomerDto2()
  {
    return new StripeCustomerDto("Akash Sharma", "kirtan@gmail.com", "8800000000");
  }

  public static StripeCustomerDto getCustomerDto3()
  {
    return new StripeCustomerDto("Kirtan Jain", "kirtan@gmail.com", "9900000000");
  }

  public static StripeProductDto getProductDto1()
  {
    final String feature1 =
      "Familiar messaging system with text, media, polls, search, and message receipts accessed through multiple devices, browsers, and tablets.";
    final String feature2 = "Cross-sheet data references and spreadsheet formulas to simplify complex workflows";
    final String feature3 =
      "Prebuilt integration with SMS, Email, WhatsApp, Google Maps, Pdf documents, and Payment systems";

    return new StripeProductDto(
      "Neoxl Standard",
      "This product include all the basic functionalities",
      List.of("IMAGE_URL1"),
      List.of(feature1, feature2, feature3)
    );
  }

  public static StripeProductDto getProductDto2()
  {
    final String feature1 =
      "Familiar messaging system with text, media, polls, search, and message receipts accessed through multiple devices, browsers, and tablets.";
    final String feature2 = "Cross-sheet data references and spreadsheet formulas to simplify complex workflows";
    final String feature3 =
      "Prebuilt integration with SMS, Email, WhatsApp, Google Maps, Pdf documents, and Payment systems";

    return new StripeProductDto(
      "Neoxl Advanced",
      "This product include all the basic functionalities",
      List.of("https://www.orgbeat.com/assets/images/common/header/header_logo_image.svg"),
      List.of(feature1, feature2, feature3)
    );
  }
}