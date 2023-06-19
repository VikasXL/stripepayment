package org.neoxl.service;

import java.util.Map;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import org.neoxl.dto.StripeProductDto;
import org.neoxl.resources.Data;

public class ProductService
{
  public Product createProduct()
  {
    StripeProductDto productDto = Data.getProductDto2();

    ProductCreateParams params = ProductCreateParams.builder()
      .setName(productDto.name)
      .setDescription(productDto.description)
      .addAllImage(productDto.imageUrlList)
      .setType(ProductCreateParams.Type.SERVICE)
      .build();

    try
    {
      Product product = Product.create(params);
      product.setAttributes(productDto.featureList);
      System.out.println(product);
      return product;
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  public Product updateProduct(String productId)
  {
    try
    {
      Product product = Product.retrieve(productId);
      ProductUpdateParams newPrams = ProductUpdateParams.builder()
        .setDescription("Dummy caption for test")
        .setMetadata(Map.of("sysProductId", "p-abcd123"))
        .build();

      Product newProduct = product.update(newPrams);
      System.out.println(newProduct);
      return newProduct;
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void deleteProduct(String productId)
  {
    try
    {
      Product product = Product.retrieve(productId);
      product.delete();
    }
    catch(StripeException e)
    {
      throw new RuntimeException(e);
    }
  }
}