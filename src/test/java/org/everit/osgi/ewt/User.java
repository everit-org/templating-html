package org.everit.osgi.ewt;

public class User {

    public String firstName;

    public String lastName;

    public int userId;

    public User(final int userId, final String firstName, final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
    }

}
