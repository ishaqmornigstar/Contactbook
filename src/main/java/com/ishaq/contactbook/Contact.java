package com.ishaq.contactbook;

// A record automatically provides a constructor and accessor methods.
public record Contact(long id, String name, String phone, String email, String address) {}
