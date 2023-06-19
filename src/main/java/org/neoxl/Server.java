package org.neoxl;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import org.neoxl.resources.Data;
import org.neoxl.service.CustomerService;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class Server
{
  private static Gson gson = new Gson();

  static class CreatePayment
  {
    @SerializedName("items")
    Object[] items;

    public Object[] getItems()
    {
      return items;
    }
  }

  static class CreateInvoice
  {
    @SerializedName("subscriptionId")
    String subscriptionId;

    public String getSubscriptionId()
    {
      return subscriptionId;
    }
  }

  static class CreateSubscription
  {
    @SerializedName("planId")
    String planId;

    @SerializedName("productId")
    String productId;

    @SerializedName("paymentMethodId")
    String paymentMethodId;

    public String getPlanId()
    {
      return planId;
    }

    public String getProductId()
    {
      return productId;
    }

    public String getPaymentMethodId()
    {
      return paymentMethodId;
    }
  }

  static class CreatePaymentResponse
  {
    private String clientSecret;

    public CreatePaymentResponse(String clientSecret)
    {
      this.clientSecret = clientSecret;
    }
  }

  static class CreateSubscriptionResponse
  {
    private String subscriptionId;

    private String clientSecret;

    public CreateSubscriptionResponse(String subscriptionId, String clientSecret)
    {
      this.subscriptionId = subscriptionId;
      this.clientSecret = clientSecret;
    }
  }

  static class InvoiceObject
  {
    Long amount;
    String currency;
    String downloadUrl;
  }

  static int calculateOrderAmount(Object[] items)
  {
    // Replace this constant with a calculation of the order's amount
    // Calculate the order total on the server to prevent
    // people from directly manipulating the amount on the client
    return 5000;
  }

  public static void main(String[] args)
  {
    port(4242);
    staticFiles.externalLocation(Paths.get("public").toAbsolutePath().toString());

    // This is your test secret API key.
    Stripe.apiKey =
      "sk_test_51NISEfSIy1V6zzmJKowkXlHSNB7yOMXWSxyN49RMvlMfV3h63woL6Z8mdP5uMzxJNPikp0sO57WRKlYNlWvQcgVn002N9AQU4X";

    String endpointSecret = "whsec_fda32a61cfbb714975d383ceecb7d1b86aaae10133be8b0a3a73d0604384739b";

    // Enable CORS for all routes
    options("/*", (req, res) -> {
      String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
      if(accessControlRequestHeaders != null)
      {
        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
      if(accessControlRequestMethod != null)
      {
        res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });
    before((req, res) -> {
      res.header("Access-Control-Allow-Origin", "*");
      res.header("Access-Control-Allow-Headers", "*");
      res.type("application/json");
    });

    post("/initiate-payment", (request, response) -> {
      response.type("application/json");

      CreatePayment reqBody = gson.fromJson(request.body(), CreatePayment.class);

      //we can create new customer or can use existing one
      CustomerService customerService = new CustomerService();
      Customer customer = customerService.createCustomer();

      PaymentIntentCreateParams params =
        PaymentIntentCreateParams.builder()
          .setAmount((long) calculateOrderAmount(reqBody.getItems()))
          .setCurrency("usd")
          .setDescription("Dummy payment description")
          .setCustomer(customer.getId())
          .setAutomaticPaymentMethods(
            PaymentIntentCreateParams.AutomaticPaymentMethods
              .builder()
              .setEnabled(true)
              .build()
          )
          .build();

      // Create a PaymentIntent with the order amount and currency
      PaymentIntent paymentIntent = PaymentIntent.create(params);

      CreatePaymentResponse paymentResponse = new CreatePaymentResponse(paymentIntent.getClientSecret());
      return gson.toJson(paymentResponse);
    });

    post("/initiate-subscription", (request, response) -> {
      response.type("application/json");

      CreateSubscription reqBody = gson.fromJson(request.body(), CreateSubscription.class);

      //1. create new customer or can use existing one
      CustomerService customerService = new CustomerService();
      Customer customer = customerService.createCustomer();

      //2. Add customer and billing address to payment Method
      PaymentMethod paymentMethod = PaymentMethod.retrieve(reqBody.getPaymentMethodId()); //test payment method
      PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
        .setCustomer(customer.getId())    //Customer also should have address [ billing and this address should be in same country]
        .build();
      paymentMethod.attach(attachParams);
      paymentMethod.setBillingDetails(Data.getBillingDetailsOutsideIndia());

      //3. setup payment setting to save default payment method for future use
      SubscriptionCreateParams.PaymentSettings paymentSettings =
        SubscriptionCreateParams.PaymentSettings
          .builder()
          .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
          .build();

      SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams.builder()
        .setCustomer(customer.getId())
        .setDefaultPaymentMethod(paymentMethod.getId())
        .addItem(SubscriptionCreateParams.Item.builder().setPlan(reqBody.getPlanId()).build())
        .setPaymentSettings(paymentSettings)
        .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)     // This means that the subscription will follow the default behavior configured in your Stripe Dashboard for handling incomplete payments.
        .addAllExpand(List.of("latest_invoice.payment_intent"))    //to get the extra info like payment_intent for client_secret
        .build();

      Subscription subscription = Subscription.create(subscriptionCreateParams);

      String paymentIntentId = subscription.getLatestInvoiceObject().getPaymentIntent();
      PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

      CreateSubscriptionResponse createSubscriptionResponse =
        new CreateSubscriptionResponse(subscription.getId(), paymentIntent.getClientSecret());
      return gson.toJson(createSubscriptionResponse);
    });

    get("/subscription-invoice", (request, response) -> {
      response.type("application/json");
      String subscriptionId = request.queryParams("subscriptionId");

      InvoiceCollection invoices = Invoice.list(
        InvoiceListParams.builder()
          .setSubscription(subscriptionId)
          .build()
      );
      List<InvoiceObject> invoiceObjects = new ArrayList<>();
      for(Invoice invoice : invoices.getData())
      {
        System.out.println(invoice);
        InvoiceObject invoiceObject = new InvoiceObject();
        invoiceObject.amount = invoice.getAmountPaid();
        invoiceObject.currency = invoice.getCurrency();
        invoiceObject.downloadUrl = invoice.getHostedInvoiceUrl();
        invoiceObjects.add(invoiceObject);
      }

      // Convert the list to JSON using Gson
      Gson gson = new Gson();

      return gson.toJson(invoiceObjects);
    });

    post("/webhook", (request, response) -> {
      String payload = request.body();
      String sigHeader = request.headers("Stripe-Signature");
      Event event = null;

      try
      {
        event = Webhook.constructEvent(
          payload, sigHeader, endpointSecret
        );
      }
      catch(JsonSyntaxException e)
      {
        // Invalid payload
        response.status(400);
        return "";
      }
      catch(SignatureVerificationException e)
      {
        // Invalid signature
        response.status(400);
        return "";
      }

      // Deserialize the nested object inside the event
      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      StripeObject stripeObject = null;
      if(dataObjectDeserializer.getObject().isPresent())
      {
        stripeObject = dataObjectDeserializer.getObject().get();
      }
      else
      {
        // Deserialization failed, probably due to an API version mismatch.
        // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
        // instructions on how to handle this case, or return an error here.
      }
      // Handle the event
      switch(event.getType())
      {
        case "payment_intent.succeeded":
        {
          System.out.println("Case : payment_intent.succeeded");
          // Then define and call a function to handle the event payment_intent.succeeded
          break;
        }
        // ... handle other event types
        default:
          System.out.println("Unhandled event type: " + event.getType());
      }

      response.status(200);
      return "";
    });
  }
}