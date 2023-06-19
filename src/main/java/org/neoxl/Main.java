package org.neoxl;

import com.stripe.Stripe;
import com.stripe.model.Customer;
import org.neoxl.service.CustomerService;
import org.neoxl.service.PaymentService;

public class Main
{
  public static void main(String[] args)
  {
    System.out.println("Started stripe api....");
    Stripe.apiKey =
      "sk_test_51NISEfSIy1V6zzmJKowkXlHSNB7yOMXWSxyN49RMvlMfV3h63woL6Z8mdP5uMzxJNPikp0sO57WRKlYNlWvQcgVn002N9AQU4X";

    //Customer create, update and delete
    CustomerService customerService = new CustomerService();
    Customer customer = customerService.createCustomer();
    // customerService.updateCustomer(customer.getId());
    //  customerService.deleteCustomer("cus_O4eiMDTPD50c0J");

    //Product create, update and delete
    //ProductService productService = new ProductService();
    //Product product = productService.createProduct();
    // productService.updateProduct("prod_O4fNmZwvKZz6rL");
    // productService.deleteProduct("prod_O4fNmZwvKZz6rL"); //cant delete if it has price attached

    //Add plan to the product
    //PlanService planService = new PlanService();
    //planService.addPlanToProduct(product.getId());

    PaymentService paymentService = new PaymentService();
    String productId = "prod_O4xIJNF4JnDNbB";
    String priceId = "price_1NInSzSIy1V6zzmJ2R1RwuZ4";
    String priceIdRecurring = "price_1NIq9DSIy1V6zzmJMPIlg4Sz";
    String planId = "plan_O4xIxoKYCCuIY6";
    String customerId = "cus_O50wBRxLLoJOgA";
    paymentService.doPayment(productId, priceId, customerId);
    //paymentService.createSubscription(planId, customer.getId());
  }
}