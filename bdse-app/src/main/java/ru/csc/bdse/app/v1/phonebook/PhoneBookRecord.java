package ru.csc.bdse.app.v1.phonebook;

import java.util.LinkedHashSet;
import java.util.Set;

public class PhoneBookRecord implements Record {

    private final String name;
    private final String surname;
    private final String phone;

    public PhoneBookRecord(String name, String surname, String phone) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    @Override
    public Set<Character> literals() {
        LinkedHashSet<Character> s = new LinkedHashSet<>();
        s.add(surname.charAt(0));
        return  s;
    }
}
