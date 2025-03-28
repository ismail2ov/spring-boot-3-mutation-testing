package com.github.ismail2ov.ecommerce.infrastructure;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ismail2ov.ecommerce.application.BasketService;
import com.github.ismail2ov.ecommerce.domain.Basket;
import com.github.ismail2ov.ecommerce.domain.BasketNotFoundException;
import com.github.ismail2ov.ecommerce.domain.Items;
import com.github.ismail2ov.ecommerce.domain.Product;
import com.github.ismail2ov.ecommerce.infrastructure.controller.BasketController;
import com.github.ismail2ov.ecommerce.infrastructure.controller.mapper.BasketMapperImpl;
import com.github.ismail2ov.ecommerce.infrastructure.controller.mapper.ProductMapperImpl;
import com.github.ismail2ov.ecommerce.infrastructure.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@ContextConfiguration(classes = {BasketController.class, GlobalExceptionHandler.class})
@Import({ProductMapperImpl.class, BasketMapperImpl.class})
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketService basketService;

    @Test
    void return_basket_without_items() throws Exception {

        Basket basket = new Basket(1L, 1L, new Items());

        when(basketService.getBy(1L)).thenReturn(basket);

        this.mockMvc
            .perform(get("/users/1/basket"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.items.products.size()").value(0));
    }

    @Test
    void add_product_to_basket() throws Exception {
        Product product = new Product(3L, "Logitech Wireless Mouse M185", "10,78 €");
        Basket basket = new Basket(1L, 1L, new Items(List.of(product)));

        when(basketService.addProductToBasket(1L, product)).thenReturn(basket);

        this.mockMvc
            .perform(post("/users/1/basket")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(product)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.items.size()").value(1));
    }

    @Test
    void return_basket_not_found() throws Exception {

        doThrow(BasketNotFoundException.class).when(basketService).getBy(100L);

        this.mockMvc
            .perform(get("/users/100/basket"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Basket not found!"))
            .andExpect(jsonPath("$.detail").value("Something went wrong!"))
            .andExpect(jsonPath("$.type").value("https://example.org/basket-not-found"));
    }

    public String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
