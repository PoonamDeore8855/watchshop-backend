package com.watchshop.watchshop_backend.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    private String name;
    private String phone;
    private String city;
    private String pincode;
    private String street;
}

