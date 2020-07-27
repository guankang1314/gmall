package com.atguan.gmall.cart.constant;


import lombok.Data;

import java.io.Serializable;

@Data
public class CartConst implements Serializable {

    public static final String USER_KEY_PREFIX ="user:";

    public static final String USER_CART_KEY_SUFFIX =":cart";

    public static final String USER_CHECKED_KEY_SUFFIX =":checked";

    public static final String  USERINFOKEY_SUFFIX=":info";

}
