package org.neoxl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Subscription;
import com.stripe.model.Token;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import org.neoxl.resources.Data;

public class PaymentService
{
  public void doPayment(String productId, String priceId, String customerId)
  {
    // Amount in cents (e.g., $10.00) and will get from product id so frontend cant alter
    try
    {
      PriceCollection priceCollection = Price.list(Map.of("product", productId));
      List<Price> matchingPriceList = priceCollection.getData().stream()
        .filter(price -> price.getId().equals(priceId))
        .toList();

      if(matchingPriceList.size() == 0)
      {
        return;
      }

      PaymentIntentCreateParams.Shipping.Address shippingAddress = Data.getShippingAddress();

      final PaymentIntentCreateParams.Shipping shipping = PaymentIntentCreateParams.Shipping.builder()
        .setName("Vikas")
        .setAddress(shippingAddress)
        .build();

      PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(matchingPriceList.get(0).getUnitAmount())
        .setCurrency(matchingPriceList.get(0).getCurrency())
        .setDescription("Payment for Product")
        .setPaymentMethod("pm_card_visa")    //payment method will be created from the frontend side and will get it from api.
        .setCustomer(customerId)
        .setShipping(shipping)
        .build();

      PaymentIntent paymentIntent = PaymentIntent.create(params);
      System.out.println(paymentIntent);
      // PaymentIntent confirmPaymentIntent = paymentIntent.confirm();

      //now send paymentIntent.getClientSecret() to frontend to confirm the payment.
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  public String createSubscription(String planId, String customerId)
  {
    //1. create payment method  - [we will get payment method id from frontend as payload]
    //2. Create customer and add payment method to the customer
    try
    {
      //pm_card_threeDSecureRequired : for 3D Secure authentication confirmation
      PaymentMethod paymentMethod = PaymentMethod.retrieve("pm_card_visa"); //test payment method
      PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
        .setCustomer(customerId)    //Customer also should have address [ billing and this address should be in same country]
        .build();
      paymentMethod.attach(attachParams);
      paymentMethod.setBillingDetails(Data.getBillingDetailsOutsideIndia());  //we should have billing address with payment method

      SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams.builder()
        .setCustomer(customerId)
        .setDefaultPaymentMethod(paymentMethod.getId())
        .addItem(SubscriptionCreateParams.Item.builder().setPlan(planId).build())
        .addAllExpand(List.of("latest_invoice.payment_intent"))    //to get the extra info like payment_intent for client_secret
        .build();

      Subscription subscription = Subscription.create(subscriptionCreateParams);

      String paymentIntentId = subscription.getLatestInvoiceObject().getPaymentIntent();
      PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

      if("requires_action".equals(paymentIntent.getStatus()) &&
        paymentIntent.getNextAction() != null)
      {
        switch(paymentIntent.getNextAction().getType())
        {
          case "use_stripe_sdk":
            if(paymentIntent.getNextAction().getUseStripeSdk() != null)
            {
              String clientSecret = paymentIntent.getClientSecret();

           /*  Map<String, Object> response = new HashMap<>();
             response.put("requires_action", true);
             response.put("client_secret",clientSecret);
              // Return this response to the frontend so they can call  stripe.confirmCardPayment(clientSecret)
            */
              return clientSecret;
            }
            break;
          case "redirect_to_url":
            // Handle the callback URL after authentication completion
            break;
          default:
            break;
        }
        //System.out.println(subscription);
      }
      return subscription.getId();
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  private static String createCardToken(String cardNumber, int expMonth, int expYear, String cvc)
  {
    Map<String, Object> cardParams = new HashMap<>();
    cardParams.put("number", cardNumber);
    cardParams.put("exp_month", expMonth);
    cardParams.put("exp_year", expYear);
    cardParams.put("cvc", cvc);

    Map<String, Object> tokenParams = new HashMap<>();
    tokenParams.put("card", cardParams);

    try
    {
      final Token token = Token.create(tokenParams);
      System.out.println(token);
      return token.getId();
    }
    catch(StripeException e)
    {
      e.printStackTrace();
    }
    return null;
  }
}