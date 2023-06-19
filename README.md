# Stripe integration

**Account Creation**

1. Create a stripe account https://stripe.com/en-in
2. There are three types of token in stripe account

- Publishable key :  Identify your stripe account. This is not a secret. Can safely be published. Used with stripe
  frontend components
- Secret key  : Keep confidential. Store in backend. Can make any request without any restriction.
- Webhook signing secrets: Verify the events that stripe sends to your webhook endpoint. Stripe generate signatures
  using an Hash-based message authentication code (HMAC) with SHA-256.

**Subscription**

1. With Subscriptions, customers make recurring payments for access to a product.
2. Subscriptions require you to retain more information about your customers than one-time purchases do because you need
   to charge
   customers in the future.

**Subscription Objects**

- Product - This is what you business offers
- Price - How much and how often to charge your product, what currency to use
- Customer - Stripe customer allow you to perform recurring charges for the same customer and to track multiple charges
- Payment Method - Your customers payment instruments - How they pay your service. For example credit card
- Subscription - The product detail associated with the plan that the customer subscribes
- Invoice - A statement of amounts owed by a customer. They track the status of payments from draft through paid or
  otherwise finalized

# Project Run

this project has two main classes

- Main.java -> To run individual services like Customer, Payment , Plan and Product Service
- Server.java -> Expose some apis initiate payment, subscription and invoice 