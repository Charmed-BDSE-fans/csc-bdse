package ru.csc.bdse.app.v1.phonebook;

import ru.csc.bdse.app.common.Record;

import java.util.Collections;
import java.util.Set;

public class PhoneBookRecord implements Record {

    private String name;
    private String surname;
    private String phone;

    protected PhoneBookRecord() { }

    public PhoneBookRecord(String name, String surname, String phone) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "PhoneBookRecord{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public Set<Character> literals() {
        return Collections.singleton(surname.charAt(0));
    }
}
