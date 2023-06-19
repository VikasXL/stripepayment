package org.neoxl.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Plan;
import com.stripe.param.PlanCreateParams;

public class PlanService
{
  public void addPlanToProduct(String productId)
  {
    PlanCreateParams planParamsWeekly = PlanCreateParams.builder()
      .setAmount(2000L)
      .setCurrency("usd")
      .setInterval(PlanCreateParams.Interval.WEEK)
      .setIntervalCount(1L)
      .setProduct(productId)
      .build();

    PlanCreateParams planParamsMonthly = PlanCreateParams.builder()
      .setAmount(4000L)
      .setCurrency("usd")
      .setInterval(PlanCreateParams.Interval.MONTH)
      .setIntervalCount(1L)
      .setProduct(productId)
      .build();

    PlanCreateParams planParamsYearly = PlanCreateParams.builder()
      .setAmount(38000L)
      .setCurrency("usd")
      .setInterval(PlanCreateParams.Interval.YEAR)
      .setIntervalCount(1L)
      .setProduct(productId)
      .build();

    try
    {
      Plan planWeely = Plan.create(planParamsWeekly);
      Plan planMonthly = Plan.create(planParamsMonthly);
      Plan planYeary = Plan.create(planParamsYearly);
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }
}